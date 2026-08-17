package com.gien.gits.api.service;

import com.gien.gits.engagement.port.LlmClient;
import com.gien.gits.ontology.port.WritableCommitmentRepository;
import com.gien.gits.ontology.port.WritableFactReconciliationRepository;
import com.gien.gits.ontology.port.WritableOpportunitySignalRepository;
import com.gien.gits.ontology.port.WritableRelationshipReportRepository;
import com.gien.gits.api.service.report.*;
import com.gien.gits.engagement.*;
import com.gien.gits.ontology.RelationshipReport;

import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 报告生成服务 — 委托策略模式实现不同报告类型
 * R5A(内部关系) / R5B(CRM通话) / R7(更新关系) / R8(下次访前)
 * CRM回写命令委托给 CrmWritebackService
 * P9 Loop G6: 注入CustomerOperatingViewService，从客户经营视图动态组装报告
 * P11 G6: 注入LlmClient，策略先尝试LLM生成，失败fallback到模板逻辑
 */
public class ReportGenerationService {

    private final WritableRelationshipReportRepository reportRepo;
    private final CrmWritebackService crmWritebackService;
    private final CustomerOperatingViewService customerOperatingViewService;
    private final Map<RelationshipReport.ReportType, ReportStrategy> strategies;

    public ReportGenerationService(
            WritableRelationshipReportRepository reportRepo,
            WritableCommitmentRepository commitmentRepo,
            WritableFactReconciliationRepository factRecRepo,
            WritableOpportunitySignalRepository signalRepo,
            CustomerContextService customerContextService,
            ContextInheritanceService contextInheritanceService,
            CustomerOperatingViewService customerOperatingViewService,
            CrmWritebackService crmWritebackService,
            LlmClient llmClient) {
        this.reportRepo = Objects.requireNonNull(reportRepo);
        this.crmWritebackService = Objects.requireNonNull(crmWritebackService);
        this.customerOperatingViewService = Objects.requireNonNull(customerOperatingViewService);

        // 注册策略 — P11 G6: 所有策略注入LlmClient
        this.strategies = new EnumMap<>(RelationshipReport.ReportType.class);
        this.strategies.put(RelationshipReport.ReportType.INTERNAL_RELATIONSHIP,
            new InternalRelationshipReportStrategy(llmClient));
        this.strategies.put(RelationshipReport.ReportType.CRM_CALL,
            new CrmCallReportStrategy(llmClient));
        this.strategies.put(RelationshipReport.ReportType.UPDATED_RELATIONSHIP,
            new UpdatedRelationshipReportStrategy(llmClient));
        this.strategies.put(RelationshipReport.ReportType.NEXT_PREVISIT,
            new NextPrevisitReportStrategy(contextInheritanceService, llmClient));
    }

    /**
     * 生成内部关系报告 (R5A) — 基于访后分析
     */
    @Transactional
    public RelationshipReport generateInternalRelationshipReport(
            String operatingCaseId, String journeyId,
            String customerId,
            PostvisitAnalysisContent analysis) {
        Optional<CustomerOperatingView> view = buildView(customerId, operatingCaseId);
        ReportContext ctx = ReportContext.forPostvisit(operatingCaseId, journeyId, customerId, analysis, view);
        RelationshipReport report = getStrategy(RelationshipReport.ReportType.INTERNAL_RELATIONSHIP).generate(ctx);
        reportRepo.save(report);
        return report;
    }

    /**
     * 生成CRM通话报告 (R5B) — 结构化CRM数据
     */
    @Transactional
    public RelationshipReport generateCrmCallReport(
            String operatingCaseId, String journeyId,
            String customerId,
            PostvisitAnalysisContent analysis) {
        Optional<CustomerOperatingView> view = buildView(customerId, operatingCaseId);
        ReportContext ctx = ReportContext.forPostvisit(operatingCaseId, journeyId, customerId, analysis, view);
        RelationshipReport report = getStrategy(RelationshipReport.ReportType.CRM_CALL).generate(ctx);
        reportRepo.save(report);
        return report;
    }

    /**
     * 生成CRM回写命令 — 委托给 CrmWritebackService
     * 所有命令强制 require_human_confirm=true (RULE-CRM-001)
     */
    @Transactional
    public List<CrmWritebackCommand> generateCrmWritebackCommands(
            String operatingCaseId, String journeyId,
            PostvisitAnalysisContent analysis) {
        return crmWritebackService.generateFromAnalysis(operatingCaseId, journeyId, analysis);
    }

    /**
     * 生成更新关系报告 (R7) — 基于新证据
     */
    @Transactional
    public RelationshipReport generateUpdatedRelationshipReport(
            String operatingCaseId, String journeyId,
            String customerId,
            String newEvidenceDescription, String previousReportId) {
        UUID previousId = (previousReportId != null && !previousReportId.isBlank()) ? UUID.fromString(previousReportId) : null;
        Optional<RelationshipReport> previousReport = previousId != null
            ? reportRepo.findById(previousId) : Optional.empty();
        Optional<CustomerOperatingView> view = buildView(customerId, operatingCaseId);

        ReportContext ctx = ReportContext.forUpdatedReport(
            operatingCaseId, journeyId, customerId, newEvidenceDescription, previousId, previousReport, view);
        RelationshipReport report = getStrategy(RelationshipReport.ReportType.UPDATED_RELATIONSHIP).generate(ctx);
        reportRepo.save(report);
        return report;
    }

    /**
     * 生成下次访前报告 (R8) — 继承上次访后分析上下文
     */
    @Transactional
    public RelationshipReport generateNextPrevisitReport(
            String operatingCaseId, String journeyId,
            String customerId,
            PostvisitAnalysisContent previousAnalysis,
            String previousReportId) {
        UUID previousId = (previousReportId != null && !previousReportId.isBlank()) ? UUID.fromString(previousReportId) : null;
        Optional<CustomerOperatingView> view = buildView(customerId, operatingCaseId);

        ReportContext ctx = ReportContext.forNextPrevisit(operatingCaseId, journeyId, customerId, previousAnalysis, previousId, view);
        RelationshipReport report = getStrategy(RelationshipReport.ReportType.NEXT_PREVISIT).generate(ctx);
        reportRepo.save(report);
        return report;
    }

    private Optional<CustomerOperatingView> buildView(String customerId, String operatingCaseId) {
        if (customerId == null || customerId.isBlank()) {
            return Optional.empty();
        }
        try {
            return customerOperatingViewService.buildView(customerId, operatingCaseId);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private ReportStrategy getStrategy(RelationshipReport.ReportType type) {
        ReportStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy registered for report type: " + type);
        }
        return strategy;
    }
}
