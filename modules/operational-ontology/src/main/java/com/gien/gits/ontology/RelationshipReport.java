package com.gien.gits.ontology;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 关系报告 — 各阶段产出的结构化报告
 */
public record RelationshipReport(
        UUID reportId,
        String operatingCaseId,
        String journeyId,
        ReportType reportType,
        String content,
        List<String> basedOnEvidence,
        List<String> basedOnReconciliations,
        Instant generatedAt,
        UUID supersedesReportId,
        Instant createdAt,
        Instant updatedAt) {

    public enum ReportType { INTERNAL_RELATIONSHIP, CRM_CALL, UPDATED_RELATIONSHIP, NEXT_PREVISIT }

    public RelationshipReport {
        Objects.requireNonNull(reportId, "reportId");
        Objects.requireNonNull(reportType, "reportType");
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content is required");
        }
        Objects.requireNonNull(generatedAt, "generatedAt");
        basedOnEvidence = List.copyOf(basedOnEvidence != null ? basedOnEvidence : List.of());
        basedOnReconciliations = List.copyOf(basedOnReconciliations != null ? basedOnReconciliations : List.of());
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = createdAt;
    }

    /** 兼容旧构造器（无审计字段） */
    public RelationshipReport(UUID reportId, String operatingCaseId, String journeyId,
                              ReportType reportType, String content, List<String> basedOnEvidence,
                              List<String> basedOnReconciliations, Instant generatedAt,
                              UUID supersedesReportId) {
        this(reportId, operatingCaseId, journeyId, reportType, content, basedOnEvidence,
             basedOnReconciliations, generatedAt, supersedesReportId, Instant.now(), Instant.now());
    }
}
