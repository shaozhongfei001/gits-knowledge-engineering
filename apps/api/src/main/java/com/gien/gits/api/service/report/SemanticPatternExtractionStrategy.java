package com.gien.gits.api.service.report;

import com.gien.gits.engagement.InteractionExtraction;
import com.gien.gits.engagement.InteractionExtraction.*;
import com.gien.gits.engagement.port.LlmClient;
import com.gien.gits.engagement.port.LlmClientException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 语义模式提取策略 — 基于关键词和语义规则提取结构化信息
 * 实现: 融资需求识别、承诺识别、风险信号识别、扩展意向识别、
 *       材料提供识别、跟进事项识别、客户陈述识别、产品兴趣识别
 *
 * P11 G1: 新增LLM语义提取能力，失败时fallback到原有正则逻辑
 */
public class SemanticPatternExtractionStrategy implements TranscriptExtractionStrategy {

    private static final Logger log = LoggerFactory.getLogger(SemanticPatternExtractionStrategy.class);

    private final LlmClient llmClient;
    private final ObjectMapper objectMapper;

    // ── G3: 8种语义模式正则 ──────────────────────────────────────

    /** 融资需求: 金额表达 + 意图关键词 */
    private static final Pattern FINANCING_AMOUNT_PATTERN =
        Pattern.compile("(\\d+[,.]?\\d*)\\s*(万|亿|万元|亿元|W|Y)", Pattern.CASE_INSENSITIVE);
    private static final Pattern FINANCING_INTENT_PATTERN =
        Pattern.compile("融资|贷款|借款|授信|额度|资金需求|增加支持|信贷|放款|支持|资金|lending|loan|credit|financing|support");

    /** 承诺: "我会/我们承诺/保证" 等 — 注意: 长匹配优先避免"我"抢先匹配"我们行" */
    private static final Pattern COMMITMENT_PATTERN =
        Pattern.compile("(我们行|我行|本行|我们|我)\\s*(会|将|承诺|保证|一定|务必|计划)\\s*[，,]?(.+?)([。；!！]|$)");

    /** 风险信号 */
    private static final Pattern RISK_SIGNAL_PATTERN =
        Pattern.compile("风险|逾期|违约|不良|坏账|损失|诉讼|执行|失信|欠款|拖欠|经营困难|资金链|risk|overdue|default|NPL|bad debt");

    /** 扩展意向 */
    private static final Pattern EXPANSION_INTENT_PATTERN =
        Pattern.compile("扩展|新增|追加|增信|扩产|二期|三期|新项目|增资|扩产|增额|expansion|expand|increase|additional");

    /** 材料提供 */
    private static final Pattern MATERIAL_PROVIDE_PATTERN =
        Pattern.compile("提供资料|补充材料|准备文件|提交报告|送过来|带过来|发给你|提供报表|财务报表|审计报告|provide.*material|submit.*document");

    /** 跟进事项 */
    private static final Pattern FOLLOW_UP_PATTERN =
        Pattern.compile("下次|后续|跟进|之后|回去后|回头|再联系|再沟通|follow.?up|next.?time|later");

    /** 客户陈述事实 — 支持复合主语(我们公司)和复合维度(目前营收) */
    private static final Pattern CUSTOMER_STATEMENT_PATTERN =
        Pattern.compile("(我们公司|我们|我司|公司|企业|厂里)\\s*(目前|现在|今年|去年)?\\s*(营收|利润|产值|产能|员工|订单|客户|收入|资产|负债)\\s*(是|有|达到|完成|约为|大约|大概|超过|接近)\\s*(.+?)([。；!！]|$)");

    /** 产品兴趣 */
    private static final Pattern PRODUCT_INTEREST_PATTERN =
        Pattern.compile("产品|方案|利率|费率|期限|还款方式|额度|担保|抵押|质押|product|plan|rate|interest");

    /**
     * 构造函数 — 注入LlmClient
     */
    public SemanticPatternExtractionStrategy(LlmClient llmClient) {
        this.llmClient = llmClient;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<InteractionExtraction> extract(String rawContent) {
        if (rawContent == null || rawContent.isBlank()) {
            log.debug("rawContent为空，跳过语义提取");
            return List.of();
        }

        // P11 G1: 先尝试LLM模式，失败时fallback到原有正则逻辑
        try {
            List<InteractionExtraction> llmResults = extractWithLlm(rawContent);
            if (llmResults != null && !llmResults.isEmpty()) {
                log.debug("LLM语义提取成功，返回{}条结果", llmResults.size());
                return llmResults;
            }
        } catch (LlmClientException e) {
            log.warn("LLM语义提取失败，fallback到正则逻辑: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("LLM语义提取异常，fallback到正则逻辑: {}", e.getMessage());
        }

        // fallback: 原有正则逻辑
        return extractWithRegex(rawContent);
    }

    // ── P11 G1: LLM语义提取 ─────────────────────────────────────

    private static final String SYSTEM_PROMPT = """
            你是一个银行客户互动语义分析专家。你的任务是从客户互动记录中提取结构化的语义模式。

            你需要识别以下8种语义模式:
            1. FINANCING_NEED — 融资需求: 客户提及金额、贷款、授信等
            2. COMMITMENT — 承诺: 客户或RM承诺做某事
            3. RISK_SIGNAL — 风险信号: 逾期、违约、不良等风险指标
            4. EXPANSION_INTENT — 扩展意向: 客户表达扩展、新增、增资等
            5. MATERIAL_PROVIDE — 材料提供: 客户表示将提供材料或文件
            6. FOLLOW_UP — 跟进事项: 提及后续跟进或下次沟通
            7. CUSTOMER_STATEMENT — 客户陈述: 客户陈述关于自身经营的事实
            8. PRODUCT_INTEREST — 产品兴趣: 客户对产品方案表示兴趣

            请以JSON格式返回结果，格式如下:
            {
              "patterns": [
                {
                  "objectId": "EXT-xxxxxxxx",
                  "type": "OPPORTUNITY_SIGNAL|CUSTOMER_COMMITMENT|BANK_COMMITMENT|RISK_INDICATOR|FACT_CLAIM|INTENT",
                  "claimType": "FINANCING_NEED|MATERIAL_PROVIDE|FOLLOW_UP|EXPANSION_INTENT|CUSTOMER_STATEMENT|RM_COMMITMENT|RISK_SIGNAL",
                  "content": "提取的内容描述",
                  "speaker": "客户方|RM|双方",
                  "evidenceRef": "TR-RAW",
                  "status": "DETECTED",
                  "confidence": 0.80,
                  "notFact": true,
                  "requiresReconciliation": true,
                  "conflictWith": null,
                  "nextQuestion": "后续跟进问题"
                }
              ]
            }

            只返回JSON，不要附加任何解释文字。""";

    /**
     * 使用LLM进行语义提取
     */
    List<InteractionExtraction> extractWithLlm(String rawContent) {
        String response = llmClient.complete(SYSTEM_PROMPT, rawContent);
        return parseLlmResponse(response);
    }

    /**
     * 解析LLM返回的JSON响应
     */
    private List<InteractionExtraction> parseLlmResponse(String response) {
        List<InteractionExtraction> extractions = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode patterns = root.get("patterns");
            if (patterns == null || !patterns.isArray()) {
                log.warn("LLM响应缺少patterns数组");
                return extractions;
            }

            for (JsonNode node : patterns) {
                String objectId = getTextOrDefault(node, "objectId", "EXT-" + UUID.randomUUID().toString().substring(0, 8));
                String typeStr = getTextOrDefault(node, "type", "FACT_CLAIM");
                String claimTypeStr = getTextOrDefault(node, "claimType", "CUSTOMER_STATEMENT");
                String content = getTextOrDefault(node, "content", "");
                String speaker = getTextOrDefault(node, "speaker", null);
                String evidenceRef = getTextOrDefault(node, "evidenceRef", "TR-RAW");
                String statusStr = getTextOrDefault(node, "status", "DETECTED");
                double confidence = node.has("confidence") ? node.get("confidence").asDouble() : 0.50;
                boolean notFact = node.has("notFact") && node.get("notFact").asBoolean();
                boolean requiresReconciliation = node.has("requiresReconciliation") && node.get("requiresReconciliation").asBoolean();
                String conflictWith = getTextOrDefault(node, "conflictWith", null);
                String nextQuestion = getTextOrDefault(node, "nextQuestion", null);

                if (content.isBlank()) {
                    continue;
                }

                extractions.add(new InteractionExtraction(
                    objectId,
                    ExtractionType.valueOf(typeStr),
                    ClaimType.valueOf(claimTypeStr),
                    content,
                    speaker,
                    evidenceRef,
                    ExtractionStatus.valueOf(statusStr),
                    BigDecimal.valueOf(confidence),
                    notFact,
                    requiresReconciliation,
                    conflictWith,
                    nextQuestion
                ));
            }
        } catch (Exception e) {
            log.warn("解析LLM响应失败: {}", e.getMessage());
            throw new LlmClientException("Failed to parse LLM response", e);
        }
        return extractions;
    }

    private String getTextOrDefault(JsonNode node, String field, String defaultValue) {
        if (node.has(field) && !node.get(field).isNull() && !node.get(field).asText().isBlank()) {
            return node.get(field).asText();
        }
        return defaultValue;
    }

    // ── 原有正则提取逻辑（保留不变） ────────────────────────────

    private List<InteractionExtraction> extractWithRegex(String rawContent) {
        List<InteractionExtraction> extractions = new ArrayList<>();

        // ── 1. FINANCING_NEED: 融资需求 ──────────────────────────
        extractFinancingNeed(rawContent, extractions);

        // ── 2. COMMITMENT: 承诺识别 ──────────────────────────────
        extractCommitment(rawContent, extractions);

        // ── 3. RISK_SIGNAL: 风险信号 ─────────────────────────────
        extractRiskSignal(rawContent, extractions);

        // ── 4. EXPANSION_INTENT: 扩展意向 ─────────────────────────
        extractExpansionIntent(rawContent, extractions);

        // ── 5. MATERIAL_PROVIDE: 材料提供 ────────────────────────
        extractMaterialProvide(rawContent, extractions);

        // ── 6. FOLLOW_UP: 跟进事项 ───────────────────────────────
        extractFollowUp(rawContent, extractions);

        // ── 7. CUSTOMER_STATEMENT: 客户陈述 ───────────────────────
        extractCustomerStatement(rawContent, extractions);

        // ── 8. PRODUCT_INTEREST: 产品兴趣 ─────────────────────────
        extractProductInterest(rawContent, extractions);

        return extractions;
    }

    // ── 模式1: 融资需求 ──────────────────────────────────────────

    private void extractFinancingNeed(String text, List<InteractionExtraction> extractions) {
        Matcher amountMatcher = FINANCING_AMOUNT_PATTERN.matcher(text);
        boolean hasIntent = FINANCING_INTENT_PATTERN.matcher(text).find();

        // 先检查是否有金额匹配
        boolean hasAmount = amountMatcher.find();
        String amount = hasAmount ? amountMatcher.group(1) + amountMatcher.group(2) : null;

        // 金额+意图 → 高置信度
        if (hasAmount && hasIntent) {
            extractions.add(new InteractionExtraction(
                "EXT-" + UUID.randomUUID().toString().substring(0, 8),
                ExtractionType.OPPORTUNITY_SIGNAL,
                ClaimType.FINANCING_NEED,
                "客户提及融资需求，金额约" + amount,
                "客户方", "TR-RAW",
                ExtractionStatus.DETECTED, new BigDecimal("0.80"),
                true, true,
                "授信额度", "确认" + amount + "的具体含义: 新增授信? 流动资金? 项目融资?"));
            return;
        }

        // 仅金额 → 中等置信度
        if (hasAmount) {
            extractions.add(new InteractionExtraction(
                "EXT-" + UUID.randomUUID().toString().substring(0, 8),
                ExtractionType.OPPORTUNITY_SIGNAL,
                ClaimType.FINANCING_NEED,
                "客户提及金额" + amount + "，可能存在融资需求",
                "客户方", "TR-RAW",
                ExtractionStatus.DETECTED, new BigDecimal("0.60"),
                true, true,
                null, "确认" + amount + "是否为融资需求"));
            return;
        }

        // 仅意图 → 低置信度
        if (hasIntent) {
            extractions.add(new InteractionExtraction(
                "EXT-" + UUID.randomUUID().toString().substring(0, 8),
                ExtractionType.OPPORTUNITY_SIGNAL,
                ClaimType.FINANCING_NEED,
                "客户表达融资需求意图(模糊表达)",
                "客户方", "TR-RAW",
                ExtractionStatus.DETECTED, new BigDecimal("0.50"),
                true, true,
                null, "确认融资需求的具体金额和用途"));
        }
    }

    // ── 模式2: 承诺识别 ──────────────────────────────────────────

    private void extractCommitment(String text, List<InteractionExtraction> extractions) {
        Matcher matcher = COMMITMENT_PATTERN.matcher(text);
        boolean found = false;
        while (matcher.find()) {
            found = true;
            String subject = matcher.group(1);
            String commitmentContent = matcher.group(3).trim();
            boolean isBank = subject.contains("行") || subject.contains("我们");
            extractions.add(new InteractionExtraction(
                "EXT-" + UUID.randomUUID().toString().substring(0, 8),
                isBank ? ExtractionType.BANK_COMMITMENT : ExtractionType.CUSTOMER_COMMITMENT,
                isBank ? ClaimType.RM_COMMITMENT : ClaimType.CUSTOMER_STATEMENT,
                (isBank ? "RM" : "客户") + "承诺: " + commitmentContent,
                isBank ? "RM" : "客户方", "TR-RAW",
                ExtractionStatus.DETECTED, new BigDecimal("0.85"),
                false, false,
                null, "跟进承诺兑现: " + commitmentContent));
        }

        // 降级匹配: "提供/安排" + 上下文
        if (!found && (text.contains("提供") || text.contains("安排"))) {
            extractions.add(new InteractionExtraction(
                "EXT-" + UUID.randomUUID().toString().substring(0, 8),
                ExtractionType.CUSTOMER_COMMITMENT,
                ClaimType.MATERIAL_PROVIDE,
                "客户承诺提供相关材料",
                "客户方", "TR-RAW",
                ExtractionStatus.DETECTED, new BigDecimal("0.70"),
                false, false, null, "跟进材料清单"));
        }

        // 降级匹配: "尽快/跟进" → 银行承诺
        if (text.contains("尽快") || text.contains("跟进")) {
            extractions.add(new InteractionExtraction(
                "EXT-" + UUID.randomUUID().toString().substring(0, 8),
                ExtractionType.BANK_COMMITMENT,
                ClaimType.RM_COMMITMENT,
                "RM承诺尽快跟进方案",
                "RM", "TR-RAW",
                ExtractionStatus.DETECTED, new BigDecimal("0.90"),
                false, false, null, null));
        }
    }

    // ── 模式3: 风险信号 ──────────────────────────────────────────

    private void extractRiskSignal(String text, List<InteractionExtraction> extractions) {
        Matcher matcher = RISK_SIGNAL_PATTERN.matcher(text);
        List<String> riskKeywords = new ArrayList<>();
        while (matcher.find()) {
            riskKeywords.add(matcher.group());
        }

        if (!riskKeywords.isEmpty()) {
            String riskDesc = String.join("、", riskKeywords.stream().distinct().toList());
            double confidence = Math.min(0.50 + riskKeywords.size() * 0.10, 0.95);
            extractions.add(new InteractionExtraction(
                "EXT-" + UUID.randomUUID().toString().substring(0, 8),
                ExtractionType.RISK_INDICATOR,
                ClaimType.RISK_SIGNAL,
                "检测到风险指标: " + riskDesc,
                "客户方", "TR-RAW",
                ExtractionStatus.DETECTED, BigDecimal.valueOf(confidence),
                true, true,
                "风险评级", "核实风险指标的具体情况和影响程度"));
        }
    }

    // ── 模式4: 扩展意向 ──────────────────────────────────────────

    private void extractExpansionIntent(String text, List<InteractionExtraction> extractions) {
        Matcher matcher = EXPANSION_INTENT_PATTERN.matcher(text);
        if (matcher.find()) {
            String keyword = matcher.group();
            extractions.add(new InteractionExtraction(
                "EXT-" + UUID.randomUUID().toString().substring(0, 8),
                ExtractionType.INTENT,
                ClaimType.EXPANSION_INTENT,
                "客户表达扩展意向: " + keyword,
                "客户方", "TR-RAW",
                ExtractionStatus.DETECTED, new BigDecimal("0.75"),
                true, true,
                "产能规划", "确认扩展项目的备案状态和资金需求"));
        }
    }

    // ── 模式5: 材料提供 ──────────────────────────────────────────

    private void extractMaterialProvide(String text, List<InteractionExtraction> extractions) {
        Matcher matcher = MATERIAL_PROVIDE_PATTERN.matcher(text);
        if (matcher.find()) {
            String matchText = matcher.group();
            extractions.add(new InteractionExtraction(
                "EXT-" + UUID.randomUUID().toString().substring(0, 8),
                ExtractionType.CUSTOMER_COMMITMENT,
                ClaimType.MATERIAL_PROVIDE,
                "客户表示将提供材料: " + matchText,
                "客户方", "TR-RAW",
                ExtractionStatus.DETECTED, new BigDecimal("0.80"),
                false, false,
                null, "确认材料清单和提交时间"));
        }
    }

    // ── 模式6: 跟进事项 ──────────────────────────────────────────

    private void extractFollowUp(String text, List<InteractionExtraction> extractions) {
        Matcher matcher = FOLLOW_UP_PATTERN.matcher(text);
        if (matcher.find()) {
            String keyword = matcher.group();
            extractions.add(new InteractionExtraction(
                "EXT-" + UUID.randomUUID().toString().substring(0, 8),
                ExtractionType.BANK_COMMITMENT,
                ClaimType.FOLLOW_UP,
                "提及跟进事项: " + keyword,
                "双方", "TR-RAW",
                ExtractionStatus.DETECTED, new BigDecimal("0.70"),
                false, false,
                null, "设定跟进时间和具体事项"));
        }
    }

    // ── 模式7: 客户陈述事实 ──────────────────────────────────────

    private void extractCustomerStatement(String text, List<InteractionExtraction> extractions) {
        Matcher matcher = CUSTOMER_STATEMENT_PATTERN.matcher(text);
        while (matcher.find()) {
            String subject = matcher.group(1);
            String timeQual = matcher.group(2);  // 可选时间限定词
            String dimension = matcher.group(3);
            String qualifier = matcher.group(4);
            String value = matcher.group(5).trim();
            String fullDimension = (timeQual != null ? timeQual : "") + dimension;
            extractions.add(new InteractionExtraction(
                "EXT-" + UUID.randomUUID().toString().substring(0, 8),
                ExtractionType.FACT_CLAIM,
                ClaimType.CUSTOMER_STATEMENT,
                subject + fullDimension + qualifier + value,
                "客户方", "TR-RAW",
                ExtractionStatus.DETECTED, new BigDecimal("0.65"),
                true, true,
                dimension, "与系统记录对账: " + fullDimension));
        }
    }

    // ── 模式8: 产品兴趣 ──────────────────────────────────────────

    private void extractProductInterest(String text, List<InteractionExtraction> extractions) {
        Matcher matcher = PRODUCT_INTEREST_PATTERN.matcher(text);
        if (matcher.find()) {
            String keyword = matcher.group();
            extractions.add(new InteractionExtraction(
                "EXT-" + UUID.randomUUID().toString().substring(0, 8),
                ExtractionType.OPPORTUNITY_SIGNAL,
                ClaimType.FINANCING_NEED,
                "客户对产品方案表示兴趣: " + keyword,
                "客户方", "TR-RAW",
                ExtractionStatus.DETECTED, new BigDecimal("0.60"),
                true, false,
                null, "了解客户对" + keyword + "的具体需求和偏好"));
        }
    }
}
