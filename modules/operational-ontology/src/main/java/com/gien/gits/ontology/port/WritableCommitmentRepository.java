package com.gien.gits.ontology.port;

import com.gien.gits.ontology.Commitment;

import java.util.UUID;

/**
 * 可写承诺仓储端口 — 在 {@link CommitmentRepository} 只读契约基础上增加写操作。
 *
 * <p>适配器层的 JDBC 实现类应实现此接口，从而同时满足读/写契约。</p>
 */
public interface WritableCommitmentRepository extends CommitmentRepository {

    /**
     * 保存承诺聚合。
     *
     * @param commitment 待保存的承诺
     */
    void save(Commitment commitment);

    /**
     * 更新承诺状态。
     *
     * @param commitmentId 承诺唯一标识
     * @param status       目标状态
     */
    void updateStatus(UUID commitmentId, Commitment.CommitmentStatus status);
}
