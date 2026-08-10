package com.gien.gits.ontology.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * 证据版本链接 — 证据版本链的跟踪
 */
public record EvidenceVersionLink(
    String linkId,
    String evidenceId,
    String previousVersionId,
    String nextVersionId,
    int versionNumber,
    String changeType,       // CREATE, UPDATE, CORRECT, AMEND
    String changeReason,
    String changedBy,
    Instant changedAt) {

    public EvidenceVersionLink {
        Objects.requireNonNull(linkId, "linkId");
        Objects.requireNonNull(evidenceId, "evidenceId");
        Objects.requireNonNull(changeType, "changeType");
    }

    public EvidenceVersionLink(String linkId, String evidenceId, String previousVersionId,
                               String nextVersionId, int versionNumber, String changeType,
                               String changeReason, String changedBy) {
        this(linkId, evidenceId, previousVersionId, nextVersionId, versionNumber,
             changeType, changeReason, changedBy, Instant.now());
    }
}
