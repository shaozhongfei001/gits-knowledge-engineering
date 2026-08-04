package com.gien.gits.ontology;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * 外部事件 — 来自新闻/公告/监管等外部来源的事件
 */
public record ExternalEvent(
        String eventId,
        LocalDate eventDate,
        SourceType sourceType,
        String sourceName,
        String entity,
        String title,
        String content,
        Confidence confidence,
        Reliability reliability,
        boolean bankUseAllowed,
        List<String> linkedThemes,
        String possibleBusinessSignal,
        String noGoStatement,
        String evidenceRef) {

    /** 来源类型 */
    public enum SourceType { NEWS, REGULATORY, INDUSTRY, SOCIAL_MEDIA, OFFICIAL_ANNOUNCEMENT }

    /** 置信度 — 与SQL CHECK约束对齐: HIGH / MEDIUM / LOW */
    public enum Confidence { HIGH, MEDIUM, LOW }

    /** 可靠性 */
    public enum Reliability { VERIFIED, UNVERIFIED, DISPUTED }

    public ExternalEvent {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("eventId is required");
        }
        Objects.requireNonNull(eventDate, "eventDate");
        Objects.requireNonNull(sourceType, "sourceType");
        Objects.requireNonNull(confidence, "confidence");
        Objects.requireNonNull(reliability, "reliability");
        linkedThemes = List.copyOf(linkedThemes != null ? linkedThemes : List.of());
    }

    /** 兼容旧构造器（String版confidence/reliability/sourceType） */
    public ExternalEvent(String eventId, LocalDate eventDate, String sourceType, String sourceName,
                         String entity, String title, String content, String confidence, String reliability,
                         boolean bankUseAllowed, List<String> linkedThemes, String possibleBusinessSignal,
                         String noGoStatement, String evidenceRef) {
        this(eventId, eventDate,
             sourceType != null ? SourceType.valueOf(sourceType.toUpperCase().replace(" ", "_")) : null,
             sourceName, entity, title, content,
             confidence != null ? Confidence.valueOf(confidence.toUpperCase()) : Confidence.MEDIUM,
             reliability != null ? Reliability.valueOf(reliability.toUpperCase()) : Reliability.UNVERIFIED,
             bankUseAllowed, linkedThemes, possibleBusinessSignal, noGoStatement, evidenceRef);
    }
}
