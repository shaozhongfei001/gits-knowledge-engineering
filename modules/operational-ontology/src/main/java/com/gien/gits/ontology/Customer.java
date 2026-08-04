package com.gien.gits.ontology;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * 客户主档 — 客户基本信息与经营标签
 */
public record Customer(
        String customerId,
        String customerName,
        String customerShortName,
        String unifiedSocialCreditCode,
        LocalDate establishedDate,
        long registeredCapitalCny,
        Industry industry,
        String region,
        EnterpriseScale enterpriseScale,
        CustomerTier customerTier,
        LocalDate relationshipSince,
        String rmId,
        String rmName,
        String managingBranch,
        boolean groupFlag,
        ListedStatus listedStatus,
        RiskLevel riskLevel,
        List<String> mainProducts,
        List<String> coreTags,
        String relationshipSummary,
        Instant createdAt,
        Instant updatedAt) {

    public Customer {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("customerId is required");
        }
        if (customerName == null || customerName.isBlank()) {
            throw new IllegalArgumentException("customerName is required");
        }
        mainProducts = List.copyOf(mainProducts != null ? mainProducts : List.of());
        coreTags = List.copyOf(coreTags != null ? coreTags : List.of());
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = createdAt;
    }

    /** 兼容旧构造器（无审计字段，String参数自动转枚举） */
    public Customer(String customerId, String customerName, String customerShortName,
                    String unifiedSocialCreditCode, LocalDate establishedDate, long registeredCapitalCny,
                    String industry, String region, String enterpriseScale, String customerTier,
                    LocalDate relationshipSince, String rmId, String rmName, String managingBranch,
                    boolean groupFlag, String listedStatus, String riskLevel,
                    List<String> mainProducts, List<String> coreTags, String relationshipSummary) {
        this(customerId, customerName, customerShortName, unifiedSocialCreditCode, establishedDate,
             registeredCapitalCny,
             industry != null ? Industry.valueOf(industry) : null, region,
             enterpriseScale != null ? EnterpriseScale.valueOf(enterpriseScale) : null,
             customerTier != null ? CustomerTier.valueOf(customerTier) : null,
             relationshipSince,
             rmId, rmName, managingBranch, groupFlag,
             listedStatus != null ? ListedStatus.valueOf(listedStatus) : null,
             riskLevel != null ? RiskLevel.valueOf(riskLevel) : null,
             mainProducts, coreTags, relationshipSummary, Instant.now(), Instant.now());
    }
}
