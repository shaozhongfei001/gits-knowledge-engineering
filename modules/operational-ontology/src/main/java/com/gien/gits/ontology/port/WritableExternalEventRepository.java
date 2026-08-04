package com.gien.gits.ontology.port;

import com.gien.gits.ontology.ExternalEvent;

/**
 * 可写外部事件仓储端口 — 在 {@link ExternalEventRepository} 只读契约基础上增加写操作。
 *
 * <p>适配器层的 JDBC 实现类应实现此接口，从而同时满足读/写契约。</p>
 */
public interface WritableExternalEventRepository extends ExternalEventRepository {

    /**
     * 保存外部事件聚合。
     *
     * @param externalEvent 待保存的外部事件
     */
    void save(ExternalEvent externalEvent);
}
