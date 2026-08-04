package com.gien.gits.adapter.dmn;

import com.gien.gits.api.metrics.BusinessMetrics;
import com.gien.gits.ontology.port.ClaimReconciliationPort.ReconciliationResult;
import com.gien.gits.ontology.port.ClaimReconciliationPort.ReconciliationStatus;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DmnClaimReconciliationAdapterTest {

    private MeterRegistry meterRegistry;
    private BusinessMetrics metrics;
    private DmnClaimReconciliationAdapter dmnAdapter;
    private FallbackClaimReconciliationAdapter fallbackAdapter;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metrics = new BusinessMetrics(meterRegistry);
        dmnAdapter = new DmnClaimReconciliationAdapter(metrics);
        fallbackAdapter = new FallbackClaimReconciliationAdapter(metrics);
    }

    // ── DMN Rule 1: conflictDetected=true → CONFLICT_REQUIRES_HUMAN_REVIEW ──

    @Nested
    @DisplayName("DMN Rule-1: conflictDetected=true → CONFLICT_REQUIRES_HUMAN_REVIEW")
    class Rule1ConflictDetected {

        @Test
        @DisplayName("conflict=true, match=true, evidence=true → CONFLICT")
        void conflictWithAllTrue() {
            ReconciliationResult result = dmnAdapter.reconcile(true, true, true);
            assertEquals(ReconciliationStatus.CONFLICT_REQUIRES_HUMAN_REVIEW, result.status());
            assertTrue(result.reasoning().contains("DMN Rule"));
        }

        @Test
        @DisplayName("conflict=true, match=false, evidence=false → CONFLICT")
        void conflictWithAllFalse() {
            ReconciliationResult result = dmnAdapter.reconcile(true, false, false);
            assertEquals(ReconciliationStatus.CONFLICT_REQUIRES_HUMAN_REVIEW, result.status());
        }

        @Test
        @DisplayName("conflict=true, match=true, evidence=false → CONFLICT")
        void conflictWithMixed() {
            ReconciliationResult result = dmnAdapter.reconcile(true, true, false);
            assertEquals(ReconciliationStatus.CONFLICT_REQUIRES_HUMAN_REVIEW, result.status());
        }
    }

    // ── DMN Rule 2: conflict=false, match=true, evidence=true → VERIFIED_FACT ──

    @Nested
    @DisplayName("DMN Rule-2: conflict=false, match=true, evidence=true → VERIFIED_FACT")
    class Rule2VerifiedFact {

        @Test
        @DisplayName("no conflict + authoritative match + evidence complete → VERIFIED_FACT")
        void verifiedFact() {
            ReconciliationResult result = dmnAdapter.reconcile(false, true, true);
            assertEquals(ReconciliationStatus.VERIFIED_FACT, result.status());
            assertTrue(result.reasoning().contains("DMN Rule"));
        }
    }

    // ── DMN Rule 3: conflict=false, others → CANDIDATE_CLAIM ──

    @Nested
    @DisplayName("DMN Rule-3: conflict=false, fallback → CANDIDATE_CLAIM")
    class Rule3CandidateClaim {

        @Test
        @DisplayName("no conflict, no match, no evidence → CANDIDATE_CLAIM")
        void noMatchNoEvidence() {
            ReconciliationResult result = dmnAdapter.reconcile(false, false, false);
            assertEquals(ReconciliationStatus.CANDIDATE_CLAIM, result.status());
        }

        @Test
        @DisplayName("no conflict, match=true, evidence=false → CANDIDATE_CLAIM")
        void matchButIncomplete() {
            ReconciliationResult result = dmnAdapter.reconcile(false, true, false);
            assertEquals(ReconciliationStatus.CANDIDATE_CLAIM, result.status());
        }

        @Test
        @DisplayName("no conflict, match=false, evidence=true → CANDIDATE_CLAIM")
        void noMatchButComplete() {
            ReconciliationResult result = dmnAdapter.reconcile(false, false, true);
            assertEquals(ReconciliationStatus.CANDIDATE_CLAIM, result.status());
        }
    }

    // ── DMN 与 Fallback 结果一致性 ──

    @Nested
    @DisplayName("DMN 与 Fallback 结果一致性验证")
    class DmnFallbackConsistency {

        @Test
        @DisplayName("所有8种输入组合 DMN 与 Fallback 结果一致")
        void allCombinationsConsistent() {
            boolean[] bools = {true, false};
            for (boolean conflict : bools) {
                for (boolean match : bools) {
                    for (boolean evidence : bools) {
                        ReconciliationResult dmnResult = dmnAdapter.reconcile(conflict, match, evidence);
                        ReconciliationResult fallbackResult = fallbackAdapter.reconcile(conflict, match, evidence);
                        assertEquals(fallbackResult.status(), dmnResult.status(),
                            String.format("Mismatch at conflict=%s, match=%s, evidence=%s", conflict, match, evidence));
                    }
                }
            }
        }
    }

    // ── 指标记录验证 ──

    @Nested
    @DisplayName("BusinessMetrics 记录验证")
    class MetricsRecording {

        @Test
        @DisplayName("DMN决策后 metrics 被记录")
        void metricsRecorded() {
            dmnAdapter.reconcile(false, true, true);
            // recordDmnDecision uses tag "status", so find by counter name
            double total = meterRegistry.find("dmn_decision_total").counters().stream()
                .mapToDouble(c -> c.count())
                .sum();
            assertTrue(total >= 1.0, "At least 1 DMN decision should be recorded, got: " + total);
        }

        @Test
        @DisplayName("多次决策 metrics 累加")
        void metricsAccumulate() {
            double before = meterRegistry.find("dmn_decision_total").counters().stream()
                .mapToDouble(c -> c.count())
                .sum();
            dmnAdapter.reconcile(true, true, true);
            dmnAdapter.reconcile(false, true, true);
            dmnAdapter.reconcile(false, false, false);
            double after = meterRegistry.find("dmn_decision_total").counters().stream()
                .mapToDouble(c -> c.count())
                .sum();
            assertEquals(before + 3.0, after, 0.01, "3 more decisions should be recorded");
        }
    }

    // ── DMN 规则加载验证 ──

    @Nested
    @DisplayName("DMN 规则加载验证")
    class RuleLoading {

        @Test
        @DisplayName("DMN 文件成功加载，规则数 ≥3")
        void rulesLoadedFromDmn() {
            // 验证DMN适配器能正常工作（即规则已加载）
            ReconciliationResult result = dmnAdapter.reconcile(true, true, true);
            // DMN Rule命中时 reason 包含 "DMN Rule-"
            assertTrue(result.reasoning().contains("DMN Rule-"),
                "DMN adapter should use DMN rules, got: " + result.reasoning());
        }

        @Test
        @DisplayName("DMN Rule ID 在 reasoning 中体现")
        void ruleIdInReasoning() {
            ReconciliationResult conflictResult = dmnAdapter.reconcile(true, false, false);
            assertTrue(conflictResult.reasoning().contains("rule-conflict") ||
                       conflictResult.reasoning().contains("Rule-"),
                "Reasoning should reference rule ID, got: " + conflictResult.reasoning());
        }
    }
}
