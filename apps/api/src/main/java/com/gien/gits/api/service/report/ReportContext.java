package com.gien.gits.api.service.report;

import com.gien.gits.engagement.CustomerOperatingView;
import com.gien.gits.engagement.PostvisitAnalysisContent;
import com.gien.gits.ontology.RelationshipReport;

import java.util.Optional;
import java.util.UUID;

/**
 * 报告生成上下文 — 封装所有报告生成所需的输入数据
 */
public record ReportContext(
    String operatingCaseId,
    String journeyId,
    String customerId,
    PostvisitAnalysisContent analysis,
    String newEvidenceDescription,
    UUID previousReportId,
    Optional<RelationshipReport> previousReport,
    Optional<CustomerOperatingView> customerView) {

    public static ReportContext forPostvisit(
            String operatingCaseId, String journeyId,
            String customerId,
            PostvisitAnalysisContent analysis,
            Optional<CustomerOperatingView> customerView) {
        return new ReportContext(operatingCaseId, journeyId, customerId, analysis, null, null, Optional.empty(), customerView);
    }

    public static ReportContext forUpdatedReport(
            String operatingCaseId, String journeyId,
            String customerId,
            String newEvidenceDescription, UUID previousReportId,
            Optional<RelationshipReport> previousReport,
            Optional<CustomerOperatingView> customerView) {
        return new ReportContext(operatingCaseId, journeyId, customerId, null,
            newEvidenceDescription, previousReportId, previousReport, customerView);
    }

    public static ReportContext forNextPrevisit(
            String operatingCaseId, String journeyId,
            String customerId,
            PostvisitAnalysisContent previousAnalysis, UUID previousReportId,
            Optional<CustomerOperatingView> customerView) {
        return new ReportContext(operatingCaseId, journeyId, customerId, previousAnalysis,
            null, previousReportId, Optional.empty(), customerView);
    }
}
