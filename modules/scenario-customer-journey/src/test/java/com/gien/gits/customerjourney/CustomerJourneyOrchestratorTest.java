package com.gien.gits.customerjourney;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.gien.gits.ontology.CaseStatus;
import com.gien.gits.ontology.CaseType;
import com.gien.gits.ontology.Claim;
import com.gien.gits.ontology.ClaimStatus;
import com.gien.gits.ontology.ClaimType;
import com.gien.gits.ontology.OperatingCase;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CustomerJourneyOrchestratorTest {

    private OperatingCase openCase() {
        return new OperatingCase(UUID.randomUUID(), CaseType.CLAIM_RECONCILIATION,
                CaseStatus.OPEN, "Test case", Instant.now(), null, Instant.now(), "tester");
    }

    private Claim candidateClaim() {
        return new Claim(UUID.randomUUID(), UUID.randomUUID(), ClaimType.CUSTOMER_JOURNEY,
                ClaimStatus.CANDIDATE, "Test claim", Instant.now(), null, Instant.now(), null);
    }

    @Test
    void openJourneyCreatesJourneyAndInteraction() {
        OperatingCase oc = openCase();
        CustomerJourneyOrchestrator.JourneyStartResult result =
                CustomerJourneyOrchestrator.openJourney(oc, "CUST-001", "张三", "Signal description");

        assertNotNull(result.journey());
        assertEquals(oc.caseId(), result.journey().operatingCaseId());
        assertEquals("CUST-001", result.journey().customerId());
        assertEquals(JourneyPhase.KYC_COLLECT, result.journey().phase());
        assertNotNull(result.signalInteraction());
    }

    @Test
    void openJourneyRejectsNonOpenCase() {
        OperatingCase inProgress = new OperatingCase(UUID.randomUUID(), CaseType.CLAIM_RECONCILIATION,
                CaseStatus.IN_PROGRESS, "Test", Instant.now(), null, Instant.now(), "tester");

        assertThrows(IllegalStateException.class,
                () -> CustomerJourneyOrchestrator.openJourney(inProgress, "C-1", "Name", "Signal"));
    }

    @Test
    void openJourneyRejectsBlankCustomerId() {
        assertThrows(IllegalArgumentException.class,
                () -> CustomerJourneyOrchestrator.openJourney(openCase(), "  ", "Name", "Signal"));
    }

    @Test
    void openJourneyRejectsNullCase() {
        assertThrows(NullPointerException.class,
                () -> CustomerJourneyOrchestrator.openJourney(null, "C-1", "Name", "Signal"));
    }

    @Test
    void analyzeInsightCreatesInsightAndInteraction() {
        Claim claim = candidateClaim();
        CustomerJourneyOrchestrator.InsightResult result =
                CustomerJourneyOrchestrator.analyzeInsight(claim, "FINANCING", "Insight summary",
                        UUID.randomUUID(), "RM-001", "客户经理A");

        assertNotNull(result.insight());
        assertEquals(claim.claimId(), result.insight().claimId());
        assertNotNull(result.pushInteraction());
    }

    @Test
    void analyzeInsightRejectsNonCandidateClaim() {
        Claim verified = new Claim(UUID.randomUUID(), UUID.randomUUID(), ClaimType.CUSTOMER_JOURNEY,
                ClaimStatus.VERIFIED_FACT, "Verified claim", Instant.now(), null, Instant.now(), null);

        assertThrows(IllegalArgumentException.class,
                () -> CustomerJourneyOrchestrator.analyzeInsight(verified, "CAT", "Summary",
                        UUID.randomUUID(), "RM-1", "Name"));
    }

    @Test
    void matchProductCreatesProductAndInteraction() {
        Claim claim = candidateClaim();
        CustomerJourneyOrchestrator.InsightResult insight =
                CustomerJourneyOrchestrator.analyzeInsight(claim, "FINANCING", "Summary",
                        UUID.randomUUID(), "RM-001", "客户经理A");

        CustomerJourneyOrchestrator.ProductMatchResult result =
                CustomerJourneyOrchestrator.matchProduct(insight.insight(), "FX-HEDGE", "FX对冲套件",
                        "Risk management need", "RM-001", "客户经理A");

        assertNotNull(result.product());
        assertEquals("FX-HEDGE", result.product().productCode());
        assertNotNull(result.matchInteraction());
    }

    @Test
    void matchProductRejectsBlankProductCode() {
        Claim claim = candidateClaim();
        CustomerJourneyOrchestrator.InsightResult insight =
                CustomerJourneyOrchestrator.analyzeInsight(claim, "CAT", "Summary",
                        UUID.randomUUID(), "RM-1", "Name");

        assertThrows(IllegalArgumentException.class,
                () -> CustomerJourneyOrchestrator.matchProduct(insight.insight(), "  ", "Name",
                        "Reason", "RM-1", "Name"));
    }

    @Test
    void executePrevisitCreatesReportAndInteraction() {
        OperatingCase oc = openCase();
        CustomerJourneyOrchestrator.PrevisitResult result =
                CustomerJourneyOrchestrator.executePrevisit(oc.caseId(), UUID.randomUUID(),
                        List.of(), "RM-001", "客户经理A", "CUST-001", "张三", "Previsit summary");

        assertNotNull(result.report());
        assertNotNull(result.visitInteraction());
        assertEquals("Previsit summary", result.report().summary());
    }

    @Test
    void closeWithPostvisitCreatesAnalysisAndInteraction() {
        CustomerJourneyOrchestrator.PostvisitResult result =
                CustomerJourneyOrchestrator.closeWithPostvisit(UUID.randomUUID(), UUID.randomUUID(),
                        UUID.randomUUID(), "Customer agreed", "Follow up next week",
                        "RM-001", "客户经理A", "CUST-001", "张三", true);

        assertNotNull(result.analysis());
        assertNotNull(result.followUpInteraction());
        assertEquals("Customer agreed", result.analysis().outcome());
    }

    @Test
    void closeWithPostvisitCustomerDeclined() {
        CustomerJourneyOrchestrator.PostvisitResult result =
                CustomerJourneyOrchestrator.closeWithPostvisit(UUID.randomUUID(), UUID.randomUUID(),
                        UUID.randomUUID(), "Customer declined", "Try again later",
                        "RM-001", "客户经理A", "CUST-001", "张三", false);

        assertNotNull(result.analysis());
    }
}
