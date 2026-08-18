package com.gien.gits.adapter.persistence.foundation.ontology.service;

import com.gien.gits.adapter.persistence.foundation.ontology.mapper.ClaimMapper;
import com.gien.gits.ontology.Claim;
import com.gien.gits.ontology.ClaimStatus;
import com.gien.gits.ontology.port.WritableClaimRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * MyBatis 声明仓储实现 — foundation/ontology 层
 */
public class MyBatisClaimService implements WritableClaimRepository {

    private final ClaimMapper mapper;

    public MyBatisClaimService(ClaimMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(Claim claim) {
        mapper.insert(claim);
    }

    @Override
    public void updateStatus(UUID claimId, ClaimStatus status) {
        mapper.updateStatus(claimId, status);
    }

    @Override
    public Optional<Claim> findById(UUID claimId) {
        return mapper.findById(claimId);
    }

    @Override
    public List<Claim> findByCaseId(UUID caseId) {
        return mapper.findByCaseId(caseId);
    }
}
