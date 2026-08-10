package com.gien.gits.api.service.report;

import com.gien.gits.engagement.InteractionExtraction;
import com.gien.gits.engagement.InteractionExtraction.*;
import com.gien.gits.engagement.port.LlmClient;
import com.gien.gits.engagement.port.LlmClientException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * SemanticPatternExtractionStrategy 严谨测试
 * 
 * 覆盖:
 * - 8种语义模式正则提取（含边界条件）
 * - LLM提取成功/失败/fallback
 * - 置信度分层逻辑
 * - 空输入/无匹配输入
 */
class SemanticPatternExtractionStrategyTest {

    private LlmClient llmClient;
    private SemanticPatternExtractionStrategy strategy;

    @BeforeEach
    void setUp() {
        llmClient = mock(LlmClient.class);
        // 默认让LLM返回空patterns，触发fallback到正则
        doReturn("{\"patterns\":[]}").when(llmClient).complete(anyString(), anyString());
        strategy = new SemanticPatternExtractionStrategy(llmClient);
    }

    // ── 空输入和无匹配 ──────────────────────────────────────────

    @Nested
    @DisplayName("空输入和无匹配场景")
    class EmptyAndNoMatchTests {

        @Test
        @DisplayName("空字符串应返回空列表")
        void emptyInput_returnsEmptyList() {
            List<InteractionExtraction> result = strategy.extract("");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("null输入应抛出异常")
        void nullInput_throwsException() {
            assertThatThrownBy(() -> strategy.extract(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("无语义模式的普通文本应返回空列表")
        void plainText_noSemanticPatterns_returnsEmpty() {
            List<InteractionExtraction> result = strategy.extract("今天天气不错，我们去散步吧。");
            assertThat(result).isEmpty();
        }
    }

    // ── 模式1: 融资需求 ─────────────────────────────────────────

    @Nested
    @DisplayName("融资需求识别")
    class FinancingNeedTests {

        @Test
        @DisplayName("金额+意图 → 高置信度(0.80)")
        void amountAndIntent_highConfidence() {
            List<InteractionExtraction> result = strategy.extract("我们需要融资500万元扩大生产");
            assertThat(result).hasSizeGreaterThanOrEqualTo(1);
            InteractionExtraction ext = result.stream()
                .filter(e -> e.claimType() == ClaimType.FINANCING_NEED)
                .findFirst().orElseThrow();
            assertThat(ext.confidence()).isEqualByComparingTo(new BigDecimal("0.80"));
            assertThat(ext.content()).contains("500万");
            assertThat(ext.type()).isEqualTo(ExtractionType.OPPORTUNITY_SIGNAL);
        }

        @Test
        @DisplayName("仅金额(无意图关键词) → 中等置信度(0.60)")
        void amountOnly_mediumConfidence() {
            List<InteractionExtraction> result = strategy.extract("项目预算3000万，分两期实施");
            InteractionExtraction ext = result.stream()
                .filter(e -> e.claimType() == ClaimType.FINANCING_NEED)
                .findFirst().orElseThrow();
            assertThat(ext.confidence()).isEqualByComparingTo(new BigDecimal("0.60"));
        }

        @Test
        @DisplayName("仅意图(无金额) → 低置信度(0.50)")
        void intentOnly_lowConfidence() {
            List<InteractionExtraction> result = strategy.extract("我们有资金需求，希望能获得银行支持");
            InteractionExtraction ext = result.stream()
                .filter(e -> e.claimType() == ClaimType.FINANCING_NEED)
                .findFirst().orElseThrow();
            assertThat(ext.confidence()).isEqualByComparingTo(new BigDecimal("0.50"));
        }
    }

    // ── 模式2: 承诺识别 ─────────────────────────────────────────

    @Nested
    @DisplayName("承诺识别")
    class CommitmentTests {

        @Test
        @DisplayName("银行承诺 → BANK_COMMITMENT + RM_COMMITMENT")
        void bankCommitment_classifiedCorrectly() {
            List<InteractionExtraction> result = strategy.extract("我们行将尽快为您安排授信审批");
            InteractionExtraction ext = result.stream()
                .filter(e -> e.type() == ExtractionType.BANK_COMMITMENT)
                .findFirst().orElseThrow();
            assertThat(ext.claimType()).isEqualTo(ClaimType.RM_COMMITMENT);
            assertThat(ext.speaker()).isEqualTo("RM");
        }

        @Test
        @DisplayName("客户承诺 → CUSTOMER_COMMITMENT")
        void customerCommitment_classifiedCorrectly() {
            List<InteractionExtraction> result = strategy.extract("我会尽快补充财务报表");
            InteractionExtraction ext = result.stream()
                .filter(e -> e.type() == ExtractionType.CUSTOMER_COMMITMENT)
                .findFirst().orElseThrow();
            assertThat(ext.speaker()).isEqualTo("客户方");
        }
    }

    // ── 模式3: 风险信号 ─────────────────────────────────────────

    @Nested
    @DisplayName("风险信号识别")
    class RiskSignalTests {

        @Test
        @DisplayName("单个风险关键词 → 置信度0.60")
        void singleRiskKeyword_confidence0_60() {
            List<InteractionExtraction> result = strategy.extract("客户近期出现逾期情况");
            InteractionExtraction ext = result.stream()
                .filter(e -> e.claimType() == ClaimType.RISK_SIGNAL)
                .findFirst().orElseThrow();
            assertThat(ext.confidence()).isEqualByComparingTo(BigDecimal.valueOf(0.60));
            assertThat(ext.type()).isEqualTo(ExtractionType.RISK_INDICATOR);
            assertThat(ext.notFact()).isTrue();
            assertThat(ext.requiresReconciliation()).isTrue();
        }

        @Test
        @DisplayName("多个风险关键词 → 置信度递增(上限0.95)")
        void multipleRiskKeywords_confidenceIncreases() {
            List<InteractionExtraction> result = strategy.extract("客户存在逾期和违约风险，可能产生不良贷款");
            InteractionExtraction ext = result.stream()
                .filter(e -> e.claimType() == ClaimType.RISK_SIGNAL)
                .findFirst().orElseThrow();
            assertThat(ext.confidence()).isGreaterThanOrEqualTo(BigDecimal.valueOf(0.70));
        }
    }

    // ── 模式4: 扩展意向 ─────────────────────────────────────────

    @Test
    @DisplayName("扩展意向识别 → INTENT + EXPANSION_INTENT")
    void expansionIntent_detected() {
        List<InteractionExtraction> result = strategy.extract("我们计划新增生产线，进行扩产");
        InteractionExtraction ext = result.stream()
            .filter(e -> e.claimType() == ClaimType.EXPANSION_INTENT)
            .findFirst().orElseThrow();
        assertThat(ext.type()).isEqualTo(ExtractionType.INTENT);
        assertThat(ext.confidence()).isEqualByComparingTo(new BigDecimal("0.75"));
    }

    // ── 模式5: 材料提供 ─────────────────────────────────────────

    @Test
    @DisplayName("材料提供识别 → CUSTOMER_COMMITMENT + MATERIAL_PROVIDE")
    void materialProvide_detected() {
        List<InteractionExtraction> result = strategy.extract("我会提供资料和财务报表给你们");
        InteractionExtraction ext = result.stream()
            .filter(e -> e.claimType() == ClaimType.MATERIAL_PROVIDE)
            .findFirst().orElseThrow();
        assertThat(ext.type()).isEqualTo(ExtractionType.CUSTOMER_COMMITMENT);
        assertThat(ext.notFact()).isFalse();
    }

    // ── 模式6: 跟进事项 ─────────────────────────────────────────

    @Test
    @DisplayName("跟进事项识别 → BANK_COMMITMENT + FOLLOW_UP")
    void followUp_detected() {
        List<InteractionExtraction> result = strategy.extract("我们下次再详细沟通方案细节");
        InteractionExtraction ext = result.stream()
            .filter(e -> e.claimType() == ClaimType.FOLLOW_UP)
            .findFirst().orElseThrow();
        assertThat(ext.type()).isEqualTo(ExtractionType.BANK_COMMITMENT);
        assertThat(ext.speaker()).isEqualTo("双方");
    }

    // ── 模式7: 客户陈述 ─────────────────────────────────────────

    @Test
    @DisplayName("客户陈述事实 → FACT_CLAIM + CUSTOMER_STATEMENT")
    void customerStatement_detected() {
        List<InteractionExtraction> result = strategy.extract("我们公司目前营收约为3亿元");
        InteractionExtraction ext = result.stream()
            .filter(e -> e.type() == ExtractionType.FACT_CLAIM)
            .findFirst().orElseThrow();
        assertThat(ext.claimType()).isEqualTo(ClaimType.CUSTOMER_STATEMENT);
        assertThat(ext.notFact()).isTrue();
        assertThat(ext.requiresReconciliation()).isTrue();
    }

    // ── 模式8: 产品兴趣 ─────────────────────────────────────────

    @Test
    @DisplayName("产品兴趣识别 → OPPORTUNITY_SIGNAL")
    void productInterest_detected() {
        List<InteractionExtraction> result = strategy.extract("请问你们的贷款产品利率是多少");
        assertThat(result.stream().anyMatch(e -> e.type() == ExtractionType.OPPORTUNITY_SIGNAL)).isTrue();
    }

    // ── LLM集成 ─────────────────────────────────────────────────

    @Nested
    @DisplayName("LLM集成与降级")
    class LlmIntegrationTests {

        @Test
        @DisplayName("LLM成功返回 → 优先使用LLM结果")
        void llmSuccess_prefersLlmResults() {
            String llmResponse = """
                {"patterns":[{"objectId":"EXT-LLM001","type":"RISK_INDICATOR","claimType":"RISK_SIGNAL","content":"LLM提取的风险信号","speaker":"客户方","evidenceRef":"TR-RAW","status":"DETECTED","confidence":0.92,"notFact":true,"requiresReconciliation":true,"conflictWith":null,"nextQuestion":"核实风险"}]}
                """;
            doReturn(llmResponse).when(llmClient).complete(anyString(), anyString());

            List<InteractionExtraction> result = strategy.extract("客户存在逾期风险");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).content()).isEqualTo("LLM提取的风险信号");
            assertThat(result.get(0).confidence()).isEqualByComparingTo(BigDecimal.valueOf(0.92));
        }

        @Test
        @DisplayName("LLM抛出LlmClientException → fallback到正则")
        void llmClientException_fallsBackToRegex() {
            doThrow(new LlmClientException("connection refused"))
                .when(llmClient).complete(anyString(), anyString());

            List<InteractionExtraction> result = strategy.extract("客户存在逾期风险");

            assertThat(result).isNotEmpty();
            assertThat(result.stream().anyMatch(e -> e.claimType() == ClaimType.RISK_SIGNAL)).isTrue();
        }

        @Test
        @DisplayName("LLM返回空patterns → fallback到正则")
        void llmEmptyPatterns_fallsBackToRegex() {
            doReturn("{\"patterns\":[]}").when(llmClient).complete(anyString(), anyString());

            List<InteractionExtraction> result = strategy.extract("客户存在逾期风险");

            assertThat(result).isNotEmpty();
            assertThat(result.stream().anyMatch(e -> e.claimType() == ClaimType.RISK_SIGNAL)).isTrue();
        }

        @Test
        @DisplayName("LLM返回无效JSON → fallback到正则")
        void llmInvalidJson_fallsBackToRegex() {
            doReturn("not valid json").when(llmClient).complete(anyString(), anyString());

            List<InteractionExtraction> result = strategy.extract("客户存在逾期风险");

            assertThat(result).isNotEmpty();
        }
    }

    // ── 多模式组合 ──────────────────────────────────────────────

    @Test
    @DisplayName("一段文本可同时触发多种模式")
    void multiplePatternsInOneText() {
        String text = "我们公司目前营收约为3亿元，计划新增生产线需要融资5000万元，我会尽快补充财务报表";
        List<InteractionExtraction> result = strategy.extract(text);

        assertThat(result.size()).isGreaterThanOrEqualTo(3);
        assertThat(result.stream().map(InteractionExtraction::claimType))
            .contains(ClaimType.CUSTOMER_STATEMENT, ClaimType.FINANCING_NEED, ClaimType.MATERIAL_PROVIDE);
    }
}
