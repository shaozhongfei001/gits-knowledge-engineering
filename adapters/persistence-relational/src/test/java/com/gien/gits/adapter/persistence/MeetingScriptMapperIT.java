package com.gien.gits.adapter.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.Test;

import com.gien.gits.adapter.persistence.foundation.engagement.mapper.MeetingScriptMapper;
import com.gien.gits.engagement.MeetingScript;
import com.gien.gits.engagement.MeetingScript.AgendaItem;
import com.gien.gits.engagement.MeetingScript.KycQuestionItem;
import com.gien.gits.engagement.MeetingScript.ProductDiscussionItem;

/**
 * Integration test for MeetingScriptMapper — verifies 13-field constructor order
 * and InstantTypeHandler for createdAt.
 */
class MeetingScriptMapperIT extends AbstractMapperIT {

    private static final String CASE_ID = "IT-MS-CASE-001";
    private static final String JOURNEY_ID = "IT-MS-JNY-001";

    @Test
    void insertAndFindById() {
        insertOperatingCase(CASE_ID);
        insertJourney(JOURNEY_ID, CASE_ID);

        Instant createdAt = Instant.parse("2026-08-12T10:30:00Z");
        MeetingScript script = new MeetingScript(
                "IT-MS-001",                           // scriptId
                "IT-CUST-001",                          // customerId
                "IT-RM-001",                            // rmId
                CASE_ID,                                // operatingCaseId
                JOURNEY_ID,                             // journeyId
                "了解客户跨境业务需求",                   // meetingObjective
                "客户有跨境结算和供应链融资需求",          // previsitSummary
                List.of(                                // agendaItems
                        new AgendaItem("KYC更新", 15, "核实客户信息变更", "获取最新营业执照"),
                        new AgendaItem("产品推荐", 20, "推荐跨境结算方案", "客户确认方案")
                ),
                List.of(                                // kycQuestions
                        new KycQuestionItem("实际控制人", "请确认实际控制人信息", "合规要求", "TEXT"),
                        new KycQuestionItem("经营范围", "是否有跨境业务？", "产品匹配", "BOOLEAN")
                ),
                List.of(                                // productDiscussions
                        new ProductDiscussionItem("P001", "跨境人民币结算", "从供应链角度切入", List.of("费率低", "到账快"))
                ),
                List.of("关注行业周期性风险", "核实贸易背景真实性"),  // riskPoints
                "建议先做KYC更新再推荐产品",              // closingSummary
                createdAt                               // createdAt
        );

        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            MeetingScriptMapper mapper = session.getMapper(MeetingScriptMapper.class);
            mapper.insert("IT-MS-001", script);

            Optional<MeetingScript> found = mapper.findByScriptId("IT-MS-001");

            assertThat(found).isPresent();
            MeetingScript actual = found.get();

            // Verify all 13 fields in constructor order
            assertThat(actual.scriptId()).isEqualTo("IT-MS-001");
            assertThat(actual.customerId()).isEqualTo("IT-CUST-001");
            assertThat(actual.rmId()).isEqualTo("IT-RM-001");
            assertThat(actual.operatingCaseId()).isEqualTo(CASE_ID);
            assertThat(actual.journeyId()).isEqualTo(JOURNEY_ID);
            assertThat(actual.meetingObjective()).isEqualTo("了解客户跨境业务需求");
            assertThat(actual.previsitSummary()).isEqualTo("客户有跨境结算和供应链融资需求");

            assertThat(actual.agendaItems()).hasSize(2);
            assertThat(actual.agendaItems().get(0).topic()).isEqualTo("KYC更新");
            assertThat(actual.agendaItems().get(0).durationMinutes()).isEqualTo(15);
            assertThat(actual.agendaItems().get(0).keyPoints()).isEqualTo("核实客户信息变更");
            assertThat(actual.agendaItems().get(0).expectedOutcome()).isEqualTo("获取最新营业执照");

            assertThat(actual.kycQuestions()).hasSize(2);
            assertThat(actual.kycQuestions().get(0).gapArea()).isEqualTo("实际控制人");
            assertThat(actual.kycQuestions().get(0).question()).isEqualTo("请确认实际控制人信息");
            assertThat(actual.kycQuestions().get(0).purpose()).isEqualTo("合规要求");
            assertThat(actual.kycQuestions().get(0).expectedAnswerType()).isEqualTo("TEXT");

            assertThat(actual.productDiscussions()).hasSize(1);
            assertThat(actual.productDiscussions().get(0).productId()).isEqualTo("P001");
            assertThat(actual.productDiscussions().get(0).productName()).isEqualTo("跨境人民币结算");
            assertThat(actual.productDiscussions().get(0).discussionAngle()).isEqualTo("从供应链角度切入");
            assertThat(actual.productDiscussions().get(0).keySellingPoints()).containsExactly("费率低", "到账快");

            assertThat(actual.riskPoints()).containsExactly("关注行业周期性风险", "核实贸易背景真实性");
            assertThat(actual.closingSummary()).isEqualTo("建议先做KYC更新再推荐产品");
            assertThat(actual.createdAt()).isEqualTo(createdAt);
        }
    }

    @Test
    void instantFieldsPrecision() {
        insertOperatingCase(CASE_ID);
        insertJourney(JOURNEY_ID, CASE_ID);

        // Use microsecond precision — H2 TIMESTAMP supports up to 6 decimal places
        Instant createdAt = Instant.parse("2026-08-12T10:30:00.123456Z");

        MeetingScript script = new MeetingScript(
                "IT-MS-002", "IT-CUST-001", "IT-RM-001", CASE_ID, JOURNEY_ID,
                "目标", "摘要", List.of(), List.of(), List.of(), List.of(), "总结", createdAt
        );

        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            MeetingScriptMapper mapper = session.getMapper(MeetingScriptMapper.class);
            mapper.insert("IT-MS-002", script);

            Optional<MeetingScript> found = mapper.findByScriptId("IT-MS-002");

            assertThat(found).isPresent();
            assertThat(found.get().createdAt()).isEqualTo(createdAt);
        }
    }
}
