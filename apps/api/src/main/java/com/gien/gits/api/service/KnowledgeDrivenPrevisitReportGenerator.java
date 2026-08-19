package com.gien.gits.api.service;

import com.gien.gits.engagement.PrevisitReportContent;
import com.gien.gits.engagement.port.LlmClient;
import com.gien.gits.engagement.port.LlmClientException;
import com.gien.gits.knowledge.ActivationContract;
import com.gien.gits.knowledge.port.ActivationContractPort;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 知识地图驱动的访前报告生成（P23）。
 *
 * <p>核心流程：知识地图锁定知识领域 → 任务关联知识条目（KI，来自激活合同
 * {@code AC-PREVISIT-001.knowledgeItemIds}）→ 组装知识 → 扔给大模型生成访前报告。
 * 结构化事实字段（客户概览/KYC/产品方案）由 {@code PrevisitWorkflowService} 规则装配（业务事实，
 * LLM 不臆造）；策略/关键问题/风险提醒由 LLM 基于知识地图上下文增强生成。</p>
 *
 * <p>fail-closed：LLM 调用或解析失败时整体 fallback 到纯规则版（{@code PrevisitWorkflowService}），
 * 不返回部分结果，不改变既有业务行为。</p>
 */
public final class KnowledgeDrivenPrevisitReportGenerator {

    /** 访前准备任务的激活合同 ID（知识地图任务映射，含 knowledgeItemIds）。 */
    static final String PREVISIT_CONTRACT_ID = "AC-PREVISIT-001";

    /** 访前准备任务的知识地图/域范围（锁定知识领域）。 */
    static final String PREVISIT_KNOWLEDGE_SCOPE = "KM-CORP-RM-PREVISIT";

    private final KnowledgeAssembler assembler;
    private final LlmClient llmClient;
    private final PrevisitWorkflowService workflowService;
    private final ActivationContractPort activationContractPort;

    public KnowledgeDrivenPrevisitReportGenerator(
            KnowledgeAssembler assembler,
            LlmClient llmClient,
            PrevisitWorkflowService workflowService,
            ActivationContractPort activationContractPort) {
        this.assembler = Objects.requireNonNull(assembler);
        this.llmClient = Objects.requireNonNull(llmClient);
        this.workflowService = Objects.requireNonNull(workflowService);
        this.activationContractPort = Objects.requireNonNull(activationContractPort);
    }

    /**
     * 生成知识地图驱动的访前报告（R1）。知识地图/域范围与任务关联 KI 自动从激活合同读取。
     *
     * @param customerId       客户 ID
     * @param journeyId        旅程 ID
     * @param operatingCaseId  经营案件 ID
     * @param visitObjective   拜访目标
     * @return 知识地图驱动的 {@link PrevisitReportContent}
     */
    public PrevisitReportContent generate(
            String customerId,
            String journeyId,
            String operatingCaseId,
            String visitObjective) {

        String knowledgeScope = PREVISIT_KNOWLEDGE_SCOPE;
        List<String> knowledgeItemIds = resolveKnowledgeItemIds();

        // 1. 规则装配结构化基础（业务事实，fail-closed 兜底）
        PrevisitReportContent base = workflowService.generatePrevisitReport(
                customerId, journeyId, operatingCaseId, visitObjective);

        // 2. 组装知识上下文（知识地图导航 + 任务关联 KI/KE + 业务数据）
        String knowledgeContext = assembler.assemble(
                knowledgeScope, knowledgeItemIds, base.customerOverview() != null
                        ? "客户：" + base.customerName() + "，目标：" + visitObjective : visitObjective);

        // 3. LLM 基于知识地图增强生成策略/问题/风险
        try {
            String llmResponse = llmClient.complete(buildSystemPrompt(), buildUserPrompt(knowledgeContext, base));
            return applyLlmInsights(base, llmResponse);
        } catch (LlmClientException error) {
            // 4. fallback：LLM 失败返回规则版（不改变既有行为）
            return base;
        }
    }

    /** 从激活合同读取任务关联的知识条目（KI），数据驱动；合同缺失时返回空。 */
    private List<String> resolveKnowledgeItemIds() {
        try {
            return activationContractPort.find(PREVISIT_CONTRACT_ID)
                    .map(ActivationContract::knowledgeItemIds)
                    .orElse(List.of());
        } catch (RuntimeException e) {
            return List.of();
        }
    }

    private String buildSystemPrompt() {
        return "你是银行客户经理的访前分析助手。你已读取受控知识地图（权威知识条目与要素）。"
                + "请严格基于提供的知识地图和业务数据，给出访前报告的策略、关键问题与风险提醒。"
                + "禁止臆造知识地图未提供的客户事实。仅返回 JSON。";
    }

    private String buildUserPrompt(String knowledgeContext, PrevisitReportContent base) {
        return "基于以下知识地图与业务数据，生成访前报告洞察。\n\n"
                + knowledgeContext
                + "\n\n请返回 JSON：{\"visitStrategy\":\"...\",\"keyQuestions\":[\"..\"],\"riskReminders\":[\"..\"]}";
    }

    private PrevisitReportContent applyLlmInsights(PrevisitReportContent base, String llmResponse) {
        String strategy = extractJsonField(llmResponse, "visitStrategy", base.visitStrategy());
        List<String> questions = extractJsonArrayField(llmResponse, "keyQuestions");
        List<String> risks = extractJsonArrayField(llmResponse, "riskReminders");

        List<String> mergedQuestions = new ArrayList<>(questions.isEmpty() ? base.keyQuestions() : questions);
        List<String> mergedRisks = new ArrayList<>(risks.isEmpty() ? base.riskReminders() : risks);

        return new PrevisitReportContent(
                base.reportId(), base.customerId(), base.customerName(), base.rmName(),
                base.visitObjective(), base.customerOverview(), base.kycGapSummary(),
                base.productSchemes(), mergedQuestions, mergedRisks,
                strategy.isBlank() ? base.visitStrategy() : strategy);
    }

    private String extractJsonField(String json, String fieldName, String fallback) {
        try {
            String pattern = "\"" + fieldName + "\"";
            int idx = json.indexOf(pattern);
            if (idx < 0) {
                return fallback;
            }
            int colonIdx = json.indexOf(":", idx + pattern.length());
            if (colonIdx < 0) {
                return fallback;
            }
            int valueStart = json.indexOf("\"", colonIdx + 1);
            if (valueStart < 0) {
                return fallback;
            }
            int valueEnd = json.indexOf("\"", valueStart + 1);
            if (valueEnd < 0) {
                return fallback;
            }
            return json.substring(valueStart + 1, valueEnd);
        } catch (Exception e) {
            return fallback;
        }
    }

    private List<String> extractJsonArrayField(String json, String fieldName) {
        try {
            String pattern = "\"" + fieldName + "\"";
            int idx = json.indexOf(pattern);
            if (idx < 0) {
                return List.of();
            }
            int arrStart = json.indexOf("[", idx);
            int arrEnd = json.indexOf("]", arrStart);
            if (arrStart < 0 || arrEnd < 0) {
                return List.of();
            }
            String arrContent = json.substring(arrStart + 1, arrEnd);
            List<String> result = new ArrayList<>();
            for (String item : arrContent.split(",")) {
                String trimmed = item.trim().replaceAll("^\"|\"$", "");
                if (!trimmed.isEmpty()) {
                    result.add(trimmed);
                }
            }
            return result;
        } catch (Exception e) {
            return List.of();
        }
    }
}
