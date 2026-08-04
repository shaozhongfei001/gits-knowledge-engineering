package com.gien.gits.api.service;

import com.gien.gits.engagement.PostvisitAnalysisContent;
import com.gien.gits.engagement.PrevisitReportContent;
import com.gien.gits.engagement.port.WritablePostvisitAnalysisContentRepository;
import com.gien.gits.engagement.port.WritablePrevisitReportContentRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 上下文继承服务 — 从最近一次访后分析继承上下文，生成访前报告的建议字段。
 *
 * <p>P9 Loop G2: 实现R4→R8的上下文继承闭环，使下次访前报告能继承上次访后分析内容。</p>
 */
public class ContextInheritanceService {

    private final WritablePostvisitAnalysisContentRepository postvisitRepo;
    private final WritablePrevisitReportContentRepository previsitRepo;

    public ContextInheritanceService(
            WritablePostvisitAnalysisContentRepository postvisitRepo,
            WritablePrevisitReportContentRepository previsitRepo) {
        this.postvisitRepo = Objects.requireNonNull(postvisitRepo, "postvisitRepo");
        this.previsitRepo = Objects.requireNonNull(previsitRepo, "previsitRepo");
    }

    /**
     * 从最近一次PostvisitAnalysisContent继承上下文，生成PrevisitReportContent的建议字段。
     *
     * @param operatingCaseId 运营案例唯一标识
     * @return 继承的建议字段，若无历史分析则返回空
     */
    public Optional<InheritedContext> inheritContext(String operatingCaseId) {
        Optional<PostvisitAnalysisContent> prev = getInheritedAnalysis(operatingCaseId);
        if (prev.isEmpty()) {
            return Optional.empty();
        }
        PostvisitAnalysisContent analysis = prev.get();
        List<String> keyQuestions = buildInheritedKeyQuestions(analysis);
        List<String> riskReminders = buildInheritedRiskReminders(analysis);
        String visitStrategy = buildInheritedVisitStrategy(analysis);
        return Optional.of(new InheritedContext(keyQuestions, riskReminders, visitStrategy));
    }

    /**
     * 获取运营案例最近一次访后分析内容。
     *
     * @param operatingCaseId 运营案例唯一标识
     * @return 最近的访后分析内容
     */
    public Optional<PostvisitAnalysisContent> getInheritedAnalysis(String operatingCaseId) {
        return postvisitRepo.findLatestByOperatingCaseId(operatingCaseId);
    }

    /**
     * 从followUpActions + reconciliationItems生成继承的关键问题列表。
     *
     * @param prev 上次访后分析内容
     * @return 关键问题列表
     */
    public List<String> buildInheritedKeyQuestions(PostvisitAnalysisContent prev) {
        List<String> questions = new ArrayList<>();

        // 从跟进行动生成问题
        if (prev.followUpActions() != null) {
            for (String action : prev.followUpActions()) {
                questions.add("跟进: " + action);
            }
        }

        // 从事实对账项生成问题
        if (prev.reconciliationItems() != null) {
            for (PostvisitAnalysisContent.FactReconciliationItem item : prev.reconciliationItems()) {
                if (item.nextAction() != null) {
                    questions.add(item.nextAction());
                } else {
                    questions.add("确认: " + item.topic());
                }
            }
        }

        return questions;
    }

    /**
     * 从reconciliationItems生成风险提醒列表。
     *
     * @param prev 上次访后分析内容
     * @return 风险提醒列表
     */
    public List<String> buildInheritedRiskReminders(PostvisitAnalysisContent prev) {
        List<String> reminders = new ArrayList<>();

        if (prev.reconciliationItems() != null) {
            for (PostvisitAnalysisContent.FactReconciliationItem item : prev.reconciliationItems()) {
                reminders.add("待对账: " + item.topic() + " — " + item.correctJudgment());
            }
        }

        // 机会信号的风险提醒
        if (prev.opportunitySignals() != null) {
            for (PostvisitAnalysisContent.OpportunitySignalItem signal : prev.opportunitySignals()) {
                if (signal.notOpportunityYet()) {
                    reminders.add("信号待确认: " + signal.content() + " (OpportunitySignal≠Opportunity)");
                }
            }
        }

        return reminders;
    }

    /**
     * 从访后分析生成下次访问策略建议。
     */
    private String buildInheritedVisitStrategy(PostvisitAnalysisContent prev) {
        StringBuilder sb = new StringBuilder();
        sb.append("基于上次访后分析: ");
        if (prev.nextStepRecommendation() != null) {
            sb.append(prev.nextStepRecommendation());
        } else {
            sb.append("继续常规经营跟进");
        }
        return sb.toString();
    }

    /**
     * 继承上下文的值对象 — 包含访前报告的建议字段。
     */
    public record InheritedContext(
        List<String> keyQuestions,
        List<String> riskReminders,
        String visitStrategy) {}
}
