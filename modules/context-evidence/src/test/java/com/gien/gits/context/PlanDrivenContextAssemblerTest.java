package com.gien.gits.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gien.gits.knowledge.ActivationPlan;
import com.gien.gits.ontology.ClaimStatus;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PlanDrivenContextAssemblerTest {

    private final DefaultContextAssembler assembler = new DefaultContextAssembler();

    private static ActivationPlan plan(String permissionDecisionId) {
        return new ActivationPlan(
                "1.0.0",
                "AP-PRE_VISIT_PREPARATION-CUST-001-SHADOW",
                "TASK-PRE_VISIT_PREPARATION-CUST-001-SHADOW",
                "PRE_VISIT_PREPARATION",
                "ONTOLOGY_THEN_MAP",
                new ActivationPlan.Versions("KM-GITS-ROOT@0.1.0", "RP-CORP-RM-001@0.1.0", "AC-PREVISIT-001@0.1.0", "ONT"),
                List.of(
                        new ActivationPlan.SelectedAsset("ASSET-DATA-CUSTOMER-PROFILE", "0.1.0", true, 1),
                        new ActivationPlan.SelectedAsset("ASSET-KNOW-CUSTOMER-ONTOLOGY", "0.2.0", false, 2)),
                List.of("SQ-CUSTOMER-RELATIONSHIP", "SQ-KYC-GAPS"),
                List.of("CLAIM_NOT_FACT", "CALLER_SCOPE_ALLOWED"),
                List.of("SP-02"),
                new ActivationPlan.Context(12000, "CONTRACT_PRIORITY"),
                permissionDecisionId,
                new ActivationPlan.Trace("hash", Instant.parse("2026-08-18T09:00:00Z").toString(), "p20-shadow-0.1.0"));
    }

    @Test
    void planDrivenAssemblyProducesNonEmptyBundle() {
        ContextAssemblyPort.PlanRequest request = new ContextAssemblyPort.PlanRequest(
                UUID.randomUUID(), plan("PD-ALLOW"), Map.of("customerId", "CUST-001"));

        EvidenceBundle bundle = assembler.assemble(request);

        // G4 退出条件：EvidenceBundle 不再为空，权限与来源完整
        assertEquals("PD-ALLOW", bundle.permissionDecisionId());
        assertFalse(bundle.evidence().isEmpty(), "evidence must be non-empty");
        assertEquals(2, bundle.evidence().size());
        assertFalse(bundle.candidateClaims().isEmpty(), "candidate claims must be non-empty");
        assertEquals(2, bundle.candidateClaims().size());
        assertTrue(bundle.candidateClaims().stream().allMatch(c -> c.status() == ClaimStatus.CANDIDATE));
        assertEquals("PRE_VISIT_PREPARATION", bundle.purpose());
    }

    @Test
    void planDrivenAssemblyRejectsPendingPermissionFailClosed() {
        ContextAssemblyPort.PlanRequest request = new ContextAssemblyPort.PlanRequest(
                UUID.randomUUID(), plan("PENDING"), Map.of("customerId", "CUST-001"));

        assertThrows(IllegalArgumentException.class, () -> assembler.assemble(request));
    }

    @Test
    void planDrivenAssemblyRejectsNullPlan() {
        ContextAssemblyPort.PlanRequest request = new ContextAssemblyPort.PlanRequest(
                UUID.randomUUID(), null, Map.of("customerId", "CUST-001"));

        assertThrows(IllegalArgumentException.class, () -> assembler.assemble(request));
    }

    @Test
    void planDrivenAssemblyRejectsNullCaseId() {
        ContextAssemblyPort.PlanRequest request = new ContextAssemblyPort.PlanRequest(
                null, plan("PD-ALLOW"), Map.of("customerId", "CUST-001"));

        assertThrows(IllegalArgumentException.class, () -> assembler.assemble(request));
    }
}
