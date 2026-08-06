package com.gien.gits.adapter.dmn;

import com.gien.gits.api.metrics.BusinessMetrics;
import com.gien.gits.ontology.port.ClaimReconciliationPort.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * FallbackClaimReconciliationAdapter 严谨测试 — 验证DMN决策表的3条规则
 * 
 * 决策表逻辑 (FIRST命中策略):
 *   Rule 1: conflictDetected=true  → CONFLICT_REQUIRES_HUMAN_REVIEW
 *   Rule 2: conflictDetected=false AND authoritativeMatch=true AND evidenceComplete=true → VERIFIED_FACT
 *   Rule 3: conflictDetected=false AND (其余情况) → CANDIDATE_CLAIM
 */
class FallbackClaimReconciliationAdapterTest {

    private BusinessMetrics businessMetrics;
    private FallbackClaimReconciliationAdapter adapter;

    @BeforeEach
    void setUp() {
        businessMetrics = mock(BusinessMetrics.class);
        adapter = new FallbackClaimReconciliationAdapter(businessMetrics);
    }

    // ── Rule 1: conflictDetected=true → CONFLICT_REQUIRES_HUMAN_REVIEW ──

    @Nested
    @DisplayName("Rule 1: 冲突检测 → 人工审核")
    class Rule1ConflictTests {

        @Test
        @DisplayName("冲突=true, 权威匹配=true, 证据完整=true → 仍需人工审核")
        void conflictTrue_authoritativeAndComplete_stillHumanReview() {
            ReconciliationResult result = adapter.reconcile(true, true, true);
            assertThat(result.status()).isEqualTo(ReconciliationStatus.CONFLICT_REQUIRES_HUMAN_REVIEW);
            assertThat(result.reasoning()).contains("Rule-1");
        }

        @Test
        @DisplayName("冲突=true, 权威匹配=false, 证据完整=false → 仍需人工审核")
        void conflictTrue_noAuthoritativeNoComplete_stillHumanReview() {
            ReconciliationResult result = adapter.reconcile(true, false, false);
            assertThat(result.status()).isEqualTo(ReconciliationStatus.CONFLICT_REQUIRES_HUMAN_REVIEW);
        }

        @Test
        @DisplayName("冲突=true时，其他参数不影响结果（Rule 1优先级最高）")
        void conflictTrue_dominatesOtherInputs() {
            ReconciliationResult r1 = adapter.reconcile(true, true, true);
            ReconciliationResult r2 = adapter.reconcile(true, false, false);
            assertThat(r1.status()).isEqualTo(r2.status());
        }
    }

    // ── Rule 2: conflictDetected=false AND authoritativeMatch=true AND evidenceComplete=true → VERIFIED_FACT ──

    @Nested
    @DisplayName("Rule 2: 无冲突+权威匹配+证据完整 → 已验证事实")
    class Rule2VerifiedFactTests {

        @Test
        @DisplayName("三个条件全部满足 → VERIFIED_FACT")
        void allConditionsMet_verifiedFact() {
            ReconciliationResult result = adapter.reconcile(false, true, true);
            assertThat(result.status()).isEqualTo(ReconciliationStatus.VERIFIED_FACT);
            assertThat(result.reasoning()).contains("Rule-2");
        }

        @Test
        @DisplayName("权威匹配=true但证据不完整 → 不是VERIFIED_FACT")
        void authoritativeButIncomplete_notVerifiedFact() {
            ReconciliationResult result = adapter.reconcile(false, true, false);
            assertThat(result.status()).isNotEqualTo(ReconciliationStatus.VERIFIED_FACT);
        }

        @Test
        @DisplayName("证据完整=true但无权威匹配 → 不是VERIFIED_FACT")
        void completeButNotAuthoritative_notVerifiedFact() {
            ReconciliationResult result = adapter.reconcile(false, false, true);
            assertThat(result.status()).isNotEqualTo(ReconciliationStatus.VERIFIED_FACT);
        }
    }

    // ── Rule 3: 其余情况 → CANDIDATE_CLAIM ──

    @Nested
    @DisplayName("Rule 3: 其余情况 → 候选声明")
    class Rule3CandidateClaimTests {

        @ParameterizedTest
        @CsvSource({"false, false, false", "false, true, false", "false, false, true"})
        @DisplayName("非Rule1/Rule2的所有组合 → CANDIDATE_CLAIM")
        void nonRule1NonRule2_candidateClaim(boolean conflict, boolean authoritative, boolean complete) {
            ReconciliationResult result = adapter.reconcile(conflict, authoritative, complete);
            assertThat(result.status()).isEqualTo(ReconciliationStatus.CANDIDATE_CLAIM);
            assertThat(result.reasoning()).contains("Rule-3");
        }
    }

    // ── 完整决策表覆盖 ──────────────────────────────────────────

    @Test
    @DisplayName("3个布尔参数的8种组合，结果必须覆盖3种状态")
    void allEightCombinations_coverAllThreeStatuses() {
        ReconciliationStatus[] statuses = new ReconciliationStatus[8];
        int i = 0;
        for (boolean conflict : new boolean[]{true, false}) {
            for (boolean authoritative : new boolean[]{true, false}) {
                for (boolean complete : new boolean[]{true, false}) {
                    statuses[i++] = adapter.reconcile(conflict, authoritative, complete).status();
                }
            }
        }

        assertThat(statuses)
            .containsExactlyInAnyOrder(
                ReconciliationStatus.CONFLICT_REQUIRES_HUMAN_REVIEW,
                ReconciliationStatus.CONFLICT_REQUIRES_HUMAN_REVIEW,
                ReconciliationStatus.CONFLICT_REQUIRES_HUMAN_REVIEW,
                ReconciliationStatus.CONFLICT_REQUIRES_HUMAN_REVIEW,
                ReconciliationStatus.VERIFIED_FACT,
                ReconciliationStatus.CANDIDATE_CLAIM,
                ReconciliationStatus.CANDIDATE_CLAIM,
                ReconciliationStatus.CANDIDATE_CLAIM
            );
    }

    // ── 指标记录验证 ────────────────────────────────────────────

    @Test
    @DisplayName("每次决策都应记录BusinessMetrics")
    void eachDecision_recordsMetrics() {
        adapter.reconcile(true, true, true);
        adapter.reconcile(false, true, true);
        adapter.reconcile(false, false, false);

        verify(businessMetrics, times(3)).recordDmnDecision(anyString());
    }

    @Test
    @DisplayName("不同决策结果记录不同的指标标签")
    void differentDecisions_recordDifferentLabels() {
        adapter.reconcile(true, true, true);
        verify(businessMetrics).recordDmnDecision("CONFLICT_REQUIRES_HUMAN_REVIEW");

        adapter.reconcile(false, true, true);
        verify(businessMetrics).recordDmnDecision("VERIFIED_FACT");

        adapter.reconcile(false, false, false);
        verify(businessMetrics).recordDmnDecision("CANDIDATE_CLAIM");
    }

    // ── ReconciliationResult record验证 ────────────────────────

    @Test
    @DisplayName("ReconciliationResult包含决策理由")
    void result_containsReasoning() {
        ReconciliationResult result = adapter.reconcile(false, true, true);
        assertThat(result.reasoning()).isNotBlank();
        assertThat(result.reasoning()).contains("authoritativeMatch=true");
        assertThat(result.reasoning()).contains("evidenceComplete=true");
    }
}
