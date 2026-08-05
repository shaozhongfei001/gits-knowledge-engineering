package com.gien.gits.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gien.gits.ontology.Claim;
import com.gien.gits.ontology.ClaimStatus;
import com.gien.gits.ontology.ClaimType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EvidenceBundleTest {

    @Test
    void emptyBundleIsValid() {
        EvidenceBundle bundle = new EvidenceBundle(
                UUID.randomUUID(), UUID.randomUUID(), "purpose", "PENDING", Instant.now(),
                List.of(), List.of(), List.of(), List.of(), List.of());

        assertTrue(bundle.facts().isEmpty());
        assertTrue(bundle.candidateClaims().isEmpty());
        assertTrue(bundle.evidence().isEmpty());
        assertTrue(bundle.unknowns().isEmpty());
        assertTrue(bundle.conflicts().isEmpty());
    }

    @Test
    void bundleWithVerifiedFactsAndCandidates() {
        Claim fact = new Claim(UUID.randomUUID(), UUID.randomUUID(), ClaimType.SYSTEM_FACT,
                ClaimStatus.VERIFIED_FACT, "Fact statement", Instant.now(), null, Instant.now(), null);
        Claim candidate = new Claim(UUID.randomUUID(), UUID.randomUUID(), ClaimType.CUSTOMER_STATEMENT,
                ClaimStatus.CANDIDATE, "Candidate statement", null, null, Instant.now(), null);

        EvidenceBundle bundle = new EvidenceBundle(
                UUID.randomUUID(), UUID.randomUUID(), "reconciliation", "APPROVED", Instant.now(),
                List.of(fact), List.of(candidate), List.of(), List.of(), List.of());

        assertEquals(1, bundle.facts().size());
        assertEquals(1, bundle.candidateClaims().size());
        assertTrue(fact.isAuthoritative());
    }

    @Test
    void bundleRejectsNonAuthoritativeFact() {
        Claim candidate = new Claim(UUID.randomUUID(), UUID.randomUUID(), ClaimType.CUSTOMER_STATEMENT,
                ClaimStatus.CANDIDATE, "Not a fact", null, null, Instant.now(), null);

        assertThrows(IllegalArgumentException.class, () -> new EvidenceBundle(
                UUID.randomUUID(), UUID.randomUUID(), "purpose", "PENDING", Instant.now(),
                List.of(candidate), List.of(), List.of(), List.of(), List.of()));
    }

    @Test
    void bundleListsAreImmutable() {
        EvidenceBundle bundle = new EvidenceBundle(
                UUID.randomUUID(), UUID.randomUUID(), "purpose", "PENDING", Instant.now(),
                List.of(), List.of(), List.of(), List.of(), List.of());

        assertThrows(UnsupportedOperationException.class, () -> bundle.facts().add(null));
        assertThrows(UnsupportedOperationException.class, () -> bundle.candidateClaims().add(null));
    }
}
