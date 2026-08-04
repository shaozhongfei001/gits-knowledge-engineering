package com.gien.gits.ontology;

import java.time.Instant;

/**
 * 法人实体 — 集团内关联主体
 */
public record LegalEntity(
        String entityId,
        String groupId,
        String name,
        String role,
        String ownership,
        String bankCustomerId,
        String relationshipStatus,
        String evidenceRef,
        Instant createdAt) {

    public LegalEntity {
        if (entityId == null || entityId.isBlank()) {
            throw new IllegalArgumentException("entityId is required");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (createdAt == null) createdAt = Instant.now();
    }

    /** 兼容旧构造器（无审计字段） */
    public LegalEntity(String entityId, String groupId, String name, String role,
                       String ownership, String bankCustomerId, String relationshipStatus,
                       String evidenceRef) {
        this(entityId, groupId, name, role, ownership, bankCustomerId, relationshipStatus,
             evidenceRef, Instant.now());
    }
}
