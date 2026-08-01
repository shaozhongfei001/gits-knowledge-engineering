package com.gientech.hzb.kno.ontology;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record ControlledAction(
        UUID actionId,
        UUID proposalId,
        HumanConfirmation confirmation,
        Target target,
        String idempotencyKey,
        Instant requestedAt,
        Status status) {

    public enum Status { REQUESTED, DISPATCHED, SUCCEEDED, FAILED, COMPENSATION_REQUIRED, COMPENSATED }

    public record Target(String system, String objectType, String objectId, String expectedVersion, Operation operation, Map<String, Object> payload) {
        public enum Operation { CREATE_TASK, UPDATE_WHITELISTED_FIELDS }

        public Target {
            if (system == null || system.isBlank() || objectType == null || objectType.isBlank()
                    || objectId == null || objectId.isBlank() || expectedVersion == null || expectedVersion.isBlank()) {
                throw new IllegalArgumentException("target identity and expectedVersion are required");
            }
            Objects.requireNonNull(operation, "operation");
            payload = Map.copyOf(Objects.requireNonNull(payload, "payload"));
        }
    }

    public ControlledAction {
        Objects.requireNonNull(actionId, "actionId");
        Objects.requireNonNull(proposalId, "proposalId");
        Objects.requireNonNull(confirmation, "confirmation");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(requestedAt, "requestedAt");
        Objects.requireNonNull(status, "status");
        if (!confirmation.authorizesAction()) {
            throw new IllegalArgumentException("an approving human confirmation is required");
        }
        if (idempotencyKey == null || idempotencyKey.length() < 16) {
            throw new IllegalArgumentException("idempotencyKey must contain at least 16 characters");
        }
    }
}
