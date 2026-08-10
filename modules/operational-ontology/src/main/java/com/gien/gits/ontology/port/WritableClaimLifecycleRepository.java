package com.gien.gits.ontology.port;

import com.gien.gits.ontology.domain.ClaimLifecycleEvent;

/**
 * 声明生命周期事件可写仓储端口
 */
public interface WritableClaimLifecycleRepository extends ClaimLifecycleRepository {
    void save(ClaimLifecycleEvent event);
}
