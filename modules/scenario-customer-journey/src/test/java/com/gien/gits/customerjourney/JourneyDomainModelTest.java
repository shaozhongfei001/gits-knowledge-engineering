package com.gien.gits.customerjourney;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JourneyDomainModelTest {

    @Test
    void customerJourneyConstruction() {
        UUID journeyId = UUID.randomUUID();
        UUID caseId = UUID.randomUUID();
        Instant now = Instant.now();

        CustomerJourney journey = new CustomerJourney(journeyId, caseId, "CUST-001", "张三",
                JourneyPhase.KYC_COLLECT, now, now);

        assertEquals(journeyId, journey.journeyId());
        assertEquals(caseId, journey.operatingCaseId());
        assertEquals("CUST-001", journey.customerId());
        assertEquals(JourneyPhase.KYC_COLLECT, journey.phase());
    }

    @Test
    void customerJourneyRejectsNullJourneyId() {
        assertThrows(NullPointerException.class, () -> new CustomerJourney(
                null, UUID.randomUUID(), "C-1", "Name", JourneyPhase.KYC_COLLECT,
                Instant.now(), Instant.now()));
    }

    @Test
    void customerJourneyRejectsBlankCustomerId() {
        assertThrows(IllegalArgumentException.class, () -> new CustomerJourney(
                UUID.randomUUID(), UUID.randomUUID(), "  ", "Name", JourneyPhase.KYC_COLLECT,
                Instant.now(), Instant.now()));
    }

    @Test
    void journeyPhaseValues() {
        assertEquals(6, JourneyPhase.values().length);
        assertEquals(JourneyPhase.KYC_COLLECT, JourneyPhase.valueOf("KYC_COLLECT"));
        assertEquals(JourneyPhase.INSIGHT_ANALYSIS, JourneyPhase.valueOf("INSIGHT_ANALYSIS"));
        assertEquals(JourneyPhase.PRODUCT_MATCHING, JourneyPhase.valueOf("PRODUCT_MATCHING"));
        assertEquals(JourneyPhase.PREVISIT_PREP, JourneyPhase.valueOf("PREVISIT_PREP"));
        assertEquals(JourneyPhase.POSTVISIT_REVIEW, JourneyPhase.valueOf("POSTVISIT_REVIEW"));
        assertEquals(JourneyPhase.COMPLETED, JourneyPhase.valueOf("COMPLETED"));
    }

    @Test
    void previsitReportRejectsBlankSummary() {
        assertThrows(IllegalArgumentException.class, () -> new PrevisitReport(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                null, null, "  ", Instant.now()));
    }

    @Test
    void postvisitAnalysisRejectsBlankOutcome() {
        assertThrows(IllegalArgumentException.class, () -> new PostvisitAnalysis(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "  ", "action", Instant.now()));
    }

    @Test
    void insightClaimRejectsWrongClaimType() {
        com.gien.gits.ontology.Claim wrongType = new com.gien.gits.ontology.Claim(
                UUID.randomUUID(), UUID.randomUUID(), com.gien.gits.ontology.ClaimType.SYSTEM_FACT,
                com.gien.gits.ontology.ClaimStatus.CANDIDATE, "Statement",
                Instant.now(), null, Instant.now(), null);

        assertThrows(IllegalArgumentException.class,
                () -> InsightClaim.fromClaim(wrongType, "CAT", "Summary"));
    }
}
