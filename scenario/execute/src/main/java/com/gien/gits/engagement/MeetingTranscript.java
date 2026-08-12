package com.gien.gits.engagement;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record MeetingTranscript(
        String transcriptId,
        String journeyId,
        String rawContent,
        List<InteractionExtraction> extractions,
        List<String> qualityNotes,
        Instant recordedAt) {

    public MeetingTranscript {
        if (transcriptId == null || transcriptId.isBlank()) {
            throw new IllegalArgumentException("transcriptId is required");
        }
        if (journeyId == null || journeyId.isBlank()) {
            throw new IllegalArgumentException("journeyId is required");
        }
        extractions = List.copyOf(extractions != null ? extractions : List.of());
        qualityNotes = List.copyOf(qualityNotes != null ? qualityNotes : List.of());
        Objects.requireNonNull(recordedAt, "recordedAt");
    }
}
