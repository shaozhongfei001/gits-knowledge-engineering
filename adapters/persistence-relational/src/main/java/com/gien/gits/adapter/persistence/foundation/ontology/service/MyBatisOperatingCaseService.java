package com.gien.gits.adapter.persistence.foundation.ontology.service;

import com.gien.gits.adapter.persistence.foundation.ontology.mapper.OperatingCaseMapper;
import com.gien.gits.ontology.CaseStatus;
import com.gien.gits.ontology.OperatingCase;
import com.gien.gits.ontology.port.WritableOperatingCaseRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * MyBatis 运营案例仓储实现 — foundation/ontology 层
 */
public class MyBatisOperatingCaseService implements WritableOperatingCaseRepository {

    private final OperatingCaseMapper mapper;

    public MyBatisOperatingCaseService(OperatingCaseMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(OperatingCase operatingCase) {
        mapper.insert(operatingCase);
    }

    @Override
    public Optional<OperatingCase> findById(UUID caseId) {
        return mapper.findById(caseId);
    }

    @Override
    public void updateStatus(UUID caseId, CaseStatus status) {
        mapper.updateStatus(caseId, status);
    }
}
