package com.gien.gits.adapter.persistence.foundation.ontology.service;

import com.gien.gits.adapter.persistence.foundation.ontology.mapper.KycGapProfileMapper;
import com.gien.gits.ontology.KycGapProfile;
import com.gien.gits.ontology.port.WritableKycGapProfileRepository;

import java.util.List;
import java.util.Optional;

/**
 * MyBatis KYC缺口画像仓储实现 — foundation/ontology 层
 */
public class MyBatisKycGapProfileService implements WritableKycGapProfileRepository {

    private final KycGapProfileMapper mapper;

    public MyBatisKycGapProfileService(KycGapProfileMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(KycGapProfile profile) {
        mapper.insert(profile);
    }

    @Override
    public Optional<KycGapProfile> findByProfileId(String profileId) {
        return mapper.findByProfileId(profileId);
    }

    @Override
    public Optional<KycGapProfile> findByCustomerId(String customerId) {
        return mapper.findByCustomerId(customerId);
    }

    @Override
    public Optional<KycGapProfile> findLatestByCustomerId(String customerId) {
        return mapper.findLatestByCustomerId(customerId);
    }

    @Override
    public List<KycGapProfile> findByRiskImpact(String riskImpact) {
        return List.of();
    }

    @Override
    public List<KycGapProfile> findByEntity(String entity) {
        return mapper.findByEntity(entity);
    }

    @Override
    public List<KycGapProfile> findStale(int daysSinceLastAssessment) {
        return List.of();
    }
}
