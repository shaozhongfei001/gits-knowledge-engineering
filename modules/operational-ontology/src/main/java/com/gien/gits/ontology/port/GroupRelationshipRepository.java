package com.gien.gits.ontology.port;

import com.gien.gits.ontology.GroupRelationship;

import java.util.List;

/**
 * 集团关系仓储端口 — 只读操作。
 *
 * <p>定义对 {@link GroupRelationship} 聚合的查询契约，由适配器层实现。
 * 写操作见 {@link WritableGroupRelationshipRepository}。</p>
 */
public interface GroupRelationshipRepository {

    /**
     * 根据集团ID查找集团下所有关系。
     *
     * @param groupId 集团唯一标识
     * @return 该集团下的关系列表
     */
    List<GroupRelationship> findByGroupId(String groupId);
}
