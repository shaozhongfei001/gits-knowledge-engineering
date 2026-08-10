package com.gien.gits.ontology.port;

import com.gien.gits.ontology.domain.InteractionExtension;
import java.util.Optional;

/**
 * 交互扩展仓储端口
 */
public interface InteractionExtensionRepository {
    Optional<InteractionExtension> findByInteractionId(String interactionId);
}
