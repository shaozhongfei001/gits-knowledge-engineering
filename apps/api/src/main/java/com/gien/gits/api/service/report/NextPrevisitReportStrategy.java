package com.gien.gits.api.service.report;

import com.gien.gits.api.service.ContextInheritanceService;
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
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * 下次访前报告策略 (R8) — 继承上次访后分析上下文
 * P9 Loop G6: 从CustomerOperatingView动态组装访前建议
 * P9 Loop G2: 通过ContextInheritanceService实现R4→R8上下文继承闭环
 * P11 G6: 注入LlmClient，先尝试LLM生成，失败fallback到原有StringBuilder逻辑
 */
public class NextPrevisitReportStrategy implements ReportStrategy {

    private final ContextInheritanceService contextInheritanceService;
    private final LlmClient llmClient;

    public NextPrevisitReportStrategy(ContextInheritanceService contextInheritanceService,
                                       LlmClient llmClient) {
        this.contextInheritanceService = Objects.requireNonNull(contextInheritanceService, "contextInheritanceService");
        this.llmClient = Objects.requireNonNull(llmClient, "llmClient");
    }

    @Override
    public RelationshipReport generate(ReportContext context) {
        // 优先使用context中的分析，若为空则从持久化存储继承
        PostvisitAnalysisContent previous = context.analysis();
        if (previous == null) {
            Optional<PostvisitAnalysisContent> inherited =
                contextInheritanceService.getInheritedAnalysis(context.operatingCaseId());
            previous = inherited.orElseThrow(() ->
                new IllegalStateException("No previous postvisit analysis found for operating case: " + context.operatingCaseId()));
        }

        CustomerOperatingView view = context.customerView().orElse(null);

        // P11 G6: 先尝试LLM生成
        String content;
        try {
            content = generateWithLlm(previous, context.operatingCaseId(), view);
        } catch (LlmClientException e) {
            // LLM失败，fallback到原有StringBuilder逻辑
            content = buildContent(previous, context.operatingCaseId(), view);
        }

        return new RelationshipReport(
            UUID.randomUUID(), context.operatingCaseId(), context.journeyId(),
            supportedType(), content,
            previous.keyFindings().stream().map(InteractionExtraction::evidenceRef).toList(),
            previous.reconciliationItems().stream().map(PostvisitAnalysisContent.FactReconciliationItem::topic).toList(),
            Instant.now(), context.previousReportId());
    }

    @Override
    public RelationshipReport.ReportType supportedType() {
        return RelationshipReport.ReportType.NEXT_PREVISIT;
    }

    /**
     * P11 G6: 使用LLM生成下次访前报告
     */
    private String generateWithLlm(PostvisitAnalysisContent previous, String operatingCaseId,
                                    CustomerOperatingView view) {
        String systemPrompt = PromptTemplate.reportSystemPrompt("下次访前报告");
        String contextSummary = buildContextSummary(previous, view);
        String userPrompt = PromptTemplate.reportUserPrompt(
            "下次访前报告", operatingCaseId,
            view != null ? view.customerId() : "unknown",
            contextSummary);

        String llmResponse = llmClient.complete(systemPrompt, userPrompt);

        // 解析LLM JSON响应，组装报告内容
        StringBuilder sb = new StringBuilder();
        sb.append("# 下次访前报告 (继承上下文, LLM增强)\n\n");

        // 客户经营视图数据驱动部分
        if (view != null) {
            sb.append("## 客户当前状态\n");
            sb.append("- 客户名称: ").append(view.customerName()).append("\n");
            sb.append("- 风险等级: ").append(view.riskLevel()).append("\n");
            sb.append("- 客户等级: ").append(view.customerTier()).append("\n");
            sb.append("- 客户经理: ").append(view.rmName()).append("\n\n");

            if (!view.unknownKycItems().isEmpty() || !view.partialKycItems().isEmpty()) {
                sb.append("## KYC信息缺口\n");
                if (!view.unknownKycItems().isEmpty()) {
                    sb.append("- 需收集: ").append(String.join("、", view.unknownKycItems())).append("\n");
                }
                if (!view.partialKycItems().isEmpty()) {
                    sb.append("- 需补充: ").append(String.join("、", view.partialKycItems())).append("\n");
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

            if (!view.pendingCommitments().isEmpty()) {
                sb.append("## 待履行承诺\n");
                for (CustomerOperatingView.CommitmentSummary c : view.pendingCommitments()) {
                    if (!c.fulfilled()) {
                        sb.append("- [").append(c.commitmentType()).append("] ").append(c.content())
                            .append(" (负责人: ").append(c.owner())
                            .append(", 截止: ").append(c.dueDate()).append(")\n");
                    }
                }
                sb.append("\n");
            }

            if (view.openReconciliationCount() > 0) {
                sb.append("## 待对账事实\n");
                sb.append("当前待对账事实: ").append(view.openReconciliationCount()).append("项，本次访问需确认。\n\n");
            }

            if (!view.riskIndicators().isEmpty()) {
                sb.append("## 风险提醒\n");
                for (String r : view.riskIndicators()) {
                    sb.append("- ").append(r).append("\n");
                }
                sb.append("\n");
            }
        }

        // 上次访后要点
        sb.append("## 上次访后要点\n");
        sb.append("- 摘要: ").append(previous.visitSummary()).append("\n");
        for (PostvisitAnalysisContent.OpportunitySignalItem s : previous.opportunitySignals()) {
            sb.append("- 待确认信号: ").append(s.content()).append("\n");
        }

        // LLM增强的本次重点
        sb.append("\n## 本次重点 (LLM增强)\n");
        sb.append(extractJsonField(llmResponse, "summary", "跟进未完成事项")).append("\n\n");

        sb.append("### 关键发现 (LLM增强)\n");
        List<String> llmFindings = extractJsonArrayField(llmResponse, "keyFindings");
        for (String f : llmFindings) {
            sb.append("- ").append(f).append("\n");
        }

        sb.append("\n### 风险提醒 (LLM增强)\n");
        List<String> llmRisks = extractJsonArrayField(llmResponse, "riskIndicators");
        if (!llmRisks.isEmpty()) {
            for (String r : llmRisks) {
                sb.append("- ").append(r).append("\n");
            }
        } else {
            // Fallback: 使用ContextInheritanceService继承的关键问题和风险提醒
            Optional<ContextInheritanceService.InheritedContext> inheritedOpt =
                contextInheritanceService.inheritContext(operatingCaseId);

            if (inheritedOpt.isPresent()) {
                ContextInheritanceService.InheritedContext inherited = inheritedOpt.get();

                List<String> keyQuestions = inherited.keyQuestions();
                if (keyQuestions != null && !keyQuestions.isEmpty()) {
                    sb.append("#### 继承的关键问题\n");
                    for (String q : keyQuestions) {
                        sb.append("- ").append(q).append("\n");
                    }
                }

                List<String> riskReminders = inherited.riskReminders();
                if (riskReminders != null && !riskReminders.isEmpty()) {
                    sb.append("#### 风险提醒\n");
                    for (String r : riskReminders) {
                        sb.append("- ").append(r).append("\n");
                    }
                }

                if (inherited.visitStrategy() != null) {
                    sb.append("\n#### 访问策略\n");
                    sb.append(inherited.visitStrategy()).append("\n");
                }
            } else {
                sb.append("- 跟进未完成的事实对账\n");
                sb.append("- 确认机会信号的真实性\n");
                sb.append("- 收集缺失的KYC信息\n");
            }
        }

        sb.append("\n### 建议 (LLM增强)\n");
        List<String> llmRecs = extractJsonArrayField(llmResponse, "recommendations");
        for (String r : llmRecs) {
            sb.append("- ").append(r).append("\n");
        }

        sb.append("\n### 行动项 (LLM增强)\n");
        List<String> llmActions = extractJsonArrayField(llmResponse, "actionItems");
        for (String a : llmActions) {
            sb.append("- ").append(a).append("\n");
        }

        return sb.toString();
    }

    private String buildContextSummary(PostvisitAnalysisContent previous, CustomerOperatingView view) {
        StringBuilder summary = new StringBuilder();
        if (view != null) {
            summary.append("客户: ").append(view.customerName())
                .append(", 风险: ").append(view.riskLevel());
        }
        summary.append(", 上次访后摘要: ").append(previous.visitSummary());
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

    private String buildContent(PostvisitAnalysisContent previous, String operatingCaseId, CustomerOperatingView view) {
        StringBuilder sb = new StringBuilder();
        sb.append("# 下次访前报告 (继承上下文)\n\n");

        // 客户经营视图数据驱动部分
        if (view != null) {
            sb.append("## 客户当前状态\n");
            sb.append("- 客户名称: ").append(view.customerName()).append("\n");
            sb.append("- 风险等级: ").append(view.riskLevel()).append("\n");
            sb.append("- 客户等级: ").append(view.customerTier()).append("\n");
            sb.append("- 客户经理: ").append(view.rmName()).append("\n\n");

            // KYC缺口提醒
            if (!view.unknownKycItems().isEmpty() || !view.partialKycItems().isEmpty()) {
                sb.append("## KYC信息缺口\n");
                if (!view.unknownKycItems().isEmpty()) {
                    sb.append("- 需收集: ").append(String.join("、", view.unknownKycItems())).append("\n");
                }
                if (!view.partialKycItems().isEmpty()) {
                    sb.append("- 需补充: ").append(String.join("、", view.partialKycItems())).append("\n");
                }
                sb.append("\n");
            }

            // 活跃机会信号
            if (!view.activeSignals().isEmpty()) {
                sb.append("## 活跃机会信号\n");
                for (CustomerOperatingView.OpportunitySignalSummary s : view.activeSignals()) {
                    sb.append("- [").append(s.signalType()).append("] ").append(s.content())
                        .append(" (置信度: ").append(s.confidence()).append(")\n");
                }
                sb.append("\n");
            }

            // 待履行承诺
            if (!view.pendingCommitments().isEmpty()) {
                sb.append("## 待履行承诺\n");
                for (CustomerOperatingView.CommitmentSummary c : view.pendingCommitments()) {
                    if (!c.fulfilled()) {
                        sb.append("- [").append(c.commitmentType()).append("] ").append(c.content())
                            .append(" (负责人: ").append(c.owner())
                            .append(", 截止: ").append(c.dueDate()).append(")\n");
                    }
                }
                sb.append("\n");
            }

            // 事实对账状态
            if (view.openReconciliationCount() > 0) {
                sb.append("## 待对账事实\n");
                sb.append("当前待对账事实: ").append(view.openReconciliationCount()).append("项，本次访问需确认。\n\n");
            }

            // 风险指标
            if (!view.riskIndicators().isEmpty()) {
                sb.append("## 风险提醒\n");
                for (String r : view.riskIndicators()) {
                    sb.append("- ").append(r).append("\n");
                }
                sb.append("\n");
            }
        }

        // 上次访后要点
        sb.append("## 上次访后要点\n");
        sb.append("- 摘要: ").append(previous.visitSummary()).append("\n");
        for (PostvisitAnalysisContent.OpportunitySignalItem s : previous.opportunitySignals()) {
            sb.append("- 待确认信号: ").append(s.content()).append("\n");
        }
        sb.append("\n## 本次重点\n");

        // P9 Loop G2: 使用ContextInheritanceService生成继承的关键问题和风险提醒
        Optional<ContextInheritanceService.InheritedContext> inheritedOpt =
            contextInheritanceService.inheritContext(operatingCaseId);

        if (inheritedOpt.isPresent()) {
            ContextInheritanceService.InheritedContext inherited = inheritedOpt.get();

            List<String> keyQuestions = inherited.keyQuestions();
            if (keyQuestions != null && !keyQuestions.isEmpty()) {
                sb.append("### 继承的关键问题\n");
                for (String q : keyQuestions) {
                    sb.append("- ").append(q).append("\n");
                }
            }

            List<String> riskReminders = inherited.riskReminders();
            if (riskReminders != null && !riskReminders.isEmpty()) {
                sb.append("### 风险提醒\n");
                for (String r : riskReminders) {
                    sb.append("- ").append(r).append("\n");
                }
            }

            if (inherited.visitStrategy() != null) {
                sb.append("\n### 访问策略\n");
                sb.append(inherited.visitStrategy()).append("\n");
            }
        } else {
            sb.append("- 跟进未完成的事实对账\n");
            sb.append("- 确认机会信号的真实性\n");
            sb.append("- 收集缺失的KYC信息\n");
        }

        return sb.toString();
    }
}
