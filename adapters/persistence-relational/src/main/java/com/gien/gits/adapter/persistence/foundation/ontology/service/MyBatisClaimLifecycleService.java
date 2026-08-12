package com.gien.gits.adapter.persistence.foundation.ontology.service;

import com.gien.gits.adapter.persistence.foundation.ontology.mapper.ClaimLifecycleEventMapper;
import com.gien.gits.ontology.domain.ClaimLifecycleEvent;
import com.gien.gits.ontology.port.WritableClaimLifecycleRepository;

import java.util.List;

/**
 * MyBatis 声明生命周期事件仓储实现 — foundation/ontology 层
 */
public class MyBatisClaimLifecycleService implements WritableClaimLifecycleRepository {

    private final ClaimLifecycleEventMapper mapper;

    public MyBatisClaimLifecycleService(ClaimLifecycleEventMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(ClaimLifecycleEvent event) {
        mapper.insert(event);
    }

    @Override
    public List<ClaimLifecycleEvent> findByClaimId(String claimId) {
        return mapper.findByClaimId(claimId);
    }

    @Override
    public List<ClaimLifecycleEvent> findByActorId(String actorId) {
        return mapper.findByActorId(actorId);
    }
}
