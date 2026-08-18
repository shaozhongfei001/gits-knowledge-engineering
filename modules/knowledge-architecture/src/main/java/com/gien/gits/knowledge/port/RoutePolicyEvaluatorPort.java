package com.gien.gits.knowledge.port;

import com.gien.gits.knowledge.route.RouteDecision;
import com.gien.gits.knowledge.plan.ExecutionMode;

/**
 * 路由策略评估 Port（CTR-ROUTE-001 消费者：route_policy_evaluator）。
 *
 * <p>根据任务类型与执行模式，在 Route Policy 中解析唯一合法 route，返回确定性、可解释的
 * {@link RouteDecision}。任务未映射、合同不在 P20、或请求 PRODUCTION 模式时 fail-closed 拒绝。</p>
 */
public interface RoutePolicyEvaluatorPort {

    RouteDecision evaluate(String taskType, ExecutionMode executionMode);
}
