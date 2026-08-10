package com.gien.gits.ontology.port;

import com.gien.gits.ontology.domain.ClaimLifecycleEvent;
import java.util.List;

/**
 * 声明生命周期事件仓储端口
 */
public interface ClaimLifecycleRepository {
    List<ClaimLifecycleEvent> findByClaimId(String claimId);
    List<ClaimLifecycleEvent> findByActorId(String actorId);
}
