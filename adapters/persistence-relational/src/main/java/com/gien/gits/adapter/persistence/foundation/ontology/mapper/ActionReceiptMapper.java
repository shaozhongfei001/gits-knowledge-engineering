package com.gien.gits.adapter.persistence.foundation.ontology.mapper;

import com.gien.gits.ontology.ActionReceipt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * 行动回执 Mapper — foundation/ontology 层
 */
@Mapper
public interface ActionReceiptMapper {

    void insert(ActionReceipt receipt);

    Optional<ActionReceipt> findByActionId(@Param("actionId") UUID actionId);
}
