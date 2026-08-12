package com.gien.gits.adapter.persistence.foundation.ontology.service;

import com.gien.gits.adapter.persistence.foundation.ontology.mapper.HumanConfirmationMapper;
import com.gien.gits.ontology.HumanConfirmation;

import java.util.Optional;
import java.util.UUID;

/**
 * MyBatis 人工确认仓储实现 — foundation/ontology 层
 */
public class MyBatisHumanConfirmationService {

    private final HumanConfirmationMapper mapper;

    public MyBatisHumanConfirmationService(HumanConfirmationMapper mapper) {
        this.mapper = mapper;
    }

    public void save(HumanConfirmation confirmation) {
        mapper.insert(confirmation);
    }

    public Optional<HumanConfirmation> findByConfirmationId(UUID confirmationId) {
        return mapper.findByConfirmationId(confirmationId);
    }
}
