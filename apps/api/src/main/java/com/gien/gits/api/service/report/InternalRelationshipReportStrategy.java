package com.gien.gits.api.service.report;

import com.gien.gits.api.service.prompt.PromptTemplate;
import com.gien.gits.engagement.CustomerOperatingView;
import com.gien.gits.engagement.InteractionExtraction;
import com.gien.gits.engagement.PostvisitAnalysisContent;
import com.gien.gits.engagement.port.LlmClient;
import com.gien.gits.engagement.port.LlmClientException;
import com.gien.gits.ontology.RelationshipReport;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 内部关系报告策略 (R5A) — 基于访后分析和客户经营视图生成内部关系报告
 * P9 Loop G6: 从CustomerOperatingView动态组装分析结论，替换硬编码
 * P11 G6: 注入LlmClient，先尝试LLM生成，失败fallback到原有StringBuilder逻辑
 */
public class InternalRelationshipReportStrategy implements ReportStrategy {

    private final LlmClient llmClient;

    public InternalRelationshipReportStrategy(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    @Override
    public RelationshipReport generate(ReportContext context) {
        PostvisitAnalysisContent analysis = context.analysis();
        CustomerOperatingView view = context.customerView().orElse(null);

        // P11 G6: 先尝试LLM生成
        String content;
        try {
            content = generateWithLlm(analysis, view, context);
        } catch (LlmClientException e) {
            // LLM失败，fallback到原有StringBuilder逻辑
            content = buildContent(analysis, view);
        }

        return new RelationshipReport(
            UUID.randomUUID(), context.operatingCaseId(), context.journeyId(),
            supportedType(), content,
            analysis.keyFindings().stream().map(InteractionExtraction::evidenceRef).toList(),
            analysis.reconciliationItems().stream().map(PostvisitAnalysisContent.FactReconciliationItem::topic).toList(),
            Instant.now(), null);
    }

    @Override
    public RelationshipReport.ReportType supportedType() {
        return RelationshipReport.ReportType.INTERNAL_RELATIONSHIP;
    }

    /**
     * P11 G6: 使用LLM生成内部关系报告
     */
    private String generateWithLlm(PostvisitAnalysisContent analysis,
                                    CustomerOperatingView view,
                                    ReportContext context) {
        String systemPrompt = PromptTemplate.reportSystemPrompt("内部关系报告");
        String contextSummary = buildContextSummary(analysis, view, context);
        String userPrompt = PromptTemplate.reportUserPrompt(
            "内部关系报告", context.operatingCaseId(),
            view != null ? view.customerId() : "unknown",
            contextSummary);

        String llmResponse = llmClient.complete(systemPrompt, userPrompt);

        // 解析LLM JSON响应，组装报告内容
        StringBuilder sb = new StringBuilder();
        sb.append("# 内部关系报告 (LLM增强)\n\n");

        // 客户经营视图数据驱动部分
        if (view != null) {
            sb.append("## 客户基本信息\n");
            sb.append("- 客户名称: ").append(view.customerName()).append("\n");
            sb.append("- 行业: ").append(view.industry()).append("\n");
            sb.append("- 企业规模: ").append(view.enterpriseScale()).append("\n");
            sb.append("- 客户等级: ").append(view.customerTier()).append("\n");
            sb.append("- 风险等级: ").append(view.riskLevel()).append("\n");
            sb.append("- 客户经理: ").append(view.rmName()).append("\n\n");

            sb.append("## KYC信息覆盖\n");
            sb.append("- 已知项(").append(view.knownKycItems().size()).append("): ")
                .append(String.join("、", view.knownKycItems())).append("\n");
            sb.append("- 部分已知项(").append(view.partialKycItems().size()).append("): ")
                .append(String.join("、", view.partialKycItems())).append("\n");
            sb.append("- 未知项(").append(view.unknownKycItems().size()).append("): ")
                .append(String.join("、", view.unknownKycItems())).append("\n\n");

            sb.append("## 活跃机会信号\n");
            if (view.activeSignals().isEmpty()) {
                sb.append("当前无活跃机会信号。\n");
            } else {
                view.activeSignals().forEach(s ->
                    sb.append("- [").append(s.signalType()).append("] ")
                        .append(s.content())
                        .append(" (置信度: ").append(s.confidence()).append(", 状态: ").append(s.status()).append(")\n")
                );
            }
            sb.append("\n");

            sb.append("## 交互概况\n");
            sb.append("- 累计交互次数: ").append(view.totalInteractions()).append("\n");
            sb.append("- 最近交互时间: ").append(
                view.lastInteractionTime() != null ? view.lastInteractionTime().toString() : "无记录").append("\n\n");

            sb.append("## 旅程状态\n");
            sb.append("- 活跃旅程数: ").append(view.activeJourneyCount()).append("\n");
            sb.append("- 当前阶段: ").append(
                view.currentJourneyPhase() != null ? view.currentJourneyPhase() : "无活跃旅程").append("\n\n");

            if (!view.riskIndicators().isEmpty()) {
                sb.append("## 风险指标\n");
                for (String r : view.riskIndicators()) {
                    sb.append("- ").append(r).append("\n");
                }
                sb.append("\n");
            }
        }

        // LLM增强的分析内容
        sb.append("## LLM分析摘要\n");
        sb.append(extractJsonField(llmResponse, "summary", analysis.visitSummary())).append("\n\n");

        sb.append("## 关键发现 (LLM增强)\n");
        List<String> llmFindings = extractJsonArrayField(llmResponse, "keyFindings");
        if (!llmFindings.isEmpty()) {
            for (String f : llmFindings) {
                sb.append("- ").append(f).append("\n");
            }
        } else {
            for (InteractionExtraction f : analysis.keyFindings()) {
                sb.append("- [").append(f.type()).append("] ").append(f.content());
                if (f.notFact()) sb.append(" ⚠️Claim≠Fact");
                sb.append("\n");
            }
        }

        sb.append("\n## 风险指标 (LLM增强)\n");
        List<String> llmRisks = extractJsonArrayField(llmResponse, "riskIndicators");
        if (!llmRisks.isEmpty()) {
            for (String r : llmRisks) {
                sb.append("- ").append(r).append("\n");
            }
        } else if (view != null && !view.riskIndicators().isEmpty()) {
            for (String r : view.riskIndicators()) {
                sb.append("- ").append(r).append("\n");
            }
        }

        sb.append("\n## 机会指标 (LLM增强)\n");
        List<String> llmOpps = extractJsonArrayField(llmResponse, "opportunityIndicators");
        if (!llmOpps.isEmpty()) {
            for (String o : llmOpps) {
                sb.append("- ").append(o).append("\n");
            }
        } else {
            for (PostvisitAnalysisContent.OpportunitySignalItem s : analysis.opportunitySignals()) {
                sb.append("- ").append(s.signalType()).append(": ").append(s.content()).append("\n");
            }
        }

        sb.append("\n## 建议 (LLM增强)\n");
        List<String> llmRecs = extractJsonArrayField(llmResponse, "recommendations");
        if (!llmRecs.isEmpty()) {
            for (String r : llmRecs) {
                sb.append("- ").append(r).append("\n");
            }
        } else {
            sb.append(analysis.nextStepRecommendation()).append("\n");
        }

        sb.append("\n## 行动项 (LLM增强)\n");
        List<String> llmActions = extractJsonArrayField(llmResponse, "actionItems");
        if (!llmActions.isEmpty()) {
            for (String a : llmActions) {
                sb.append("- ").append(a).append("\n");
            }
        } else {
            sb.append(analysis.nextStepRecommendation()).append("\n");
        }

        // 承诺跟踪和事实对账状态（来自经营视图）
        if (view != null) {
            if (!view.pendingCommitments().isEmpty()) {
                sb.append("\n\n## 待履行承诺\n");
                for (CustomerOperatingView.CommitmentSummary c : view.pendingCommitments()) {
                    sb.append("- [").append(c.commitmentType()).append("] ").append(c.content())
                        .append(" (负责人: ").append(c.owner())
                        .append(", 截止: ").append(c.dueDate())
                        .append(c.fulfilled() ? ", 已完成" : ", 未完成")
                        .append(")\n");
                }
            }
            if (view.openReconciliationCount() > 0) {
                sb.append("\n## 对账状态\n");
                sb.append("当前待对账事实: ").append(view.openReconciliationCount()).append("项\n");
            }
        }

        return sb.toString();
    }

    private String buildContextSummary(PostvisitAnalysisContent analysis,
                                        CustomerOperatingView view,
                                        ReportContext context) {
        StringBuilder summary = new StringBuilder();
        if (view != null) {
            summary.append("客户: ").append(view.customerName())
                .append(", 行业: ").append(view.industry())
                .append(", 风险: ").append(view.riskLevel());
        }
        summary.append(", 访问摘要: ").append(analysis.visitSummary());
        summary.append(", 关键发现数: ").append(analysis.keyFindings().size());
        return summary.toString();
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

    private List<String> extractJsonArrayField(String json, String fieldName) {
        try {
            String pattern = "\"" + fieldName + "\"";
            int idx = json.indexOf(pattern);
            if (idx < 0) return List.of();
            int arrStart = json.indexOf("[", idx);
            int arrEnd = json.indexOf("]", arrStart);
            if (arrStart < 0 || arrEnd < 0) return List.of();
            String arrContent = json.substring(arrStart + 1, arrEnd);
            List<String> result = new ArrayList<>();
            for (String item : arrContent.split(",")) {
                String trimmed = item.trim().replaceAll("\"", "");
                if (!trimmed.isEmpty()) {
                    result.add(trimmed);
                }
            }
            return result;
        } catch (Exception e) {
            return List.of();
        }
    }

    private String buildContent(PostvisitAnalysisContent analysis, CustomerOperatingView view) {
        StringBuilder sb = new StringBuilder();
        sb.append("# 内部关系报告\n\n");

        // 客户经营视图数据驱动部分
        if (view != null) {
            sb.append("## 客户基本信息\n");
            sb.append("- 客户名称: ").append(view.customerName()).append("\n");
            sb.append("- 行业: ").append(view.industry()).append("\n");
            sb.append("- 企业规模: ").append(view.enterpriseScale()).append("\n");
            sb.append("- 客户等级: ").append(view.customerTier()).append("\n");
            sb.append("- 风险等级: ").append(view.riskLevel()).append("\n");
            sb.append("- 客户经理: ").append(view.rmName()).append("\n\n");

            sb.append("## KYC信息覆盖\n");
            sb.append("- 已知项(").append(view.knownKycItems().size()).append("): ")
                .append(String.join("、", view.knownKycItems())).append("\n");
            sb.append("- 部分已知项(").append(view.partialKycItems().size()).append("): ")
                .append(String.join("、", view.partialKycItems())).append("\n");
            sb.append("- 未知项(").append(view.unknownKycItems().size()).append("): ")
                .append(String.join("、", view.unknownKycItems())).append("\n\n");

            sb.append("## 活跃机会信号\n");
            if (view.activeSignals().isEmpty()) {
                sb.append("当前无活跃机会信号。\n");
            } else {
                view.activeSignals().forEach(s ->
                    sb.append("- [").append(s.signalType()).append("] ")
                        .append(s.content())
                        .append(" (置信度: ").append(s.confidence()).append(", 状态: ").append(s.status()).append(")\n")
                );
            }
            sb.append("\n");

            sb.append("## 交互概况\n");
            sb.append("- 累计交互次数: ").append(view.totalInteractions()).append("\n");
            sb.append("- 最近交互时间: ").append(
                view.lastInteractionTime() != null ? view.lastInteractionTime().toString() : "无记录").append("\n\n");

            sb.append("## 旅程状态\n");
            sb.append("- 活跃旅程数: ").append(view.activeJourneyCount()).append("\n");
            sb.append("- 当前阶段: ").append(
                view.currentJourneyPhase() != null ? view.currentJourneyPhase() : "无活跃旅程").append("\n\n");

            if (!view.riskIndicators().isEmpty()) {
                sb.append("## 风险指标\n");
                for (String r : view.riskIndicators()) {
                    sb.append("- ").append(r).append("\n");
                }
                sb.append("\n");
            }
        }

        // 访后分析内容
        sb.append("## 访问摘要\n").append(analysis.visitSummary()).append("\n\n");
        sb.append("## 关键发现\n");
        for (InteractionExtraction f : analysis.keyFindings()) {
            sb.append("- [").append(f.type()).append("] ").append(f.content());
            if (f.notFact()) sb.append(" ⚠️Claim≠Fact");
            sb.append("\n");
        }
        sb.append("\n## 机会信号\n");
        for (PostvisitAnalysisContent.OpportunitySignalItem s : analysis.opportunitySignals()) {
            sb.append("- ").append(s.signalType()).append(": ").append(s.content());
            if (s.notOpportunityYet()) sb.append(" (信号≠机会)");
            sb.append("\n");
        }
        sb.append("\n## 事实对账\n");
        for (PostvisitAnalysisContent.FactReconciliationItem r : analysis.reconciliationItems()) {
            sb.append("- ").append(r.topic()).append(": ").append(r.correctJudgment()).append("\n");
        }
        sb.append("\n## 下一步\n").append(analysis.nextStepRecommendation());

        // 承诺跟踪和事实对账状态（来自经营视图）
        if (view != null) {
            if (!view.pendingCommitments().isEmpty()) {
                sb.append("\n\n## 待履行承诺\n");
                for (CustomerOperatingView.CommitmentSummary c : view.pendingCommitments()) {
                    sb.append("- [").append(c.commitmentType()).append("] ").append(c.content())
                        .append(" (负责人: ").append(c.owner())
                        .append(", 截止: ").append(c.dueDate())
                        .append(c.fulfilled() ? ", 已完成" : ", 未完成")
                        .append(")\n");
                }
            }
            if (view.openReconciliationCount() > 0) {
                sb.append("\n## 对账状态\n");
                sb.append("当前待对账事实: ").append(view.openReconciliationCount()).append("项\n");
            }
        }

        return sb.toString();
    }
}
