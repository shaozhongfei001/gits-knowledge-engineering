package com.gien.gits.action.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * 任务 — 跟进任务/行动项的跟踪
 */
public record Task(
    String taskId,
    String interactionId,
    String customerId,
    String operatingCaseId,
    String taskType,          // FOLLOW_UP, DOCUMENT_COLLECTION, COMPLIANCE_CHECK, CREDIT_REVIEW, CUSTOMER_VISIT
    String title,
    String description,
    String status,            // TODO, IN_PROGRESS, DONE, CANCELLED, OVERDUE
    String priority,          // URGENT, HIGH, MEDIUM, LOW
    String assignedTo,
    String assignedRole,
    String dueDate,
    String completedDate,
    List<String> tags,
    String parentTaskId,
    Instant createdAt,
    Instant updatedAt) {

    public Task {
        Objects.requireNonNull(taskId, "taskId");
        Objects.requireNonNull(taskType, "taskType");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(status, "status");
        tags = List.copyOf(tags != null ? tags : List.of());
    }

    public Task(String taskId, String interactionId, String customerId,
                String operatingCaseId, String taskType, String title,
                String description, String status, String priority,
                String assignedTo, String assignedRole, String dueDate,
                String completedDate, List<String> tags, String parentTaskId) {
        this(taskId, interactionId, customerId, operatingCaseId, taskType, title,
             description, status, priority, assignedTo, assignedRole, dueDate,
             completedDate, tags, parentTaskId, Instant.now(), Instant.now());
    }
}
