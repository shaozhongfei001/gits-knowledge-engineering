package com.gien.gits.adapter.persistence.foundation.ontology.dto;

import com.gien.gits.ontology.GroupRelationship;

import java.time.Instant;
import java.util.UUID;

/**
 * Flat DTO for MyBatis row mapping of group_relationship table.
 * Uses wrapper types (Integer) to match MyBatis constructor lookup.
 */
public record GroupRelationshipRow(
        UUID id,
        String groupId,
        String fromEntityId,
        String toEntityId,
        String relationshipType,
        Integer ownershipRatio,
        Instant createdAt) {

    public GroupRelationship toGroupRelationship() {
        return new GroupRelationship(
                id, groupId, fromEntityId, toEntityId, relationshipType,
                ownershipRatio != null ? ownershipRatio : 0,
                createdAt);
    }
}
