package com.gien.gits.customerjourney;

import com.gien.gits.ontology.Claim;
import com.gien.gits.ontology.ClaimStatus;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record InsightClaim(
        UUID insightId,
        UUID claimId,
        UUID operatingCaseId,
        String insightCategory,
        String insightSummary,
        Instant generatedAt) {

    public InsightClaim {
        Objects.requireNonNull(insightId, "insightId");
        Objects.requireNonNull(claimId, "claimId");
        Objects.requireNonNull(operatingCaseId, "operatingCaseId");
        insightCategory = requireText(insightCategory, "insightCategory");
        insightSummary = requireText(insightSummary, "insightSummary");
        Objects.requireNonNull(generatedAt, "generatedAt");
    }

    public static InsightClaim fromClaim(Claim claim, String insightCategory, String insightSummary) {
        Objects.requireNonNull(claim, "claim");
        if (!"CUSTOMER_JOURNEY".equals(claim.claimType())) {
            throw new IllegalArgumentException(
                    "Claim must have claimType CUSTOMER_JOURNEY, but was: " + claim.claimType());
        }
        return new InsightClaim(
                UUID.randomUUID(),
                claim.claimId(),
                claim.caseId(),
                insightCategory,
                insightSummary,
                Instant.now());
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}