package com.gien.gits.api.service;

import com.gien.gits.ontology.domain.Opportunity;
import com.gien.gits.ontology.port.WritableOpportunityRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 商机服务 — 销售机会管理
 */
public class OpportunityService {

    private final WritableOpportunityRepository opportunityRepo;

    public OpportunityService(WritableOpportunityRepository opportunityRepo) {
        this.opportunityRepo = Objects.requireNonNull(opportunityRepo);
    }

    public Optional<Opportunity> findById(String opportunityId) {
        return opportunityRepo.findByOpportunityId(opportunityId);
    }

    public List<Opportunity> findByCustomerId(String customerId) {
        return opportunityRepo.findByCustomerId(customerId);
    }

    public List<Opportunity> findByStatus(String status) {
        return opportunityRepo.findByStatus(status);
    }

    public List<Opportunity> findByOpportunityType(String opportunityType) {
        return opportunityRepo.findByOpportunityType(opportunityType);
    }

    public List<Opportunity> findByAssignedTo(String assignedTo) {
        return opportunityRepo.findByAssignedTo(assignedTo);
    }

    public List<Opportunity> findActiveByCustomerId(String customerId) {
        return opportunityRepo.findActiveByCustomerId(customerId);
    }

    public List<Opportunity> findAll() {
        return opportunityRepo.findAll();
    }

    public Opportunity create(Opportunity opportunity) {
        opportunityRepo.save(opportunity);
        return opportunity;
    }

    public Optional<Opportunity> updateStatus(String opportunityId, String status) {
        opportunityRepo.updateStatus(opportunityId, status);
        return opportunityRepo.findByOpportunityId(opportunityId);
    }
}
