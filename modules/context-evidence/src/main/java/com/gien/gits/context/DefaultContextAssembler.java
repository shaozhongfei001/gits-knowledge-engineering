package com.gien.gits.context;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class DefaultContextAssembler implements ContextAssemblyPort {

    private static final String PENDING_PERMISSION_DECISION_ID = "PENDING";
    private static final String PERMISSION_DECISION_ID_KEY = "permissionDecisionId";

    @Override
    public EvidenceBundle assemble(Request request) {
        Objects.requireNonNull(request, "request");
        if (request.caseId() == null) {
            throw new IllegalArgumentException("caseId is required");
        }
        if (request.purpose() == null || request.purpose().isBlank()) {
            throw new IllegalArgumentException("purpose is required");
        }

        UUID bundleId = UUID.randomUUID();
        String permissionDecisionId = resolvePermissionDecisionId(request.permissionContext());
        Instant assembledAt = request.asOf() != null ? request.asOf() : Instant.now();

        return new EvidenceBundle(
                bundleId,
                request.caseId(),
                request.purpose(),
                permissionDecisionId,
                assembledAt,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of());
    }

    private static String resolvePermissionDecisionId(Map<String, Object> permissionContext) {
        if (permissionContext == null || permissionContext.isEmpty()) {
            return PENDING_PERMISSION_DECISION_ID;
        }
        Object value = permissionContext.get(PERMISSION_DECISION_ID_KEY);
        if (value == null) {
            return PENDING_PERMISSION_DECISION_ID;
        }
        String resolved = value.toString();
        return resolved.isBlank() ? PENDING_PERMISSION_DECISION_ID : resolved;
    }
}
