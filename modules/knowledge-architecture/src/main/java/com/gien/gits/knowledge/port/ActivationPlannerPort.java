package com.gien.gits.knowledge.port;

import com.gien.gits.knowledge.plan.ActivationPlanRequest;
import com.gien.gits.knowledge.plan.PlanDecision;

/**
 * 激活计划生成 Port（CTR-PLAN-001 消费者：activation_planner）。
 *
 * <p>由调用方注入路由策略、激活合同、资产清单与知识地图读取 Port，
 * 将双路径（Wiki-first / Ontology-first）汇合为可重放的 {@code ActivationPlan}。
 * 任何未决/未映射/资产未登记情况一律返回 {@code Deny}（fail-closed），不返回部分计划。</p>
 */
public interface ActivationPlannerPort {

    PlanDecision plan(ActivationPlanRequest request);
}
