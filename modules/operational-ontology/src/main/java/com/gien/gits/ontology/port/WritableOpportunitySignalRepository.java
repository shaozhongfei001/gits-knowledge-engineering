package com.gien.gits.ontology.port;

import com.gien.gits.ontology.OpportunitySignal;

import java.util.UUID;

/**
 * 可写机会信号仓储端口 — 在 {@link OpportunitySignalRepository} 只读契约基础上增加写操作。
 *
 * <p>适配器层的 JDBC 实现类应实现此接口，从而同时满足读/写契约。</p>
 */
public interface WritableOpportunitySignalRepository extends OpportunitySignalRepository {

    /**
     * 保存机会信号聚合。
     *
     * @param opportunitySignal 待保存的机会信号
     */
    void save(OpportunitySignal opportunitySignal);

    /**
     * 更新机会信号状态。
     *
     * @param signalId 信号唯一标识
     * @param status   目标状态
     */
    void updateStatus(UUID signalId, OpportunitySignal.SignalStatus status);
}
