package com.gien.gits.api.service;

import com.gien.gits.engagement.CustomerOperatingView;
import com.gien.gits.engagement.CustomerOperatingView.CommitmentSummary;
import com.gien.gits.engagement.CustomerOperatingView.OpportunitySignalSummary;
import com.gien.gits.ontology.*;
import com.gien.gits.ontology.port.*;
import com.gien.gits.customerjourney.CustomerJourney;
import com.gien.gits.customerjourney.JourneyPhase;
import com.gien.gits.customerjourney.port.CustomerJourneyRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 客户经营视图服务 — 聚合客户全维度经营信息
 * 从多个数据源动态组装CustomerOperatingView，用于报告数据驱动化
 */
public class CustomerOperatingViewService {

    private final CustomerRepository customerRepo;
    private final InteractionRepository interactionRepo;
    private final ClaimRepository claimRepo;
    private final KycGapProfileRepository kycGapRepo;
    private final OpportunitySignalRepository signalRepo;
    private final FactReconciliationRepository factRecRepo;
    private final CustomerJourneyRepository journeyRepo;

    public CustomerOperatingViewService(
            CustomerRepository customerRepo,
            InteractionRepository interactionRepo,
            ClaimRepository claimRepo,
            KycGapProfileRepository kycGapRepo,
            OpportunitySignalRepository signalRepo,
            FactReconciliationRepository factRecRepo,
            CustomerJourneyRepository journeyRepo) {
        this.customerRepo = Objects.requireNonNull(customerRepo);
        this.interactionRepo = Objects.requireNonNull(interactionRepo);
        this.claimRepo = Objects.requireNonNull(claimRepo);
        this.kycGapRepo = Objects.requireNonNull(kycGapRepo);
        this.signalRepo = Objects.requireNonNull(signalRepo);
        this.factRecRepo = Objects.requireNonNull(factRecRepo);
        this.journeyRepo = Objects.requireNonNull(journeyRepo);
    }

    /**
     * 根据客户ID构建经营视图
     * 需要通过operatingCaseId关联查询各维度数据
     */
    public Optional<CustomerOperatingView> buildView(String customerId, String operatingCaseId) {
        Optional<Customer> customerOpt = customerRepo.findById(customerId);
        if (customerOpt.isEmpty()) {
            return Optional.empty();
        }
        Customer customer = customerOpt.get();
        UUID caseUuid = UUID.fromString(operatingCaseId);

        // KYC缺口
        List<String> knownItems = List.of();
        List<String> partialItems = List.of();
        List<String> unknownItems = List.of();
        Optional<KycGapProfile> kycProfile = kycGapRepo.findLatestByCustomerId(customerId);
        if (kycProfile.isPresent()) {
            KycGapProfile profile = kycProfile.get();
            knownItems = profile.knownItems() != null ? profile.knownItems() : List.of();
            partialItems = profile.partialKnownItems() != null ? profile.partialKnownItems() : List.of();
            unknownItems = profile.unknownItems() != null ? profile.unknownItems() : List.of();
        }

        // 机会信号 — 通过operatingCaseId查询
        List<OpportunitySignal> signals = signalRepo.findByOperatingCaseId(operatingCaseId);
        List<OpportunitySignalSummary> activeSignals = signals.stream()
                .filter(s -> s.status() == OpportunitySignal.SignalStatus.DETECTED
                        || s.status() == OpportunitySignal.SignalStatus.CONFIRMED)
                .map(s -> new OpportunitySignalSummary(
                        s.signalType().name(),
                        s.content(),
                        s.confidence(),
                        s.status().name()))
                .collect(Collectors.toList());

        // 交互历史 — 通过caseId查询
        List<Interaction> interactions = interactionRepo.findByCaseId(caseUuid);
        int totalInteractions = interactions.size();
        Instant lastInteractionTime = interactions.stream()
                .map(Interaction::occurredAt)
                .filter(Objects::nonNull)
                .max(Instant::compareTo)
                .orElse(null);

        // 旅程状态 — 通过caseId查询
        List<CustomerJourney> journeys = journeyRepo.findJourneysByCaseId(caseUuid);
        int activeJourneyCount = (int) journeys.stream()
                .filter(j -> j.phase() != null && j.phase() != JourneyPhase.COMPLETED)
                .count();
        String currentJourneyPhase = journeys.stream()
                .filter(j -> j.phase() != null && j.phase() != JourneyPhase.COMPLETED)
                .map(j -> j.phase().name())
                .findFirst()
                .orElse(null);

        // 承诺跟踪 — 从Claim中提取，通过caseId查询
        List<Claim> claims = claimRepo.findByCaseId(caseUuid);
        List<CommitmentSummary> pendingCommitments = claims.stream()
                .filter(c -> c.claimType() == ClaimType.COMMITMENT
                        || c.claimType() == ClaimType.FOLLOW_UP)
                .filter(c -> c.status() == ClaimStatus.CANDIDATE || c.status() == ClaimStatus.HUMAN_CONFIRMED)
                .map(c -> new CommitmentSummary(
                        c.claimType().name(),
                        c.statement(),
                        null,
                        null,
                        c.status() == ClaimStatus.HUMAN_CONFIRMED))
                .collect(Collectors.toList());

        // 事实对账 — 通过caseId查询
        List<FactReconciliationCase> reconciliations = factRecRepo.findByCaseId(operatingCaseId);
        int openReconciliationCount = (int) reconciliations.stream()
                .filter(r -> r.status() == ReconciliationStatus.OPEN)
                .count();

        // 风险指标
        List<String> riskIndicators = new ArrayList<>();
        if (customer.riskLevel() == RiskLevel.HIGH) {
            riskIndicators.add("客户风险等级为高风险");
        }
        if (openReconciliationCount > 0) {
            riskIndicators.add("存在" + openReconciliationCount + "项未完成事实对账");
        }
        reconciliations.stream()
                .filter(r -> r.status() == ReconciliationStatus.OPEN)
                .map(FactReconciliationCase::topic)
                .filter(Objects::nonNull)
                .forEach(riskIndicators::add);

        return Optional.of(new CustomerOperatingView(
                customer.customerId(),
                customer.customerName(),
                customer.industry() != null ? customer.industry().name() : null,
                customer.enterpriseScale() != null ? customer.enterpriseScale().name() : null,
                customer.customerTier() != null ? customer.customerTier().name() : null,
                customer.riskLevel() != null ? customer.riskLevel().name() : null,
                customer.rmId(),
                customer.rmName(),
                knownItems, partialItems, unknownItems,
                activeSignals,
                totalInteractions, lastInteractionTime,
                activeJourneyCount, currentJourneyPhase,
                pendingCommitments,
                openReconciliationCount,
                riskIndicators));
    }
}
