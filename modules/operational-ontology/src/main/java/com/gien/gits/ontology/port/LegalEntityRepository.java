package com.gien.gits.ontology.port;

import com.gien.gits.ontology.LegalEntity;

import java.util.List;
import java.util.Optional;

/**
 * 法律实体仓储端口 — 只读操作。
 *
 * <p>定义对 {@link LegalEntity} 聚合的查询契约，由适配器层实现。
 * 写操作见 {@link WritableLegalEntityRepository}。</p>
 */
public interface LegalEntityRepository {

    /**
     * 根据实体ID查找法律实体。
     *
     * @param entityId 法律实体唯一标识
     * @return 找到的法律实体，若不存在则返回空
     */
    Optional<LegalEntity> findByEntityId(String entityId);

    /**
     * 根据集团ID查找集团下所有法律实体。
     *
     * @param groupId 集团唯一标识
     * @return 该集团下的法律实体列表
     */
    List<LegalEntity> findByGroupId(String groupId);
}
