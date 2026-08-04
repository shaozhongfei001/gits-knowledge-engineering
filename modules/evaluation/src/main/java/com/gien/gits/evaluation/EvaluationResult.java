package com.gien.gits.evaluation;

import java.util.Map;
import java.util.Objects;

/**
 * Structured evaluation result with a composite score and dimension sub-scores.
 *
 * @param composite  overall score in [0, 1]
 * @param dimensions map of dimension name to its sub-score in [0, 1]
 */
public record EvaluationResult(double composite, Map<String, Double> dimensions) {

    public EvaluationResult {
        Objects.requireNonNull(dimensions, "dimensions");
        if (composite < 0 || composite > 1) throw new IllegalArgumentException("composite must be in [0,1]");
        for (var entry : dimensions.entrySet()) {
            double v = entry.getValue();
            if (v < 0 || v > 1) throw new IllegalArgumentException(
                    "dimension '%s' score must be in [0,1], got %s".formatted(entry.getKey(), v));
        }
    }
}
