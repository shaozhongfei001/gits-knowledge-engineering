package com.gien.gits.evaluation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class DefaultEvaluatorEdgeCaseTest {

    private final DefaultEvaluator evaluator = new DefaultEvaluator();

    @Test
    void scoreWithZeroEvidence() {
        EvaluationContext ctx = new EvaluationContext(0, 0, Instant.now(), 0, 0);

        EvaluationResult result = evaluator.score(ctx);

        assertNotNull(result);
        assertEquals(0.0, result.dimensions().get("evidence"), 0.001);
    }

    @Test
    void scoreWithNullContextThrows() {
        assertThrows(NullPointerException.class, () -> evaluator.score(null));
    }

    @Test
    void scoreReturnsValidComposite() {
        EvaluationContext ctx = new EvaluationContext(5, 4, Instant.now(), 3, 10);

        EvaluationResult result = evaluator.score(ctx);

        assertTrue(result.composite() >= 0.0 && result.composite() <= 1.0);
        assertEquals(3, result.dimensions().size());
        assertTrue(result.dimensions().containsKey("evidence"));
        assertTrue(result.dimensions().containsKey("freshness"));
        assertTrue(result.dimensions().containsKey("ruleHit"));
    }

    @Test
    void scoreWithAllFreshData() {
        EvaluationContext ctx = new EvaluationContext(8, 7, Instant.now(), 5, 10);

        EvaluationResult result = evaluator.score(ctx);

        assertEquals(1.0, result.dimensions().get("freshness"), 0.001);
        assertEquals(1.0, result.dimensions().get("evidence"), 0.001);
    }
}
