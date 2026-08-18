package com.gien.gits.adapter.persistence.foundation.action.mapper;

import com.gien.gits.action.domain.RecordingConsent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 录音授权 Mapper — foundation/action 层
 */
@Mapper
public interface RecordingConsentMapper {

    void insert(RecordingConsent consent);

    Optional<RecordingConsent> findByConsentId(@Param("consentId") String consentId);

    List<RecordingConsent> findByInteractionId(@Param("interactionId") String interactionId);

    Optional<RecordingConsent> findLatestByInteractionId(@Param("interactionId") String interactionId);

    List<RecordingConsent> findByCustomerId(@Param("customerId") String customerId);

    List<RecordingConsent> findByStatus(@Param("status") String status);

    void updateStatus(@Param("consentId") String consentId,
                      @Param("status") String status,
                      @Param("withdrawalReason") String withdrawalReason);
}
