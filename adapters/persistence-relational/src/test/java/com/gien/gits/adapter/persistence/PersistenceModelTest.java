package com.gien.gits.adapter.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.gien.gits.ontology.CaseStatus;
import com.gien.gits.ontology.CaseType;
import com.gien.gits.ontology.OperatingCase;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PersistenceModelTest {

    @Test
    void operatingCaseConstruction() {
        UUID caseId = UUID.randomUUID();
        Instant validFrom = Instant.now();
        Instant recordedAt = Instant.now();
        OperatingCase oc = new OperatingCase(caseId, CaseType.CLAIM_RECONCILIATION,
                CaseStatus.OPEN, "Test case purpose", validFrom, null, recordedAt, "tester");

        assertEquals(caseId, oc.caseId());
        assertEquals(CaseType.CLAIM_RECONCILIATION, oc.caseType());
        assertEquals(CaseStatus.OPEN, oc.status());
        assertEquals("Test case purpose", oc.purpose());
        assertNotNull(oc.validFrom());
        assertNotNull(oc.recordedAt());
    }

    @Test
    void operatingCaseRejectsNullCaseId() {
        assertThrows(NullPointerException.class, () -> new OperatingCase(
                null, CaseType.CLAIM_RECONCILIATION, CaseStatus.OPEN,
                "purpose", Instant.now(), null, Instant.now(), "tester"));
    }

    @Test
    void operatingCaseRejectsNullCaseType() {
        assertThrows(NullPointerException.class, () -> new OperatingCase(
                UUID.randomUUID(), null, CaseStatus.OPEN,
                "purpose", Instant.now(), null, Instant.now(), "tester"));
    }

    @Test
    void operatingCaseRejectsNullStatus() {
        assertThrows(NullPointerException.class, () -> new OperatingCase(
                UUID.randomUUID(), CaseType.CLAIM_RECONCILIATION, null,
                "purpose", Instant.now(), null, Instant.now(), "tester"));
    }

    @Test
    void operatingCaseRejectsBlankPurpose() {
        assertThrows(IllegalArgumentException.class, () -> new OperatingCase(
                UUID.randomUUID(), CaseType.CLAIM_RECONCILIATION, CaseStatus.OPEN,
                "  ", Instant.now(), null, Instant.now(), "tester"));
    }

    @Test
    void operatingCaseWithValidTo() {
        Instant validFrom = Instant.now();
        Instant validTo = validFrom.plusSeconds(3600);
        OperatingCase oc = new OperatingCase(UUID.randomUUID(), CaseType.CLAIM_RECONCILIATION,
                CaseStatus.CLOSED, "Closed case", validFrom, validTo, Instant.now(), "tester");

        assertEquals(validTo, oc.validTo());
    }

    @Test
    void operatingCaseRejectsValidToBeforeValidFrom() {
        Instant validFrom = Instant.now();
        Instant validTo = validFrom.minusSeconds(3600);
        assertThrows(IllegalArgumentException.class, () -> new OperatingCase(
                UUID.randomUUID(), CaseType.CLAIM_RECONCILIATION, CaseStatus.CLOSED,
                "purpose", validFrom, validTo, Instant.now(), "tester"));
    }
}
