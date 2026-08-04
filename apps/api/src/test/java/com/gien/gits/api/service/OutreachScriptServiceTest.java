package com.gien.gits.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.gien.gits.engagement.OutreachScript;
import com.gien.gits.engagement.OutreachScript.OutreachChannel;
import com.gien.gits.engagement.port.LlmClient;
import com.gien.gits.engagement.port.WritableOutreachScriptRepository;
import com.gien.gits.ontology.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;

class OutreachScriptServiceTest {

    @Mock private CustomerContextService customerContextService;
    @Mock private KycInsightService kycInsightService;
    @Mock private CustomerJourneyService journeyService;
    @Mock private WritableOutreachScriptRepository scriptRepo;
    @Mock private LlmClient llmClient;

    private AutoCloseable mocks;
    private OutreachScriptService service;
    private Customer strategicCustomer;
    private Customer growthCustomer;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        // LLM fallback: throw so service uses template logic
        doThrow(new com.gien.gits.engagement.port.LlmClientException("test fallback")).when(llmClient).complete(anyString(), anyString());
        service = new OutreachScriptService(customerContextService, kycInsightService, journeyService, scriptRepo, llmClient);

        strategicCustomer = new Customer(
            "CUST-001", "华东精工", "华东精工制造有限公司",
            "91330000MA27DEMO", LocalDate.of(2005, 3, 15), 50000000L,
            Industry.MANUFACTURING.name(), "浙江省",
            EnterpriseScale.LARGE.name(), CustomerTier.STRATEGIC.name(),
            LocalDate.of(2018, 1, 1), "RM-001", "张经理", "杭州分行",
            false, ListedStatus.UNLISTED.name(), RiskLevel.MEDIUM.name(),
            List.of("精密制造"), List.of("战略客户"), "长期合作");

        growthCustomer = new Customer(
            "CUST-002", "成长企业", "成长科技有限公司",
            "91330000MA27DEMO", LocalDate.of(2015, 6, 1), 10000000L,
            Industry.TECHNOLOGY.name(), "浙江省",
            EnterpriseScale.MEDIUM.name(), CustomerTier.GROWTH.name(),
            LocalDate.of(2020, 1, 1), "RM-002", "李经理", "杭州分行",
            false, ListedStatus.UNLISTED.name(), RiskLevel.LOW.name(),
            List.of("软件开发"), List.of("成长客户"), "合作中");
    }

    // ── 渠道测试 ────────────────────────────────────────────────

    @Test
    void generateScript_phoneChannel_containsPhoneOpeningLine() {
        when(customerContextService.findCustomer("CUST-001")).thenReturn(Optional.of(strategicCustomer));
        when(kycInsightService.getKycGapProfile("CUST-001")).thenReturn(Optional.empty());
        when(kycInsightService.getSignalsByCase(anyString())).thenReturn(List.of());

        OutreachScript script = service.generateScript(
            "CUST-001", "RM-001", "case-001", "journey-001", OutreachChannel.PHONE);

        assertEquals(OutreachChannel.PHONE, script.channel());
        assertNotNull(script.openingLine());
        assertTrue(script.openingLine().contains("华东精工"));
    }

    @Test
    void generateScript_wechatChannel_containsWechatOpeningLine() {
        when(customerContextService.findCustomer("CUST-001")).thenReturn(Optional.of(strategicCustomer));
        when(kycInsightService.getKycGapProfile("CUST-001")).thenReturn(Optional.empty());
        when(kycInsightService.getSignalsByCase(anyString())).thenReturn(List.of());

        OutreachScript script = service.generateScript(
            "CUST-001", "RM-001", "case-001", "journey-001", OutreachChannel.WECHAT);

        assertEquals(OutreachChannel.WECHAT, script.channel());
        assertNotNull(script.openingLine());
    }

    @Test
    void generateScript_emailChannel_containsEmailOpeningLine() {
        when(customerContextService.findCustomer("CUST-001")).thenReturn(Optional.of(strategicCustomer));
        when(kycInsightService.getKycGapProfile("CUST-001")).thenReturn(Optional.empty());
        when(kycInsightService.getSignalsByCase(anyString())).thenReturn(List.of());

        OutreachScript script = service.generateScript(
            "CUST-001", "RM-001", "case-001", "journey-001", OutreachChannel.EMAIL);

        assertNotNull(script.openingLine());
        assertTrue(script.openingLine().contains("感谢"));
    }

    @Test
    void generateScript_faceToFaceChannel_containsFaceToFaceOpeningLine() {
        when(customerContextService.findCustomer("CUST-001")).thenReturn(Optional.of(strategicCustomer));
        when(kycInsightService.getKycGapProfile("CUST-001")).thenReturn(Optional.empty());
        when(kycInsightService.getSignalsByCase(anyString())).thenReturn(List.of());

        OutreachScript script = service.generateScript(
            "CUST-001", "RM-001", "case-001", "journey-001", OutreachChannel.FACE_TO_FACE);

        assertNotNull(script.openingLine());
        assertTrue(script.openingLine().contains("感谢") || script.openingLine().contains("交流"));
    }

    // ── 客户层级 ────────────────────────────────────────────────

    @Test
    void generateScript_strategicCustomer_openingLineContainsRespectful() {
        when(customerContextService.findCustomer("CUST-001")).thenReturn(Optional.of(strategicCustomer));
        when(kycInsightService.getKycGapProfile("CUST-001")).thenReturn(Optional.empty());
        when(kycInsightService.getSignalsByCase(anyString())).thenReturn(List.of());

        OutreachScript script = service.generateScript(
            "CUST-001", "RM-001", "case-001", "journey-001", OutreachChannel.PHONE);

        assertTrue(script.openingLine().contains("尊敬") || script.openingLine().contains("专属"));
    }

    @Test
    void generateScript_growthCustomer_openingLineMoreCasual() {
        when(customerContextService.findCustomer("CUST-002")).thenReturn(Optional.of(growthCustomer));
        when(kycInsightService.getKycGapProfile("CUST-002")).thenReturn(Optional.empty());
        when(kycInsightService.getSignalsByCase(anyString())).thenReturn(List.of());

        OutreachScript script = service.generateScript(
            "CUST-002", "RM-002", "case-002", "journey-002", OutreachChannel.PHONE);

        assertNotNull(script.openingLine());
    }

    // ── KYC缺口 ─────────────────────────────────────────────────

    @Test
    void generateScript_withKycGap_talkingPointsIncludeKycItems() {
        KycGapProfile gap = new KycGapProfile(
            "KP-001", "CUST-001", LocalDate.now(),
            List.of("已知项"), List.of("营收规模"), List.of(), List.of(),
            List.of("股东结构"), List.of());
        when(customerContextService.findCustomer("CUST-001")).thenReturn(Optional.of(strategicCustomer));
        when(kycInsightService.getKycGapProfile("CUST-001")).thenReturn(Optional.of(gap));
        when(kycInsightService.getSignalsByCase(anyString())).thenReturn(List.of());

        OutreachScript script = service.generateScript(
            "CUST-001", "RM-001", "case-001", "journey-001", OutreachChannel.PHONE);

        assertFalse(script.talkingPoints().isEmpty());
        assertTrue(script.talkingPoints().stream()
            .anyMatch(tp -> tp.topic().contains("KYC")));
    }

    // ── 风险提醒 ────────────────────────────────────────────────

    @Test
    void generateScript_highRiskCustomer_riskRemindersIncludeHighRiskWarning() {
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

        OutreachScript script = service.generateScript(
            "CUST-HIGH", "RM-001", "case-high", "journey-high", OutreachChannel.PHONE);

        assertTrue(script.riskReminders().stream().anyMatch(r -> r.contains("HIGH")));
    }

    // ── 客户不存在 ──────────────────────────────────────────────

    @Test
    void generateScript_customerNotFound_throwsException() {
        when(customerContextService.findCustomer("CUST-UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(Exception.class, () ->
            service.generateScript("CUST-UNKNOWN", "RM-001", "case-001", "journey-001", OutreachChannel.PHONE));
    }

    // ── 基本字段验证 ────────────────────────────────────────────

    @Test
    void generateScript_allFieldsPopulated() {
        when(customerContextService.findCustomer("CUST-001")).thenReturn(Optional.of(strategicCustomer));
        when(kycInsightService.getKycGapProfile("CUST-001")).thenReturn(Optional.empty());
        when(kycInsightService.getSignalsByCase(anyString())).thenReturn(List.of());

        OutreachScript script = service.generateScript(
            "CUST-001", "RM-001", "case-001", "journey-001", OutreachChannel.PHONE);

        assertNotNull(script.scriptId());
        assertTrue(script.scriptId().startsWith("OS-"));
        assertEquals("CUST-001", script.customerId());
        assertEquals("RM-001", script.rmId());
        assertNotNull(script.objective());
        assertNotNull(script.closingLine());
        assertNotNull(script.followUpAction());
    }
}
