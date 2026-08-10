package com.gien.gits.worker.config;

import com.gien.gits.action.ActionDispatchPort;
import com.gien.gits.action.RecordingActionDispatcher;
import com.gien.gits.action.port.CrmWritebackChannel;
import com.gien.gits.engagement.CrmWritebackCommand;
import com.gien.gits.ontology.Claim;
import com.gien.gits.ontology.ClaimStatus;
import com.gien.gits.ontology.event.CloudEvent;
import com.gien.gits.ontology.port.ClaimReconciliationPort;
import com.gien.gits.ontology.port.DomainEventPublisher;
import com.gien.gits.ontology.port.WritableClaimRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Worker模块配置 — 注册事件处理器所需的端口实现Bean。
 *
 * <p>Worker作为独立应用运行时，使用以下适配器实现：
 * <ul>
 *   <li>RecordingActionDispatcher — 仅记录动作派发（不实际执行）</li>
 *   <li>WorkerLoggingCrmWritebackChannel — 仅日志CRM回写</li>
 *   <li>WorkerFallbackClaimReconciliationAdapter — 无BusinessMetrics依赖的对账适配器</li>
 *   <li>SpringEventPublisher — 桥接Spring ApplicationEventPublisher</li>
 * </ul>
 * 生产环境中应替换为真实的HTTP/JDBC实现。</p>
 */
@Configuration
public class WorkerConfig {

    @Bean
    public ActionDispatchPort actionDispatchPort() {
        return new RecordingActionDispatcher();
    }

    @Bean
    public CrmWritebackChannel crmWritebackChannel() {
        return new WorkerLoggingCrmWritebackChannel();
    }

    @Bean
    public ClaimReconciliationPort claimReconciliationPort() {
        return new WorkerFallbackClaimReconciliationAdapter();
    }

    @Bean
    public DomainEventPublisher domainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        return new WorkerSpringEventPublisher(applicationEventPublisher);
    }

    @Bean
    public WritableClaimRepository writableClaimRepository() {
        return new WorkerInMemoryClaimRepository();
    }

    /**
     * Worker模块专用的日志回写通道 — 不依赖api模块的BusinessMetrics。
     * 生产环境应替换为HttpCrmWritebackChannel。
     */
    static class WorkerLoggingCrmWritebackChannel implements CrmWritebackChannel {

        private static final Logger log = LoggerFactory.getLogger(WorkerLoggingCrmWritebackChannel.class);

        @Override
        public WritebackResult send(CrmWritebackCommand command) {
            log.info("[CRM-WRITEBACK] commandId={}, objectType={}, operation={}, riskLevel={}, " +
                     "requiresHumanConfirm={}, rmAction={}, idempotencyKey={}",
                     command.commandId(), command.objectType(), command.operation(),
                     command.riskLevel(), command.requiresHumanConfirm(),
                     command.rmAction(), command.idempotencyKey());
            log.debug("[CRM-WRITEBACK] beforeValue={}, proposedValue={}, auditRef={}",
                      command.beforeValue(), command.proposedValue(), command.auditRef());

            String messageId = "LOG-" + UUID.randomUUID().toString().substring(0, 8);
            return WritebackResult.success(messageId);
        }
    }

    /**
     * Worker模块专用的对账适配器 — 不依赖api模块的BusinessMetrics。
     * 决策逻辑与FallbackClaimReconciliationAdapter完全一致（3条规则，FIRST命中策略）。
     */
    static class WorkerFallbackClaimReconciliationAdapter implements ClaimReconciliationPort {

        private static final Logger log = LoggerFactory.getLogger(WorkerFallbackClaimReconciliationAdapter.class);

        @Override
        public ReconciliationResult reconcile(boolean conflictDetected, boolean authoritativeMatch, boolean evidenceComplete) {
            ReconciliationResult result;

            // Rule 1: conflictDetected=true → CONFLICT_REQUIRES_HUMAN_REVIEW
            if (conflictDetected) {
                result = new ReconciliationResult(
                    ReconciliationStatus.CONFLICT_REQUIRES_HUMAN_REVIEW,
                    "Rule-1: conflictDetected=true → CONFLICT_REQUIRES_HUMAN_REVIEW");
            }
            // Rule 2: conflictDetected=false AND authoritativeMatch=true AND evidenceComplete=true → VERIFIED_FACT
            else if (authoritativeMatch && evidenceComplete) {
                result = new ReconciliationResult(
                    ReconciliationStatus.VERIFIED_FACT,
                    "Rule-2: conflictDetected=false, authoritativeMatch=true, evidenceComplete=true → VERIFIED_FACT");
            }
            // Rule 3: conflictDetected=false AND (其余情况) → CANDIDATE_CLAIM
            else {
                result = new ReconciliationResult(
                    ReconciliationStatus.CANDIDATE_CLAIM,
                    "Rule-3: conflictDetected=false, fallback → CANDIDATE_CLAIM");
            }

            log.info("[CLAIM-RECONCILIATION] conflictDetected={}, authoritativeMatch={}, evidenceComplete={} → {}",
                     conflictDetected, authoritativeMatch, evidenceComplete, result.status());
            return result;
        }
    }

    /**
     * Worker模块专用的事件发布器 — 桥接Spring ApplicationEventPublisher。
     */
    static class WorkerSpringEventPublisher implements DomainEventPublisher {

        private static final Logger log = LoggerFactory.getLogger(WorkerSpringEventPublisher.class);
        private final ApplicationEventPublisher delegate;

        WorkerSpringEventPublisher(ApplicationEventPublisher delegate) {
            this.delegate = delegate;
        }

        @Override
        public void publish(CloudEvent event) {
            log.info("[DOMAIN-EVENT] Publishing: type={}, id={}, subject={}",
                     event.type(), event.id(), event.subject());
            delegate.publishEvent(event);
        }
    }

    /**
     * Worker模块专用的内存声明仓储 — 用于开发和测试环境。
     * 生产环境应替换为JdbcClaimRepository。
     */
    static class WorkerInMemoryClaimRepository implements WritableClaimRepository {

        private static final Logger log = LoggerFactory.getLogger(WorkerInMemoryClaimRepository.class);
        private final ConcurrentHashMap<UUID, Claim> store = new ConcurrentHashMap<>();

        @Override
        public Optional<Claim> findById(UUID claimId) {
            return Optional.ofNullable(store.get(claimId));
        }

        @Override
        public List<Claim> findByCaseId(UUID caseId) {
            return store.values().stream()
                    .filter(c -> c.caseId().equals(caseId))
                    .toList();
        }

        @Override
        public void save(Claim claim) {
            store.put(claim.claimId(), claim);
            log.info("[CLAIM-REPO] Saved claim: claimId={}, type={}, status={}",
                     claim.claimId(), claim.claimType(), claim.status());
        }

        @Override
        public void updateStatus(UUID claimId, ClaimStatus status) {
            Claim existing = store.get(claimId);
            if (existing != null) {
                Claim updated = new Claim(
                        existing.claimId(), existing.caseId(), existing.claimType(), status,
                        existing.statement(), existing.validFrom(), existing.validTo(),
                        existing.recordedAt(), existing.supersedesClaimId());
                store.put(claimId, updated);
                log.info("[CLAIM-REPO] Updated status: claimId={}, newStatus={}", claimId, status);
            } else {
                log.warn("[CLAIM-REPO] Claim not found for status update: claimId={}", claimId);
            }
        }
    }
}
