package com.gien.gits.customerjourney;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ProductCandidateClaim(
        UUID productId,
        UUID claimId,
        UUID insightClaimId,
        UUID operatingCaseId,
        String productCode,
        String productName,
        String matchReason,
        Instant proposedAt) {

    public ProductCandidateClaim {
        Objects.requireNonNull(productId, "productId");
        Objects.requireNonNull(claimId, "claimId");
        Objects.requireNonNull(insightClaimId, "insightClaimId");
        Objects.requireNonNull(operatingCaseId, "operatingCaseId");
        productCode = requireText(productCode, "productCode");
        if (productName == null) {
            productName = "";
        }
        matchReason = requireText(matchReason, "matchReason");
        Objects.requireNonNull(proposedAt, "proposedAt");
    }

    public static ProductCandidateClaim fromInsight(InsightClaim insight, String productCode,
                                                     String productName, String matchReason) {
        Objects.requireNonNull(insight, "insight");
        return new ProductCandidateClaim(
                UUID.randomUUID(),
                insight.claimId(),
                insight.insightId(),
                insight.operatingCaseId(),
                productCode,
                productName,
                matchReason,
                Instant.now());
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}