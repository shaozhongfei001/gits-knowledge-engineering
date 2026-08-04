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
 * CRM通话报告策略 (R5B) — 生成结构化CRM数据
 * P9 Loop G6: 从CustomerOperatingView动态组装CRM调用内容，包含风险指标、机会信号、KYC缺口
 * P11 G6: 注入LlmClient，先尝试LLM生成，失败fallback到原有StringBuilder逻辑
 */
public class CrmCallReportStrategy implements ReportStrategy {

    private final LlmClient llmClient;

    public CrmCallReportStrategy(LlmClient llmClient) {
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
            List.of(),
            Instant.now(), null);
    }

    @Override
    public RelationshipReport.ReportType supportedType() {
        return RelationshipReport.ReportType.CRM_CALL;
    }

    /**
     * P11 G6: 使用LLM生成CRM通话报告
     */
    private String generateWithLlm(PostvisitAnalysisContent analysis,
                                    CustomerOperatingView view,
                                    ReportContext context) {
        String systemPrompt = PromptTemplate.reportSystemPrompt("CRM通话报告");
        String contextSummary = buildContextSummary(analysis, view);
        String userPrompt = PromptTemplate.reportUserPrompt(
            "CRM通话报告", context.operatingCaseId(),
            view != null ? view.customerId() : "unknown",
            contextSummary);

        String llmResponse = llmClient.complete(systemPrompt, userPrompt);

        // 解析LLM JSON响应，组装报告内容
        StringBuilder sb = new StringBuilder();
        sb.append("# CRM通话报告 (LLM增强)\n\n");

        // 客户经营视图数据驱动部分
        if (view != null) {
            sb.append("## 客户标识\n");
            sb.append("- 客户ID: ").append(view.customerId()).append("\n");
            sb.append("- 客户名称: ").append(view.customerName()).append("\n");
            sb.append("- 客户经理: ").append(view.rmName()).append(" (").append(view.rmId()).append(")\n\n");

            if (!view.riskIndicators().isEmpty()) {
                sb.append("## 风险指标\n");
                for (String r : view.riskIndicators()) {
                    sb.append("- ").append(r).append("\n");
                }
                sb.append("\n");
            }

            if (!view.activeSignals().isEmpty()) {
                sb.append("## 活跃机会信号\n");
                for (CustomerOperatingView.OpportunitySignalSummary s : view.activeSignals()) {
                    sb.append("- [").append(s.signalType()).append("] ").append(s.content())
                        .append(" (置信度: ").append(s.confidence()).append(")\n");
                }
                sb.append("\n");
            }

            if (!view.unknownKycItems().isEmpty() || !view.partialKycItems().isEmpty()) {
                sb.append("## KYC缺口\n");
                if (!view.unknownKycItems().isEmpty()) {
                    sb.append("- 未知项: ").append(String.join("、", view.unknownKycItems())).append("\n");
                }
                if (!view.partialKycItems().isEmpty()) {
                    sb.append("- 部分已知项: ").append(String.join("、", view.partialKycItems())).append("\n");
                }
                sb.append("\n");
            }
        }

        // LLM增强内容
        sb.append("## 互动类型: 客户拜访\n\n");

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
                sb.append("- ").append(f.content()).append("\n");
            }
        }

        sb.append("\n## 后续行动 (LLM增强)\n");
        List<String> llmActions = extractJsonArrayField(llmResponse, "actionItems");
        if (!llmActions.isEmpty()) {
            for (String a : llmActions) {
                sb.append("- ").append(a).append("\n");
            }
        } else {
            for (String action : analysis.followUpActions()) {
                sb.append("- ").append(action).append("\n");
            }
        }

        return sb.toString();
    }

    private String buildContextSummary(PostvisitAnalysisContent analysis, CustomerOperatingView view) {
        StringBuilder summary = new StringBuilder();
        if (view != null) {
            summary.append("客户: ").append(view.customerName())
                .append(", 风险: ").append(view.riskLevel());
        }
        summary.append(", 访问摘要: ").append(analysis.visitSummary());
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
        sb.append("# CRM通话报告\n\n");

        // 客户经营视图数据驱动部分
        if (view != null) {
            sb.append("## 客户标识\n");
            sb.append("- 客户ID: ").append(view.customerId()).append("\n");
            sb.append("- 客户名称: ").append(view.customerName()).append("\n");
            sb.append("- 客户经理: ").append(view.rmName()).append(" (").append(view.rmId()).append(")\n\n");

            // 风险指标
            if (!view.riskIndicators().isEmpty()) {
                sb.append("## 风险指标\n");
                for (String r : view.riskIndicators()) {
                    sb.append("- ").append(r).append("\n");
                }
                sb.append("\n");
            }

            // 机会信号
            if (!view.activeSignals().isEmpty()) {
                sb.append("## 活跃机会信号\n");
                for (CustomerOperatingView.OpportunitySignalSummary s : view.activeSignals()) {
                    sb.append("- [").append(s.signalType()).append("] ").append(s.content())
                        .append(" (置信度: ").append(s.confidence()).append(")\n");
                }
                sb.append("\n");
            }

            // KYC缺口
            if (!view.unknownKycItems().isEmpty() || !view.partialKycItems().isEmpty()) {
                sb.append("## KYC缺口\n");
                if (!view.unknownKycItems().isEmpty()) {
                    sb.append("- 未知项: ").append(String.join("、", view.unknownKycItems())).append("\n");
                }
                if (!view.partialKycItems().isEmpty()) {
                    sb.append("- 部分已知项: ").append(String.join("、", view.partialKycItems())).append("\n");
                }
                sb.append("\n");
            }
        }

        // 访后分析内容
        sb.append("## 互动类型: 客户拜访\n\n");
        sb.append("## 关键内容\n");
        for (InteractionExtraction f : analysis.keyFindings()) {
            sb.append("- ").append(f.content()).append("\n");
        }
        sb.append("\n## 后续行动\n");
        for (String action : analysis.followUpActions()) {
            sb.append("- ").append(action).append("\n");
        }

        return sb.toString();
    }
}
