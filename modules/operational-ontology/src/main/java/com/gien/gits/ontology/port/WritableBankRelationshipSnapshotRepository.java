package com.gien.gits.ontology.port;

import com.gien.gits.ontology.BankRelationshipSnapshot;

/**
 * 可写银行关系快照仓储端口 — 在 {@link BankRelationshipSnapshotRepository} 只读契约基础上增加写操作。
 *
 * <p>适配器层的 JDBC 实现类应实现此接口，从而同时满足读/写契约。</p>
 */
public interface WritableBankRelationshipSnapshotRepository extends BankRelationshipSnapshotRepository {

    /**
     * 保存银行关系快照聚合。
     *
     * @param snapshot 待保存的银行关系快照
     */
    void save(BankRelationshipSnapshot snapshot);
}
