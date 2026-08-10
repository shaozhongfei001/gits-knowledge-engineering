package com.gien.gits.api.service;

import com.gien.gits.ontology.Commitment;
import com.gien.gits.ontology.port.WritableCommitmentRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 承诺服务 — 承诺跟踪与状态管理
 */
public class CommitmentService {

    private final WritableCommitmentRepository commitmentRepo;

    public CommitmentService(WritableCommitmentRepository commitmentRepo) {
        this.commitmentRepo = Objects.requireNonNull(commitmentRepo);
    }

    public Optional<Commitment> findById(String commitmentId) {
        return commitmentRepo.findByCommitmentId(commitmentId);
    }

    public List<Commitment> findByInteractionId(String interactionId) {
        return commitmentRepo.findByInteractionId(interactionId);
    }

    public List<Commitment> findByCustomerId(String customerId) {
        return commitmentRepo.findByCustomerId(customerId);
    }

    public List<Commitment> findByStatus(String status) {
        return commitmentRepo.findByStatus(status);
    }

    public List<Commitment> findByCommitmentType(String commitmentType) {
        return commitmentRepo.findByCommitmentType(commitmentType);
    }

    public List<Commitment> findOverdue() {
        return commitmentRepo.findOverdue();
    }

    public List<Commitment> findAll() {
        return commitmentRepo.findAll();
    }

    public Commitment create(Commitment commitment) {
        commitmentRepo.save(commitment);
        return commitment;
    }

    public Optional<Commitment> updateStatus(String commitmentId, String status, String verifiedBy) {
        commitmentRepo.updateStatus(commitmentId, status, verifiedBy);
        return commitmentRepo.findByCommitmentId(commitmentId);
    }
}
