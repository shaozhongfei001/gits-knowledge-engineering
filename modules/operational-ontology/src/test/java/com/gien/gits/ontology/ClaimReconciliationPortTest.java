package com.gien.gits.ontology;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gien.gits.ontology.port.ClaimReconciliationPort;
import org.junit.jupiter.api.Test;

class ClaimReconciliationPortTest {

    @Test
    void reconciliationResultRecordHoldsValues() {
        ClaimReconciliationPort.ReconciliationResult result =
                new ClaimReconciliationPort.ReconciliationResult(
                        ClaimReconciliationPort.ReconciliationStatus.VERIFIED_FACT,
                        "All claims verified");

        assertEquals(ClaimReconciliationPort.ReconciliationStatus.VERIFIED_FACT, result.status());
        assertEquals("All claims verified", result.reasoning());
    }

    @Test
    void reconciliationStatusValues() {
        assertEquals(3, ClaimReconciliationPort.ReconciliationStatus.values().length);
        assertEquals(ClaimReconciliationPort.ReconciliationStatus.CONFLICT_REQUIRES_HUMAN_REVIEW,
                ClaimReconciliationPort.ReconciliationStatus.valueOf("CONFLICT_REQUIRES_HUMAN_REVIEW"));
        assertEquals(ClaimReconciliationPort.ReconciliationStatus.VERIFIED_FACT,
                ClaimReconciliationPort.ReconciliationStatus.valueOf("VERIFIED_FACT"));
        assertEquals(ClaimReconciliationPort.ReconciliationStatus.CANDIDATE_CLAIM,
                ClaimReconciliationPort.ReconciliationStatus.valueOf("CANDIDATE_CLAIM"));
    }

    @Test
    void claimStatusValues() {
        assertEquals(5, ClaimStatus.values().length);
        assertEquals(ClaimStatus.CANDIDATE, ClaimStatus.valueOf("CANDIDATE"));
        assertEquals(ClaimStatus.VERIFIED_FACT, ClaimStatus.valueOf("VERIFIED_FACT"));
    }

    @Test
    void caseStatusValues() {
        assertEquals(5, CaseStatus.values().length);
    }

    @Test
    void claimTypeEnumValues() {
        assertEquals(8, ClaimType.values().length);
    }

    @Test
    void caseTypeEnumValues() {
        assertEquals(2, CaseType.values().length);
    }

    @Test
    void industryEnumValues() {
        assertEquals(10, Industry.values().length);
    }

    @Test
    void channelEnumValues() {
        assertEquals(12, Channel.values().length);
    }

    @Test
    void reconciliationStatusEnumValues() {
        assertEquals(3, ReconciliationStatus.values().length);
    }
}
