package com.gien.gits.evaluation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EvaluationModelTest {

    @Test
    void evaluationContextConstruction() {
        Instant now = Instant.now();
        EvaluationContext ctx = new EvaluationContext(5, 3, now, 2, 10);

        assertEquals(5, ctx.evidenceCount());
        assertEquals(3, ctx.evidenceCompleteCount());
        assertEquals(now, ctx.lastDataUpdateAt());
        assertEquals(2, ctx.ruleHitCount());
        assertEquals(10, ctx.totalRuleCount());
    }

    @Test
    void evaluationContextOfConvenience() {
        Instant now = Instant.now();
        EvaluationContext ctx = EvaluationContext.of(5, now, 2, 10);

        assertEquals(5, ctx.evidenceCount());
        assertEquals(5, ctx.evidenceCompleteCount());
        assertEquals(now, ctx.lastDataUpdateAt());
    }

    @Test
    void evaluationContextValidatesNegativeEvidenceCount() {
        assertThrows(IllegalArgumentException.class, () ->
                new EvaluationContext(-1, 0, Instant.now(), 0, 0));
    }

    @Test
    void evaluationContextValidatesCompleteExceedsTotal() {
        assertThrows(IllegalArgumentException.class, () ->
                new EvaluationContext(2, 5, Instant.now(), 0, 0));
    }

    @Test
    void evaluationResultConstruction() {
        EvaluationResult result = new EvaluationResult(0.85, Map.of("evidence", 0.9, "freshness", 0.8));

        assertEquals(0.85, result.composite(), 0.001);
        assertEquals(0.9, result.dimensions().get("evidence"), 0.001);
        assertEquals(0.8, result.dimensions().get("freshness"), 0.001);
    }

    @Test
    void evaluationResultValidatesCompositeRange() {
        assertThrows(IllegalArgumentException.class, () ->
                new EvaluationResult(1.5, Map.of()));
        assertThrows(IllegalArgumentException.class, () ->
                new EvaluationResult(-0.1, Map.of()));
    }

    @Test
    void evaluationResultValidatesDimensionRange() {
        assertThrows(IllegalArgumentException.class, () ->
                new EvaluationResult(0.5, Map.of("bad", 1.5)));
    }

    @Test
    void runManifestConstruction() {
        Instant now = Instant.now();
        RunManifest.ModelVersion model = new RunManifest.ModelVersion("openai", "gpt-4", "abc123");
        RunManifest manifest = new RunManifest(
                java.util.UUID.randomUUID(), now, "1.0.0", "1.0.0", "1.0.0",
                model, java.util.List.of("rule-v1"), "snap-1", "perm-1", "trace-1");

        assertNotNull(manifest.runId());
        assertEquals("1.0.0", manifest.ontologyVersion());
        assertEquals("openai", manifest.model().provider());
    }
}
