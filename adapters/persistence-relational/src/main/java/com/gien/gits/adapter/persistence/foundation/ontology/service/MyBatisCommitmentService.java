package com.gien.gits.adapter.persistence.foundation.ontology.service;

import com.gien.gits.adapter.persistence.foundation.ontology.mapper.CommitmentMapper;
import com.gien.gits.ontology.Commitment;
import com.gien.gits.ontology.port.WritableCommitmentRepository;

import java.util.List;
import java.util.Optional;

/**
 * MyBatis 承诺仓储实现 — foundation/ontology 层
 */
public class MyBatisCommitmentService implements WritableCommitmentRepository {

    private final CommitmentMapper mapper;

    public MyBatisCommitmentService(CommitmentMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(Commitment commitment) {
        mapper.insert(commitment);
    }

    @Override
    public void updateStatus(String commitmentId, String status, String verifiedBy) {
        mapper.updateStatus(commitmentId, status, verifiedBy);
    }

    @Override
    public Optional<Commitment> findByCommitmentId(String commitmentId) {
        return mapper.findByCommitmentId(commitmentId);
    }

    @Override
    public List<Commitment> findByInteractionId(String interactionId) {
        return mapper.findByInteractionId(interactionId);
    }

    @Override
    public List<Commitment> findByCustomerId(String customerId) {
        return mapper.findByCustomerId(customerId);
    }

    @Override
    public List<Commitment> findByStatus(String status) {
        return mapper.findByStatus(status);
    }

    @Override
    public List<Commitment> findByCommitmentType(String commitmentType) {
        return mapper.findByCommitmentType(commitmentType);
    }

    @Override
    public List<Commitment> findByOperatingCaseId(String operatingCaseId) {
        return mapper.findByOperatingCaseId(operatingCaseId);
    }

    @Override
    public List<Commitment> findOverdue() {
        return mapper.findOverdue();
    }

    @Override
    public List<Commitment> findAll() {
        return mapper.findAll();
    }
}
