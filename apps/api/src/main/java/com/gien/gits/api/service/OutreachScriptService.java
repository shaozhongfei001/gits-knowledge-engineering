package com.gien.gits.api.service;

import com.gien.gits.api.service.prompt.PromptTemplate;
import com.gien.gits.engagement.OutreachScript;
import com.gien.gits.engagement.OutreachScript.OutreachChannel;
import com.gien.gits.engagement.OutreachScript.TalkingPoint;
import com.gien.gits.engagement.port.LlmClient;
import com.gien.gits.engagement.port.LlmClientException;
import com.gien.gits.engagement.port.WritableOutreachScriptRepository;
import com.gien.gits.ontology.Customer;
import com.gien.gits.ontology.CustomerTier;
import com.gien.gits.ontology.KycGapProfile;
import com.gien.gits.ontology.OpportunitySignal;
import com.gien.gits.ontology.RiskLevel;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 外联脚本生成服务 — 基于客户画像+KYC缺口+机会信号动态生成外联话术
 * P11 G5: 注入LlmClient，先尝试LLM生成，失败fallback到原有模板逻辑
 */
public class OutreachScriptService {

    private final CustomerContextService customerContextService;
    private final KycInsightService kycInsightService;
    private final CustomerJourneyService journeyService;
    private final WritableOutreachScriptRepository scriptRepository;
    private final LlmClient llmClient;

    public OutreachScriptService(
            CustomerContextService customerContextService,
            KycInsightService kycInsightService,
            CustomerJourneyService journeyService,
            WritableOutreachScriptRepository scriptRepository,
            LlmClient llmClient) {
        this.customerContextService = Objects.requireNonNull(customerContextService);
        this.kycInsightService = Objects.requireNonNull(kycInsightService);
        this.journeyService = Objects.requireNonNull(journeyService);
        this.scriptRepository = Objects.requireNonNull(scriptRepository);
        this.llmClient = Objects.requireNonNull(llmClient);
    }

    /**
     * 生成外联脚本 — 先尝试LLM，失败fallback到模板逻辑
     */
    public OutreachScript generateScript(String customerId, String rmId,
                                          String operatingCaseId, String journeyId,
                                          OutreachChannel channel) {
        Customer customer = customerContextService.findCustomer(customerId)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));

        Optional<KycGapProfile> kycGap = kycInsightService.getKycGapProfile(customerId);
        List<OpportunitySignal> signals = operatingCaseId != null
            ? kycInsightService.getSignalsByCase(operatingCaseId)
            : List.of();

        // P11 G5: 先尝试LLM生成
        try {
            OutreachScript script = generateWithLlm(customer, kycGap, signals,
                customerId, rmId, operatingCaseId, journeyId, channel);
            scriptRepository.save(script);
            return script;
        } catch (LlmClientException e) {
            // LLM失败，fallback到原有模板逻辑
        }

        // 原有模板逻辑
        String objective = buildObjective(customer, kycGap, signals);
        String openingLine = buildOpeningLine(customer, channel);
        List<TalkingPoint> talkingPoints = buildTalkingPoints(customer, kycGap, signals);
        List<String> riskReminders = buildRiskReminders(customer);
        String closingLine = buildClosingLine(talkingPoints);
        String followUpAction = buildFollowUpAction(talkingPoints, kycGap);

        OutreachScript script = new OutreachScript(
            "OS-" + UUID.randomUUID().toString().substring(0, 8),
            customerId, rmId, operatingCaseId, journeyId,
            channel, objective, openingLine, talkingPoints,
            riskReminders, closingLine, followUpAction,
            Instant.now());
        scriptRepository.save(script);
        return script;
    }

    /**
     * P11 G5: 使用LLM生成外联脚本
     */
    private OutreachScript generateWithLlm(Customer customer,
                                            Optional<KycGapProfile> kycGap,
                                            List<OpportunitySignal> signals,
                                            String customerId, String rmId,
                                            String operatingCaseId, String journeyId,
                                            OutreachChannel channel) {
        String systemPrompt = PromptTemplate.outreachScriptSystemPrompt();
        String customerContext = customer.customerName() + " | " + customer.industry()
            + " | " + customer.enterpriseScale() + " | " + customer.customerTier();
        String kycGapsStr = kycGap.map(g -> {
            List<String> all = new ArrayList<>();
            all.addAll(g.unknownItems());
            all.addAll(g.partialKnownItems());
            return String.join("、", all);
        }).orElse("无");
        String journeyStage = journeyId != null ? "进行中" : "初始阶段";

        String userPrompt = PromptTemplate.outreachScriptUserPrompt(
            customer.customerName(), customerContext, kycGapsStr, journeyStage);

        String llmResponse = llmClient.complete(systemPrompt, userPrompt);

        // 解析LLM JSON响应，构建OutreachScript
        String objective = extractJsonField(llmResponse, "purposeStatement",
            buildObjective(customer, kycGap, signals));
        String openingLine = extractJsonField(llmResponse, "greeting",
            buildOpeningLine(customer, channel));
        List<TalkingPoint> talkingPoints = extractTalkingPointsFromJson(llmResponse,
            buildTalkingPoints(customer, kycGap, signals));
        List<String> riskReminders = buildRiskReminders(customer);
        String closingLine = extractJsonField(llmResponse, "closingStatement",
            buildClosingLine(talkingPoints));
        String followUpAction = String.join(" → ",
            extractJsonArrayField(llmResponse, "proposedActions",
                List.of(buildFollowUpAction(talkingPoints, kycGap))));

        return new OutreachScript(
            "OS-" + UUID.randomUUID().toString().substring(0, 8),
            customerId, rmId, operatingCaseId, journeyId,
            channel, objective, openingLine, talkingPoints,
            riskReminders, closingLine, followUpAction,
            Instant.now());
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

    private List<TalkingPoint> extractTalkingPointsFromJson(String json, List<TalkingPoint> fallback) {
        try {
            List<String> topics = extractJsonArrayField(json, "keyTopics", List.of());
            if (topics.isEmpty()) return fallback;
            List<TalkingPoint> points = new ArrayList<>();
            int priority = 1;
            for (String topic : topics) {
                points.add(new TalkingPoint(topic, topic, "关于" + topic + "，我们可以提供相应的方案。", priority++));
            }
            return points;
        } catch (Exception e) {
            return fallback;
        }
    }

    // ── 开场白生成 ──────────────────────────────────────────────

    private String buildOpeningLine(Customer customer, OutreachChannel channel) {
        String name = customer.customerShortName() != null
            ? customer.customerShortName() : customer.customerName();
        CustomerTier tier = customer.customerTier();

        return switch (channel) {
            case PHONE -> switch (tier) {
                case STRATEGIC -> String.format("尊敬的%s负责人您好，我是您的专属客户经理，今天致电是想和您沟通一下近期的合作进展。", name);
                case KEY -> String.format("%s您好，我是您的客户经理，最近我们有一些新的服务方案想和您分享。", name);
                case GROWTH -> String.format("您好，我是负责%s的客户经理，想跟您聊聊我们能为贵司提供哪些支持。", name);
                case GENERAL -> String.format("您好，我是银行客户经理，想和您介绍一下我们针对%s行业的最新方案。", name);
            };
            case WECHAT -> switch (tier) {
                case STRATEGIC -> String.format("%s领导好，我是您的专属客户经理，有新的合作方案想和您交流。", name);
                case KEY -> String.format("%s您好，我是您的客户经理，最近有一些适合贵司的方案想分享给您。", name);
                default -> String.format("您好，我是负责贵司的客户经理，有一些最新方案想跟您分享。");
            };
            case EMAIL -> String.format("尊敬的%s，您好！感谢贵司长期以来的信任与合作。", name);
            case FACE_TO_FACE -> switch (tier) {
                case STRATEGIC -> String.format("尊敬的%s领导，非常感谢您百忙之中抽出时间，我们今天主要就近期合作方向做个沟通。", name);
                default -> String.format("%s您好，感谢您的时间，今天想就贵司的金融服务需求做一些交流。", name);
            };
        };
    }

    // ── 谈话要点生成 ────────────────────────────────────────────

    private List<TalkingPoint> buildTalkingPoints(Customer customer,
                                                   Optional<KycGapProfile> kycGap,
                                                   List<OpportunitySignal> signals) {
        List<TalkingPoint> points = new ArrayList<>();
        int priority = 1;

        // 1. KYC缺口 → 高优先级谈话要点
        if (kycGap.isPresent()) {
            KycGapProfile gap = kycGap.get();
            for (String unknown : gap.unknownItems()) {
                points.add(new TalkingPoint(
                    "KYC信息补全", unknown, "能否请您介绍一下" + unknown + "的情况？", priority++));
            }
            for (String partial : gap.partialKnownItems()) {
                points.add(new TalkingPoint(
                    "KYC信息确认", partial, "关于" + partial + "，我们想确认一下最新的情况。", priority++));
            }
        }

        // 2. 机会信号 → 中优先级谈话要点
        for (OpportunitySignal signal : signals) {
            if (signal.status() == OpportunitySignal.SignalStatus.DETECTED
                || signal.status() == OpportunitySignal.SignalStatus.CONFIRMED) {
                points.add(new TalkingPoint(
                    "业务机会", signal.content(),
                    "关于" + signal.content() + "，我们可以提供相应的金融支持方案。",
                    priority++));
            }
        }

        // 3. 行业/规模相关通用话题
        if (points.isEmpty()) {
            points.add(new TalkingPoint(
                "经营状况了解", customer.industry() + "行业经营情况",
                "贵司近期经营情况如何？有没有新的发展计划？", priority));
        }

        return points;
    }

    // ── 风险提醒生成 ────────────────────────────────────────────

    private List<String> buildRiskReminders(Customer customer) {
        List<String> reminders = new ArrayList<>();
        RiskLevel riskLevel = customer.riskLevel();

        if (riskLevel == RiskLevel.HIGH) {
            reminders.add("客户风险等级为HIGH，请严格遵守高风险客户接触规范");
            reminders.add("避免做出超出授权范围的承诺");
            reminders.add("所有承诺事项需在24小时内录入系统");
        } else if (riskLevel == RiskLevel.MEDIUM) {
            reminders.add("客户风险等级为MEDIUM，请注意风险相关话题的措辞");
            reminders.add("涉及额度调整需经审批流程确认");
        }

        // 事实对账提醒
        reminders.add("注意核实客户陈述事实与系统记录的一致性");

        return reminders;
    }

    // ── 目标生成 ────────────────────────────────────────────────

    private String buildObjective(Customer customer, Optional<KycGapProfile> kycGap,
                                   List<OpportunitySignal> signals) {
        StringBuilder obj = new StringBuilder();
        obj.append("维护").append(customer.customerShortName() != null
            ? customer.customerShortName() : customer.customerName()).append("客户关系");

        if (!signals.isEmpty()) {
            obj.append("，跟进").append(signals.size()).append("个业务机会信号");
        }
        if (kycGap.isPresent() && !kycGap.get().unknownItems().isEmpty()) {
            obj.append("，补全KYC信息缺口");
        }

        return obj.toString();
    }

    // ── 结束语生成 ──────────────────────────────────────────────

    private String buildClosingLine(List<TalkingPoint> talkingPoints) {
        if (talkingPoints.isEmpty()) {
            return "感谢您的时间，后续有任何需要请随时联系。";
        }
        String mainTopic = talkingPoints.get(0).topic();
        return "感谢您关于" + mainTopic + "的交流，我们会尽快跟进相关事项，后续保持联系。";
    }

    // ── 跟进动作生成 ────────────────────────────────────────────

    private String buildFollowUpAction(List<TalkingPoint> talkingPoints,
                                        Optional<KycGapProfile> kycGap) {
        List<String> actions = new ArrayList<>();

        if (kycGap.isPresent() && !kycGap.get().unknownItems().isEmpty()) {
            actions.add("更新KYC信息档案");
        }
        if (!talkingPoints.isEmpty()) {
            actions.add("整理谈话要点并录入系统");
        }
        boolean hasOpportunity = talkingPoints.stream()
            .anyMatch(tp -> "业务机会".equals(tp.topic()));
        if (hasOpportunity) {
            actions.add("准备产品方案并安排下次沟通");
        }

        if (actions.isEmpty()) {
            actions.add("记录本次联系情况");
        }

        return String.join(" → ", actions);
    }
}
