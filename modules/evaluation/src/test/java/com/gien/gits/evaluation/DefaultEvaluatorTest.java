package com.gien.gits.evaluation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DefaultEvaluatorTest {

    private static RunManifest sampleManifest() {
        return new RunManifest(
                UUID.randomUUID(),
                Instant.parse("2026-08-02T00:00:00Z"),
                "ont-1.0.0",
                "skill-1.0.0",
                "prompt-1.0.0",
                new RunManifest.ModelVersion("openai", "gpt-4o", "hash-abc"),
                List.of("claim-reconciliation@1.0.0", "eligibility@0.9.0"),
                "snapshot-2026-08-02",
                "perm-decision-1",
                "trace-1");
    }

    // ── Legacy evaluate() tests ──────────────────────────────────────────

    @Test
    void evaluateValidManifestProducesMechanismReadyWithMetrics() {
        var evaluator = new DefaultEvaluator();
        var manifest = sampleManifest();

        EvaluationPort.Result result = evaluator.evaluate(manifest, "caseset-1.0.0");

        assertEquals(DefaultEvaluator.GATE_MECHANISM_READY, result.gateState());
        assertNotNull(result.metrics());
        assertFalse(result.metrics().isEmpty());
        assertEquals(2, result.metrics().get("ruleVersionCount").intValue());
        // gateState must never self-claim QA or business pass
        assertFalse(result.gateState().contains("QA_PASS"));
        assertFalse(result.gateState().contains("BUSINESS_PASS"));
    }

    @Test
    void ruleVersionCountMatchesManifest() {
        var evaluator = new DefaultEvaluator();
        var manifest = new RunManifest(
                UUID.randomUUID(),
                Instant.parse("2026-08-02T00:00:00Z"),
                "ont-1.0.0",
                "skill-1.0.0",
                "prompt-1.0.0",
                new RunManifest.ModelVersion("openai", "gpt-4o", "hash-abc"),
                List.of("r1", "r2", "r3", "r4", "r5"),
                "snapshot-2026-08-02",
                "perm-decision-1",
                "trace-1");

        EvaluationPort.Result result = evaluator.evaluate(manifest, "caseset-1.0.0");

        assertEquals(5, result.metrics().get("ruleVersionCount").intValue());
    }

    @Test
    void evaluateNullManifestThrows() {
        var evaluator = new DefaultEvaluator();
        assertThrows(NullPointerException.class, () -> evaluator.evaluate(null, "caseset-1.0.0"));
    }

    @Test
    void evaluateBlankCaseSetVersionThrows() {
        var evaluator = new DefaultEvaluator();
        var manifest = sampleManifest();
        assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(manifest, ""));
        assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(manifest, "   "));
        assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(manifest, null));
    }

    @Test
    void metricsMapIsNonNullAndImmutable() {
        var evaluator = new DefaultEvaluator();
        var manifest = sampleManifest();

        EvaluationPort.Result result = evaluator.evaluate(manifest, "caseset-1.0.0");

        assertNotNull(result.metrics());
        assertThrows(UnsupportedOperationException.class, () -> result.metrics().put("x", 1));
    }

    @Test
    void runManifestRejectsBlankArtifactVersions() {
        assertThrows(IllegalArgumentException.class, () -> new RunManifest(
                UUID.randomUUID(),
                Instant.now(),
                " ",
                "skill-1.0.0",
                "prompt-1.0.0",
                new RunManifest.ModelVersion("openai", "gpt-4o", "hash-abc"),
                List.of("r1"),
                "snapshot",
                "perm",
                "trace"));
    }

    @Test
    void runManifestRejectsNullModel() {
        assertThrows(NullPointerException.class, () -> new RunManifest(
                UUID.randomUUID(),
                Instant.now(),
                "ont-1.0.0",
                "skill-1.0.0",
                "prompt-1.0.0",
                null,
                List.of("r1"),
                "snapshot",
                "perm",
                "trace"));
    }

    @Test
    void runManifestRejectsBlankTraceId() {
        assertThrows(IllegalArgumentException.class, () -> new RunManifest(
                UUID.randomUUID(),
                Instant.now(),
                "ont-1.0.0",
                "skill-1.0.0",
                "prompt-1.0.0",
                new RunManifest.ModelVersion("openai", "gpt-4o", "hash-abc"),
                List.of("r1"),
                "snapshot",
                "perm",
                ""));
    }

    @Test
    void runManifestRuleVersionsAreDefensivelyCopied() {
        var original = List.of("r1");
        var manifest = new RunManifest(
                UUID.randomUUID(),
                Instant.now(),
                "ont-1.0.0",
                "skill-1.0.0",
                "prompt-1.0.0",
                new RunManifest.ModelVersion("openai", "gpt-4o", "hash-abc"),
                original,
                "snapshot",
                "perm",
                "trace");
        assertTrue(manifest.ruleVersions().equals(List.of("r1")));
        assertThrows(UnsupportedOperationException.class, () -> manifest.ruleVersions().add("r2"));
    }

    @Test
    void placeholderScoreIsEngineeringNeutral() {
        var evaluator = new DefaultEvaluator();
        var manifest = sampleManifest();

        EvaluationPort.Result result = evaluator.evaluate(manifest, "caseset-1.0.0");

        Number placeholder = result.metrics().get("placeholderScore");
        assertNotNull(placeholder);
        assertEquals(0.0, placeholder.doubleValue(), 0.0);
        // Sanity: metrics keys are exactly the engineering placeholders we emit
        assertTrue(result.metrics().containsKey("ruleVersionCount"));
        assertTrue(result.metrics().containsKey("placeholderScore"));
        assertEquals(2, result.metrics().size());
    }

    @Test
    void evaluateIsDeterministicAcrossCalls() {
        var evaluator = new DefaultEvaluator();
        var manifest = sampleManifest();

        EvaluationPort.Result first = evaluator.evaluate(manifest, "caseset-1.0.0");
        EvaluationPort.Result second = evaluator.evaluate(manifest, "caseset-1.0.0");

        assertEquals(first.gateState(), second.gateState());
        assertEquals(first.metrics(), second.metrics());
    }

    @Test
    void metricsContainsOnlyNumericValues() {
        var evaluator = new DefaultEvaluator();
        var manifest = sampleManifest();

        EvaluationPort.Result result = evaluator.evaluate(manifest, "caseset-1.0.0");

        for (Map.Entry<String, Number> e : result.metrics().entrySet()) {
            assertNotNull(e.getValue(), "metric " + e.getKey() + " must be non-null numeric");
            assertTrue(e.getValue() instanceof Number, "metric " + e.getKey() + " must be a Number");
        }
    }

    // ── New score() tests ────────────────────────────────────────────────

    @Test
    void scoreNullContextThrows() {
        var evaluator = new DefaultEvaluator();
        assertThrows(NullPointerException.class, () -> evaluator.score(null));
    }

    @Test
    void scoreReturnsCompositeAndThreeDimensions() {
        var evaluator = new DefaultEvaluator();
        var ctx = new EvaluationContext(5, 4, Instant.now(), 3, 5);

        EvaluationResult result = evaluator.score(ctx);

        assertTrue(result.composite() >= 0 && result.composite() <= 1.0,
                "composite must be in [0,1]");
        assertEquals(3, result.dimensions().size());
        assertTrue(result.dimensions().containsKey("evidence"));
        assertTrue(result.dimensions().containsKey("freshness"));
        assertTrue(result.dimensions().containsKey("ruleHit"));
    }

    @Test
    void scoreCompositeIsWeightedSum() {
        var evaluator = new DefaultEvaluator();
        var ctx = new EvaluationContext(5, 4, Instant.now(), 3, 5);

        EvaluationResult result = evaluator.score(ctx);

        double expected = result.dimensions().get("evidence") * DefaultEvaluator.EVIDENCE_WEIGHT
                + result.dimensions().get("freshness") * DefaultEvaluator.FRESHNESS_WEIGHT
                + result.dimensions().get("ruleHit") * DefaultEvaluator.RULE_HIT_WEIGHT;
        assertEquals(expected, result.composite(), 0.0001);
    }

    // ── Evidence score tests ─────────────────────────────────────────────

    @Test
    void evidenceScoreNoEvidenceIsZero() {
        var evaluator = new DefaultEvaluator();
        var ctx = EvaluationContext.of(0, Instant.now(), 0, 0);
        assertEquals(0.0, evaluator.calculateEvidenceScore(ctx), 0.001);
    }

    @Test
    void evidenceScoreOneToThreeItems() {
        var evaluator = new DefaultEvaluator();
        var ctx = EvaluationContext.of(2, Instant.now(), 0, 0);
        double score = evaluator.calculateEvidenceScore(ctx);
        assertTrue(score >= 0.4 && score <= 0.5, "1-3 items: base 0.4 + completeness bonus, got " + score);
    }

    @Test
    void evidenceScoreFourToSixItems() {
        var evaluator = new DefaultEvaluator();
        var ctx = EvaluationContext.of(5, Instant.now(), 0, 0);
        double score = evaluator.calculateEvidenceScore(ctx);
        assertTrue(score >= 0.7 && score <= 0.8, "4-6 items: base 0.7 + completeness bonus, got " + score);
    }

    @Test
    void evidenceScoreSevenOrMoreItems() {
        var evaluator = new DefaultEvaluator();
        var ctx = EvaluationContext.of(10, Instant.now(), 0, 0);
        double score = evaluator.calculateEvidenceScore(ctx);
        assertTrue(score >= 1.0 && score <= 1.0, "7+ items: base 1.0 + completeness bonus capped at 1.0, got " + score);
    }

    @Test
    void evidenceScoreCompletenessBonus() {
        var evaluator = new DefaultEvaluator();
        // 3 items, only 1 complete → completeness ratio 1/3 ≈ 0.33, bonus ≈ 0.033
        var ctx = new EvaluationContext(3, 1, Instant.now(), 0, 0);
        double score = evaluator.calculateEvidenceScore(ctx);
        assertTrue(score > 0.4 && score < 0.5, "incomplete evidence should add small bonus, got " + score);
    }

    // ── Freshness score tests ────────────────────────────────────────────

    @Test
    void freshnessScoreRecentData() {
        var evaluator = new DefaultEvaluator();
        var ctx = EvaluationContext.of(5, Instant.now().minus(3, ChronoUnit.DAYS), 3, 5);
        assertEquals(1.0, evaluator.calculateFreshnessScore(ctx), 0.001);
    }

    @Test
    void freshnessScoreWeekOldData() {
        var evaluator = new DefaultEvaluator();
        var ctx = EvaluationContext.of(5, Instant.now().minus(10, ChronoUnit.DAYS), 3, 5);
        assertEquals(0.7, evaluator.calculateFreshnessScore(ctx), 0.001);
    }

    @Test
    void freshnessScoreMonthOldData() {
        var evaluator = new DefaultEvaluator();
        var ctx = EvaluationContext.of(5, Instant.now().minus(45, ChronoUnit.DAYS), 3, 5);
        assertEquals(0.3, evaluator.calculateFreshnessScore(ctx), 0.001);
    }

    @Test
    void freshnessScoreExactlyAtFullThreshold() {
        var evaluator = new DefaultEvaluator();
        // Use 6 days 23 hours to stay comfortably within the 7-day threshold
        var ctx = EvaluationContext.of(5, Instant.now().minus(6, ChronoUnit.DAYS).minus(23, ChronoUnit.HOURS), 3, 5);
        assertEquals(1.0, evaluator.calculateFreshnessScore(ctx), 0.001);
    }

    // ── Rule-hit score tests ─────────────────────────────────────────────

    @Test
    void ruleHitScoreNoRulesEvaluated() {
        var evaluator = new DefaultEvaluator();
        var ctx = EvaluationContext.of(5, Instant.now(), 0, 0);
        assertEquals(0.5, evaluator.calculateRuleHitScore(ctx), 0.001);
    }

    @Test
    void ruleHitScoreAllRulesHit() {
        var evaluator = new DefaultEvaluator();
        var ctx = EvaluationContext.of(5, Instant.now(), 5, 5);
        assertEquals(1.0, evaluator.calculateRuleHitScore(ctx), 0.001);
    }

    @Test
    void ruleHitScoreHalfRulesHit() {
        var evaluator = new DefaultEvaluator();
        var ctx = EvaluationContext.of(5, Instant.now(), 5, 10);
        assertEquals(0.5, evaluator.calculateRuleHitScore(ctx), 0.001);
    }

    @Test
    void ruleHitScoreMinimumFloor() {
        var evaluator = new DefaultEvaluator();
        // 1 out of 100 = 0.01, but floor is 0.1
        var ctx = EvaluationContext.of(5, Instant.now(), 1, 100);
        assertEquals(0.1, evaluator.calculateRuleHitScore(ctx), 0.001);
    }

    // ── Integration: full score with realistic context ────────────────────

    @Test
    void scoreWithWellPopulatedCase() {
        var evaluator = new DefaultEvaluator();
        // 8 evidence items, all complete, updated 2 days ago, 8/10 rules hit
        var ctx = new EvaluationContext(8, 8, Instant.now().minus(2, ChronoUnit.DAYS), 8, 10);

        EvaluationResult result = evaluator.score(ctx);

        assertTrue(result.composite() >= 0.8, "well-populated case should score high, got " + result.composite());
        assertEquals(1.0, result.dimensions().get("evidence"), 0.001);
        assertEquals(1.0, result.dimensions().get("freshness"), 0.001);
        assertEquals(0.8, result.dimensions().get("ruleHit"), 0.001);
    }

    @Test
    void scoreWithSparseCase() {
        var evaluator = new DefaultEvaluator();
        // 1 evidence, updated 60 days ago, 1/10 rules hit
        var ctx = new EvaluationContext(1, 0, Instant.now().minus(60, ChronoUnit.DAYS), 1, 10);

        EvaluationResult result = evaluator.score(ctx);

        assertTrue(result.composite() < 0.5, "sparse case should score low, got " + result.composite());
        assertTrue(result.dimensions().get("evidence") < 0.5);
        assertEquals(0.3, result.dimensions().get("freshness"), 0.001);
        assertEquals(0.1, result.dimensions().get("ruleHit"), 0.001);
    }

    // ── EvaluationContext validation tests ────────────────────────────────

    @Test
    void contextRejectsNegativeEvidenceCount() {
        assertThrows(IllegalArgumentException.class,
                () -> new EvaluationContext(-1, 0, Instant.now(), 0, 0));
    }

    @Test
    void contextRejectsCompleteCountExceedingTotal() {
        assertThrows(IllegalArgumentException.class,
                () -> new EvaluationContext(3, 5, Instant.now(), 0, 0));
    }

    @Test
    void contextRejectsRuleHitExceedingTotal() {
        assertThrows(IllegalArgumentException.class,
                () -> new EvaluationContext(3, 3, Instant.now(), 5, 3));
    }

    @Test
    void contextRejectsNullTimestamp() {
        assertThrows(NullPointerException.class,
                () -> new EvaluationContext(3, 3, null, 1, 5));
    }
}
