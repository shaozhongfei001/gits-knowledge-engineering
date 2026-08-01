package com.gien.gits.context;

import com.gien.gits.ontology.Claim;
import com.gien.gits.ontology.Evidence;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record EvidenceBundle(UUID bundleId, UUID caseId, String purpose, String permissionDecisionId, Instant assembledAt,
        List<Claim> facts, List<Claim> candidateClaims, List<Evidence> evidence, List<String> unknowns, List<String> conflicts) {
    public EvidenceBundle {
        Objects.requireNonNull(bundleId, "bundleId");
        Objects.requireNonNull(caseId, "caseId");
        Objects.requireNonNull(assembledAt, "assembledAt");
        if (purpose == null || purpose.isBlank() || permissionDecisionId == null || permissionDecisionId.isBlank()) {
            throw new IllegalArgumentException("purpose and permissionDecisionId are required");
        }
        facts = List.copyOf(facts);
        candidateClaims = List.copyOf(candidateClaims);
        evidence = List.copyOf(evidence);
        unknowns = List.copyOf(unknowns);
        conflicts = List.copyOf(conflicts);
        if (facts.stream().anyMatch(claim -> !claim.isAuthoritative())) {
            throw new IllegalArgumentException("facts collection may only contain verified facts");
        }
    }
}
