package com.gien.gits.adapter.persistence.foundation.ontology.dto;

import com.gien.gits.ontology.ExternalEvent;

import java.time.LocalDate;
import java.time.Instant;
import java.util.List;

/**
 * Flat DTO for MyBatis row mapping of external_event table.
 * Uses wrapper types (Boolean) to match MyBatis constructor lookup.
 */
public record ExternalEventRow(
        String eventId,
        LocalDate eventDate,
        ExternalEvent.SourceType sourceType,
        String sourceName,
        String entity,
        String title,
        String content,
        ExternalEvent.Confidence confidence,
        ExternalEvent.Reliability reliability,
        Boolean bankUseAllowed,
        List<String> linkedThemes,
        String possibleBusinessSignal,
        String noGoStatement,
        String evidenceRef) {

    public ExternalEvent toExternalEvent() {
        return new ExternalEvent(
                eventId, eventDate, sourceType, sourceName, entity, title, content,
                confidence, reliability,
                bankUseAllowed != null && bankUseAllowed,
                linkedThemes, possibleBusinessSignal, noGoStatement, evidenceRef);
    }
}
