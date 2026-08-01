package com.gien.gits.evaluation;

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
 */
public final class DefaultEvaluator implements EvaluationPort {

    /** Neutral engineering gate state: the evaluation mechanism ran end-to-end. Not a pass verdict. */
    public static final String GATE_MECHANISM_READY = "MECHANISM_READY";

    /** Placeholder engineering score emitted by the default mechanism; not a business metric. */
    static final double PLACEHOLDER_SCORE = 0.0;

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
}
