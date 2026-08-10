package com.gien.gits.ontology.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * 交互扩展 — Interaction与新增实体的关联
 */
public record InteractionExtension(
    String extensionId,
    String interactionId,
    String recordingConsentId,
    List<String> commitmentIds,
    List<String> taskIds,
    List<String> opportunityIds,
    String kycGapProfileId,
    Instant createdAt,
    Instant updatedAt) {

    public InteractionExtension {
        Objects.requireNonNull(extensionId, "extensionId");
        Objects.requireNonNull(interactionId, "interactionId");
        commitmentIds = List.copyOf(commitmentIds != null ? commitmentIds : List.of());
        taskIds = List.copyOf(taskIds != null ? taskIds : List.of());
        opportunityIds = List.copyOf(opportunityIds != null ? opportunityIds : List.of());
    }

    public InteractionExtension(String extensionId, String interactionId,
                                String recordingConsentId, List<String> commitmentIds,
                                List<String> taskIds, List<String> opportunityIds,
                                String kycGapProfileId) {
        this(extensionId, interactionId, recordingConsentId, commitmentIds,
             taskIds, opportunityIds, kycGapProfileId, Instant.now(), Instant.now());
    }
}
