package com.gien.gits.adapter.persistence.foundation.action.service;

import com.gien.gits.action.domain.RecordingConsent;
import com.gien.gits.action.port.RecordingConsentRepository;
import com.gien.gits.action.port.WritableRecordingConsentRepository;
import com.gien.gits.adapter.persistence.foundation.action.mapper.RecordingConsentMapper;

import java.util.List;
import java.util.Optional;

/**
 * MyBatis 实现 — 录音授权仓储
 */
public class MyBatisRecordingConsentService implements RecordingConsentRepository, WritableRecordingConsentRepository {

    private final RecordingConsentMapper mapper;

    public MyBatisRecordingConsentService(RecordingConsentMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<RecordingConsent> findByConsentId(String consentId) {
        return mapper.findByConsentId(consentId);
    }

    @Override
    public List<RecordingConsent> findByInteractionId(String interactionId) {
        return mapper.findByInteractionId(interactionId);
    }

    @Override
    public Optional<RecordingConsent> findLatestByInteractionId(String interactionId) {
        return mapper.findLatestByInteractionId(interactionId);
    }

    @Override
    public List<RecordingConsent> findByCustomerId(String customerId) {
        return mapper.findByCustomerId(customerId);
    }

    @Override
    public List<RecordingConsent> findByStatus(String status) {
        return mapper.findByStatus(status);
    }

    @Override
    public void save(RecordingConsent consent) {
        mapper.insert(consent);
    }

    @Override
    public void updateStatus(String consentId, String status, String withdrawalReason) {
        mapper.updateStatus(consentId, status, withdrawalReason);
    }
}
