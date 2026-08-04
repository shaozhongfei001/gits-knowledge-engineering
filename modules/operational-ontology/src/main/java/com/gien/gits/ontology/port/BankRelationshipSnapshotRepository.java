package com.gien.gits.ontology.port;

import com.gien.gits.ontology.BankRelationshipSnapshot;

import java.util.Optional;

/**
 * 银行关系快照仓储端口 — 只读操作。
 *
 * <p>定义对 {@link BankRelationshipSnapshot} 聚合的查询契约，由适配器层实现。
 * 写操作见 {@link WritableBankRelationshipSnapshotRepository}。</p>
 */
public interface BankRelationshipSnapshotRepository {

    /**
     * 根据客户ID查找最新的银行关系快照。
     *
     * @param customerId 客户唯一标识
     * @return 最新的银行关系快照，若不存在则返回空
     */
    Optional<BankRelationshipSnapshot> findLatestByCustomerId(String customerId);
}
