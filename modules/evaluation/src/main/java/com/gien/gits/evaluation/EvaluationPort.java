package com.gien.gits.evaluation;

import java.util.Map;

public interface EvaluationPort {
    Result evaluate(RunManifest manifest, String caseSetVersion);

    /**
     * Rule-based evaluation producing a composite score and dimension sub-scores.
     */
    EvaluationResult score(EvaluationContext context);

    record Result(String gateState, Map<String, Number> metrics) {}
}
