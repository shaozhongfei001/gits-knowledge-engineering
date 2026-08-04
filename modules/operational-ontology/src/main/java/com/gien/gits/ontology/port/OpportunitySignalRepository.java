package com.gien.gits.ontology.port;

import com.gien.gits.ontology.OpportunitySignal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 机会信号仓储端口 — 只读操作。
 *
 * <p>定义对 {@link OpportunitySignal} 聚合的查询契约，由适配器层实现。
 * 写操作见 {@link WritableOpportunitySignalRepository}。</p>
 */
public interface OpportunitySignalRepository {

    /**
     * 根据信号ID查找机会信号。
     *
     * @param signalId 信号唯一标识
     * @return 找到的机会信号，若不存在则返回空
     */
    Optional<OpportunitySignal> findById(UUID signalId);

    /**
     * 根据运营案例ID查找其关联的所有机会信号。
     *
     * @param operatingCaseId 运营案例唯一标识
     * @return 关联的机会信号列表
     */
    List<OpportunitySignal> findByOperatingCaseId(String operatingCaseId);
}
