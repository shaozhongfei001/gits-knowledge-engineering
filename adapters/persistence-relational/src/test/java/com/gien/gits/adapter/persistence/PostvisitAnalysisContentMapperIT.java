package com.gien.gits.adapter.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.Test;

import com.gien.gits.adapter.persistence.foundation.engagement.mapper.PostvisitAnalysisContentMapper;
import com.gien.gits.engagement.PostvisitAnalysisContent;
import com.gien.gits.engagement.PostvisitAnalysisContent.OpportunitySignalItem;
import com.gien.gits.engagement.PostvisitAnalysisContent.CommitmentItem;
import com.gien.gits.engagement.PostvisitAnalysisContent.FactReconciliationItem;
import com.gien.gits.engagement.InteractionExtraction;
import com.gien.gits.engagement.InteractionExtraction.ExtractionType;
import com.gien.gits.engagement.InteractionExtraction.ClaimType;
import com.gien.gits.engagement.InteractionExtraction.ExtractionStatus;

/**
 * Integration test for PostvisitAnalysisContentMapper — verifies keyFindings,
 * opportunitySignals, commitments and other JSON fields map correctly.
 */
class PostvisitAnalysisContentMapperIT extends AbstractMapperIT {

    private static final String CASE_ID = "IT-PA-CASE-001";
    private static final String JOURNEY_ID = "IT-PA-JNY-001";

    @Test
    void insertAndFindByAnalysisId() {
        insertOperatingCase(CASE_ID);
        insertJourney(JOURNEY_ID, CASE_ID);

        InteractionExtraction finding = new InteractionExtraction(
                "FIND-001", ExtractionType.FACT_CLAIM, ClaimType.CUSTOMER_STATEMENT,
                "客户跨境业务增长迅速", "客户", null, ExtractionStatus.VERIFIED_FACT,
                BigDecimal.valueOf(0.9), false, false, null, null);

        PostvisitAnalysisContent content = new PostvisitAnalysisContent(
                "IT-PA-001",                            // analysisId
                JOURNEY_ID,                             // journeyId
                "客户有明确的跨境结算需求",               // visitSummary
                List.of(finding),                       // keyFindings
                List.of(),                              // opportunitySignals
                List.of(),                              // commitments
                List.of(),                              // reconciliationItems
                List.of("安排产品经理跟进", "准备授信方案"),  // followUpActions
                "推荐跨境人民币结算方案"                   // nextStepRecommendation
        );

        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            PostvisitAnalysisContentMapper mapper = session.getMapper(PostvisitAnalysisContentMapper.class);
            mapper.insert(content, CASE_ID);

            Optional<PostvisitAnalysisContent> found = mapper.findRowByAnalysisId("IT-PA-001");

            assertThat(found).isPresent();
            PostvisitAnalysisContent actual = found.get();
            assertThat(actual.analysisId()).isEqualTo("IT-PA-001");
            assertThat(actual.journeyId()).isEqualTo(JOURNEY_ID);
            assertThat(actual.visitSummary()).isEqualTo("客户有明确的跨境结算需求");
            assertThat(actual.keyFindings()).hasSize(1);
            assertThat(actual.keyFindings().get(0).content()).isEqualTo("客户跨境业务增长迅速");
            assertThat(actual.followUpActions()).containsExactly("安排产品经理跟进", "准备授信方案");
            assertThat(actual.nextStepRecommendation()).isEqualTo("推荐跨境人民币结算方案");
        }
    }

    @Test
    void jsonFieldsSerialization() {
        insertOperatingCase(CASE_ID);
        insertJourney(JOURNEY_ID, CASE_ID);

        OpportunitySignalItem signalItem = new OpportunitySignalItem(
                "FINANCING_NEED", "客户有跨境结算需求", "INTERACTION",
                BigDecimal.valueOf(0.85), false);
        CommitmentItem commitmentItem = new CommitmentItem(
                "CUSTOMER_COMMITMENT", "提供贸易合同", "客户", "2026-09-01");
        FactReconciliationItem reconciliationItem = new FactReconciliationItem(
                "营收核实", "客户年营收5亿", "客户表示营收5亿", "CONFIRMED", "获取财务报表核实");

        PostvisitAnalysisContent content = new PostvisitAnalysisContent(
                "IT-PA-002", JOURNEY_ID,
                "深度分析", List.of(), List.of(signalItem), List.of(commitmentItem),
                List.of(reconciliationItem), List.of("行动1"), "下一步"
        );

        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            PostvisitAnalysisContentMapper mapper = session.getMapper(PostvisitAnalysisContentMapper.class);
            mapper.insert(content, CASE_ID);

            Optional<PostvisitAnalysisContent> found = mapper.findRowByAnalysisId("IT-PA-002");

            assertThat(found).isPresent();
            PostvisitAnalysisContent actual = found.get();

            // Verify opportunitySignals JSON round-trip
            assertThat(actual.opportunitySignals()).hasSize(1);
            OpportunitySignalItem actualSignal = actual.opportunitySignals().get(0);
            assertThat(actualSignal.signalType()).isEqualTo("FINANCING_NEED");
            assertThat(actualSignal.content()).isEqualTo("客户有跨境结算需求");
            assertThat(actualSignal.sourceType()).isEqualTo("INTERACTION");
            assertThat(actualSignal.confidence()).isEqualByComparingTo(BigDecimal.valueOf(0.85));
            assertThat(actualSignal.notOpportunityYet()).isFalse();

            // Verify commitments JSON round-trip
            assertThat(actual.commitments()).hasSize(1);
            CommitmentItem actualCommitment = actual.commitments().get(0);
            assertThat(actualCommitment.commitmentType()).isEqualTo("CUSTOMER_COMMITMENT");
            assertThat(actualCommitment.content()).isEqualTo("提供贸易合同");
            assertThat(actualCommitment.owner()).isEqualTo("客户");
            assertThat(actualCommitment.dueDate()).isEqualTo("2026-09-01");

            // Verify reconciliationItems JSON round-trip
            assertThat(actual.reconciliationItems()).hasSize(1);
            FactReconciliationItem actualRecon = actual.reconciliationItems().get(0);
            assertThat(actualRecon.topic()).isEqualTo("营收核实");
            assertThat(actualRecon.structuredFact()).isEqualTo("客户年营收5亿");
            assertThat(actualRecon.interactionClaim()).isEqualTo("客户表示营收5亿");
            assertThat(actualRecon.correctJudgment()).isEqualTo("CONFIRMED");
            assertThat(actualRecon.nextAction()).isEqualTo("获取财务报表核实");
        }
    }
}
