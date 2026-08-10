package com.gien.gits.ontology.port;

import com.gien.gits.ontology.Commitment;

/**
 * 承诺可写仓储端口
 */
public interface WritableCommitmentRepository extends CommitmentRepository {
    void save(Commitment commitment);
    void updateStatus(String commitmentId, String status, String verifiedBy);
}
