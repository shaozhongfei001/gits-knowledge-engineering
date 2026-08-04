package com.gien.gits.ontology;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * 机会信号 — 从交互/事件/分析中检测到的业务机会
 */
public record OpportunitySignal(
        UUID signalId,
        String operatingCaseId,
        String journeyId,
        SignalType signalType,
        String content,
        SignalSourceType sourceType,
        String sourceRef,
        BigDecimal confidence,
        SignalStatus status,
        String evidenceRef,
        Instant detectedAt,
        Instant confirmedAt,
        Instant createdAt,
        Instant updatedAt) {

    public enum SignalType { FINANCING_NEED, PRODUCT_OPPORTUNITY, RELATIONSHIP_CHANGE }
    public enum SignalSourceType { INTERACTION, EXTERNAL_EVENT, ANALYSIS }
    public enum SignalStatus { DETECTED, CONFIRMED, DISMISSED, CONVERTED }

    public OpportunitySignal {
        Objects.requireNonNull(signalId, "signalId");
        Objects.requireNonNull(signalType, "signalType");
        Objects.requireNonNull(sourceType, "sourceType");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(detectedAt, "detectedAt");
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = createdAt;
    }

    /** 兼容旧构造器（无审计字段） */
    public OpportunitySignal(UUID signalId, String operatingCaseId, String journeyId,
                             SignalType signalType, String content, SignalSourceType sourceType,
                             String sourceRef, BigDecimal confidence, SignalStatus status,
                             String evidenceRef, Instant detectedAt, Instant confirmedAt) {
        this(signalId, operatingCaseId, journeyId, signalType, content, sourceType, sourceRef,
             confidence, status, evidenceRef, detectedAt, confirmedAt, Instant.now(), Instant.now());
    }
}
