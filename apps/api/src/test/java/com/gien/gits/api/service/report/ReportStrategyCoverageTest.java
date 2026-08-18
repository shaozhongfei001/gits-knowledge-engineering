package com.gien.gits.api.service.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.gien.gits.api.service.ContextInheritanceService;
import com.gien.gits.engagement.CustomerOperatingView;
import com.gien.gits.engagement.InteractionExtraction;
import com.gien.gits.engagement.InteractionExtraction.ClaimType;
import com.gien.gits.engagement.InteractionExtraction.ExtractionType;
import com.gien.gits.engagement.PostvisitAnalysisContent;
import com.gien.gits.engagement.port.LlmClient;
import com.gien.gits.engagement.port.LlmClientException;
import com.gien.gits.ontology.RelationshipReport;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** P1b 覆盖率补测：内部关系报告（R5A）与下次访前报告（R8）策略。 */
class ReportStrategyCoverageTest {

    private LlmClient llmClient;
    private ContextInheritanceService inheritanceService;

    @BeforeEach
    void setUp() {
        llmClient = mock(LlmClient.class);
        inheritanceService = mock(ContextInheritanceService.class);
    }

    private static InteractionExtraction finding() {
        return new InteractionExtraction(
                "EXT-001", ExtractionType.RISK_INDICATOR, "客户存在逾期风险",
                true, true, "TR-RAW-001", ClaimType.RISK_SIGNAL, 0.60);
    }

    private static CustomerOperatingView view() {
        return new CustomerOperatingView(
                "CUST-001", "示例客户", "制造业", "中大型", "A", "中", "RM-1", "张经理",
                List.of("客户名称"), List.of("行业"), List.of("风险等级"),
                List.of(new CustomerOperatingView.OpportunitySignalSummary(
                        "FINANCING_NEED", "扩产融资", BigDecimal.valueOf(0.80), "OPEN")),
                3, Instant.parse("2026-08-01T00:00:00Z"), 1, "PRE_VISIT",
                List.of(), 0, List.of("存在逾期"));
    }

    private static PostvisitAnalysisContent analysis() {
        return new PostvisitAnalysisContent(
                "AN-001", "J-001", "本次拜访正常",
                List.of(finding()),
                List.of(new PostvisitAnalysisContent.OpportunitySignalItem(
                        "FINANCING_NEED", "扩产融资", "CUSTOMER", BigDecimal.valueOf(0.80), false)),
                List.of(new PostvisitAnalysisContent.CommitmentItem(
                        "MATERIAL_PROVIDE", "提供报表", "客户方", "PENDING")),
                List.of(new PostvisitAnalysisContent.FactReconciliationItem(
                        "3000万授信", "项目融资", "客户提出3000万", "非本行事实", "补充核实")),
                List.of("补充报表"), "跟进授信");
    }

    // ── 内部关系报告 R5A ─────────────────────────────────────────

    @Test
    void internalReport_llmSuccess_includesCustomerView() {
        doReturn("{}").when(llmClient).complete(anyString(), anyString());
        InternalRelationshipReportStrategy strategy = new InternalRelationshipReportStrategy(llmClient);

        ReportContext context = ReportContext.forPostvisit(
                "OC-1", "J-1", "CUST-001", analysis(), Optional.of(view()));

        RelationshipReport report = strategy.generate(context);

        assertThat(report).isNotNull();
        assertThat(report.reportType()).isEqualTo(RelationshipReport.ReportType.INTERNAL_RELATIONSHIP);
        assertThat(report.content()).contains("示例客户").contains("扩产融资");
        assertThat(strategy.supportedType()).isEqualTo(RelationshipReport.ReportType.INTERNAL_RELATIONSHIP);
    }

    @Test
    void internalReport_llmFailure_fallsBackToStringBuilder() {
        doThrow(new LlmClientException("down")).when(llmClient).complete(anyString(), anyString());
        InternalRelationshipReportStrategy strategy = new InternalRelationshipReportStrategy(llmClient);

        ReportContext context = ReportContext.forPostvisit(
                "OC-1", "J-1", "CUST-001", analysis(), Optional.of(view()));

        RelationshipReport report = strategy.generate(context);
        assertThat(report.content()).isNotNull();
        assertThat(report.basedOnEvidence()).contains("TR-RAW-001");
    }

    @Test
    void internalReport_nullView_handlesGracefully() {
        doThrow(new LlmClientException("down")).when(llmClient).complete(anyString(), anyString());
        InternalRelationshipReportStrategy strategy = new InternalRelationshipReportStrategy(llmClient);

        ReportContext context = ReportContext.forPostvisit(
                "OC-1", "J-1", "CUST-001", analysis(), Optional.empty());

        RelationshipReport report = strategy.generate(context);
        assertThat(report.reportType()).isEqualTo(RelationshipReport.ReportType.INTERNAL_RELATIONSHIP);
    }

    // ── 下次访前报告 R8 ─────────────────────────────────────────

    @Test
    void nextPrevisit_usesProvidedAnalysisAndLlm() {
        doReturn("{}").when(llmClient).complete(anyString(), anyString());
        NextPrevisitReportStrategy strategy =
                new NextPrevisitReportStrategy(inheritanceService, llmClient);

        ReportContext context = ReportContext.forNextPrevisit(
                "OC-1", "J-1", "CUST-001", analysis(), UUID.randomUUID(), Optional.of(view()));

        RelationshipReport report = strategy.generate(context);
        assertThat(report.reportType()).isEqualTo(RelationshipReport.ReportType.NEXT_PREVISIT);
        assertThat(report.content()).isNotNull();
    }

    @Test
    void nextPrevisit_inheritsAnalysisWhenNull() {
        doThrow(new LlmClientException("down")).when(llmClient).complete(anyString(), anyString());
        when(inheritanceService.getInheritedAnalysis("OC-1")).thenReturn(Optional.of(analysis()));

        NextPrevisitReportStrategy strategy =
                new NextPrevisitReportStrategy(inheritanceService, llmClient);

        ReportContext context = ReportContext.forNextPrevisit(
                "OC-1", "J-1", "CUST-001", null, null, Optional.of(view()));

        RelationshipReport report = strategy.generate(context);
        assertThat(report.content()).isNotNull();
    }

    @Test
    void nextPrevisit_noInheritedAnalysis_throws() {
        when(inheritanceService.getInheritedAnalysis("OC-1")).thenReturn(Optional.empty());
        NextPrevisitReportStrategy strategy =
                new NextPrevisitReportStrategy(inheritanceService, llmClient);

        ReportContext context = ReportContext.forNextPrevisit(
                "OC-1", "J-1", "CUST-001", null, null, Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class, () -> strategy.generate(context));
    }

    // ── 更新关系报告 R7 ─────────────────────────────────────────

    @Test
    void updatedReport_llmSuccess_buildsFromNewEvidence() {
        doReturn("{}").when(llmClient).complete(anyString(), anyString());
        UpdatedRelationshipReportStrategy strategy = new UpdatedRelationshipReportStrategy(llmClient);

        ReportContext context = ReportContext.forUpdatedReport(
                "OC-1", "J-1", "CUST-001", "客户新增授信需求", UUID.randomUUID(),
                Optional.empty(), Optional.of(view()));

        RelationshipReport report = strategy.generate(context);
        assertThat(report.reportType()).isEqualTo(RelationshipReport.ReportType.UPDATED_RELATIONSHIP);
        assertThat(report.content()).isNotNull();
        assertThat(report.basedOnEvidence()).contains("NEW-EVIDENCE");
    }

    @Test
    void updatedReport_llmFailure_fallsBack() {
        doThrow(new LlmClientException("down")).when(llmClient).complete(anyString(), anyString());
        UpdatedRelationshipReportStrategy strategy = new UpdatedRelationshipReportStrategy(llmClient);

        ReportContext context = ReportContext.forUpdatedReport(
                "OC-1", "J-1", "CUST-001", "客户新增授信需求", UUID.randomUUID(),
                Optional.of(new RelationshipReport(
                        UUID.randomUUID(), "OC-1", "J-1", RelationshipReport.ReportType.INTERNAL_RELATIONSHIP,
                        "旧内容", List.of(), List.of(), Instant.now(), null, Instant.now(), Instant.now())),
                Optional.of(view()));

        RelationshipReport report = strategy.generate(context);
        assertThat(report.content()).isNotNull();
    }

    // ── CRM 通话报告 R5B ────────────────────────────────────────

    @Test
    void crmCallReport_llmSuccess_includesAnalysis() {
        doReturn("{}").when(llmClient).complete(anyString(), anyString());
        CrmCallReportStrategy strategy = new CrmCallReportStrategy(llmClient);

        ReportContext context = ReportContext.forPostvisit(
                "OC-1", "J-1", "CUST-001", analysis(), Optional.of(view()));

        RelationshipReport report = strategy.generate(context);
        assertThat(report.reportType()).isEqualTo(RelationshipReport.ReportType.CRM_CALL);
        assertThat(report.content()).isNotNull();
    }

    @Test
    void crmCallReport_llmFailure_fallsBack() {
        doThrow(new LlmClientException("down")).when(llmClient).complete(anyString(), anyString());
        CrmCallReportStrategy strategy = new CrmCallReportStrategy(llmClient);

        ReportContext context = ReportContext.forPostvisit(
                "OC-1", "J-1", "CUST-001", analysis(), Optional.empty());

        RelationshipReport report = strategy.generate(context);
        assertThat(report.content()).isNotNull();
        assertThat(report.basedOnEvidence()).contains("TR-RAW-001");
    }

    // ── 追加分支：fallback 的 buildContent/buildContextSummary ───

    @Test
    void nextPrevisit_llmFailure_viewPresent_buildsContent() {
        doThrow(new LlmClientException("down")).when(llmClient).complete(anyString(), anyString());
        NextPrevisitReportStrategy strategy =
                new NextPrevisitReportStrategy(inheritanceService, llmClient);

        // analysis 直接提供，view 存在 → 命中 buildContent + buildContextSummary(view) 分支
        ReportContext context = ReportContext.forNextPrevisit(
                "OC-1", "J-1", "CUST-001", analysis(), null, Optional.of(view()));

        RelationshipReport report = strategy.generate(context);
        assertThat(report.content()).isNotNull();
        assertThat(report.basedOnEvidence()).contains("TR-RAW-001");
    }

    @Test
    void internalReport_viewWithEmptySignals_coversEmptyBranches() {
        doThrow(new LlmClientException("down")).when(llmClient).complete(anyString(), anyString());

        CustomerOperatingView emptyView = new CustomerOperatingView(
                "CUST-001", "示例客户", "制造业", "中大型", "A", "中", "RM-1", "张经理",
                List.of(), List.of(), List.of(),
                List.of(), 0, null, 0, null, List.of(), 0, List.of());

        InternalRelationshipReportStrategy strategy = new InternalRelationshipReportStrategy(llmClient);
        ReportContext context = ReportContext.forPostvisit(
                "OC-1", "J-1", "CUST-001", analysis(), Optional.of(emptyView));

        RelationshipReport report = strategy.generate(context);
        assertThat(report.content()).isNotNull();
        // 空 signals → "当前无活跃机会信号"
        assertThat(report.content()).contains("无活跃机会信号");
    }

    @Test
    void updatedReport_llmFailure_richView_coversRiskCommitmentBranches() {
        doThrow(new LlmClientException("down")).when(llmClient).complete(anyString(), anyString());

        CustomerOperatingView richView = new CustomerOperatingView(
                "CUST-001", "示例客户", "制造业", "中大型", "A", "中", "RM-1", "张经理",
                List.of("客户名称"), List.of(), List.of(),
                List.of(), 2, Instant.parse("2026-08-01T00:00:00Z"), 1, "POST_VISIT",
                List.of(new CustomerOperatingView.CommitmentSummary("MATERIAL_PROVIDE", "提供报表", "客户方", "2026-09-01", false)),
                1, List.of("存在逾期"));

        UpdatedRelationshipReportStrategy strategy = new UpdatedRelationshipReportStrategy(llmClient);
        ReportContext context = ReportContext.forUpdatedReport(
                "OC-1", "J-1", "CUST-001", "新证据", UUID.randomUUID(),
                Optional.of(new RelationshipReport(
                        UUID.randomUUID(), "OC-1", "J-1", RelationshipReport.ReportType.INTERNAL_RELATIONSHIP,
                        "前次报告内容", List.of(), List.of(), Instant.now(), null, Instant.now(), Instant.now())),
                Optional.of(richView));

        RelationshipReport report = strategy.generate(context);
        assertThat(report.content()).isNotNull();
        assertThat(report.content()).contains("新证据");
    }

    @Test
    void crmCallReport_llmFailure_viewPresent() {
        doThrow(new LlmClientException("down")).when(llmClient).complete(anyString(), anyString());
        CrmCallReportStrategy strategy = new CrmCallReportStrategy(llmClient);

        ReportContext context = ReportContext.forPostvisit(
                "OC-1", "J-1", "CUST-001", analysis(), Optional.of(view()));

        RelationshipReport report = strategy.generate(context);
        assertThat(report.content()).isNotNull();
    }
}
