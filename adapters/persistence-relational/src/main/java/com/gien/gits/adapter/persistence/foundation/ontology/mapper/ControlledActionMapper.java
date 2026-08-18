package com.gien.gits.adapter.persistence.foundation.ontology.mapper;

import com.gien.gits.ontology.ControlledAction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * 受控行动 Mapper — foundation/ontology 层
 */
@Mapper
public interface ControlledActionMapper {

    void insert(ControlledAction controlledAction);

    Optional<ControlledAction> findByActionId(@Param("actionId") UUID actionId);
}
