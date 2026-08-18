package com.gien.gits.ontology;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public record Commitment(
        UUID commitmentId,
        String operatingCaseId,
        String journeyId,
        CommitmentType commitmentType,
        String content,
        String owner,
        LocalDate dueDate,
        CommitmentStatus status,
        String evidenceRef,
        Instant createdAt,
        Instant fulfilledAt,
        UUID interactionId,
        UUID customerId,
        String fulfilledDate,
        String assignedTo,
        String verifiedBy,
        Instant recordedAt,
        Instant updatedAt) {

    public enum CommitmentType { CUSTOMER_COMMITMENT, BANK_COMMITMENT }
    public enum CommitmentStatus { OPEN, FULFILLED, OVERDUE, CANCELLED }

    public Commitment {
        Objects.requireNonNull(commitmentId, "commitmentId");
        Objects.requireNonNull(commitmentType, "commitmentType");
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content is required");
        }
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(createdAt, "createdAt");
    }

    /**
     * 兼容旧构造器 — V1.0 数据不包含 V011 新增字段
     */
    public Commitment(UUID commitmentId, String operatingCaseId, String journeyId,
                      CommitmentType commitmentType, String content, String owner,
                      LocalDate dueDate, CommitmentStatus status, String evidenceRef,
                      Instant createdAt, Instant fulfilledAt) {
        this(commitmentId, operatingCaseId, journeyId, commitmentType, content, owner,
             dueDate, status, evidenceRef, createdAt, fulfilledAt,
             null, null, null, null, null, null, null);
    }
}
