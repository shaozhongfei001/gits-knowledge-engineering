package com.gien.gits.customerjourney;

import com.gien.gits.ontology.CaseStatus;
import com.gien.gits.ontology.Claim;
import com.gien.gits.ontology.ClaimStatus;
import com.gien.gits.ontology.OperatingCase;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CustomerJourneyTest {

    // ── CustomerJourney ──────────────────────────────────────────

    @Test
    void customerJourneyCreation() {
        Instant now = Instant.now();
        CustomerJourney journey = new CustomerJourney(
                UUID.randomUUID(), UUID.randomUUID(), "C-001", "Acme Corp",
                JourneyPhase.KYC_COLLECT, now, now);
        assertEquals(JourneyPhase.KYC_COLLECT, journey.phase());
        assertEquals("C-001", journey.customerId());
    }

    @Test
    void customerJourneyRejectsNullRequiredFields() {
        assertThrows(NullPointerException.class, () ->
                new CustomerJourney(null, UUID.randomUUID(), "C-001", "Name",
                        JourneyPhase.KYC_COLLECT, Instant.now(), null));
        assertThrows(NullPointerException.class, () ->
                new CustomerJourney(UUID.randomUUID(), null, "C-001", "Name",
                        JourneyPhase.KYC_COLLECT, Instant.now(), null));
        assertThrows(IllegalArgumentException.class, () ->
                new CustomerJourney(UUID.randomUUID(), UUID.randomUUID(), "", "Name",
                        JourneyPhase.KYC_COLLECT, Instant.now(), null));
        assertThrows(NullPointerException.class, () ->
                new CustomerJourney(UUID.randomUUID(), UUID.randomUUID(), "C-001", "Name",
                        null, Instant.now(), null));
        assertThrows(NullPointerException.class, () ->
                new CustomerJourney(UUID.randomUUID(), UUID.randomUUID(), "C-001", "Name",
                        JourneyPhase.KYC_COLLECT, null, null));
    }

    @Test
    void customerJourneyPhaseProgression() {
        Instant now = Instant.now();
        CustomerJourney j = new CustomerJourney(UUID.randomUUID(), UUID.randomUUID(), "C-001", "Acme",
                JourneyPhase.KYC_COLLECT, now, now);
        CustomerJourney j2 = new CustomerJourney(j.journeyId(), j.operatingCaseId(), j.customerId(),
                j.customerName(), JourneyPhase.INSIGHT_ANALYSIS, j.startedAt(), Instant.now());
        assertEquals(JourneyPhase.INSIGHT_ANALYSIS, j2.phase());
    }

    // ── InsightClaim ────────────────────────────────────────────

    @Test
    void insightClaimFromValidClaim() {
        UUID caseId = UUID.randomUUID();
        Claim claim = new Claim(UUID.randomUUID(), caseId, "CUSTOMER_JOURNEY", ClaimStatus.CANDIDATE,
                "Customer shows expansion intent", Instant.now(), null, Instant.now(), null);
        InsightClaim insight = InsightClaim.fromClaim(claim, "OPPORTUNITY", "Expansion signal detected");
        assertEquals(claim.claimId(), insight.claimId());
        assertEquals(caseId, insight.operatingCaseId());
        assertEquals("OPPORTUNITY", insight.insightCategory());
    }

    @Test
    void insightClaimRejectsWrongCaseType() {
        Claim claim = new Claim(UUID.randomUUID(), UUID.randomUUID(), "SOMETHING_ELSE", ClaimStatus.CANDIDATE,
                "Some statement", Instant.now(), null, Instant.now(), null);
        assertThrows(IllegalArgumentException.class, () ->
                InsightClaim.fromClaim(claim, "RISK", "Bad claim type"));
    }

    @Test
    void insightClaimRejectsNullRequiredFields() {
        assertThrows(NullPointerException.class, () ->
                new InsightClaim(null, UUID.randomUUID(), UUID.randomUUID(), "RISK", "summary", Instant.now()));
        assertThrows(IllegalArgumentException.class, () ->
                new InsightClaim(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "", "summary", Instant.now()));
        assertThrows(IllegalArgumentException.class, () ->
                new InsightClaim(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "RISK", "", Instant.now()));
    }

    // ── ProductCandidateClaim ───────────────────────────────────

    @Test
    void productCandidateFromInsight() {
        UUID caseId = UUID.randomUUID();
        InsightClaim insight = new InsightClaim(UUID.randomUUID(), UUID.randomUUID(), caseId,
                "OPPORTUNITY", "Expansion signal", Instant.now());
        ProductCandidateClaim product = ProductCandidateClaim.fromInsight(insight, "FX-HEDGE-01",
                "FX Hedging Suite", "Matches expansion into international settlement");
        assertEquals(insight.claimId(), product.claimId());
        assertEquals(insight.insightId(), product.insightClaimId());
        assertEquals(caseId, product.operatingCaseId());
        assertEquals("FX-HEDGE-01", product.productCode());
    }

    @Test
    void productCandidateRejectsBlankProductCode() {
        assertThrows(IllegalArgumentException.class, () ->
                new ProductCandidateClaim(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                        UUID.randomUUID(), "  ", "Name", "reason", Instant.now()));
    }

    @Test
    void productCandidateRejectsBlankMatchReason() {
        assertThrows(IllegalArgumentException.class, () ->
                new ProductCandidateClaim(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                        UUID.randomUUID(), "CODE", "Name", "", Instant.now()));
    }

    // ── PrevisitReport ──────────────────────────────────────────

    @Test
    void previsitReportComposition() {
        UUID insight1 = UUID.randomUUID();
        UUID insight2 = UUID.randomUUID();
        UUID product1 = UUID.randomUUID();
        PrevisitReport report = new PrevisitReport(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                List.of(insight1, insight2), List.of(product1), "Previsit summary", Instant.now());
        assertEquals(2, report.insightIds().size());
        assertEquals(1, report.productCandidateIds().size());
        assertEquals("Previsit summary", report.summary());
    }

    @Test
    void previsitReportListsAreImmutable() {
        List<UUID> mutable = new java.util.ArrayList<>();
        mutable.add(UUID.randomUUID());
        PrevisitReport report = new PrevisitReport(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                mutable, List.of(), "Summary", Instant.now());
        assertThrows(UnsupportedOperationException.class, () -> report.insightIds().add(UUID.randomUUID()));
    }

    @Test
    void previsitReportRejectsBlankSummary() {
        assertThrows(IllegalArgumentException.class, () ->
                new PrevisitReport(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                        List.of(), List.of(), "  ", Instant.now()));
    }

    // ── PostvisitAnalysis ───────────────────────────────────────

    @Test
    void postvisitAnalysisCreation() {
        PostvisitAnalysis analysis = new PostvisitAnalysis(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), "Visit completed successfully", "Schedule follow-up", Instant.now());
        assertEquals("Visit completed successfully", analysis.outcome());
        assertEquals("Schedule follow-up", analysis.followUpAction());
    }

    @Test
    void postvisitAnalysisRejectsBlankOutcome() {
        assertThrows(IllegalArgumentException.class, () ->
                new PostvisitAnalysis(UUID.randomUUID(), UUID.randomUUID(),
                        UUID.randomUUID(), UUID.randomUUID(), "", "action", Instant.now()));
    }

    @Test
    void postvisitAnalysisAllowsNullOptionalFields() {
        PostvisitAnalysis analysis = new PostvisitAnalysis(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), null, "Outcome", null, Instant.now());
        assertNull(analysis.previsitReportId());
        assertNull(analysis.followUpAction());
    }

    // ── OperatingCaseStateMachine ───────────────────────────────

    private OperatingCase makeCase(CaseStatus status) {
        return new OperatingCase(UUID.randomUUID(), "CUSTOMER_JOURNEY", status, "Test purpose",
                Instant.now(), null, Instant.now(), "tester");
    }

    @Test
    void openToInProgress() {
        OperatingCase result = OperatingCaseStateMachine.transition(makeCase(CaseStatus.OPEN), CaseStatus.IN_PROGRESS);
        assertEquals(CaseStatus.IN_PROGRESS, result.status());
    }

    @Test
    void inProgressSelfTransition() {
        OperatingCase result = OperatingCaseStateMachine.transition(makeCase(CaseStatus.IN_PROGRESS), CaseStatus.IN_PROGRESS);
        assertEquals(CaseStatus.IN_PROGRESS, result.status());
    }

    @Test
    void inProgressToWaitingForHuman() {
        OperatingCase result = OperatingCaseStateMachine.transition(makeCase(CaseStatus.IN_PROGRESS), CaseStatus.WAITING_FOR_HUMAN);
        assertEquals(CaseStatus.WAITING_FOR_HUMAN, result.status());
    }

    @Test
    void waitingForHumanToInProgress() {
        OperatingCase result = OperatingCaseStateMachine.transition(makeCase(CaseStatus.WAITING_FOR_HUMAN), CaseStatus.IN_PROGRESS);
        assertEquals(CaseStatus.IN_PROGRESS, result.status());
    }

    @Test
    void inProgressToClosed() {
        OperatingCase result = OperatingCaseStateMachine.transition(makeCase(CaseStatus.IN_PROGRESS), CaseStatus.CLOSED);
        assertEquals(CaseStatus.CLOSED, result.status());
    }

    @Test
    void cancelledFromAnyState() {
        for (CaseStatus from : CaseStatus.values()) {
            if (from == CaseStatus.CLOSED || from == CaseStatus.CANCELLED) continue;
            assertTrue(OperatingCaseStateMachine.validateTransition(from, CaseStatus.CANCELLED),
                    "Should allow CANCELLED from " + from);
            OperatingCase result = OperatingCaseStateMachine.transition(makeCase(from), CaseStatus.CANCELLED);
            assertEquals(CaseStatus.CANCELLED, result.status());
        }
    }

    @Test
    void invalidTransitionsThrow() {
        // OPEN → CLOSED is invalid
        assertThrows(IllegalStateException.class, () ->
                OperatingCaseStateMachine.transition(makeCase(CaseStatus.OPEN), CaseStatus.CLOSED));
        // OPEN → WAITING_FOR_HUMAN is invalid
        assertThrows(IllegalStateException.class, () ->
                OperatingCaseStateMachine.transition(makeCase(CaseStatus.OPEN), CaseStatus.WAITING_FOR_HUMAN));
        // WAITING_FOR_HUMAN → CLOSED is invalid
        assertThrows(IllegalStateException.class, () ->
                OperatingCaseStateMachine.transition(makeCase(CaseStatus.WAITING_FOR_HUMAN), CaseStatus.CLOSED));
        // CLOSED → anything is invalid
        for (CaseStatus to : CaseStatus.values()) {
            if (to == CaseStatus.CLOSED) continue;
            assertFalse(OperatingCaseStateMachine.validateTransition(CaseStatus.CLOSED, to));
        }
        // CANCELLED → anything is invalid
        for (CaseStatus to : CaseStatus.values()) {
            if (to == CaseStatus.CANCELLED) continue;
            assertFalse(OperatingCaseStateMachine.validateTransition(CaseStatus.CANCELLED, to));
        }
    }

    @Test
    void validateTransitionReturnsExpected() {
        assertTrue(OperatingCaseStateMachine.validateTransition(CaseStatus.OPEN, CaseStatus.IN_PROGRESS));
        assertTrue(OperatingCaseStateMachine.validateTransition(CaseStatus.IN_PROGRESS, CaseStatus.WAITING_FOR_HUMAN));
        assertFalse(OperatingCaseStateMachine.validateTransition(CaseStatus.OPEN, CaseStatus.CLOSED));
        assertFalse(OperatingCaseStateMachine.validateTransition(CaseStatus.CLOSED, CaseStatus.IN_PROGRESS));
    }

    @Test
    void transitionPreservesCaseIdentity() {
        OperatingCase original = makeCase(CaseStatus.OPEN);
        OperatingCase transitioned = OperatingCaseStateMachine.transition(original, CaseStatus.IN_PROGRESS);
        assertEquals(original.caseId(), transitioned.caseId());
        assertEquals(original.caseType(), transitioned.caseType());
        assertEquals(original.purpose(), transitioned.purpose());
        assertEquals(original.validFrom(), transitioned.validFrom());
        assertEquals(original.createdBy(), transitioned.createdBy());
    }
}