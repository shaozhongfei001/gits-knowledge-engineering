package com.gien.gits.ontology.port;

import com.gien.gits.ontology.ExternalEvent;

/**
 * 外部事件可写仓储端口
 */
public interface WritableExternalEventRepository extends ExternalEventRepository {
    void save(ExternalEvent event);
}
