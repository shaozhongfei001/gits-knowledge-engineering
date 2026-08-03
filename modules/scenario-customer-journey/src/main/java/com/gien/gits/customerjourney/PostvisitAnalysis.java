package com.gien.gits.customerjourney;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record PostvisitAnalysis(
        UUID analysisId,
        UUID operatingCaseId,
        UUID journeyId,
        UUID previsitReportId,
        String outcome,
        String followUpAction,
        Instant analyzedAt) {

    public PostvisitAnalysis {
        Objects.requireNonNull(analysisId, "analysisId");
        Objects.requireNonNull(operatingCaseId, "operatingCaseId");
        Objects.requireNonNull(journeyId, "journeyId");
        outcome = requireText(outcome, "outcome");
        Objects.requireNonNull(analyzedAt, "analyzedAt");
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}