package com.gien.gits.api.service;

import com.gien.gits.action.domain.RecordingConsent;
import com.gien.gits.action.port.WritableRecordingConsentRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 录音录像同意服务 — 交互记录合规授权管理
 */
public class RecordingConsentService {

    private final WritableRecordingConsentRepository consentRepo;

    public RecordingConsentService(WritableRecordingConsentRepository consentRepo) {
        this.consentRepo = Objects.requireNonNull(consentRepo);
    }

    public Optional<RecordingConsent> findByConsentId(String consentId) {
        return consentRepo.findByConsentId(consentId);
    }

    public List<RecordingConsent> findByInteractionId(String interactionId) {
        return consentRepo.findByInteractionId(interactionId);
    }

    public Optional<RecordingConsent> findLatestByInteractionId(String interactionId) {
        return consentRepo.findLatestByInteractionId(interactionId);
    }

    public List<RecordingConsent> findByCustomerId(String customerId) {
        return consentRepo.findByCustomerId(customerId);
    }

    public List<RecordingConsent> findByStatus(String status) {
        return consentRepo.findByStatus(status);
    }

    public RecordingConsent create(RecordingConsent consent) {
        consentRepo.save(consent);
        return consent;
    }

    public Optional<RecordingConsent> updateStatus(String consentId, String status, String withdrawalReason) {
        consentRepo.updateStatus(consentId, status, withdrawalReason);
        return consentRepo.findByConsentId(consentId);
    }
}
