package com.gien.gits.ontology.port;

import com.gien.gits.ontology.GroupRelationship;

/**
 * 可写集团关系仓储端口 — 在 {@link GroupRelationshipRepository} 只读契约基础上增加写操作。
 *
 * <p>适配器层的 JDBC 实现类应实现此接口，从而同时满足读/写契约。</p>
 */
public interface WritableGroupRelationshipRepository extends GroupRelationshipRepository {

    /**
     * 保存集团关系聚合。
     *
     * @param groupRelationship 待保存的集团关系
     */
    void save(GroupRelationship groupRelationship);
}
