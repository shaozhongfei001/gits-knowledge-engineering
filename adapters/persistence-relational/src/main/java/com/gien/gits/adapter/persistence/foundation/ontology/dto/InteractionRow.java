package com.gien.gits.adapter.persistence.foundation.ontology.dto;

import com.gien.gits.ontology.Channel;
import com.gien.gits.ontology.Interaction;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Interaction 表的扁平行映射 DTO，用于 MyBatis constructor 映射。
 * 在 Service 层组装为 Interaction record。
 */
public record InteractionRow(
        UUID interactionId,
        UUID caseId,
        UUID journeyId,
        Interaction.InteractionType type,
        Interaction.Direction direction,
        Channel channel,
        String initiatorId,
        Interaction.Participant.Role initiatorRole,
        String initiatorDisplayName,
        String contentSummary,
        List<UUID> producedClaimIds,
        Interaction.InteractionOutcome outcome,
        Instant occurredAt,
        Instant endedAt,
        String sourceHash,
        String sourceUri,
        String sourceVersion,
        Instant recordedAt) {
}
