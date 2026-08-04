package com.gien.gits.evaluation;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Evaluation context carrying the data needed for rule-based scoring.
 *
 * @param evidenceCount      number of evidence items collected
 * @param evidenceCompleteCount number of evidence items that are fully populated (all required fields present)
 * @param lastDataUpdateAt   timestamp of the most recent data update
 * @param ruleHitCount       number of policy rules that were hit/triggered
 * @param totalRuleCount     total number of policy rules evaluated
 */
public record EvaluationContext(
        int evidenceCount,
        int evidenceCompleteCount,
        Instant lastDataUpdateAt,
        int ruleHitCount,
        int totalRuleCount) {

    public EvaluationContext {
        if (evidenceCount < 0) throw new IllegalArgumentException("evidenceCount must not be negative");
        if (evidenceCompleteCount < 0) throw new IllegalArgumentException("evidenceCompleteCount must not be negative");
        if (evidenceCompleteCount > evidenceCount) throw new IllegalArgumentException("evidenceCompleteCount must not exceed evidenceCount");
        if (ruleHitCount < 0) throw new IllegalArgumentException("ruleHitCount must not be negative");
        if (totalRuleCount < 0) throw new IllegalArgumentException("totalRuleCount must not be negative");
        if (ruleHitCount > totalRuleCount) throw new IllegalArgumentException("ruleHitCount must not exceed totalRuleCount");
        Objects.requireNonNull(lastDataUpdateAt, "lastDataUpdateAt");
    }

    /** Convenience factory for contexts with no evidence completeness tracking. */
    public static EvaluationContext of(int evidenceCount, Instant lastDataUpdateAt, int ruleHitCount, int totalRuleCount) {
        return new EvaluationContext(evidenceCount, evidenceCount, lastDataUpdateAt, ruleHitCount, totalRuleCount);
    }
}
