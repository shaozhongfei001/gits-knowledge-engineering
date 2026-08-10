package com.gien.gits.ontology.port;

import com.gien.gits.ontology.KycGapProfile;
import java.util.List;
import java.util.Optional;

/**
 * KYC差距画像仓储端口
 */
public interface KycGapProfileRepository {
    Optional<KycGapProfile> findByProfileId(String profileId);
    Optional<KycGapProfile> findByCustomerId(String customerId);
    Optional<KycGapProfile> findLatestByCustomerId(String customerId);
    List<KycGapProfile> findByRiskImpact(String riskImpact);
    List<KycGapProfile> findByEntity(String entity);
    List<KycGapProfile> findStale(int daysSinceLastAssessment);
}
