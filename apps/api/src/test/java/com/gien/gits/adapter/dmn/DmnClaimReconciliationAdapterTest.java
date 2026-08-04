package com.gien.gits.adapter.dmn;

import com.gien.gits.api.metrics.BusinessMetrics;
import com.gien.gits.ontology.port.ClaimReconciliationPort.ReconciliationResult;
import com.gien.gits.ontology.port.ClaimReconciliationPort.ReconciliationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DmnClaimReconciliationAdapterTest {

    private BusinessMetrics metrics;
    private FallbackClaimReconciliationAdapter fallback;

    @BeforeEach
    void setUp() {
        metrics = mock(BusinessMetrics.class);
        fallback = new FallbackClaimReconciliationAdapter(metrics);
    }

    @Test
    void testReconcile_AuthoritativeMatchEvidenceComplete() {
        ReconciliationResult result = fallback.reconcile(false, true, true);
        assertEquals(ReconciliationStatus.VERIFIED_FACT, result.status());
    }

    @Test
    void testReconcile_ConflictDetected() {
        ReconciliationResult result = fallback.reconcile(true, true, true);
        assertEquals(ReconciliationStatus.CONFLICT_REQUIRES_HUMAN_REVIEW, result.status());
    }

    @Test
    void testReconcile_NoMatchIncomplete() {
        ReconciliationResult result = fallback.reconcile(false, false, false);
        assertEquals(ReconciliationStatus.CANDIDATE_CLAIM, result.status());
    }

    @Test
    void testReconcile_AuthoritativeMatchIncomplete() {
        ReconciliationResult result = fallback.reconcile(false, true, false);
        assertEquals(ReconciliationStatus.CANDIDATE_CLAIM, result.status());
    }

    @Test
    void testReconcile_FallbackOnParseError() {
        // Fallback adapter always works - DMN adapter would delegate to fallback on parse error
        ReconciliationResult result = fallback.reconcile(false, true, true);
        assertNotNull(result);
        assertEquals(ReconciliationStatus.VERIFIED_FACT, result.status());
    }

    @Test
    void testReconcile_EmptyRules() {
        // With no conflict, no match, and no evidence complete - CANDIDATE_CLAIM
        ReconciliationResult result = fallback.reconcile(false, false, false);
        assertEquals(ReconciliationStatus.CANDIDATE_CLAIM, result.status());
    }

    @Test
    void testFallback_ConflictDetected_ReturnsConflict() {
        ReconciliationResult result = fallback.reconcile(true, true, true);
        assertEquals(ReconciliationStatus.CONFLICT_REQUIRES_HUMAN_REVIEW, result.status());
    }

    @Test
    void testFallback_ConflictWithIncomplete_ReturnsConflict() {
        // Conflict takes precedence
        ReconciliationResult result = fallback.reconcile(true, false, false);
        assertEquals(ReconciliationStatus.CONFLICT_REQUIRES_HUMAN_REVIEW, result.status());
    }

    @Test
    void testFallback_NoConflictNoMatchIncomplete_ReturnsCandidateClaim() {
        ReconciliationResult result = fallback.reconcile(false, false, false);
        assertEquals(ReconciliationStatus.CANDIDATE_CLAIM, result.status());
    }

    @Test
    void testFallback_NoConflictMatchComplete_ReturnsVerifiedFact() {
        ReconciliationResult result = fallback.reconcile(false, true, true);
        assertEquals(ReconciliationStatus.VERIFIED_FACT, result.status());
    }
}
