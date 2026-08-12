package com.gien.gits.adapter.persistence.foundation.ontology.dto;

import com.gien.gits.ontology.Customer;
import com.gien.gits.ontology.EnterpriseScale;
import com.gien.gits.ontology.Industry;
import com.gien.gits.ontology.CustomerTier;
import com.gien.gits.ontology.ListedStatus;
import com.gien.gits.ontology.RiskLevel;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Customer 表的行映射 DTO，用于 MyBatis constructor 映射。
 * 在 Service 层组装为 Customer record。
 */
public record CustomerRow(
        String customerId,
        String customerName,
        String customerShortName,
        String unifiedSocialCreditCode,
        LocalDate establishedDate,
        Long registeredCapitalCny,
        Industry industry,
        String region,
        EnterpriseScale enterpriseScale,
        CustomerTier customerTier,
        LocalDate relationshipSince,
        String rmId,
        String rmName,
        String managingBranch,
        Boolean groupFlag,
        ListedStatus listedStatus,
        RiskLevel riskLevel,
        List<String> mainProducts,
        List<String> coreTags,
        String relationshipSummary,
        Instant createdAt,
        Instant updatedAt) {

    public Customer toCustomer() {
        return new Customer(
                customerId, customerName, customerShortName, unifiedSocialCreditCode,
                establishedDate, registeredCapitalCny != null ? registeredCapitalCny : 0L,
                industry, region, enterpriseScale, customerTier,
                relationshipSince, rmId, rmName, managingBranch,
                groupFlag != null ? groupFlag : false,
                listedStatus, riskLevel,
                mainProducts, coreTags, relationshipSummary,
                createdAt, updatedAt);
    }
}
