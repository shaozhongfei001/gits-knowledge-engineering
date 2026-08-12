package com.gien.gits.engagement;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * 访后分析 — R4格式
 */
public record PostvisitAnalysisContent(
        String analysisId,
        String journeyId,
        String visitSummary,
        List<InteractionExtraction> keyFindings,
        List<OpportunitySignalItem> opportunitySignals,
        List<CommitmentItem> commitments,
        List<FactReconciliationItem> reconciliationItems,
        List<String> followUpActions,
        String nextStepRecommendation) {

    public PostvisitAnalysisContent {
        Objects.requireNonNull(analysisId, "analysisId");
        keyFindings = List.copyOf(keyFindings != null ? keyFindings : List.of());
        opportunitySignals = List.copyOf(opportunitySignals != null ? opportunitySignals : List.of());
        commitments = List.copyOf(commitments != null ? commitments : List.of());
        reconciliationItems = List.copyOf(reconciliationItems != null ? reconciliationItems : List.of());
        followUpActions = List.copyOf(followUpActions != null ? followUpActions : List.of());
    }

    public record OpportunitySignalItem(
            String signalType,
            String content,
            String sourceType,
            BigDecimal confidence,
            boolean notOpportunityYet) {}

    public record CommitmentItem(
            String commitmentType,
            String content,
            String owner,
            String dueDate) {}

    public record FactReconciliationItem(
            String topic,
            String structuredFact,
            String interactionClaim,
            String correctJudgment,
            String nextAction) {}
}
