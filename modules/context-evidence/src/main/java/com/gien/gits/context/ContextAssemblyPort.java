package com.gien.gits.context;

import com.gien.gits.knowledge.ActivationPlan;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public interface ContextAssemblyPort {
    EvidenceBundle assemble(Request request);

    /** P20 计划驱动装配：由 ActivationPlan 生成非空 EvidenceBundle（权限与来源完整）。 */
    EvidenceBundle assemble(PlanRequest request);

    record Request(UUID caseId, String purpose, String identityTokenRef, Map<String, Object> permissionContext, Instant asOf) {}

    /**
     * P20 计划驱动装配请求。carries 已授权的 ActivationPlan 与绑定参数。
     *
     * @param caseId   用例 ID
     * @param plan     已生成的 ActivationPlan（含 permissionDecisionId 与 selectedAssets/semanticQueries/ruleChecks）
     * @param boundParameters 绑定参数（如 customerId）
     */
    record PlanRequest(UUID caseId, ActivationPlan plan, Map<String, String> boundParameters) {}
}
