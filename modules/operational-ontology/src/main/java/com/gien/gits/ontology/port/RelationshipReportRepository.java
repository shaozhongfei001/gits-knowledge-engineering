package com.gien.gits.ontology.port;

import com.gien.gits.ontology.RelationshipReport;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 关系报告仓储端口 — 只读操作。
 *
 * <p>定义对 {@link RelationshipReport} 聚合的查询契约，由适配器层实现。
 * 写操作见 {@link WritableRelationshipReportRepository}。</p>
 */
public interface RelationshipReportRepository {

    /**
     * 根据报告ID查找关系报告。
     *
     * @param reportId 报告唯一标识
     * @return 找到的关系报告，若不存在则返回空
     */
    Optional<RelationshipReport> findById(UUID reportId);

    /**
     * 根据运营案例ID查找其关联的所有关系报告。
     *
     * @param operatingCaseId 运营案例唯一标识
     * @return 关联的关系报告列表
     */
    List<RelationshipReport> findByOperatingCaseId(String operatingCaseId);

    /**
     * 根据旅程ID查找最新的关系报告。
     *
     * @param journeyId 旅程唯一标识
     * @return 最新的关系报告，若不存在则返回空
     */
    Optional<RelationshipReport> findLatestByJourneyId(String journeyId);
}
