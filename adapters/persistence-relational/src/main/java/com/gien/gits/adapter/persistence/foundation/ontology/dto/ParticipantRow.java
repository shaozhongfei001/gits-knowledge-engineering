package com.gien.gits.adapter.persistence.foundation.ontology.dto;

import com.gien.gits.ontology.Interaction;

import java.util.UUID;

/**
 * interaction_participant 表的行映射 DTO。
 */
public record ParticipantRow(
        UUID interactionId,
        String participantId,
        Interaction.Participant.Role participantRole,
        String displayName) {
}
