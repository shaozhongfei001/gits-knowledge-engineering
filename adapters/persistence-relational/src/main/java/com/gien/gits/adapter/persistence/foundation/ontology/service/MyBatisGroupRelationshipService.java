package com.gien.gits.adapter.persistence.foundation.ontology.service;

import com.gien.gits.adapter.persistence.foundation.ontology.dto.GroupRelationshipRow;
import com.gien.gits.adapter.persistence.foundation.ontology.mapper.GroupRelationshipMapper;
import com.gien.gits.ontology.GroupRelationship;
import com.gien.gits.ontology.port.WritableGroupRelationshipRepository;

import java.util.List;

/**
 * MyBatis 集团关系仓储实现 — foundation/ontology 层
 */
public class MyBatisGroupRelationshipService implements WritableGroupRelationshipRepository {

    private final GroupRelationshipMapper mapper;

    public MyBatisGroupRelationshipService(GroupRelationshipMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(GroupRelationship groupRelationship) {
        mapper.insert(groupRelationship);
    }

    @Override
    public List<GroupRelationship> findByGroupId(String groupId) {
        return mapper.findRowsByGroupId(groupId).stream()
                .map(GroupRelationshipRow::toGroupRelationship)
                .toList();
    }
}
