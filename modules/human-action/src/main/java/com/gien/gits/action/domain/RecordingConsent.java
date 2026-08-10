package com.gien.gits.action.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * 录音录像同意 — 交互记录的合规授权
 */
public record RecordingConsent(
    String consentId,
    String interactionId,
    String customerId,
    String consentType,       // AUDIO_RECORDING, VIDEO_RECORDING, SCREEN_CAPTURE, TRANSCRIPT
    String status,            // GRANTED, DENIED, WITHDRAWN, PENDING
    String grantedBy,
    String grantedRole,       // RM, CUSTOMER, COMPLIANCE
    Instant grantedAt,
    String withdrawalReason,
    Instant expiresAt,
    String legalBasis) {

    public RecordingConsent {
        Objects.requireNonNull(consentId, "consentId");
        Objects.requireNonNull(interactionId, "interactionId");
        Objects.requireNonNull(consentType, "consentType");
        Objects.requireNonNull(status, "status");
    }

    public RecordingConsent(String consentId, String interactionId, String customerId,
                            String consentType, String status, String grantedBy,
                            String grantedRole, String withdrawalReason,
                            Instant expiresAt, String legalBasis) {
        this(consentId, interactionId, customerId, consentType, status,
             grantedBy, grantedRole, Instant.now(), withdrawalReason, expiresAt, legalBasis);
    }
}
