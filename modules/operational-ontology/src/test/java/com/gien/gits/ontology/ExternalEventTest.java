package com.gien.gits.ontology;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class ExternalEventTest {

    @Test
    void validConstructionWithAllFields() {
        ExternalEvent event = new ExternalEvent(
                "EVT-001", LocalDate.of(2026, 1, 15),
                ExternalEvent.SourceType.NEWS, "Reuters", "Acme Corp",
                "Acme expands", "Content here",
                ExternalEvent.Confidence.HIGH, ExternalEvent.Reliability.VERIFIED,
                true, List.of("expansion"), "growth signal", null, "REF-1");

        assertEquals("EVT-001", event.eventId());
        assertEquals(ExternalEvent.SourceType.NEWS, event.sourceType());
        assertEquals(ExternalEvent.Confidence.HIGH, event.confidence());
        assertEquals(ExternalEvent.Reliability.VERIFIED, event.reliability());
        assertTrue(event.bankUseAllowed());
        assertEquals(1, event.linkedThemes().size());
    }

    @Test
    void stringConstructorParsesEnums() {
        ExternalEvent event = new ExternalEvent(
                "EVT-002", LocalDate.of(2026, 2, 1),
                "REGULATORY", "CBIRC", "Bank X",
                "New regulation", "Content",
                "MEDIUM", "UNVERIFIED",
                false, null, null, null, null);

        assertEquals(ExternalEvent.SourceType.REGULATORY, event.sourceType());
        assertEquals(ExternalEvent.Confidence.MEDIUM, event.confidence());
        assertEquals(ExternalEvent.Reliability.UNVERIFIED, event.reliability());
    }

    @Test
    void nullConfidenceDefaultsToMedium() {
        ExternalEvent event = new ExternalEvent(
                "EVT-003", LocalDate.of(2026, 3, 1),
                "INDUSTRY", "Source", "Entity",
                "Title", "Content",
                null, null,
                true, List.of(), null, null, null);

        assertEquals(ExternalEvent.Confidence.MEDIUM, event.confidence());
        assertEquals(ExternalEvent.Reliability.UNVERIFIED, event.reliability());
    }

    @Test
    void blankEventIdRejected() {
        assertThrows(IllegalArgumentException.class, () -> new ExternalEvent(
                "  ", LocalDate.now(), ExternalEvent.SourceType.NEWS, "src", "ent",
                "title", "content", ExternalEvent.Confidence.LOW,
                ExternalEvent.Reliability.DISPUTED, true, List.of(), null, null, null));
    }

    @Test
    void nullEventDateRejected() {
        assertThrows(NullPointerException.class, () -> new ExternalEvent(
                "EVT-004", null, ExternalEvent.SourceType.NEWS, "src", "ent",
                "title", "content", ExternalEvent.Confidence.LOW,
                ExternalEvent.Reliability.DISPUTED, true, List.of(), null, null, null));
    }

    @Test
    void nullLinkedThemesDefaultsToEmptyList() {
        ExternalEvent event = new ExternalEvent(
                "EVT-005", LocalDate.now(), ExternalEvent.SourceType.OFFICIAL_ANNOUNCEMENT,
                "src", "ent", "title", "content",
                ExternalEvent.Confidence.HIGH, ExternalEvent.Reliability.VERIFIED,
                true, null, null, null, null);

        assertTrue(event.linkedThemes().isEmpty());
    }

    @Test
    void linkedThemesIsImmutable() {
        ExternalEvent event = new ExternalEvent(
                "EVT-006", LocalDate.now(), ExternalEvent.SourceType.SOCIAL_MEDIA,
                "src", "ent", "title", "content",
                ExternalEvent.Confidence.LOW, ExternalEvent.Reliability.UNVERIFIED,
                false, List.of("theme1"), null, null, null);

        assertThrows(UnsupportedOperationException.class, () -> event.linkedThemes().add("theme2"));
    }
}
