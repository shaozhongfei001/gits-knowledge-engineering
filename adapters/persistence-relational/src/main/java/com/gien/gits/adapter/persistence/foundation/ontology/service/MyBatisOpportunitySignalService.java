package com.gien.gits.adapter.persistence.foundation.ontology.service;

import com.gien.gits.adapter.persistence.foundation.ontology.mapper.OpportunitySignalMapper;
import com.gien.gits.ontology.OpportunitySignal;
import com.gien.gits.ontology.port.WritableOpportunitySignalRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * MyBatis 机会信号仓储实现 — foundation/ontology 层
 */
public class MyBatisOpportunitySignalService implements WritableOpportunitySignalRepository {

    private final OpportunitySignalMapper mapper;

    public MyBatisOpportunitySignalService(OpportunitySignalMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(OpportunitySignal opportunitySignal) {
        mapper.insert(opportunitySignal);
    }

    @Override
    public void updateStatus(UUID signalId, OpportunitySignal.SignalStatus status) {
        mapper.updateStatus(signalId, status);
    }

    @Override
    public Optional<OpportunitySignal> findById(UUID signalId) {
        return mapper.findById(signalId);
    }

    @Override
    public List<OpportunitySignal> findByOperatingCaseId(String operatingCaseId) {
        return mapper.findByOperatingCaseId(operatingCaseId);
    }
}
