package com.gien.gits.adapter.persistence.foundation.ontology.mapper;

import com.gien.gits.ontology.domain.InteractionExtension;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

/**
 * 交互扩展 Mapper — foundation/ontology 层
 */
@Mapper
public interface InteractionExtensionMapper {

    void insert(InteractionExtension extension);

    Optional<InteractionExtension> findByInteractionId(@Param("interactionId") String interactionId);

    Optional<InteractionExtension> findByExtensionId(@Param("extensionId") String extensionId);
}
