package com.gien.gits.adapter.persistence.foundation.ontology.mapper;

import com.gien.gits.ontology.domain.ClaimLifecycleEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 声明生命周期事件 Mapper — foundation/ontology 层
 */
@Mapper
public interface ClaimLifecycleEventMapper {

    void insert(ClaimLifecycleEvent event);

    List<ClaimLifecycleEvent> findByClaimId(@Param("claimId") String claimId);

    List<ClaimLifecycleEvent> findByActorId(@Param("actorId") String actorId);
}
