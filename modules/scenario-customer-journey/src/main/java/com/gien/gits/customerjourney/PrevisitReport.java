package com.gien.gits.customerjourney;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record PrevisitReport(
        UUID reportId,
        UUID operatingCaseId,
        UUID journeyId,
        List<UUID> insightIds,
        List<UUID> productCandidateIds,
        String summary,
        Instant generatedAt) {

    public PrevisitReport {
        Objects.requireNonNull(reportId, "reportId");
        Objects.requireNonNull(operatingCaseId, "operatingCaseId");
        Objects.requireNonNull(journeyId, "journeyId");
        insightIds = List.copyOf(Objects.requireNonNullElse(insightIds, Collections.emptyList()));
        productCandidateIds = List.copyOf(Objects.requireNonNullElse(productCandidateIds, Collections.emptyList()));
        summary = requireText(summary, "summary");
        Objects.requireNonNull(generatedAt, "generatedAt");
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}