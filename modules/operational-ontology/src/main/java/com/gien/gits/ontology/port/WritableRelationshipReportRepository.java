package com.gien.gits.ontology.port;

import com.gien.gits.ontology.RelationshipReport;

import java.util.UUID;

/**
 * 可写关系报告仓储端口 — 在 {@link RelationshipReportRepository} 只读契约基础上增加写操作。
 *
 * <p>适配器层的 JDBC 实现类应实现此接口，从而同时满足读/写契约。</p>
 */
public interface WritableRelationshipReportRepository extends RelationshipReportRepository {

    /**
     * 保存关系报告聚合。
     *
     * @param relationshipReport 待保存的关系报告
     */
    void save(RelationshipReport relationshipReport);
}
