package com.gien.gits.ontology.port;

import com.gien.gits.ontology.FactReconciliationCase;
import com.gien.gits.ontology.ReconciliationStatus;

/**
 * 可写事实对账仓储端口 — 在 {@link FactReconciliationRepository} 只读契约基础上增加写操作。
 *
 * <p>适配器层的 JDBC 实现类应实现此接口，从而同时满足读/写契约。</p>
 */
public interface WritableFactReconciliationRepository extends FactReconciliationRepository {

    /**
     * 保存事实对账案例聚合。
     *
     * @param factReconciliationCase 待保存的事实对账案例
     */
    void save(FactReconciliationCase factReconciliationCase);

    /**
     * 更新事实对账案例状态。
     *
     * @param reconciliationId 对账唯一标识
     * @param status           目标状态
     */
    void updateStatus(String reconciliationId, ReconciliationStatus status);
}
