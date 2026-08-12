package com.gien.gits.adapter.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.Test;

import com.gien.gits.adapter.persistence.foundation.engagement.mapper.OutreachScriptMapper;
import com.gien.gits.engagement.OutreachScript;
import com.gien.gits.engagement.OutreachScript.OutreachChannel;
import com.gien.gits.engagement.OutreachScript.TalkingPoint;

/**
 * Integration test for OutreachScriptMapper — verifies 13-field constructor order,
 * channel enum, and InstantTypeHandler for createdAt.
 */
class OutreachScriptMapperIT extends AbstractMapperIT {

    private static final String CASE_ID = "IT-OS-CASE-001";
    private static final String JOURNEY_ID = "IT-OS-JNY-001";

    @Test
    void insertAndFindById() {
        insertOperatingCase(CASE_ID);
        insertJourney(JOURNEY_ID, CASE_ID);

        Instant createdAt = Instant.parse("2026-08-12T14:00:00Z");
        OutreachScript script = new OutreachScript(
                "IT-OS-001",                           // scriptId
                "IT-CUST-001",                          // customerId
                "IT-RM-001",                            // rmId
                CASE_ID,                                // operatingCaseId
                JOURNEY_ID,                             // journeyId
                OutreachChannel.PHONE,                  // channel
                "初次外拓联系",                           // objective
                "您好，我是XX银行的客户经理",              // openingLine
                List.of(                                // talkingPoints
                        new TalkingPoint("经营状况", "了解客户近期经营情况", "今年营收是否有增长？", 1),
                        new TalkingPoint("融资需求", "了解是否有授信需求", "是否有短期融资计划？", 2)
                ),
                List.of("注意客户行业风险", "核实经营数据"),  // riskReminders
                "期待与您进一步沟通",                      // closingLine
                "安排线下拜访",                           // followUpAction
                createdAt                               // createdAt
        );

        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            OutreachScriptMapper mapper = session.getMapper(OutreachScriptMapper.class);
            mapper.insert("IT-OS-001", script);

            Optional<OutreachScript> found = mapper.findByScriptId("IT-OS-001");

            assertThat(found).isPresent();
            OutreachScript actual = found.get();

            // Verify all 13 fields in constructor order
            assertThat(actual.scriptId()).isEqualTo("IT-OS-001");
            assertThat(actual.customerId()).isEqualTo("IT-CUST-001");
            assertThat(actual.rmId()).isEqualTo("IT-RM-001");
            assertThat(actual.operatingCaseId()).isEqualTo(CASE_ID);
            assertThat(actual.journeyId()).isEqualTo(JOURNEY_ID);
            assertThat(actual.channel()).isEqualTo(OutreachChannel.PHONE);
            assertThat(actual.objective()).isEqualTo("初次外拓联系");
            assertThat(actual.openingLine()).isEqualTo("您好，我是XX银行的客户经理");

            assertThat(actual.talkingPoints()).hasSize(2);
            assertThat(actual.talkingPoints().get(0).topic()).isEqualTo("经营状况");
            assertThat(actual.talkingPoints().get(0).detail()).isEqualTo("了解客户近期经营情况");
            assertThat(actual.talkingPoints().get(0).suggestedQuestion()).isEqualTo("今年营收是否有增长？");
            assertThat(actual.talkingPoints().get(0).priority()).isEqualTo(1);

            assertThat(actual.riskReminders()).containsExactly("注意客户行业风险", "核实经营数据");
            assertThat(actual.closingLine()).isEqualTo("期待与您进一步沟通");
            assertThat(actual.followUpAction()).isEqualTo("安排线下拜访");
            assertThat(actual.createdAt()).isEqualTo(createdAt);
        }
    }

    @Test
    void instantFieldsPrecision() {
        insertOperatingCase(CASE_ID);
        insertJourney(JOURNEY_ID, CASE_ID);

        Instant createdAt = Instant.parse("2026-08-12T14:00:00.987654Z");

        OutreachScript script = new OutreachScript(
                "IT-OS-002", "IT-CUST-001", "IT-RM-001", CASE_ID, JOURNEY_ID,
                OutreachChannel.EMAIL, "目标", "开场白", List.of(), List.of(), "结束语", "跟进", createdAt
        );

        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            OutreachScriptMapper mapper = session.getMapper(OutreachScriptMapper.class);
            mapper.insert("IT-OS-002", script);

            Optional<OutreachScript> found = mapper.findByScriptId("IT-OS-002");

            assertThat(found).isPresent();
            assertThat(found.get().createdAt()).isEqualTo(createdAt);
        }
    }

    @Test
    void channelEnumRoundTrip() {
        insertOperatingCase(CASE_ID);
        insertJourney(JOURNEY_ID, CASE_ID);

        for (OutreachChannel channel : OutreachChannel.values()) {
            String scriptId = "IT-OS-CH-" + channel.name();
            OutreachScript script = new OutreachScript(
                    scriptId, "IT-CUST-001", "IT-RM-001", CASE_ID, JOURNEY_ID,
                    channel, "目标", "开场白", List.of(), List.of(), "结束语", "跟进", Instant.now()
            );

            try (SqlSession session = sqlSessionFactory.openSession(true)) {
                OutreachScriptMapper mapper = session.getMapper(OutreachScriptMapper.class);
                mapper.insert(scriptId, script);
                Optional<OutreachScript> found = mapper.findByScriptId(scriptId);
                assertThat(found).isPresent();
                assertThat(found.get().channel()).isEqualTo(channel);
            }
        }
    }
}
