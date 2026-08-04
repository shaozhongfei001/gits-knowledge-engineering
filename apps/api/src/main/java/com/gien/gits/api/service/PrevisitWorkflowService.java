package com.gien.gits.api.service;

import com.gien.gits.ontology.port.WritableCommitmentRepository;
import com.gien.gits.customerjourney.*;
import com.gien.gits.engagement.*;
import com.gien.gits.ontology.*;

import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * 访前工作流服务 — 生成访前报告(R1) + 60秒作战卡(R2) + 外联脚本(R3)
 */
public class PrevisitWorkflowService {

    private final CustomerContextService customerContextService;
    private final KycInsightService kycInsightService;
    private final WritableCommitmentRepository commitmentRepo;

    public PrevisitWorkflowService(
            CustomerContextService customerContextService,
            KycInsightService kycInsightService,
            WritableCommitmentRepository commitmentRepo) {
        this.customerContextService = Objects.requireNonNull(customerContextService);
        this.kycInsightService = Objects.requireNonNull(kycInsightService);
        this.commitmentRepo = Objects.requireNonNull(commitmentRepo);
    }

    /**
     * 生成访前报告 (R1) — 基于客户上下文 + KYC缺口 + 产品匹配
     */
    @Transactional(readOnly = true)
    public PrevisitReportContent generatePrevisitReport(
            String customerId, String journeyId, String operatingCaseId, String visitObjective) {

        CustomerContextService.CustomerOperatingView view = customerContextService.buildOperatingView(customerId);
        Customer customer = view.customer();

        Optional<KycGapProfile> kycGap = kycInsightService.getKycGapProfile(customerId);
        List<ProductKnowledgeCard> products = kycInsightService.getAllProducts();
        List<PolicyRule> criticalRules = kycInsightService.getCriticalPolicyRules();
        List<ExternalEvent> events = kycInsightService.getRecentExternalEvents(5);
        List<Commitment> openCommitments = commitmentRepo.findByOperatingCaseId(operatingCaseId);

        // 组装客户概览
        PrevisitReportContent.CustomerOverview overview = new PrevisitReportContent.CustomerOverview(
            customer.industry().name(), customer.enterpriseScale().name(), customer.customerTier().name(),
            customer.registeredCapitalCny(), customer.riskLevel().name(), customer.relationshipSummary());

        // 组装KYC缺口摘要
        PrevisitReportContent.KycGapSummary kycSummary = kycGap.map(k -> new PrevisitReportContent.KycGapSummary(
            k.knownItems(), k.partialKnownItems(), k.unknownItems(), k.priorityQuestions()))
            .orElseGet(() -> new PrevisitReportContent.KycGapSummary(
                List.of(), List.of(), List.of(), List.of()));

        // 产品匹配 — 基于客户特征和触发条件
        List<PrevisitReportContent.ProductScheme> schemes = matchProducts(customer, view, products, events);

        // 关键问题 — 从KYC缺口和外部事件推导
        List<String> keyQuestions = deriveKeyQuestions(kycGap, events, openCommitments);

        // 风险提醒 — 从政策规则推导
        List<String> riskReminders = criticalRules.stream()
            .map(r -> "[" + r.severity() + "] " + r.logic())
            .toList();

        // 访问策略
        String visitStrategy = deriveVisitStrategy(customer, kycGap, events);

        return new PrevisitReportContent(
            "R1-" + UUID.randomUUID().toString().substring(0, 8),
            customerId, customer.customerName(), customer.rmName(),
            visitObjective, overview, kycSummary, schemes,
            keyQuestions, riskReminders, visitStrategy);
    }

    /**
     * 生成60秒作战卡 (R2) — 移动端快速查看
     */
    @Transactional(readOnly = true)
    public QuickBattleCard generateQuickBattleCard(
            String customerId, String visitObjective) {

        CustomerContextService.CustomerOperatingView view = customerContextService.buildOperatingView(customerId);
        Customer customer = view.customer();

        List<String> keyPoints = new ArrayList<>();
        keyPoints.add("客户等级: " + customer.customerTier().name());
        view.bankRelationship().ifPresent(b -> {
            keyPoints.add("日均存款: " + formatCny(b.avgDailyDepositCny()));
            keyPoints.add("可用额度: " + formatCny(b.availableCreditCny()));
            if (b.anomalyFlags() != null && !b.anomalyFlags().isBlank()) {
                keyPoints.add("异常标记: " + b.anomalyFlags());
            }
        });

        List<String> productHints = kycInsightService.getAllProducts().stream()
            .filter(p -> matchesTrigger(p, customer))
            .map(p -> p.name() + ": " + p.trigger())
            .limit(3)
            .toList();

        List<String> dontForget = List.of(
            "不要承诺授信额度 (禁令#3)",
            "不要将客户表达直接视为事实 (禁令#6)",
            "不要将信号等同于机会 (禁令#7)");

        return new QuickBattleCard(
            "R2-" + UUID.randomUUID().toString().substring(0, 8),
            customer.customerName(), visitObjective,
            customer.customerTier().name(), customer.riskLevel().name(),
            keyPoints, productHints, dontForget,
            "收集事实，识别信号，不做承诺");
    }

    // --- 私有辅助方法 ---

    private List<PrevisitReportContent.ProductScheme> matchProducts(
            Customer customer, CustomerContextService.CustomerOperatingView view,
            List<ProductKnowledgeCard> products, List<ExternalEvent> events) {
        return products.stream()
            .filter(p -> matchesTrigger(p, customer))
            .map(p -> new PrevisitReportContent.ProductScheme(
                p.productId(), p.name(), p.trigger(),
                estimateAmount(p, view), estimateTerm(p),
                p.keyConditions(), p.requiredMaterials(), p.riskPoints()))
            .toList();
    }

    private boolean matchesTrigger(ProductKnowledgeCard product, Customer customer) {
        String trigger = product.trigger();
        if (trigger == null) return false;
        String industry = customer.industry() != null ? customer.industry().name() : "";
        String tier = customer.customerTier() != null ? customer.customerTier().name() : "";
        return trigger.contains(industry) || trigger.contains(tier) ||
               trigger.contains("制造业") && industry.contains("MANUFACTURING");
    }

    private String estimateAmount(ProductKnowledgeCard product, CustomerContextService.CustomerOperatingView view) {
        return view.bankRelationship()
            .map(b -> "建议额度待KYC确认")
            .orElse("需评估");
    }

    private String estimateTerm(ProductKnowledgeCard product) {
        return "1年";
    }

    private List<String> deriveKeyQuestions(
            Optional<KycGapProfile> kycGap, List<ExternalEvent> events,
            List<Commitment> openCommitments) {
        List<String> questions = new ArrayList<>();
        kycGap.ifPresent(k -> questions.addAll(k.priorityQuestions()));
        events.stream()
            .filter(e -> e.possibleBusinessSignal() != null)
            .forEach(e -> questions.add("确认外部信号: " + e.title()));
        openCommitments.stream()
            .filter(c -> c.status() == Commitment.CommitmentStatus.OPEN)
            .forEach(c -> questions.add("跟进承诺: " + c.content()));
        return questions;
    }

    private String deriveVisitStrategy(
            Customer customer, Optional<KycGapProfile> kycGap, List<ExternalEvent> events) {
        StringBuilder sb = new StringBuilder();
        sb.append("以了解客户经营现状为主，");
        kycGap.ifPresent(k -> {
            if (!k.unknownItems().isEmpty()) {
                sb.append("重点补全").append(k.unknownItems().size()).append("项未知信息，");
            }
        });
        if (!events.isEmpty()) {
            sb.append("关注").append(events.size()).append("项外部信号，");
        }
        sb.append("避免直接推销产品。");
        return sb.toString();
    }

    private String formatCny(long amount) {
        if (amount >= 100_000_000) return String.format("%.1f亿", amount / 100_000_000.0);
        if (amount >= 10_000) return String.format("%.0f万", amount / 10_000.0);
        return amount + "元";
    }
}
