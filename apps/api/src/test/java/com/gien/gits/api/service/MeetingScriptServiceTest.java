package com.gien.gits.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.gien.gits.engagement.MeetingScript;
import com.gien.gits.engagement.port.LlmClient;
import com.gien.gits.engagement.port.WritableMeetingScriptRepository;
import com.gien.gits.ontology.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;

class MeetingScriptServiceTest {

    @Mock private CustomerContextService customerContextService;
    @Mock private KycInsightService kycInsightService;
    @Mock private CustomerJourneyService journeyService;
    @Mock private WritableMeetingScriptRepository scriptRepo;
    @Mock private LlmClient llmClient;

    private AutoCloseable mocks;
    private MeetingScriptService service;
    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        // LLM fallback: throw so service uses template logic
        doThrow(new com.gien.gits.engagement.port.LlmClientException("test fallback")).when(llmClient).complete(anyString(), anyString());
        service = new MeetingScriptService(customerContextService, kycInsightService, journeyService, scriptRepo, llmClient);
        testCustomer = new Customer(
            "CUST-001", "华东精工", "华东精工制造有限公司",
            "91330000MA27DEMO", LocalDate.of(2005, 3, 15), 50000000L,
            Industry.MANUFACTURING.name(), "浙江省",
            EnterpriseScale.LARGE.name(), CustomerTier.STRATEGIC.name(),
            LocalDate.of(2018, 1, 1), "RM-001", "张经理", "杭州分行",
            false, ListedStatus.UNLISTED.name(), RiskLevel.MEDIUM.name(),
            List.of("精密制造"), List.of("战略客户"), "长期合作");
    }

    private OpportunitySignal createFinancingSignal() {
        return new OpportunitySignal(
            UUID.randomUUID(), "case-001", "journey-001",
            OpportunitySignal.SignalType.FINANCING_NEED, "融资需求",
            OpportunitySignal.SignalSourceType.ANALYSIS, "src-001",
            BigDecimal.valueOf(0.8), OpportunitySignal.SignalStatus.DETECTED,
            "evidence-001", Instant.now(), null);
    }

    // ── 议程生成 ────────────────────────────────────────────────

    @Test
    void generateScript_alwaysHasAtLeast3AgendaItems() {
        when(customerContextService.findCustomer("CUST-001")).thenReturn(Optional.of(testCustomer));
        when(kycInsightService.getKycGapProfile("CUST-001")).thenReturn(Optional.empty());
        when(kycInsightService.getSignalsByCase(anyString())).thenReturn(List.of());
        when(kycInsightService.getAllProducts()).thenReturn(List.of());

        MeetingScript script = service.generateScript("CUST-001", "RM-001", "case-001", "journey-001");

        assertTrue(script.agendaItems().size() >= 3);
    }

    @Test
    void generateScript_withKycGap_includesKycAgendaItem() {
        KycGapProfile gap = new KycGapProfile(
            "KP-001", "CUST-001", LocalDate.now(),
            List.of("已知项"), List.of("营收规模"), List.of(), List.of(),
            List.of("股东结构"), List.of());
        when(customerContextService.findCustomer("CUST-001")).thenReturn(Optional.of(testCustomer));
        when(kycInsightService.getKycGapProfile("CUST-001")).thenReturn(Optional.of(gap));
        when(kycInsightService.getSignalsByCase(anyString())).thenReturn(List.of());
        when(kycInsightService.getAllProducts()).thenReturn(List.of());

        MeetingScript script = service.generateScript("CUST-001", "RM-001", "case-001", "journey-001");

        assertTrue(script.agendaItems().stream().anyMatch(item -> item.topic().contains("KYC")));
    }

    @Test
    void generateScript_withSignals_includesBusinessOpportunityItem() {
        when(customerContextService.findCustomer("CUST-001")).thenReturn(Optional.of(testCustomer));
        when(kycInsightService.getKycGapProfile("CUST-001")).thenReturn(Optional.empty());
        when(kycInsightService.getSignalsByCase(anyString()))
            .thenReturn(List.of(createFinancingSignal()));
        when(kycInsightService.getAllProducts()).thenReturn(List.of());

        MeetingScript script = service.generateScript("CUST-001", "RM-001", "case-001", "journey-001");

        assertTrue(script.agendaItems().stream().anyMatch(item -> item.topic().contains("业务机会")));
    }

    // ── KYC问题 ─────────────────────────────────────────────────

    @Test
    void generateScript_withKycGap_generatesKycQuestions() {
        KycGapProfile gap = new KycGapProfile(
            "KP-001", "CUST-001", LocalDate.now(),
            List.of("已知项"), List.of("营收规模"), List.of(), List.of(),
            List.of("股东结构"), List.of());
        when(customerContextService.findCustomer("CUST-001")).thenReturn(Optional.of(testCustomer));
        when(kycInsightService.getKycGapProfile("CUST-001")).thenReturn(Optional.of(gap));
        when(kycInsightService.getSignalsByCase(anyString())).thenReturn(List.of());
        when(kycInsightService.getAllProducts()).thenReturn(List.of());

        MeetingScript script = service.generateScript("CUST-001", "RM-001", "case-001", "journey-001");

        assertFalse(script.kycQuestions().isEmpty());
        assertTrue(script.kycQuestions().stream().anyMatch(q -> q.gapArea().contains("股东")));
    }

    @Test
    void generateScript_noKycGap_noKycQuestions() {
        when(customerContextService.findCustomer("CUST-001")).thenReturn(Optional.of(testCustomer));
        when(kycInsightService.getKycGapProfile("CUST-001")).thenReturn(Optional.empty());
        when(kycInsightService.getSignalsByCase(anyString())).thenReturn(List.of());
        when(kycInsightService.getAllProducts()).thenReturn(List.of());

        MeetingScript script = service.generateScript("CUST-001", "RM-001", "case-001", "journey-001");

        assertTrue(script.kycQuestions().isEmpty());
    }

    // ── 风险要点 ────────────────────────────────────────────────

    @Test
    void generateScript_highRiskCustomer_includesHighRiskPoints() {
        Customer highRiskCustomer = new Customer(
            "CUST-HIGH", "高风险企业", "高风险有限公司",
            "91330000MA27DEMO", LocalDate.of(2005, 3, 15), 50000000L,
            Industry.MANUFACTURING.name(), "浙江省",
            EnterpriseScale.LARGE.name(), CustomerTier.KEY.name(),
            LocalDate.of(2018, 1, 1), "RM-001", "张经理", "杭州分行",
            false, ListedStatus.UNLISTED.name(), RiskLevel.HIGH.name(),
            List.of("制造"), List.of(), "合作中");
        when(customerContextService.findCustomer("CUST-HIGH")).thenReturn(Optional.of(highRiskCustomer));
        when(kycInsightService.getKycGapProfile("CUST-HIGH")).thenReturn(Optional.empty());
        when(kycInsightService.getSignalsByCase(anyString())).thenReturn(List.of());
        when(kycInsightService.getAllProducts()).thenReturn(List.of());

        MeetingScript script = service.generateScript("CUST-HIGH", "RM-001", "case-high", "journey-high");

        assertTrue(script.riskPoints().stream().anyMatch(r -> r.contains("高风险")));
    }

    // ── 客户不存在 ──────────────────────────────────────────────

    @Test
    void generateScript_customerNotFound_throwsException() {
        when(customerContextService.findCustomer("CUST-UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(Exception.class, () ->
            service.generateScript("CUST-UNKNOWN", "RM-001", "case-001", "journey-001"));
    }

    // ── 基本字段 ────────────────────────────────────────────────

    @Test
    void generateScript_allFieldsPopulated() {
        when(customerContextService.findCustomer("CUST-001")).thenReturn(Optional.of(testCustomer));
        when(kycInsightService.getKycGapProfile("CUST-001")).thenReturn(Optional.empty());
        when(kycInsightService.getSignalsByCase(anyString())).thenReturn(List.of());
        when(kycInsightService.getAllProducts()).thenReturn(List.of());

        MeetingScript script = service.generateScript("CUST-001", "RM-001", "case-001", "journey-001");

        assertTrue(script.scriptId().startsWith("MS-"));
        assertEquals("CUST-001", script.customerId());
        assertNotNull(script.meetingObjective());
        assertNotNull(script.previsitSummary());
        assertNotNull(script.closingSummary());
    }
}
