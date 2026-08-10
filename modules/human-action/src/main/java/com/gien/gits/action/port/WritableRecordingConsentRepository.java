package com.gien.gits.action.port;

import com.gien.gits.action.domain.RecordingConsent;

/**
 * 录音录像同意可写仓储端口
 */
public interface WritableRecordingConsentRepository extends RecordingConsentRepository {
    void save(RecordingConsent consent);
    void updateStatus(String consentId, String status, String withdrawalReason);
}
