package com.gien.gits.api.dto;

import com.gien.gits.ontology.domain.EvidenceVersionLink;

import java.time.Instant;

/**
 * 证据版本链 DTO
 */
public record EvidenceVersionDto(
        String versionId,
        String evidenceId,
        int version,
        String changeDescription,
        String previousVersionId,
        String createdBy,
        Instant createdAt
) {
    public static EvidenceVersionDto from(EvidenceVersionLink link) {
        return new EvidenceVersionDto(
                link.linkId(), link.evidenceId(), link.versionNumber(),
                link.changeReason(), link.previousVersionId(),
                link.changedBy(), link.changedAt()
        );
    }
}
