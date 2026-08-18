package com.gien.gits.adapter.persistence.foundation.ontology.mapper;

import com.gien.gits.ontology.HumanConfirmation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * 人工确认 Mapper — foundation/ontology 层
 */
@Mapper
public interface HumanConfirmationMapper {

    void insert(HumanConfirmation confirmation);

    Optional<HumanConfirmation> findByConfirmationId(@Param("confirmationId") UUID confirmationId);
}
