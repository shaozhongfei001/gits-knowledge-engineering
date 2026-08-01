package com.gien.gits.evaluation;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record RunManifest(UUID runId, Instant startedAt, String ontologyVersion, String skillVersion, String promptVersion,
        ModelVersion model, List<String> ruleVersions, String dataSnapshot, String permissionDecisionId, String traceId) {
    public record ModelVersion(String provider, String modelId, String parametersHash) {}

    public RunManifest {
        Objects.requireNonNull(runId, "runId");
        Objects.requireNonNull(startedAt, "startedAt");
        Objects.requireNonNull(model, "model");
        ruleVersions = List.copyOf(ruleVersions);
        for (String value : List.of(ontologyVersion, skillVersion, promptVersion, dataSnapshot, permissionDecisionId, traceId)) {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("all artifact versions and trace identity are required");
            }
        }
    }
}
