package com.gien.gits.ontology;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record OperatingCase(
        UUID caseId,
        CaseType caseType,
        CaseStatus status,
        String purpose,
        Instant validFrom,
        Instant validTo,
        Instant recordedAt,
        String createdBy) {

    public OperatingCase {
        Objects.requireNonNull(caseId, "caseId");
        Objects.requireNonNull(caseType, "caseType");
        Objects.requireNonNull(status, "status");
        purpose = requireText(purpose, "purpose");
        Objects.requireNonNull(validFrom, "validFrom");
        Objects.requireNonNull(recordedAt, "recordedAt");
        createdBy = requireText(createdBy, "createdBy");
        if (validTo != null && validTo.isBefore(validFrom)) {
            throw new IllegalArgumentException("validTo must not precede validFrom");
        }
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}
