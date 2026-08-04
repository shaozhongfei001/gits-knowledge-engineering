package com.gien.gits.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gien.gits.ontology.Claim;
import com.gien.gits.ontology.ClaimStatus;
import com.gien.gits.ontology.ClaimType;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DefaultContextAssemblerTest {

    private final ContextAssemblyPort assembler = new DefaultContextAssembler();

    @Test
    void assembleValidRequestProducesBundleWithExpectedFields() {
        UUID caseId = UUID.randomUUID();
        Instant asOf = Instant.parse("2026-08-02T00:00:00Z");
        Map<String, Object> permissionContext = Map.of("permissionDecisionId", "DEC-123");

        ContextAssemblyPort.Request request = new ContextAssemblyPort.Request(
                caseId, "claim-reconciliation", "identity-token-ref", permissionContext, asOf);

        EvidenceBundle bundle = assembler.assemble(request);

        assertNotNull(bundle.bundleId());
        assertEquals(caseId, bundle.caseId());
        assertEquals("claim-reconciliation", bundle.purpose());
        assertEquals("DEC-123", bundle.permissionDecisionId());
        assertEquals(asOf, bundle.assembledAt());
        assertTrue(bundle.facts().isEmpty());
        assertTrue(bundle.candidateClaims().isEmpty());
        assertTrue(bundle.evidence().isEmpty());
        assertTrue(bundle.unknowns().isEmpty());
        assertTrue(bundle.conflicts().isEmpty());
    }

    @Test
    void assembleDefaultsPermissionDecisionIdToPendingWhenAbsent() {
        UUID caseId = UUID.randomUUID();
        ContextAssemblyPort.Request request = new ContextAssemblyPort.Request(
                caseId, "claim-reconciliation", "identity-token-ref", Map.of(), Instant.now());

        EvidenceBundle bundle = assembler.assemble(request);

        assertEquals("PENDING", bundle.permissionDecisionId());
    }

    @Test
    void assembleDefaultsPermissionDecisionIdToPendingWhenContextNull() {
        UUID caseId = UUID.randomUUID();
        ContextAssemblyPort.Request request = new ContextAssemblyPort.Request(
                caseId, "claim-reconciliation", "identity-token-ref", null, Instant.now());

        EvidenceBundle bundle = assembler.assemble(request);

        assertEquals("PENDING", bundle.permissionDecisionId());
    }

    @Test
    void assembleUsesInstantNowWhenAsOfAbsent() {
        UUID caseId = UUID.randomUUID();
        Instant before = Instant.now();
        ContextAssemblyPort.Request request = new ContextAssemblyPort.Request(
                caseId, "claim-reconciliation", "identity-token-ref", Map.of(), null);

        EvidenceBundle bundle = assembler.assemble(request);

        Instant after = Instant.now();
        assertNotNull(bundle.assembledAt());
        assertFalse(bundle.assembledAt().isBefore(before));
        assertFalse(bundle.assembledAt().isAfter(after));
    }

    @Test
    void assembleRejectsNullCaseId() {
        ContextAssemblyPort.Request request = new ContextAssemblyPort.Request(
                null, "claim-reconciliation", "identity-token-ref", Map.of(), Instant.now());

        assertThrows(IllegalArgumentException.class, () -> assembler.assemble(request));
    }

    @Test
    void assembleRejectsBlankPurpose() {
        ContextAssemblyPort.Request request = new ContextAssemblyPort.Request(
                UUID.randomUUID(), "  ", "identity-token-ref", Map.of(), Instant.now());

        assertThrows(IllegalArgumentException.class, () -> assembler.assemble(request));
    }

    @Test
    void assembleRejectsNullRequest() {
        assertThrows(NullPointerException.class, () -> assembler.assemble(null));
    }

    @Test
    void evidenceBundleRejectsNonAuthoritativeFact() {
        Claim candidateClaim = new Claim(
                UUID.randomUUID(), UUID.randomUUID(), ClaimType.CUSTOMER_STATEMENT, ClaimStatus.CANDIDATE,
                "客户计划扩大结算合作", null, null, Instant.now(), null);

        assertThrows(IllegalArgumentException.class, () -> new EvidenceBundle(
                UUID.randomUUID(), UUID.randomUUID(), "claim-reconciliation", "PENDING", Instant.now(),
                List.of(candidateClaim), List.of(), List.of(), List.of(), List.of()));
    }

    @Test
    void evidenceBundleAcceptsAuthoritativeFact() {
        Claim verifiedFact = new Claim(
                UUID.randomUUID(), UUID.randomUUID(), ClaimType.SYSTEM_FACT, ClaimStatus.VERIFIED_FACT,
                "权威系统事实", Instant.now(), null, Instant.now(), null);

        EvidenceBundle bundle = new EvidenceBundle(
                UUID.randomUUID(), UUID.randomUUID(), "claim-reconciliation", "PENDING", Instant.now(),
                List.of(verifiedFact), List.of(), List.of(), List.of(), List.of());

        assertEquals(1, bundle.facts().size());
        assertTrue(bundle.facts().get(0).isAuthoritative());
    }
}
