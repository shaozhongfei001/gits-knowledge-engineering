package com.gien.gits.engagement;

import java.util.List;
import java.util.Objects;

/**
 * 访前报告 — R1格式
 * 包含客户概览、KYC缺口、产品方案、访前建议
 */
public record PrevisitReportContent(
        String reportId,
        String customerId,
        String customerName,
        String rmName,
        String visitObjective,
        CustomerOverview customerOverview,
        KycGapSummary kycGapSummary,
        List<ProductScheme> productSchemes,
        List<String> keyQuestions,
        List<String> riskReminders,
        String visitStrategy) {

    public PrevisitReportContent {
        Objects.requireNonNull(reportId, "reportId");
        productSchemes = List.copyOf(productSchemes != null ? productSchemes : List.of());
        keyQuestions = List.copyOf(keyQuestions != null ? keyQuestions : List.of());
        riskReminders = List.copyOf(riskReminders != null ? riskReminders : List.of());
    }

    public record CustomerOverview(
            String industry,
            String enterpriseScale,
            String customerTier,
            long registeredCapitalCny,
            String riskLevel,
            String relationshipSummary) {}

    public record KycGapSummary(
            List<String> knownItems,
            List<String> partialKnownItems,
            List<String> unknownItems,
            List<String> priorityQuestions) {}

    public record ProductScheme(
            String productId,
            String productName,
            String matchReason,
            String suggestedAmount,
            String suggestedTerm,
            List<String> keyConditions,
            List<String> requiredMaterials,
            List<String> riskPoints) {}
}
