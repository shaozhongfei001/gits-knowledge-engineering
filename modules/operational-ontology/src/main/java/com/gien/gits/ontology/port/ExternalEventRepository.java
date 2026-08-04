package com.gien.gits.ontology.port;

import com.gien.gits.ontology.ExternalEvent;

import java.util.List;
import java.util.Optional;

/**
 * 外部事件仓储端口 — 只读操作。
 *
 * <p>定义对 {@link ExternalEvent} 聚合的查询契约，由适配器层实现。
 * 写操作见 {@link WritableExternalEventRepository}。</p>
 */
public interface ExternalEventRepository {

    /**
     * 根据事件ID查找外部事件。
     *
     * @param eventId 事件唯一标识
     * @return 找到的外部事件，若不存在则返回空
     */
    Optional<ExternalEvent> findByEventId(String eventId);

    /**
     * 根据实体名称查找其关联的所有外部事件。
     *
     * @param entity 实体名称
     * @return 关联的外部事件列表
     */
    List<ExternalEvent> findByEntity(String entity);

    /**
     * 查找最近的外部事件。
     *
     * @param limit 返回条数上限
     * @return 最近的外部事件列表
     */
    List<ExternalEvent> findRecent(int limit);
}
