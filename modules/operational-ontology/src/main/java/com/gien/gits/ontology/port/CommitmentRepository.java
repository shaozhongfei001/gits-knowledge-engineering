package com.gien.gits.ontology.port;

import com.gien.gits.ontology.Commitment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 承诺仓储端口 — 只读操作。
 *
 * <p>定义对 {@link Commitment} 聚合的查询契约，由适配器层实现。
 * 写操作见 {@link WritableCommitmentRepository}。</p>
 */
public interface CommitmentRepository {

    /**
     * 根据承诺ID查找承诺。
     *
     * @param commitmentId 承诺唯一标识
     * @return 找到的承诺，若不存在则返回空
     */
    Optional<Commitment> findById(UUID commitmentId);

    /**
     * 根据运营案例ID查找其关联的所有承诺。
     *
     * @param operatingCaseId 运营案例唯一标识
     * @return 关联的承诺列表
     */
    List<Commitment> findByOperatingCaseId(String operatingCaseId);
}
