package com.gien.gits.ontology.port;

import com.gien.gits.ontology.domain.InteractionExtension;

/**
 * 交互扩展可写仓储端口
 */
public interface WritableInteractionExtensionRepository extends InteractionExtensionRepository {
    void save(InteractionExtension extension);
}
