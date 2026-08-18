package com.gien.gits.knowledge.plan;

import java.time.Instant;

/**
 * 激活计划生成请求（调用方输入）。
 *
 * @param taskType              任务类型，例如 PRE_VISIT_PREPARATION
 * @param customerId            客户标识
 * @param role                  调用方角色
 * @param permissionDecisionId  权限决策 ID；缺省或 PENDING 视为未决（fail-closed）
 * @param asOf                  业务时点（用于 trace.createdAt）
 * @param claimRef              可选：事实核对类任务的事实引用
 * @param executionMode         执行模式；PRODUCTION 必须被拒绝（P20 仅 SHADOW）
 * @param writebackRequested    是否请求生产写回；true 必须被拒绝
 */
public record ActivationPlanRequest(
        String taskType,
        String customerId,
        String role,
        String permissionDecisionId,
        Instant asOf,
        String claimRef,
        ExecutionMode executionMode,
        boolean writebackRequested) {

    /** 便捷构造：默认 SHADOW 且不请求写回。 */
    public ActivationPlanRequest(
            String taskType,
            String customerId,
            String role,
            String permissionDecisionId,
            Instant asOf,
            String claimRef) {
        this(taskType, customerId, role, permissionDecisionId, asOf, claimRef, ExecutionMode.SHADOW, false);
    }
}
