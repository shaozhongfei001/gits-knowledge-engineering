package com.gien.gits.ontology.port;

import com.gien.gits.ontology.FactReconciliationCase;
import com.gien.gits.ontology.ReconciliationStatus;

import java.util.List;
import java.util.Optional;

/**
 * 事实对账仓储端口 — 只读操作。
 *
 * <p>定义对 {@link FactReconciliationCase} 聚合的查询契约，由适配器层实现。
 * 写操作见 {@link WritableFactReconciliationRepository}。</p>
 */
public interface FactReconciliationRepository {

    /**
     * 根据对账ID查找事实对账案例。
     *
     * @param reconciliationId 对账唯一标识
     * @return 找到的事实对账案例，若不存在则返回空
     */
    Optional<FactReconciliationCase> findByReconciliationId(String reconciliationId);

    /**
     * 根据运营案例ID查找其关联的所有事实对账案例。
     *
     * @param caseId 运营案例唯一标识
     * @return 关联的事实对账案例列表
     */
    List<FactReconciliationCase> findByCaseId(String caseId);
}
