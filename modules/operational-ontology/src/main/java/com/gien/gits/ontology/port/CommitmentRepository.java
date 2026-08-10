package com.gien.gits.ontology.port;

import com.gien.gits.ontology.Commitment;
import java.util.List;
import java.util.Optional;

/**
 * 承诺仓储端口
 */
public interface CommitmentRepository {
    Optional<Commitment> findByCommitmentId(String commitmentId);
    List<Commitment> findByInteractionId(String interactionId);
    List<Commitment> findByCustomerId(String customerId);
    List<Commitment> findByStatus(String status);
    List<Commitment> findByCommitmentType(String commitmentType);
    List<Commitment> findByOperatingCaseId(String operatingCaseId);
    List<Commitment> findOverdue();

    List<Commitment> findAll();
}
