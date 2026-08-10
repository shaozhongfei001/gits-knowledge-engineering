package com.gien.gits.ontology.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * 声明生命周期事件 — Claim状态变更跟踪
 */
public record ClaimLifecycleEvent(
    String eventId,
    String claimId,
    String fromStatus,
    String toStatus,
    String transitionReason,
    String actorId,
    String actorRole,
    Instant transitionedAt) {

    public ClaimLifecycleEvent {
        Objects.requireNonNull(eventId, "eventId");
        Objects.requireNonNull(claimId, "claimId");
        Objects.requireNonNull(toStatus, "toStatus");
    }

    public ClaimLifecycleEvent(String eventId, String claimId, String fromStatus,
                               String toStatus, String transitionReason,
                               String actorId, String actorRole) {
        this(eventId, claimId, fromStatus, toStatus, transitionReason,
             actorId, actorRole, Instant.now());
    }
}
