package com.gien.gits.knowledge.port;

import com.gien.gits.knowledge.RoutePolicy;
import java.util.Optional;

/**
 * 路由策略读取 Port（CTR-ROUTE-001 消费者：route_policy_evaluator）。
 *
 * <p>契约返回 {@link Optional}：未找到或内容不合法（fail-closed）时返回 {@link Optional#empty()}。</p>
 */
public interface RoutePolicyPort {

    Optional<RoutePolicy> find(String policyId);
}
