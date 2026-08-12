package com.gien.gits.adapter.persistence.foundation.ontology.service;

import com.gien.gits.adapter.persistence.foundation.ontology.mapper.ActionReceiptMapper;
import com.gien.gits.ontology.ActionReceipt;

import java.util.Optional;
import java.util.UUID;

/**
 * MyBatis 行动回执仓储实现 — foundation/ontology 层
 */
public class MyBatisActionReceiptService {

    private final ActionReceiptMapper mapper;

    public MyBatisActionReceiptService(ActionReceiptMapper mapper) {
        this.mapper = mapper;
    }

    public void save(ActionReceipt receipt) {
        mapper.insert(receipt);
    }

    public Optional<ActionReceipt> findByActionId(UUID actionId) {
        return mapper.findByActionId(actionId);
    }
}
