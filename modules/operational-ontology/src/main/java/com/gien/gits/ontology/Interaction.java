package com.gien.gits.ontology;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Interaction(UUID interactionId, UUID caseId, Instant occurredAt, String channel, String sourceHash) {
    public Interaction {
        Objects.requireNonNull(interactionId, "interactionId");
        Objects.requireNonNull(caseId, "caseId");
        Objects.requireNonNull(occurredAt, "occurredAt");
        if (channel == null || channel.isBlank() || sourceHash == null || sourceHash.isBlank()) {
            throw new IllegalArgumentException("channel and sourceHash are required");
        }
    }
}
