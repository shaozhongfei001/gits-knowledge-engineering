package com.gien.gits.ontology;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record EvaluationResult(UUID evaluationId, UUID runManifestId, String caseSetVersion, GateState gateState, Map<String, Number> metrics, Instant evaluatedAt) {
    public enum GateState { DEV_SELF_CHECK_PASS, READY_FOR_INDEPENDENT_QA, QA_PASS, BLOCKED }

    public EvaluationResult {
        Objects.requireNonNull(evaluationId, "evaluationId");
        Objects.requireNonNull(runManifestId, "runManifestId");
        Objects.requireNonNull(gateState, "gateState");
        Objects.requireNonNull(evaluatedAt, "evaluatedAt");
        if (caseSetVersion == null || caseSetVersion.isBlank()) {
            throw new IllegalArgumentException("caseSetVersion is required");
        }
        metrics = Map.copyOf(Objects.requireNonNull(metrics, "metrics"));
    }
}
