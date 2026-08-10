package com.gien.gits.action.port;

import com.gien.gits.action.domain.RecordingConsent;
import java.util.List;
import java.util.Optional;

/**
 * 录音录像同意仓储端口
 */
public interface RecordingConsentRepository {
    Optional<RecordingConsent> findByConsentId(String consentId);
    List<RecordingConsent> findByInteractionId(String interactionId);
    Optional<RecordingConsent> findLatestByInteractionId(String interactionId);
    List<RecordingConsent> findByCustomerId(String customerId);
    List<RecordingConsent> findByStatus(String status);
}
