package com.gien.gits.evaluation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
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
}
