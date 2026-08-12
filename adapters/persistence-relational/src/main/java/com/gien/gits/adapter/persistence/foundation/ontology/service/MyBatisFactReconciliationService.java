package com.gien.gits.adapter.persistence.foundation.ontology.service;

import com.gien.gits.adapter.persistence.foundation.ontology.mapper.FactReconciliationCaseMapper;
import com.gien.gits.ontology.FactReconciliationCase;
import com.gien.gits.ontology.ReconciliationStatus;
import com.gien.gits.ontology.port.WritableFactReconciliationRepository;

import java.util.List;
import java.util.Optional;

/**
 * MyBatis 事实对账案例仓储实现 — foundation/ontology 层
 */
public class MyBatisFactReconciliationService implements WritableFactReconciliationRepository {

    private final FactReconciliationCaseMapper mapper;

    public MyBatisFactReconciliationService(FactReconciliationCaseMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(FactReconciliationCase factReconciliationCase) {
        mapper.insert(factReconciliationCase);
    }

    @Override
    public Optional<FactReconciliationCase> findByReconciliationId(String reconciliationId) {
        return mapper.findByReconciliationId(reconciliationId);
    }

    @Override
    public List<FactReconciliationCase> findByCaseId(String caseId) {
        return mapper.findByCaseId(caseId);
    }

    @Override
    public void updateStatus(String reconciliationId, ReconciliationStatus status) {
        mapper.updateStatus(reconciliationId, status);
    }
}
