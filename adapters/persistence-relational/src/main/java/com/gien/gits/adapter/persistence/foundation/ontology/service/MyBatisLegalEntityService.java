package com.gien.gits.adapter.persistence.foundation.ontology.service;

import com.gien.gits.adapter.persistence.foundation.ontology.mapper.LegalEntityMapper;
import com.gien.gits.ontology.LegalEntity;
import com.gien.gits.ontology.port.WritableLegalEntityRepository;

import java.util.List;
import java.util.Optional;

/**
 * MyBatis 法人实体仓储实现 — foundation/ontology 层
 */
public class MyBatisLegalEntityService implements WritableLegalEntityRepository {

    private final LegalEntityMapper mapper;

    public MyBatisLegalEntityService(LegalEntityMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(LegalEntity legalEntity) {
        mapper.insert(legalEntity);
    }

    @Override
    public Optional<LegalEntity> findByEntityId(String entityId) {
        return mapper.findByEntityId(entityId);
    }

    @Override
    public List<LegalEntity> findByGroupId(String groupId) {
        return mapper.findByGroupId(groupId);
    }
}
