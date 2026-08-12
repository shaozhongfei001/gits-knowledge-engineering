package com.gien.gits.engagement;

import java.time.Instant;
import java.util.List;

/**
 * 会面脚本 — RM拜访客户前的结构化议程和问题清单
 */
public record MeetingScript(
        String scriptId,
        String customerId,
        String rmId,
        String operatingCaseId,
        String journeyId,
        String meetingObjective,
        String previsitSummary,
        List<AgendaItem> agendaItems,
        List<KycQuestionItem> kycQuestions,
        List<ProductDiscussionItem> productDiscussions,
        List<String> riskPoints,
        String closingSummary,
        Instant createdAt) {

    public record AgendaItem(
            String topic,
            int durationMinutes,
            String keyPoints,
            String expectedOutcome) {}

    public record KycQuestionItem(
            String gapArea,
            String question,
            String purpose,
            String expectedAnswerType) {}

    public record ProductDiscussionItem(
            String productId,
            String productName,
            String discussionAngle,
            List<String> keySellingPoints) {}

    public MeetingScript {
        if (scriptId == null || scriptId.isBlank()) {
            throw new IllegalArgumentException("scriptId is required");
        }
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("customerId is required");
        }
        if (rmId == null || rmId.isBlank()) {
            throw new IllegalArgumentException("rmId is required");
        }
        agendaItems = List.copyOf(agendaItems != null ? agendaItems : List.of());
        kycQuestions = List.copyOf(kycQuestions != null ? kycQuestions : List.of());
        productDiscussions = List.copyOf(productDiscussions != null ? productDiscussions : List.of());
        riskPoints = List.copyOf(riskPoints != null ? riskPoints : List.of());
        if (createdAt == null) createdAt = Instant.now();
    }
}
