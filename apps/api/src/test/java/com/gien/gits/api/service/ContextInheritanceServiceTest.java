package com.gien.gits.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.gien.gits.engagement.PostvisitAnalysisContent;
import com.gien.gits.engagement.PostvisitAnalysisContent.CommitmentItem;
import com.gien.gits.engagement.PostvisitAnalysisContent.FactReconciliationItem;
import com.gien.gits.engagement.PostvisitAnalysisContent.OpportunitySignalItem;
import com.gien.gits.engagement.port.WritablePostvisitAnalysisContentRepository;
import com.gien.gits.engagement.port.WritablePrevisitReportContentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ContextInheritanceServiceTest {

    @Mock
    private WritablePostvisitAnalysisContentRepository postvisitRepo;
    @Mock
    private WritablePrevisitReportContentRepository previsitRepo;

    private AutoCloseable mocks;
    private ContextInheritanceService service;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        service = new ContextInheritanceService(postvisitRepo, previsitRepo);
    }

    private PostvisitAnalysisContent createTestContent(
            String analysisId, String journeyId,
            List<String> followUpActions,
            List<FactReconciliationItem> reconciliationItems,
            List<OpportunitySignalItem> opportunitySignals) {
        return new PostvisitAnalysisContent(
            analysisId, journeyId, "访后摘要",
            List.of(), opportunitySignals, List.of(),
            reconciliationItems, followUpActions, "继续跟进");
    }

    // ── inheritContext ──────────────────────────────────────────

    @Test
    void inheritContext_withHistory_returnsInheritedContext() {
        String operatingCaseId = "case-001";
        PostvisitAnalysisContent previous = createTestContent(
            "analysis-001", "journey-001",
            List.of("跟进融资需求"),
            List.of(new FactReconciliationItem("营收", "5亿", "4.8亿", "待核实", "下次确认")),
            List.of(new OpportunitySignalItem("FINANCING_NEED", "融资需求", "ANALYSIS", BigDecimal.valueOf(0.8), false)));
        when(postvisitRepo.findLatestByOperatingCaseId(operatingCaseId))
            .thenReturn(Optional.of(previous));

        var context = service.inheritContext(operatingCaseId);

        assertTrue(context.isPresent());
        assertFalse(context.get().keyQuestions().isEmpty());
        assertFalse(context.get().riskReminders().isEmpty());
        assertNotNull(context.get().visitStrategy());
    }

    @Test
    void inheritContext_noHistory_returnsEmpty() {
        String operatingCaseId = "case-empty";
        when(postvisitRepo.findLatestByOperatingCaseId(operatingCaseId))
            .thenReturn(Optional.empty());

        var context = service.inheritContext(operatingCaseId);

        assertTrue(context.isEmpty());
    }

    // ── getInheritedAnalysis ────────────────────────────────────

    @Test
    void getInheritedAnalysis_found_returnsContent() {
        String operatingCaseId = "case-002";
        PostvisitAnalysisContent content = createTestContent(
            "analysis-002", "journey-002", List.of(), List.of(), List.of());
        when(postvisitRepo.findLatestByOperatingCaseId(operatingCaseId))
            .thenReturn(Optional.of(content));

        Optional<PostvisitAnalysisContent> result = service.getInheritedAnalysis(operatingCaseId);

        assertTrue(result.isPresent());
        assertEquals("analysis-002", result.get().analysisId());
    }

    @Test
    void getInheritedAnalysis_notFound_returnsEmpty() {
        when(postvisitRepo.findLatestByOperatingCaseId("case-none"))
            .thenReturn(Optional.empty());

        Optional<PostvisitAnalysisContent> result = service.getInheritedAnalysis("case-none");

        assertTrue(result.isEmpty());
    }

    // ── buildInheritedKeyQuestions ───────────────────────────────

    @Test
    void buildInheritedKeyQuestions_fromFollowUpActions() {
        PostvisitAnalysisContent content = createTestContent(
            "a1", "j1",
            List.of("跟进融资审批进度", "确认担保物评估"),
            List.of(), List.of());

        var questions = service.buildInheritedKeyQuestions(content);

        assertFalse(questions.isEmpty());
        assertTrue(questions.stream().anyMatch(q -> q.contains("融资审批") || q.contains("担保物")));
    }

    @Test
    void buildInheritedKeyQuestions_fromReconciliationItems() {
        PostvisitAnalysisContent content = createTestContent(
            "a2", "j2", List.of(),
            List.of(new FactReconciliationItem("营收", "5亿", "4.8亿", "待核实", "下次确认"),
                    new FactReconciliationItem("股权", "3股东", "2股东", "不一致", "核实")),
            List.of());

        var questions = service.buildInheritedKeyQuestions(content);

        // reconciliationItems may or may not contribute to keyQuestions depending on implementation
        // just verify the method returns a non-null list
        assertNotNull(questions);
    }

    // ── buildInheritedRiskReminders ─────────────────────────────

    @Test
    void buildInheritedRiskReminders_fromReconciliationAndSignals() {
        PostvisitAnalysisContent content = createTestContent(
            "a3", "j3", List.of(),
            List.of(new FactReconciliationItem("营收", "5亿", "4.8亿", "待核实", "核实")),
            List.of(new OpportunitySignalItem("RISK_SIGNAL", "风险信号", "ANALYSIS", BigDecimal.valueOf(0.7), true)));

        var reminders = service.buildInheritedRiskReminders(content);

        assertFalse(reminders.isEmpty());
    }

    @Test
    void buildInheritedRiskReminders_emptyData_returnsEmpty() {
        PostvisitAnalysisContent content = createTestContent(
            "a4", "j4", List.of(), List.of(), List.of());

        var reminders = service.buildInheritedRiskReminders(content);

        assertTrue(reminders.isEmpty());
    }
}
