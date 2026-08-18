package com.gien.gits.adapter.persistence.foundation.ontology.service;

import com.gien.gits.adapter.persistence.foundation.ontology.mapper.ControlledActionMapper;
import com.gien.gits.ontology.ControlledAction;

import java.util.Optional;
import java.util.UUID;

/**
 * MyBatis 受控行动仓储实现 — foundation/ontology 层
 */
public class MyBatisControlledActionService {

    private final ControlledActionMapper mapper;

    public MyBatisControlledActionService(ControlledActionMapper mapper) {
        this.mapper = mapper;
    }

    public void save(ControlledAction action) {
        mapper.insert(action);
    }

    public Optional<ControlledAction> findByActionId(UUID actionId) {
        return mapper.findByActionId(actionId);
    }
}
