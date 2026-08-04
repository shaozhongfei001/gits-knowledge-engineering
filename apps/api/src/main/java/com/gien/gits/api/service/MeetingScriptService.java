package com.gien.gits.api.service;

import com.gien.gits.api.service.prompt.PromptTemplate;
import com.gien.gits.engagement.MeetingScript;
import com.gien.gits.engagement.MeetingScript.AgendaItem;
import com.gien.gits.engagement.MeetingScript.KycQuestionItem;
import com.gien.gits.engagement.MeetingScript.ProductDiscussionItem;
import com.gien.gits.engagement.port.LlmClient;
import com.gien.gits.engagement.port.LlmClientException;
import com.gien.gits.engagement.port.WritableMeetingScriptRepository;
import com.gien.gits.ontology.Customer;
import com.gien.gits.ontology.KycGapProfile;
import com.gien.gits.ontology.OpportunitySignal;
import com.gien.gits.ontology.ProductKnowledgeCard;
import com.gien.gits.ontology.RiskLevel;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 会面脚本生成服务 — 基于访前报告+KYC缺口+产品匹配动态生成会面议程
 * P11 G5: 注入LlmClient，先尝试LLM生成，失败fallback到原有模板逻辑
 */
public class MeetingScriptService {

    private final CustomerContextService customerContextService;
    private final KycInsightService kycInsightService;
    private final CustomerJourneyService journeyService;
    private final WritableMeetingScriptRepository scriptRepository;
    private final LlmClient llmClient;

    public MeetingScriptService(
            CustomerContextService customerContextService,
            KycInsightService kycInsightService,
            CustomerJourneyService journeyService,
            WritableMeetingScriptRepository scriptRepository,
            LlmClient llmClient) {
        this.customerContextService = Objects.requireNonNull(customerContextService);
        this.kycInsightService = Objects.requireNonNull(kycInsightService);
        this.journeyService = Objects.requireNonNull(journeyService);
        this.scriptRepository = Objects.requireNonNull(scriptRepository);
        this.llmClient = Objects.requireNonNull(llmClient);
    }

    /**
     * 生成会面脚本 — 先尝试LLM，失败fallback到模板逻辑
     */
    public MeetingScript generateScript(String customerId, String rmId,
                                         String operatingCaseId, String journeyId) {
        Customer customer = customerContextService.findCustomer(customerId)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));

        Optional<KycGapProfile> kycGap = kycInsightService.getKycGapProfile(customerId);
        List<OpportunitySignal> signals = operatingCaseId != null
            ? kycInsightService.getSignalsByCase(operatingCaseId)
            : List.of();
        List<ProductKnowledgeCard> products = kycInsightService.getAllProducts();

        // P11 G5: 先尝试LLM生成
        try {
            MeetingScript script = generateWithLlm(customer, kycGap, signals, products,
                customerId, rmId, operatingCaseId, journeyId);
            scriptRepository.save(script);
            return script;
        } catch (LlmClientException e) {
            // LLM失败，fallback到原有模板逻辑
        }

        // 原有模板逻辑
        String meetingObjective = buildMeetingObjective(customer, kycGap, signals);
        String previsitSummary = buildPrevisitSummary(customer, kycGap);
        List<AgendaItem> agendaItems = buildAgendaItems(customer, kycGap, signals);
        List<KycQuestionItem> kycQuestions = buildKycQuestions(kycGap);
        List<ProductDiscussionItem> productDiscussions = buildProductDiscussions(signals, products);
        List<String> riskPoints = buildRiskPoints(customer);
        String closingSummary = buildClosingSummary(agendaItems, kycQuestions, productDiscussions);

        MeetingScript script = new MeetingScript(
            "MS-" + UUID.randomUUID().toString().substring(0, 8),
            customerId, rmId, operatingCaseId, journeyId,
            meetingObjective, previsitSummary, agendaItems,
            kycQuestions, productDiscussions, riskPoints,
            closingSummary, Instant.now());
        scriptRepository.save(script);
        return script;
    }

    /**
     * P11 G5: 使用LLM生成会面脚本
     */
    private MeetingScript generateWithLlm(Customer customer,
                                           Optional<KycGapProfile> kycGap,
                                           List<OpportunitySignal> signals,
                                           List<ProductKnowledgeCard> products,
                                           String customerId, String rmId,
                                           String operatingCaseId, String journeyId) {
        String systemPrompt = PromptTemplate.meetingScriptSystemPrompt();
        String customerContext = customer.customerName() + " | " + customer.industry()
            + " | " + customer.enterpriseScale() + " | " + customer.customerTier();
        String kycGapsStr = kycGap.map(g -> {
            List<String> all = new ArrayList<>();
            all.addAll(g.unknownItems());
            all.addAll(g.partialKnownItems());
            return String.join("、", all);
        }).orElse("无");
        String journeyStage = journeyId != null ? "进行中" : "初始阶段";

        String userPrompt = PromptTemplate.meetingScriptUserPrompt(
            customer.customerName(), customerContext, kycGapsStr, journeyStage);

        String llmResponse = llmClient.complete(systemPrompt, userPrompt);

        // 解析LLM JSON响应，构建MeetingScript
        String meetingObjective = extractJsonField(llmResponse, "opening",
            buildMeetingObjective(customer, kycGap, signals));
        String previsitSummary = buildPrevisitSummary(customer, kycGap);
        List<AgendaItem> agendaItems = extractAgendaItemsFromJson(llmResponse,
            buildAgendaItems(customer, kycGap, signals));
        List<KycQuestionItem> kycQuestions = buildKycQuestions(kycGap);
        List<ProductDiscussionItem> productDiscussions = buildProductDiscussions(signals, products);
        List<String> riskPoints = buildRiskPoints(customer);
        String closingSummary = extractJsonField(llmResponse, "closingSummary",
            buildClosingSummary(agendaItems, kycQuestions, productDiscussions));

        return new MeetingScript(
            "MS-" + UUID.randomUUID().toString().substring(0, 8),
            customerId, rmId, operatingCaseId, journeyId,
            meetingObjective, previsitSummary, agendaItems,
            kycQuestions, productDiscussions, riskPoints,
            closingSummary, Instant.now());
    }

    // ── LLM JSON解析辅助 ─────────────────────────────────────────

    private String extractJsonField(String json, String fieldName, String fallback) {
        try {
            String pattern = "\"" + fieldName + "\"";
            int idx = json.indexOf(pattern);
            if (idx < 0) return fallback;
            int colonIdx = json.indexOf(":", idx + pattern.length());
            if (colonIdx < 0) return fallback;
            int valueStart = json.indexOf("\"", colonIdx + 1);
            if (valueStart < 0) return fallback;
            int valueEnd = json.indexOf("\"", valueStart + 1);
            if (valueEnd < 0) return fallback;
            return json.substring(valueStart + 1, valueEnd);
        } catch (Exception e) {
            return fallback;
        }
    }

    private List<String> extractJsonArrayField(String json, String fieldName, List<String> fallback) {
        try {
            String pattern = "\"" + fieldName + "\"";
            int idx = json.indexOf(pattern);
            if (idx < 0) return fallback;
            int arrStart = json.indexOf("[", idx);
            int arrEnd = json.indexOf("]", arrStart);
            if (arrStart < 0 || arrEnd < 0) return fallback;
            String arrContent = json.substring(arrStart + 1, arrEnd);
            List<String> result = new ArrayList<>();
            for (String item : arrContent.split(",")) {
                String trimmed = item.trim().replaceAll("\"", "");
                if (!trimmed.isEmpty()) {
                    result.add(trimmed);
                }
            }
            return result.isEmpty() ? fallback : result;
        } catch (Exception e) {
            return fallback;
        }
    }

    private List<AgendaItem> extractAgendaItemsFromJson(String json, List<AgendaItem> fallback) {
        try {
            List<String> topics = extractJsonArrayField(json, "agendaItems", List.of());
            if (topics.isEmpty()) return fallback;
            List<AgendaItem> items = new ArrayList<>();
            int duration = 55 / Math.max(topics.size(), 1);
            for (String topic : topics) {
                items.add(new AgendaItem(topic, duration,
                    "讨论" + topic + "相关事项", "就" + topic + "达成共识"));
            }
            return items;
        } catch (Exception e) {
            return fallback;
        }
    }

    // ── 会面目标 ────────────────────────────────────────────────

    private String buildMeetingObjective(Customer customer,
                                          Optional<KycGapProfile> kycGap,
                                          List<OpportunitySignal> signals) {
        StringBuilder obj = new StringBuilder();
        String name = customer.customerShortName() != null
            ? customer.customerShortName() : customer.customerName();
        obj.append("拜访").append(name);

        boolean hasFinancing = signals.stream()
            .anyMatch(s -> s.signalType() == OpportunitySignal.SignalType.FINANCING_NEED);
        if (hasFinancing) {
            obj.append("，确认融资需求并推进方案");
        }

        if (kycGap.isPresent() && !kycGap.get().unknownItems().isEmpty()) {
            obj.append("，补全KYC关键信息");
        }

        return obj.toString();
    }

    // ── 访前摘要 ────────────────────────────────────────────────

    private String buildPrevisitSummary(Customer customer, Optional<KycGapProfile> kycGap) {
        StringBuilder summary = new StringBuilder();
        summary.append("客户: ").append(customer.customerName());
        summary.append(" | 行业: ").append(customer.industry());
        summary.append(" | 规模: ").append(customer.enterpriseScale());
        summary.append(" | 风险等级: ").append(customer.riskLevel());
        summary.append(" | 客户层级: ").append(customer.customerTier());

        if (customer.relationshipSummary() != null && !customer.relationshipSummary().isBlank()) {
            summary.append(" | 关系摘要: ").append(customer.relationshipSummary());
        }

        kycGap.ifPresent(gap -> {
            int totalGaps = gap.unknownItems().size() + gap.partialKnownItems().size();
            summary.append(" | KYC缺口: ").append(totalGaps).append("项待补全");
        });

        return summary.toString();
    }

    // ── 议程项 ──────────────────────────────────────────────────

    private List<AgendaItem> buildAgendaItems(Customer customer,
                                               Optional<KycGapProfile> kycGap,
                                               List<OpportunitySignal> signals) {
        List<AgendaItem> items = new ArrayList<>();

        // 1. 开场寒暄 (5min)
        items.add(new AgendaItem("开场寒暄", 5,
            "了解客户近期经营状况", "建立良好沟通氛围"));

        // 2. KYC信息补全 (15min, if gaps exist)
        if (kycGap.isPresent()) {
            KycGapProfile gap = kycGap.get();
            int gapCount = gap.unknownItems().size() + gap.partialKnownItems().size();
            if (gapCount > 0) {
                items.add(new AgendaItem("KYC信息补全", 15,
                    "补全" + gapCount + "项KYC信息缺口", "完善客户画像"));
            }
        }

        // 3. 业务机会讨论 (20min, if signals exist)
        List<OpportunitySignal> activeSignals = signals.stream()
            .filter(s -> s.status() == OpportunitySignal.SignalStatus.DETECTED
                || s.status() == OpportunitySignal.SignalStatus.CONFIRMED)
            .toList();
        if (!activeSignals.isEmpty()) {
            String signalDesc = activeSignals.stream()
                .map(OpportunitySignal::content)
                .collect(Collectors.joining("、"));
            items.add(new AgendaItem("业务机会讨论", 20,
                signalDesc, "确认业务需求并推进方案"));
        }

        // 4. 产品方案介绍 (10min)
        items.add(new AgendaItem("产品方案介绍", 10,
            "根据客户需求推荐合适的产品方案", "客户对产品方案有初步了解"));

        // 5. 总结与下一步 (5min)
        items.add(new AgendaItem("总结与下一步", 5,
            "确认双方行动项和时间节点", "明确后续跟进计划"));

        return items;
    }

    // ── KYC问题清单 ─────────────────────────────────────────────

    private List<KycQuestionItem> buildKycQuestions(Optional<KycGapProfile> kycGap) {
        List<KycQuestionItem> questions = new ArrayList<>();

        kycGap.ifPresent(gap -> {
            for (String unknown : gap.unknownItems()) {
                questions.add(new KycQuestionItem(
                    unknown,
                    "请问贵司的" + unknown + "情况如何？",
                    "补全KYC关键信息: " + unknown,
                    "TEXT"));
            }
            for (String partial : gap.partialKnownItems()) {
                questions.add(new KycQuestionItem(
                    partial,
                    "关于" + partial + "，我们目前掌握的信息是否仍然准确？",
                    "确认KYC信息时效性: " + partial,
                    "CONFIRMATION"));
            }
        });

        return questions;
    }

    // ── 产品讨论 ────────────────────────────────────────────────

    private List<ProductDiscussionItem> buildProductDiscussions(
            List<OpportunitySignal> signals, List<ProductKnowledgeCard> products) {
        List<ProductDiscussionItem> discussions = new ArrayList<>();

        // 基于融资需求信号匹配产品
        boolean hasFinancing = signals.stream()
            .anyMatch(s -> s.signalType() == OpportunitySignal.SignalType.FINANCING_NEED);

        for (ProductKnowledgeCard product : products) {
            // 简单匹配: 融资需求匹配所有信贷类产品
            if (hasFinancing && isCreditProduct(product)) {
                discussions.add(new ProductDiscussionItem(
                    product.productId(),
                    product.name(),
                    "基于客户融资需求推荐",
                    product.keyConditions().stream().limit(3).toList()));
            }
        }

        // 如果没有匹配的产品，添加通用讨论项
        if (discussions.isEmpty() && !products.isEmpty()) {
            ProductKnowledgeCard first = products.get(0);
            discussions.add(new ProductDiscussionItem(
                first.productId(),
                first.name(),
                "了解客户对现有产品的反馈",
                List.of("客户满意度", "改进建议")));
        }

        return discussions;
    }

    private boolean isCreditProduct(ProductKnowledgeCard product) {
        String name = product.name().toLowerCase();
        return name.contains("贷款") || name.contains("授信") || name.contains("融资")
            || name.contains("信贷") || name.contains("loan") || name.contains("credit")
            || name.contains("lending");
    }

    // ── 风险要点 ────────────────────────────────────────────────

    private List<String> buildRiskPoints(Customer customer) {
        List<String> points = new ArrayList<>();
        RiskLevel riskLevel = customer.riskLevel();

        if (riskLevel == RiskLevel.HIGH) {
            points.add("高风险客户: 所有承诺需经审批后确认");
            points.add("事实对账: 客户陈述需与系统记录交叉验证");
            points.add("合规提醒: 严格遵守高风险客户服务规范");
        } else if (riskLevel == RiskLevel.MEDIUM) {
            points.add("中风险客户: 注意核实关键经营数据");
            points.add("事实对账: 关注财务数据的一致性");
        } else {
            points.add("低风险客户: 常规服务流程");
        }

        return points;
    }

    // ── 结束总结 ────────────────────────────────────────────────

    private String buildClosingSummary(List<AgendaItem> agendaItems,
                                        List<KycQuestionItem> kycQuestions,
                                        List<ProductDiscussionItem> productDiscussions) {
        StringBuilder summary = new StringBuilder();
        summary.append("本次会面共安排").append(agendaItems.size()).append("个议程项");

        int totalMinutes = agendaItems.stream()
            .mapToInt(AgendaItem::durationMinutes).sum();
        summary.append("，预计").append(totalMinutes).append("分钟");

        if (!kycQuestions.isEmpty()) {
            summary.append("；需补全KYC信息").append(kycQuestions.size()).append("项");
        }
        if (!productDiscussions.isEmpty()) {
            summary.append("；讨论产品方案").append(productDiscussions.size()).append("个");
        }

        return summary.toString();
    }
}
