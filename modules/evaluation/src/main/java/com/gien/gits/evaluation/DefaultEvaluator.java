package com.gien.gits.evaluation;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Default engineering implementation of {@link EvaluationPort}.
 *
 * <p>Produces deterministic, manifest-derived metrics only. The {@code gateState} is a neutral
 * engineering marker ({@link #GATE_MECHANISM_READY}) and MUST NOT be interpreted as a QA pass,
 * business pass, or any authoritative release decision — those states are reserved for
 * independent actors and are not self-signed by an implementation role.
 *
 * <p>The {@link #score(EvaluationContext)} method implements a rule-based composite scoring
 * algorithm with three dimensions: evidence completeness, data freshness, and rule hit rate.
 */
public final class DefaultEvaluator implements EvaluationPort {

    /** Neutral engineering gate state: the evaluation mechanism ran end-to-end. Not a pass verdict. */
    public static final String GATE_MECHANISM_READY = "MECHANISM_READY";

    /** Placeholder engineering score emitted by the default mechanism; not a business metric. */
    static final double PLACEHOLDER_SCORE = 0.0;

    /** Weight for the evidence dimension in the composite score. */
    static final double EVIDENCE_WEIGHT = 0.4;
    /** Weight for the freshness dimension in the composite score. */
    static final double FRESHNESS_WEIGHT = 0.3;
    /** Weight for the rule-hit dimension in the composite score. */
    static final double RULE_HIT_WEIGHT = 0.3;

    /** Data updated within this threshold is considered fully fresh (score 1.0). */
    static final Duration FRESHNESS_FULL_THRESHOLD = Duration.ofDays(7);
    /** Data updated within this threshold gets partial freshness (score 0.7). */
    static final Duration FRESHNESS_PARTIAL_THRESHOLD = Duration.ofDays(30);
    /** Freshness score for data older than the partial threshold. */
    static final double FRESHNESS_STALE_SCORE = 0.3;

    @Override
    public Result evaluate(RunManifest manifest, String caseSetVersion) {
        Objects.requireNonNull(manifest, "manifest");
        if (caseSetVersion == null || caseSetVersion.isBlank()) {
            throw new IllegalArgumentException("caseSetVersion is required");
        }

        Map<String, Number> metrics = new LinkedHashMap<>();
        metrics.put("ruleVersionCount", manifest.ruleVersions().size());
        metrics.put("placeholderScore", PLACEHOLDER_SCORE);
        return new Result(GATE_MECHANISM_READY, Map.copyOf(metrics));
    }

    @Override
    public EvaluationResult score(EvaluationContext context) {
        Objects.requireNonNull(context, "context");

        double evidenceScore = calculateEvidenceScore(context);
        double freshnessScore = calculateFreshnessScore(context);
        double ruleHitScore = calculateRuleHitScore(context);

        double composite = evidenceScore * EVIDENCE_WEIGHT
                + freshnessScore * FRESHNESS_WEIGHT
                + ruleHitScore * RULE_HIT_WEIGHT;

        return new EvaluationResult(composite, Map.of(
                "evidence", evidenceScore,
                "freshness", freshnessScore,
                "ruleHit", ruleHitScore
        ));
    }

    /**
     * Evidence score based on count and completeness.
     * <ul>
     *   <li>No evidence → 0.0</li>
     *   <li>1-3 items → 0.4</li>
     *   <li>4-6 items → 0.7</li>
     *   <li>7+ items → 1.0</li>
     * </ul>
     * Completeness ratio adjusts the score upward by up to 0.1.
     */
    double calculateEvidenceScore(EvaluationContext context) {
        int count = context.evidenceCount();
        double base;
        if (count == 0) {
            base = 0.0;
        } else if (count <= 3) {
            base = 0.4;
        } else if (count <= 6) {
            base = 0.7;
        } else {
            base = 1.0;
        }

        double completenessRatio = context.evidenceCount() == 0
                ? 0.0
                : (double) context.evidenceCompleteCount() / context.evidenceCount();
        double completenessBonus = completenessRatio * 0.1;

        return Math.min(1.0, base + completenessBonus);
    }

    /**
     * Freshness score based on how recently data was updated.
     * <ul>
     *   <li>Within 7 days → 1.0</li>
     *   <li>Within 30 days → 0.7</li>
     *   <li>Older → 0.3</li>
     * </ul>
     */
    double calculateFreshnessScore(EvaluationContext context) {
        Instant now = Instant.now();
        Duration age = Duration.between(context.lastDataUpdateAt(), now);

        if (age.compareTo(FRESHNESS_FULL_THRESHOLD) <= 0) {
            return 1.0;
        } else if (age.compareTo(FRESHNESS_PARTIAL_THRESHOLD) <= 0) {
            return 0.7;
        } else {
            return FRESHNESS_STALE_SCORE;
        }
    }

    /**
     * Rule-hit score based on the ratio of triggered rules.
     * <ul>
     *   <li>No rules evaluated → 0.5 (neutral)</li>
     *   <li>Hit ratio determines score linearly, with a minimum of 0.1</li>
     * </ul>
     */
    double calculateRuleHitScore(EvaluationContext context) {
        if (context.totalRuleCount() == 0) {
            return 0.5;
        }
        double ratio = (double) context.ruleHitCount() / context.totalRuleCount();
        return Math.max(0.1, Math.min(1.0, ratio));
    }
}
