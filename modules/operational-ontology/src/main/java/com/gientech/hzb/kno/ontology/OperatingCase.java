package com.gientech.hzb.kno.ontology;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record OperatingCase(
        UUID caseId,
        String caseType,
        CaseStatus status,
        String purpose,
        Instant validFrom,
        Instant validTo,
        Instant recordedAt) {

    public OperatingCase {
        Objects.requireNonNull(caseId, "caseId");
        caseType = requireText(caseType, "caseType");
        Objects.requireNonNull(status, "status");
        purpose = requireText(purpose, "purpose");
        Objects.requireNonNull(validFrom, "validFrom");
        Objects.requireNonNull(recordedAt, "recordedAt");
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
