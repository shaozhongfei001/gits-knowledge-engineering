package com.gien.gits.api.service.report;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import com.gien.gits.engagement.InteractionExtraction;
import com.gien.gits.engagement.InteractionExtraction.ClaimType;
import com.gien.gits.engagement.InteractionExtraction.ExtractionType;
import com.gien.gits.engagement.port.LlmClient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;

class SemanticPatternExtractionStrategyTest {

    @Mock private LlmClient llmClient;

    private AutoCloseable mocks;
    private SemanticPatternExtractionStrategy strategy;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        // LLM fallback: throw so strategy uses regex logic
        doThrow(new com.gien.gits.engagement.port.LlmClientException("test fallback")).when(llmClient).complete(anyString(), anyString());
        strategy = new SemanticPatternExtractionStrategy(llmClient);
    }

    // ── FINANCING_NEED: 金额+意图 → 高置信度 ────────────────────

    @Test
    void extract_financingNeedWithAmountAndIntent_highConfidence() {
        List<InteractionExtraction> results = strategy.extract("客户表示需要融资3000万元");
        assertFalse(results.isEmpty());
        InteractionExtraction financing = results.stream()
            .filter(e -> e.claimType() == ClaimType.FINANCING_NEED)
            .findFirst().orElse(null);
        assertNotNull(financing);
        assertTrue(financing.confidence().doubleValue() >= 0.70);
        assertTrue(financing.content().contains("3000"));
    }

    @Test
    void extract_financingNeedOnlyIntent_lowConfidence() {
        List<InteractionExtraction> results = strategy.extract("客户希望获得贷款支持");
        assertFalse(results.isEmpty());
        InteractionExtraction financing = results.stream()
            .filter(e -> e.claimType() == ClaimType.FINANCING_NEED)
            .findFirst().orElse(null);
        assertNotNull(financing);
        assertTrue(financing.confidence().doubleValue() >= 0.40);
    }

    @Test
    void extract_financingNeedEnglish_matches() {
        List<InteractionExtraction> results = strategy.extract("Client is seeking financing for expansion");
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(e -> e.claimType() == ClaimType.FINANCING_NEED));
    }

    // ── COMMITMENT ──────────────────────────────────────────────

    @Test
    void extract_commitmentBank_matches() {
        List<InteractionExtraction> results = strategy.extract("我们承诺下周提交担保材料");
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(e -> e.type() == ExtractionType.BANK_COMMITMENT));
    }

    @Test
    void extract_commitmentCustomer_matches() {
        List<InteractionExtraction> results = strategy.extract("我会尽快提供财务报表");
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(e ->
            e.type() == ExtractionType.CUSTOMER_COMMITMENT
            || e.type() == ExtractionType.BANK_COMMITMENT));
    }

    // ── RISK_SIGNAL ─────────────────────────────────────────────

    @Test
    void extract_riskSignal_matches() {
        List<InteractionExtraction> results = strategy.extract("客户经营出现困难，资金链紧张，存在违约风险");
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(e -> e.claimType() == ClaimType.RISK_SIGNAL));
    }

    @Test
    void extract_multipleRiskKeywords_higherConfidence() {
        List<InteractionExtraction> results = strategy.extract("企业经营困难，资金链紧张，逾期风险高，存在违约");
        InteractionExtraction risk = results.stream()
            .filter(e -> e.claimType() == ClaimType.RISK_SIGNAL)
            .findFirst().orElse(null);
        assertNotNull(risk);
        assertTrue(risk.confidence().doubleValue() >= 0.60);
    }

    // ── EXPANSION_INTENT ────────────────────────────────────────

    @Test
    void extract_expansionIntent_matches() {
        List<InteractionExtraction> results = strategy.extract("公司计划扩展生产规模，新增厂房");
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(e -> e.claimType() == ClaimType.EXPANSION_INTENT));
    }

    // ── MATERIAL_PROVIDE ────────────────────────────────────────

    @Test
    void extract_materialProvide_matches() {
        List<InteractionExtraction> results = strategy.extract("这是我们的财务报表和审计报告");
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(e -> e.claimType() == ClaimType.MATERIAL_PROVIDE));
    }

    // ── FOLLOW_UP ───────────────────────────────────────────────

    @Test
    void extract_followUp_matches() {
        List<InteractionExtraction> results = strategy.extract("我们下次再讨论具体的方案细节");
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(e -> e.claimType() == ClaimType.FOLLOW_UP));
    }

    // ── CUSTOMER_STATEMENT ──────────────────────────────────────

    @Test
    void extract_customerStatement_matches() {
        List<InteractionExtraction> results = strategy.extract("公司营收约为5亿");
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(e -> e.claimType() == ClaimType.CUSTOMER_STATEMENT));
    }

    // ── PRODUCT_INTEREST ────────────────────────────────────────

    @Test
    void extract_productInterest_matches() {
        List<InteractionExtraction> results = strategy.extract("想了解一下你们的供应链融资产品");
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(e -> e.content().contains("产品")));
    }

    // ── 无匹配 ──────────────────────────────────────────────────

    @Test
    void extract_noMatch_returnsEmpty() {
        List<InteractionExtraction> results = strategy.extract("今天天气不错");
        assertTrue(results.isEmpty());
    }

    @Test
    void extract_emptyText_returnsEmpty() {
        List<InteractionExtraction> results = strategy.extract("");
        assertTrue(results.isEmpty());
    }

    @Test
    void extract_nullText_throwsException() {
        assertThrows(NullPointerException.class, () -> strategy.extract(null));
    }
}
