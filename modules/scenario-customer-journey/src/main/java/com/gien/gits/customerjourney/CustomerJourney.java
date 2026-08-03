package com.gien.gits.customerjourney;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record CustomerJourney(
        UUID journeyId,
        UUID operatingCaseId,
        String customerId,
        String customerName,
        JourneyPhase phase,
        Instant startedAt,
        Instant updatedAt) {

    public CustomerJourney {
        Objects.requireNonNull(journeyId, "journeyId");
        Objects.requireNonNull(operatingCaseId, "operatingCaseId");
        customerId = requireText(customerId, "customerId");
        Objects.requireNonNull(phase, "phase");
        Objects.requireNonNull(startedAt, "startedAt");
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}