package com.gien.gits.ontology;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record HumanConfirmation(UUID confirmationId, UUID subjectId, Decision decision, String actorId, Instant confirmedAt) {
    public enum Decision { APPROVED, MODIFIED_AND_APPROVED, REJECTED }

    public HumanConfirmation {
        Objects.requireNonNull(confirmationId, "confirmationId");
        Objects.requireNonNull(subjectId, "subjectId");
        Objects.requireNonNull(decision, "decision");
        Objects.requireNonNull(confirmedAt, "confirmedAt");
        if (actorId == null || actorId.isBlank()) {
            throw new IllegalArgumentException("actorId is required");
        }
    }

    public boolean authorizesAction() {
        return decision == Decision.APPROVED || decision == Decision.MODIFIED_AND_APPROVED;
    }
}
