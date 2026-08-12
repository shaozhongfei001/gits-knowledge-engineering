package com.gien.gits.adapter.persistence.foundation.engagement.dto;

import com.gien.gits.engagement.InteractionExtraction;
import com.gien.gits.engagement.PostvisitAnalysisContent;

import java.util.List;

/**
 * Flat DTO for MyBatis row mapping of postvisit_analysis_content table.
 * Handles mismatch between DB columns and domain record fields.
 */
public record PostvisitAnalysisContentRow(
        String id,
        String journeyId,
        List<InteractionExtraction> keyFindings,
        List<PostvisitAnalysisContent.OpportunitySignalItem> opportunitySignals,
        List<PostvisitAnalysisContent.CommitmentItem> commitments,
        List<PostvisitAnalysisContent.FactReconciliationItem> reconciliationItems,
        List<String> followUpActions) {

    public PostvisitAnalysisContent toPostvisitAnalysisContent() {
        return new PostvisitAnalysisContent(
                id, journeyId, null,
                keyFindings != null ? keyFindings : List.of(),
                opportunitySignals != null ? opportunitySignals : List.of(),
                commitments != null ? commitments : List.of(),
                reconciliationItems != null ? reconciliationItems : List.of(),
                followUpActions != null ? followUpActions : List.of(),
                null);
    }
}
