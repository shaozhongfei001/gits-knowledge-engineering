package com.gien.gits.adapter.persistence.foundation.ontology.mapper;

import com.gien.gits.ontology.LegalEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 法人实体 Mapper — foundation/ontology 层
 */
@Mapper
public interface LegalEntityMapper {

    void insert(LegalEntity legalEntity);

    Optional<LegalEntity> findByEntityId(@Param("entityId") String entityId);

    List<LegalEntity> findByGroupId(@Param("groupId") String groupId);
}
