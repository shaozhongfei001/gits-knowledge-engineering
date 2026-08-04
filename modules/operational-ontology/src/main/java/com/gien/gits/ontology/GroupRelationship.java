package com.gien.gits.ontology;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * 集团关系 — 集团内实体间的股权/控制关系
 */
public record GroupRelationship(
        UUID id,
        String groupId,
        String fromEntityId,
        String toEntityId,
        String relationshipType,
        int ownershipRatio,
        Instant createdAt) {

    public GroupRelationship {
        Objects.requireNonNull(id, "id");
        if (groupId == null || groupId.isBlank()) {
            throw new IllegalArgumentException("groupId is required");
        }
        if (fromEntityId == null || fromEntityId.isBlank()) {
            throw new IllegalArgumentException("fromEntityId is required");
        }
        if (toEntityId == null || toEntityId.isBlank()) {
            throw new IllegalArgumentException("toEntityId is required");
        }
        if (createdAt == null) createdAt = Instant.now();
    }

    /** 兼容旧构造器（无审计字段） */
    public GroupRelationship(UUID id, String groupId, String fromEntityId, String toEntityId,
                             String relationshipType, int ownershipRatio) {
        this(id, groupId, fromEntityId, toEntityId, relationshipType, ownershipRatio, Instant.now());
    }
}
