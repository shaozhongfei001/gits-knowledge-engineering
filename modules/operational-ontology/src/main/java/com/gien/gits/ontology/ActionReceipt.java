package com.gien.gits.ontology;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ActionReceipt(UUID receiptId, UUID actionId, Status status, String targetVersionAfter, String failureCode, Instant receivedAt) {
    public enum Status { SUCCEEDED, FAILED, COMPENSATED }

    public ActionReceipt {
        Objects.requireNonNull(receiptId, "receiptId");
        Objects.requireNonNull(actionId, "actionId");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(receivedAt, "receivedAt");
        if (status == Status.SUCCEEDED && (targetVersionAfter == null || targetVersionAfter.isBlank())) {
            throw new IllegalArgumentException("successful receipt requires targetVersionAfter");
        }
        if (status == Status.FAILED && (failureCode == null || failureCode.isBlank())) {
            throw new IllegalArgumentException("failed receipt requires failureCode");
        }
    }
}
