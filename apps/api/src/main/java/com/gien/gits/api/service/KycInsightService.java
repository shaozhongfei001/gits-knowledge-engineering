package com.gien.gits.api.service;

import com.gien.gits.api.metrics.BusinessMetrics;
import com.gien.gits.ontology.port.ClaimReconciliationPort;
import com.gien.gits.ontology.port.DomainEventPublisher;
import com.gien.gits.ontology.port.WritableExternalEventRepository;
import com.gien.gits.ontology.port.WritableFactReconciliationRepository;
import com.gien.gits.ontology.port.WritableKycGapProfileRepository;
import com.gien.gits.ontology.port.WritableOpportunitySignalRepository;
import com.gien.gits.ontology.port.WritablePolicyRuleRepository;
import com.gien.gits.ontology.port.WritableProductCatalogRepository;
import com.gien.gits.ontology.*;
import com.gien.gits.adapter.event.CloudEventFactory;

import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * KYC与洞察服务 — KYC缺口分析 + 事实对账 + 机会信号识别
 * 核心规则: Claim≠Fact (禁令#6), OpportunitySignal≠Opportunity (禁令#7)
 * P11 G3: 事实对账决策通过ClaimReconciliationPort委托给DMN决策引擎
 */
public class KycInsightService {

    private final WritableKycGapProfileRepository kycGapRepo;
    private final WritableFactReconciliationRepository factRecRepo;
    private final WritableOpportunitySignalRepository signalRepo;
    private final WritableExternalEventRepository externalEventRepo;
    private final WritableProductCatalogRepository productCatalogRepo;
    private final WritablePolicyRuleRepository policyRuleRepo;
    private final ClaimReconciliationPort claimReconciliationPort;
    private final DomainEventPublisher domainEventPublisher;
    private final BusinessMetrics businessMetrics;

    public KycInsightService(
            WritableKycGapProfileRepository kycGapRepo,
            WritableFactReconciliationRepository factRecRepo,
            WritableOpportunitySignalRepository signalRepo,
            WritableExternalEventRepository externalEventRepo,
            WritableProductCatalogRepository productCatalogRepo,
            WritablePolicyRuleRepository policyRuleRepo,
            ClaimReconciliationPort claimReconciliationPort,
            DomainEventPublisher domainEventPublisher,
            BusinessMetrics businessMetrics) {
        this.kycGapRepo = Objects.requireNonNull(kycGapRepo);
        this.factRecRepo = Objects.requireNonNull(factRecRepo);
        this.signalRepo = Objects.requireNonNull(signalRepo);
        this.externalEventRepo = Objects.requireNonNull(externalEventRepo);
        this.productCatalogRepo = Objects.requireNonNull(productCatalogRepo);
        this.policyRuleRepo = Objects.requireNonNull(policyRuleRepo);
        this.claimReconciliationPort = Objects.requireNonNull(claimReconciliationPort);
        this.domainEventPublisher = Objects.requireNonNull(domainEventPublisher);
        this.businessMetrics = Objects.requireNonNull(businessMetrics);
    }

    /**
     * 获取KYC缺口档案
     */
    public Optional<KycGapProfile> getKycGapProfile(String customerId) {
        return kycGapRepo.findLatestByCustomerId(customerId);
    }

    public void saveKycGapProfile(KycGapProfile profile) {
        kycGapRepo.save(profile);
    }

    /**
     * 事实对账 — 核心逻辑
     * AT-001/AT-002: "3000万"必须经过四维校验
     * P11 G3: 通过ClaimReconciliationPort委托DMN决策引擎确定对账结果状态
     *         原有手写if-else逻辑已迁移至FallbackClaimReconciliationAdapter
     */
    @Transactional
    public FactReconciliationCase createReconciliation(
            String caseId, String topic, String structuredFact,
            String interactionClaim, String externalFact,
            List<String> ontologyDistinction, String correctJudgment,
            List<String> wrongOutputExamples, String nextAction,
            boolean conflictDetected, boolean authoritativeMatch, boolean evidenceComplete) {
        // 委托DMN决策引擎确定对账结果状态
        ClaimReconciliationPort.ReconciliationResult result =
            claimReconciliationPort.reconcile(conflictDetected, authoritativeMatch, evidenceComplete);

        ReconciliationStatus status = mapReconciliationStatus(result.status());
        businessMetrics.recordClaimReconciliation(status.name());
        FactReconciliationCase rec = new FactReconciliationCase(
            "REC-" + UUID.randomUUID().toString().substring(0, 8),
            caseId, topic, structuredFact, interactionClaim, externalFact,
            ontologyDistinction, correctJudgment, wrongOutputExamples,
            nextAction, status);
        factRecRepo.save(rec);

        // 发布领域事件: claimCandidateRecorded
        domainEventPublisher.publish(
            CloudEventFactory.claimCandidateRecorded(
                caseId,
                Map.of("reconciliationId", rec.reconciliationId(), "topic", topic)));

        return rec;
    }

    /**
     * 兼容旧调用 — 无决策参数时默认为CANDIDATE_CLAIM
     */
    @Transactional
    public FactReconciliationCase createReconciliation(
            String caseId, String topic, String structuredFact,
            String interactionClaim, String externalFact,
            List<String> ontologyDistinction, String correctJudgment,
            List<String> wrongOutputExamples, String nextAction) {
        return createReconciliation(caseId, topic, structuredFact,
            interactionClaim, externalFact, ontologyDistinction,
            correctJudgment, wrongOutputExamples, nextAction,
            false, false, false);
    }

    /**
     * 映射DMN决策结果到领域ReconciliationStatus
     */
    private ReconciliationStatus mapReconciliationStatus(ClaimReconciliationPort.ReconciliationStatus dmStatus) {
        return switch (dmStatus) {
            case CONFLICT_REQUIRES_HUMAN_REVIEW -> ReconciliationStatus.OPEN;
            case VERIFIED_FACT -> ReconciliationStatus.RESOLVED;
            case CANDIDATE_CLAIM -> ReconciliationStatus.OPEN;
        };
    }

    public List<FactReconciliationCase> getReconciliationsByCase(String caseId) {
        return factRecRepo.findByCaseId(caseId);
    }

    @Transactional
    public void resolveReconciliation(String reconciliationId) {
        factRecRepo.updateStatus(reconciliationId, ReconciliationStatus.RESOLVED);
    }

    /**
     * 识别机会信号 — 核心逻辑
     * 禁令#7: OpportunitySignal≠Opportunity
     * "3000万" → OpportunitySignal (FINANCING_NEED), 不是Opportunity
     */
    @Transactional
    public OpportunitySignal detectSignal(
            String operatingCaseId, String journeyId,
            OpportunitySignal.SignalType signalType,
            String content, OpportunitySignal.SignalSourceType sourceType,
            String sourceRef, BigDecimal confidence, String evidenceRef) {
        OpportunitySignal signal = new OpportunitySignal(
            UUID.randomUUID(), operatingCaseId, journeyId,
            signalType, content, sourceType, sourceRef,
            confidence, OpportunitySignal.SignalStatus.DETECTED,
            evidenceRef, Instant.now(), null);
        signalRepo.save(signal);
        return signal;
    }

    public List<OpportunitySignal> getSignalsByCase(String operatingCaseId) {
        return signalRepo.findByOperatingCaseId(operatingCaseId);
    }

    @Transactional
    public void confirmSignal(UUID signalId) {
        signalRepo.updateStatus(signalId, OpportunitySignal.SignalStatus.CONFIRMED);
    }

    @Transactional
    public void dismissSignal(UUID signalId) {
        signalRepo.updateStatus(signalId, OpportunitySignal.SignalStatus.DISMISSED);
    }

    /**
     * 获取外部事件
     */
    public List<ExternalEvent> getRecentExternalEvents(int limit) {
        return externalEventRepo.findRecent(limit);
    }

    public List<ExternalEvent> getEventsByEntity(String entity) {
        return externalEventRepo.findByEntity(entity);
    }

    /**
     * 获取产品知识卡片
     */
    public List<ProductKnowledgeCard> getAllProducts() {
        return productCatalogRepo.findAll();
    }

    public Optional<ProductKnowledgeCard> getProduct(String productId) {
        return productCatalogRepo.findByProductId(productId);
    }

    /**
     * 获取政策规则
     */
    public List<PolicyRule> getAllPolicyRules() {
        return policyRuleRepo.findAll();
    }

    public List<PolicyRule> getCriticalPolicyRules() {
        return policyRuleRepo.findBySeverity("CRITICAL");
    }

    public void saveProduct(ProductKnowledgeCard card) {
        productCatalogRepo.save(card);
    }

    public void savePolicyRule(PolicyRule rule) {
        policyRuleRepo.save(rule);
    }

    public void saveExternalEvent(ExternalEvent event) {
        externalEventRepo.save(event);
    }
}
