package com.gien.gits.adapter.persistence.foundation.ontology.service;

import com.gien.gits.adapter.persistence.foundation.ontology.mapper.OpportunityMapper;
import com.gien.gits.ontology.domain.Opportunity;
import com.gien.gits.ontology.port.WritableOpportunityRepository;

import java.util.List;
import java.util.Optional;

/**
 * MyBatis 商机仓储实现 — foundation/ontology 层
 */
public class MyBatisOpportunityService implements WritableOpportunityRepository {

    private final OpportunityMapper mapper;

    public MyBatisOpportunityService(OpportunityMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(Opportunity opportunity) {
        mapper.insert(opportunity);
    }

    @Override
    public void updateStatus(String opportunityId, String status) {
        mapper.updateStatus(opportunityId, status);
    }

    @Override
    public Optional<Opportunity> findByOpportunityId(String opportunityId) {
        return mapper.findByOpportunityId(opportunityId);
    }

    @Override
    public List<Opportunity> findByCustomerId(String customerId) {
        return mapper.findByCustomerId(customerId);
    }

    @Override
    public List<Opportunity> findByStatus(String status) {
        return mapper.findByStatus(status);
    }

    @Override
    public List<Opportunity> findByOpportunityType(String opportunityType) {
        return mapper.findByOpportunityType(opportunityType);
    }

    @Override
    public List<Opportunity> findByAssignedTo(String assignedTo) {
        return mapper.findByAssignedTo(assignedTo);
    }

    @Override
    public List<Opportunity> findActiveByCustomerId(String customerId) {
        return mapper.findActiveByCustomerId(customerId);
    }

    @Override
    public List<Opportunity> findAll() {
        return mapper.findAll();
    }
}
