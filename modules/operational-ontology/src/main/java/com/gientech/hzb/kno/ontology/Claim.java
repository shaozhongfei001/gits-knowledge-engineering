package com.gientech.hzb.kno.ontology;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Claim(
        UUID claimId,
        UUID caseId,
        String claimType,
        ClaimStatus status,
        String statement,
        Instant validFrom,
        Instant validTo,
        Instant recordedAt,
        UUID supersedesClaimId) {

    public Claim {
        Objects.requireNonNull(claimId, "claimId");
        Objects.requireNonNull(caseId, "caseId");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(recordedAt, "recordedAt");
        if (claimType == null || claimType.isBlank() || statement == null || statement.isBlank()) {
            throw new IllegalArgumentException("claimType and statement are required");
        }
        if (validFrom != null && validTo != null && validTo.isBefore(validFrom)) {
            throw new IllegalArgumentException("validTo must not precede validFrom");
        }
    }

    public boolean isAuthoritative() {
        return status == ClaimStatus.VERIFIED_FACT;
    }
}
