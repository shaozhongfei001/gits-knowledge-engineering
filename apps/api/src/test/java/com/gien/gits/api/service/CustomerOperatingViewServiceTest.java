package com.gien.gits.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.gien.gits.customerjourney.CustomerJourney;
import com.gien.gits.customerjourney.JourneyPhase;
import com.gien.gits.customerjourney.port.CustomerJourneyRepository;
import com.gien.gits.engagement.CustomerOperatingView;
import com.gien.gits.ontology.*;
import com.gien.gits.ontology.port.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class CustomerOperatingViewServiceTest {

    @Mock private CustomerRepository customerRepo;
    @Mock private InteractionRepository interactionRepo;
    @Mock private ClaimRepository claimRepo;
    @Mock private KycGapProfileRepository kycGapRepo;
    @Mock private OpportunitySignalRepository signalRepo;
    @Mock private FactReconciliationRepository factRecRepo;
    @Mock private CustomerJourneyRepository journeyRepo;

    private AutoCloseable mocks;
    private CustomerOperatingViewService service;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        service = new CustomerOperatingViewService(
            customerRepo, interactionRepo, claimRepo,
            kycGapRepo, signalRepo, factRecRepo, journeyRepo);
    }

    private Customer createTestCustomer(RiskLevel riskLevel) {
        return new Customer(
            "CUST-001", "华东精工", "华东精工制造有限公司",
            "91330000MA27DEMO", LocalDate.of(2005, 3, 15), 50000000L,
            Industry.MANUFACTURING.name(), "浙江省",
            EnterpriseScale.LARGE.name(), CustomerTier.STRATEGIC.name(),
            LocalDate.of(2018, 1, 1), "RM-001", "张经理", "杭州分行",
            false, ListedStatus.UNLISTED.name(), riskLevel.name(),
            List.of("精密制造"), List.of("战略客户"), "长期合作");
    }

    @Test
    void buildView_customerExists_returnsView() {
        UUID caseUuid = UUID.randomUUID();
        when(customerRepo.findById("CUST-001")).thenReturn(Optional.of(createTestCustomer(RiskLevel.MEDIUM)));
        when(kycGapRepo.findLatestByCustomerId("CUST-001")).thenReturn(Optional.empty());
        when(signalRepo.findByOperatingCaseId(caseUuid.toString())).thenReturn(List.of());
        when(interactionRepo.findByCaseId(caseUuid)).thenReturn(List.of());
        when(journeyRepo.findJourneysByCaseId(caseUuid)).thenReturn(List.of());
        when(claimRepo.findByCaseId(caseUuid)).thenReturn(List.of());
        when(factRecRepo.findByCaseId(caseUuid.toString())).thenReturn(List.of());

        Optional<CustomerOperatingView> result = service.buildView("CUST-001", caseUuid.toString());

        assertTrue(result.isPresent());
        CustomerOperatingView view = result.get();
        assertEquals("CUST-001", view.customerId());
        assertEquals("华东精工", view.customerName());
        assertEquals("MANUFACTURING", view.industry());
        assertEquals("MEDIUM", view.riskLevel());
    }

    @Test
    void buildView_customerNotFound_returnsEmpty() {
        when(customerRepo.findById("CUST-UNKNOWN")).thenReturn(Optional.empty());

        Optional<CustomerOperatingView> result = service.buildView("CUST-UNKNOWN", UUID.randomUUID().toString());

        assertTrue(result.isEmpty());
    }

    @Test
    void buildView_withKycGap_includesGapItems() {
        UUID caseUuid = UUID.randomUUID();
        KycGapProfile gap = new KycGapProfile(
            "KP-001", "CUST-001", LocalDate.now(),
            List.of("已知项"), List.of("营收规模"), List.of(), List.of(),
            List.of("股东结构"), List.of());
        when(customerRepo.findById("CUST-001")).thenReturn(Optional.of(createTestCustomer(RiskLevel.MEDIUM)));
        when(kycGapRepo.findLatestByCustomerId("CUST-001")).thenReturn(Optional.of(gap));
        when(signalRepo.findByOperatingCaseId(caseUuid.toString())).thenReturn(List.of());
        when(interactionRepo.findByCaseId(caseUuid)).thenReturn(List.of());
        when(journeyRepo.findJourneysByCaseId(caseUuid)).thenReturn(List.of());
        when(claimRepo.findByCaseId(caseUuid)).thenReturn(List.of());
        when(factRecRepo.findByCaseId(caseUuid.toString())).thenReturn(List.of());

        Optional<CustomerOperatingView> result = service.buildView("CUST-001", caseUuid.toString());

        assertTrue(result.isPresent());
        assertFalse(result.get().unknownKycItems().isEmpty());
        assertFalse(result.get().partialKycItems().isEmpty());
    }

    @Test
    void buildView_highRiskCustomer_includesRiskIndicator() {
        UUID caseUuid = UUID.randomUUID();
        when(customerRepo.findById("CUST-001")).thenReturn(Optional.of(createTestCustomer(RiskLevel.HIGH)));
        when(kycGapRepo.findLatestByCustomerId("CUST-001")).thenReturn(Optional.empty());
        when(signalRepo.findByOperatingCaseId(caseUuid.toString())).thenReturn(List.of());
        when(interactionRepo.findByCaseId(caseUuid)).thenReturn(List.of());
        when(journeyRepo.findJourneysByCaseId(caseUuid)).thenReturn(List.of());
        when(claimRepo.findByCaseId(caseUuid)).thenReturn(List.of());
        when(factRecRepo.findByCaseId(caseUuid.toString())).thenReturn(List.of());

        Optional<CustomerOperatingView> result = service.buildView("CUST-001", caseUuid.toString());

        assertTrue(result.isPresent());
        assertTrue(result.get().riskIndicators().stream().anyMatch(r -> r.contains("高风险")));
    }

    @Test
    void buildView_withActiveSignals_includesSignals() {
        UUID caseUuid = UUID.randomUUID();
        OpportunitySignal signal = new OpportunitySignal(
            caseUuid, caseUuid.toString(), "journey-001",
            OpportunitySignal.SignalType.FINANCING_NEED, "融资需求",
            OpportunitySignal.SignalSourceType.ANALYSIS, "src-001",
            java.math.BigDecimal.valueOf(0.8), OpportunitySignal.SignalStatus.DETECTED,
            "evidence-001", Instant.now(), null);
        when(customerRepo.findById("CUST-001")).thenReturn(Optional.of(createTestCustomer(RiskLevel.MEDIUM)));
        when(kycGapRepo.findLatestByCustomerId("CUST-001")).thenReturn(Optional.empty());
        when(signalRepo.findByOperatingCaseId(caseUuid.toString())).thenReturn(List.of(signal));
        when(interactionRepo.findByCaseId(caseUuid)).thenReturn(List.of());
        when(journeyRepo.findJourneysByCaseId(caseUuid)).thenReturn(List.of());
        when(claimRepo.findByCaseId(caseUuid)).thenReturn(List.of());
        when(factRecRepo.findByCaseId(caseUuid.toString())).thenReturn(List.of());

        Optional<CustomerOperatingView> result = service.buildView("CUST-001", caseUuid.toString());

        assertTrue(result.isPresent());
        assertFalse(result.get().activeSignals().isEmpty());
    }

    @Test
    void buildView_withPendingCommitments_includesCommitments() {
        UUID caseUuid = UUID.randomUUID();
        Claim commitment = new Claim(
            UUID.randomUUID(), caseUuid, ClaimType.COMMITMENT,
            ClaimStatus.CANDIDATE, "承诺下周提交材料",
            Instant.now(), null, Instant.now(), null);
        when(customerRepo.findById("CUST-001")).thenReturn(Optional.of(createTestCustomer(RiskLevel.MEDIUM)));
        when(kycGapRepo.findLatestByCustomerId("CUST-001")).thenReturn(Optional.empty());
        when(signalRepo.findByOperatingCaseId(caseUuid.toString())).thenReturn(List.of());
        when(interactionRepo.findByCaseId(caseUuid)).thenReturn(List.of());
        when(journeyRepo.findJourneysByCaseId(caseUuid)).thenReturn(List.of());
        when(claimRepo.findByCaseId(caseUuid)).thenReturn(List.of(commitment));
        when(factRecRepo.findByCaseId(caseUuid.toString())).thenReturn(List.of());

        Optional<CustomerOperatingView> result = service.buildView("CUST-001", caseUuid.toString());

        assertTrue(result.isPresent());
        assertFalse(result.get().pendingCommitments().isEmpty());
    }
}
