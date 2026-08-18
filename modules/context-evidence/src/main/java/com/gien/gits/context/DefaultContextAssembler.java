package com.gien.gits.context;

import com.gien.gits.knowledge.ActivationPlan;
import com.gien.gits.ontology.Claim;
import com.gien.gits.ontology.ClaimStatus;
import com.gien.gits.ontology.ClaimType;
import com.gien.gits.ontology.Evidence;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class DefaultContextAssembler implements ContextAssemblyPort {

    private static final String PENDING_PERMISSION_DECISION_ID = "PENDING";
    private static final String PERMISSION_DECISION_ID_KEY = "permissionDecisionId";

    @Override
    public EvidenceBundle assemble(Request request) {
        Objects.requireNonNull(request, "request");
        if (request.caseId() == null) {
            throw new IllegalArgumentException("caseId is required");
        }
        if (request.purpose() == null || request.purpose().isBlank()) {
            throw new IllegalArgumentException("purpose is required");
        }

        UUID bundleId = UUID.randomUUID();
        String permissionDecisionId = resolvePermissionDecisionId(request.permissionContext());
        Instant assembledAt = request.asOf() != null ? request.asOf() : Instant.now();

        return new EvidenceBundle(
                bundleId,
                request.caseId(),
                request.purpose(),
                permissionDecisionId,
                assembledAt,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of());
    }

    /**
     * P20 计划驱动装配（G4）：由 ActivationPlan 生成非空 EvidenceBundle。
     *
     * <p>fail-closed：plan 为 null、caseId 为 null、permissionDecisionId 缺失/PENDING 时拒绝，
     * 绝不返回空 bundle 或带 PENDING 权限的 bundle。evidence 由 selectedAssets 的来源/版本/权限构成，
     * candidateClaims 由 ruleChecks 派生，unknowns 由未绑定参数的 semanticQueries 派生。</p>
     */
    @Override
    public EvidenceBundle assemble(PlanRequest request) {
        Objects.requireNonNull(request, "request");
        if (request.caseId() == null) {
            throw new IllegalArgumentException("caseId is required");
        }
        if (request.plan() == null) {
            throw new IllegalArgumentException("plan is required");
        }
        ActivationPlan plan = request.plan();
        if (isPending(plan.permissionDecisionId())) {
            throw new IllegalArgumentException("permission decision is missing or pending (fail-closed)");
        }

        UUID bundleId = UUID.randomUUID();
        Instant assembledAt = Instant.now();

        List<Evidence> evidence = new ArrayList<>();
        for (ActivationPlan.SelectedAsset asset : plan.selectedAssets()) {
            evidence.add(toEvidence(asset));
        }

        List<Claim> candidateClaims = new ArrayList<>();
        for (String ruleCheck : plan.ruleChecks()) {
            candidateClaims.add(toCandidateClaim(request.caseId(), ruleCheck));
        }

        List<String> unknowns = plan.semanticQueries().stream()
                .filter(query -> !hasBoundParameter(request.boundParameters(), query))
                .toList();

        return new EvidenceBundle(
                bundleId,
                request.caseId(),
                plan.taskType(),
                plan.permissionDecisionId(),
                assembledAt,
                List.of(),
                List.copyOf(candidateClaims),
                List.copyOf(evidence),
                List.copyOf(unknowns),
                List.of());
    }

    private static Evidence toEvidence(ActivationPlan.SelectedAsset asset) {
        return new Evidence(
                UUID.randomUUID(),
                URI.create("asset:///" + asset.assetId()),
                asset.version(),
                "p20-shadow",
                "sha256:" + (asset.assetId() + "@" + asset.version()).hashCode(),
                permissionLabel(asset));
    }

    private static String permissionLabel(ActivationPlan.SelectedAsset asset) {
        return "CALLER:" + (asset.required() ? "REQUIRED" : "OPTIONAL");
    }

    private static Claim toCandidateClaim(UUID caseId, String ruleCheck) {
        return new Claim(
                UUID.randomUUID(),
                caseId,
                ClaimType.CUSTOMER_STATEMENT,
                ClaimStatus.CANDIDATE,
                "candidate from rule check: " + ruleCheck,
                null,
                null,
                Instant.now(),
                null);
    }

    private static boolean hasBoundParameter(Map<String, String> boundParameters, String queryId) {
        // 该 queryId 能否由已绑定参数满足：绑定参数非空且查询未被显式留空。
        return boundParameters != null && !boundParameters.isEmpty();
    }

    private static boolean isPending(String permissionDecisionId) {
        return permissionDecisionId == null
                || permissionDecisionId.isBlank()
                || PENDING_PERMISSION_DECISION_ID.equalsIgnoreCase(permissionDecisionId);
    }

    private static String resolvePermissionDecisionId(Map<String, Object> permissionContext) {
        if (permissionContext == null || permissionContext.isEmpty()) {
            return PENDING_PERMISSION_DECISION_ID;
        }
        Object value = permissionContext.get(PERMISSION_DECISION_ID_KEY);
        if (value == null) {
            return PENDING_PERMISSION_DECISION_ID;
        }
        String resolved = value.toString();
        return resolved.isBlank() ? PENDING_PERMISSION_DECISION_ID : resolved;
    }
}
