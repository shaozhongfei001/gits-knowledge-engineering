package com.gien.gits.engagement;

import java.time.Instant;
import java.util.List;

/**
 * 外联脚本 — RM主动联系客户前的结构化话术指导
 */
public record OutreachScript(
        String scriptId,
        String customerId,
        String rmId,
        String operatingCaseId,
        String journeyId,
        OutreachChannel channel,
        String objective,
        String openingLine,
        List<TalkingPoint> talkingPoints,
        List<String> riskReminders,
        String closingLine,
        String followUpAction,
        Instant createdAt) {

    public enum OutreachChannel {
        PHONE, WECHAT, EMAIL, FACE_TO_FACE
    }

    public record TalkingPoint(
            String topic,
            String detail,
            String suggestedQuestion,
            int priority) {}

    public OutreachScript {
        if (scriptId == null || scriptId.isBlank()) {
            throw new IllegalArgumentException("scriptId is required");
        }
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("customerId is required");
        }
        if (rmId == null || rmId.isBlank()) {
            throw new IllegalArgumentException("rmId is required");
        }
        talkingPoints = List.copyOf(talkingPoints != null ? talkingPoints : List.of());
        riskReminders = List.copyOf(riskReminders != null ? riskReminders : List.of());
        if (createdAt == null) createdAt = Instant.now();
    }
}
