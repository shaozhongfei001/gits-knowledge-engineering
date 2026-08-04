package com.gien.gits.api.service.report;

import com.gien.gits.api.service.prompt.PromptTemplate;
import com.gien.gits.engagement.CustomerOperatingView;
import com.gien.gits.engagement.port.LlmClient;
import com.gien.gits.engagement.port.LlmClientException;
import com.gien.gits.ontology.RelationshipReport;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 更新关系报告策略 (R7) — 基于新证据更新关系报告
 * P9 Loop G6: 从CustomerOperatingView动态组装关系更新内容，包含交互摘要、承诺跟踪、事实对账状态
 * P11 G6: 注入LlmClient，先尝试LLM生成，失败fallback到原有StringBuilder逻辑
 */
public class UpdatedRelationshipReportStrategy implements ReportStrategy {

    private final LlmClient llmClient;

    public UpdatedRelationshipReportStrategy(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    @Override
    public RelationshipReport generate(ReportContext context) {
        CustomerOperatingView view = context.customerView().orElse(null);

        // P11 G6: 先尝试LLM生成
        String content;
        try {
            content = generateWithLlm(context.newEvidenceDescription(), context.previousReport(), view, context);
        } catch (LlmClientException e) {
            // LLM失败，fallback到原有StringBuilder逻辑
            content = buildContent(context.newEvidenceDescription(), context.previousReport(), view);
        }

        return new RelationshipReport(
            UUID.randomUUID(), context.operatingCaseId(), context.journeyId(),
            supportedType(), content,
            List.of("NEW-EVIDENCE"), List.of(),
            Instant.now(), context.previousReportId());
    }

    @Override
    public RelationshipReport.ReportType supportedType() {
        return RelationshipReport.ReportType.UPDATED_RELATIONSHIP;
    }

    /**
     * P11 G6: 使用LLM生成更新关系报告
     */
    private String generateWithLlm(String newEvidence, Optional<RelationshipReport> previous,
                                    CustomerOperatingView view, ReportContext context) {
        String systemPrompt = PromptTemplate.reportSystemPrompt("更新关系报告");
        String contextSummary = buildContextSummary(newEvidence, view);
        String userPrompt = PromptTemplate.reportUserPrompt(
            "更新关系报告", context.operatingCaseId(),
            view != null ? view.customerId() : "unknown",
            contextSummary);

        String llmResponse = llmClient.complete(systemPrompt, userPrompt);

        // 解析LLM JSON响应，组装报告内容
        StringBuilder sb = new StringBuilder();
        sb.append("# 更新关系报告 (LLM增强)\n\n");

        // 客户经营视图数据驱动部分
        if (view != null) {
            sb.append("## 客户当前状态\n");
            sb.append("- 客户名称: ").append(view.customerName()).append("\n");
            sb.append("- 风险等级: ").append(view.riskLevel()).append("\n\n");

            sb.append("## 交互摘要\n");
            sb.append("- 累计交互次数: ").append(view.totalInteractions()).append("\n");
            sb.append("- 最近交互时间: ").append(
                view.lastInteractionTime() != null ? view.lastInteractionTime().toString() : "无记录").append("\n\n");

            if (!view.pendingCommitments().isEmpty()) {
                sb.append("## 承诺跟踪\n");
                for (CustomerOperatingView.CommitmentSummary c : view.pendingCommitments()) {
                    sb.append("- [").append(c.commitmentType()).append("] ").append(c.content())
                        .append(" (负责人: ").append(c.owner())
                        .append(", 截止: ").append(c.dueDate())
                        .append(c.fulfilled() ? ", 已完成" : ", 未完成")
                        .append(")\n");
                }
                sb.append("\n");
            }

            if (view.openReconciliationCount() > 0) {
                sb.append("## 事实对账状态\n");
                sb.append("当前待对账事实: ").append(view.openReconciliationCount()).append("项\n\n");
            }

            if (!view.riskIndicators().isEmpty()) {
                sb.append("## 风险指标\n");
                for (String r : view.riskIndicators()) {
                    sb.append("- ").append(r).append("\n");
                }
                sb.append("\n");
            }
        }

        // 新证据
        sb.append("## 新证据\n").append(newEvidence).append("\n\n");

        // 前次报告
        previous.ifPresent(p -> {
            sb.append("## 基于前次报告\n").append(p.content(), 0, Math.min(200, p.content().length())).append("...\n\n");
        });

        // LLM增强的更新结论
        sb.append("## 更新结论 (LLM增强)\n");
        sb.append(extractJsonField(llmResponse, "summary", "基于新证据更新客户关系评估。")).append("\n\n");

        sb.append("## 关键发现 (LLM增强)\n");
        List<String> llmFindings = extractJsonArrayField(llmResponse, "keyFindings");
        for (String f : llmFindings) {
            sb.append("- ").append(f).append("\n");
        }

        sb.append("\n## 建议 (LLM增强)\n");
        List<String> llmRecs = extractJsonArrayField(llmResponse, "recommendations");
        for (String r : llmRecs) {
            sb.append("- ").append(r).append("\n");
        }

        sb.append("\n## 行动项 (LLM增强)\n");
        List<String> llmActions = extractJsonArrayField(llmResponse, "actionItems");
        for (String a : llmActions) {
            sb.append("- ").append(a).append("\n");
        }

        if (view != null) {
            if (!view.riskIndicators().isEmpty()) {
                sb.append("\n当前存在风险关注点，建议加强监控。");
            }
            if (!view.pendingCommitments().isEmpty()) {
                long unfulfilled = view.pendingCommitments().stream().filter(c -> !c.fulfilled()).count();
                if (unfulfilled > 0) {
                    sb.append("尚有").append(unfulfilled).append("项承诺未履行，需跟进。");
                }
            }
        }

        return sb.toString();
    }

    private String buildContextSummary(String newEvidence, CustomerOperatingView view) {
        StringBuilder summary = new StringBuilder();
        if (view != null) {
            summary.append("客户: ").append(view.customerName())
                .append(", 风险: ").append(view.riskLevel());
        }
        summary.append(", 新证据: ").append(newEvidence);
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

    private String buildContent(String newEvidence, Optional<RelationshipReport> previous, CustomerOperatingView view) {
        StringBuilder sb = new StringBuilder();
        sb.append("# 更新关系报告\n\n");

        // 客户经营视图数据驱动部分
        if (view != null) {
            sb.append("## 客户当前状态\n");
            sb.append("- 客户名称: ").append(view.customerName()).append("\n");
            sb.append("- 风险等级: ").append(view.riskLevel()).append("\n\n");

            // 交互摘要
            sb.append("## 交互摘要\n");
            sb.append("- 累计交互次数: ").append(view.totalInteractions()).append("\n");
            sb.append("- 最近交互时间: ").append(
                view.lastInteractionTime() != null ? view.lastInteractionTime().toString() : "无记录").append("\n\n");

            // 承诺跟踪
            if (!view.pendingCommitments().isEmpty()) {
                sb.append("## 承诺跟踪\n");
                for (CustomerOperatingView.CommitmentSummary c : view.pendingCommitments()) {
                    sb.append("- [").append(c.commitmentType()).append("] ").append(c.content())
                        .append(" (负责人: ").append(c.owner())
                        .append(", 截止: ").append(c.dueDate())
                        .append(c.fulfilled() ? ", 已完成" : ", 未完成")
                        .append(")\n");
                }
                sb.append("\n");
            }

            // 事实对账状态
            if (view.openReconciliationCount() > 0) {
                sb.append("## 事实对账状态\n");
                sb.append("当前待对账事实: ").append(view.openReconciliationCount()).append("项\n\n");
            }

            // 风险指标
            if (!view.riskIndicators().isEmpty()) {
                sb.append("## 风险指标\n");
                for (String r : view.riskIndicators()) {
                    sb.append("- ").append(r).append("\n");
                }
                sb.append("\n");
            }
        }

        // 新证据
        sb.append("## 新证据\n").append(newEvidence).append("\n\n");

        // 前次报告
        previous.ifPresent(p -> {
            sb.append("## 基于前次报告\n").append(p.content(), 0, Math.min(200, p.content().length())).append("...\n\n");
        });

        // 更新结论
        sb.append("## 更新结论\n基于新证据更新客户关系评估。");
        if (view != null) {
            if (!view.riskIndicators().isEmpty()) {
                sb.append("当前存在风险关注点，建议加强监控。");
            }
            if (!view.pendingCommitments().isEmpty()) {
                long unfulfilled = view.pendingCommitments().stream().filter(c -> !c.fulfilled()).count();
                if (unfulfilled > 0) {
                    sb.append("尚有").append(unfulfilled).append("项承诺未履行，需跟进。");
                }
            }
        }

        return sb.toString();
    }
}
