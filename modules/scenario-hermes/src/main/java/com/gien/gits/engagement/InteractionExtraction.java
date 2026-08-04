package com.gien.gits.engagement;

import java.math.BigDecimal;

public record InteractionExtraction(
        String objectId,
        ExtractionType type,
        ClaimType claimType,
        String content,
        String speaker,
        String evidenceRef,
        ExtractionStatus status,
        BigDecimal confidence,
        boolean notFact,
        boolean requiresReconciliation,
        String conflictWith,
        String nextQuestion) {

    /**
     * 提取类型
     */
    public enum ExtractionType {
        CLAIM, INTENT, CLARIFIED_INTENT,
        CUSTOMER_COMMITMENT, BANK_COMMITMENT, OPPORTUNITY_SIGNAL,
        FACT_CLAIM, COMMITMENT, RISK_INDICATOR
    }

    /**
     * 声明类型 — 与SQL claim_type列值一致
     */
    public enum ClaimType {
        FINANCING_NEED,
        MATERIAL_PROVIDE,
        FOLLOW_UP,
        EXPANSION_INTENT,
        CUSTOMER_STATEMENT,
        RM_COMMITMENT,
        RISK_SIGNAL,
        CUSTOMER_JOURNEY
    }

    /**
     * 提取状态 — 与SQL claim_status列值一致
     */
    public enum ExtractionStatus {
        DETECTED,
        CANDIDATE,
        VERIFIED_FACT,
        REJECTED,
        SUPERSEDED
    }

    public InteractionExtraction {
        if (objectId == null || objectId.isBlank()) {
            throw new IllegalArgumentException("objectId is required");
        }
        java.util.Objects.requireNonNull(type, "type");
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content is required");
        }
    }

    /**
     * 便捷构造函数 — 用于语义模式提取策略等场景
     */
    public InteractionExtraction(
            String objectId,
            ExtractionType type,
            String content,
            boolean notFact,
            boolean requiresReconciliation,
            String evidenceRef,
            ClaimType claimType,
            double confidence) {
        this(objectId, type, claimType, content,
             null, evidenceRef,
             ExtractionStatus.DETECTED,
             BigDecimal.valueOf(confidence),
             notFact, requiresReconciliation,
             null, null);
    }

    /**
     * 兼容旧String参数的构造函数
     */
    @Deprecated
    public InteractionExtraction(
            String objectId,
            ExtractionType type,
            String claimType,
            String content,
            String speaker,
            String evidenceRef,
            String status,
            BigDecimal confidence,
            boolean notFact,
            boolean requiresReconciliation,
            String conflictWith,
            String nextQuestion) {
        this(objectId, type,
             claimType != null ? ClaimType.valueOf(claimType) : null,
             content, speaker, evidenceRef,
             status != null ? ExtractionStatus.valueOf(status) : null,
             confidence, notFact, requiresReconciliation,
             conflictWith, nextQuestion);
    }
}
