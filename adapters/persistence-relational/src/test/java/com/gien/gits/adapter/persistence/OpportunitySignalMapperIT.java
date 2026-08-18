package com.gien.gits.adapter.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.Test;

import com.gien.gits.adapter.persistence.foundation.ontology.mapper.OpportunitySignalMapper;
import com.gien.gits.ontology.OpportunitySignal;
import com.gien.gits.ontology.OpportunitySignal.SignalType;
import com.gien.gits.ontology.OpportunitySignal.SignalSourceType;
import com.gien.gits.ontology.OpportunitySignal.SignalStatus;

/**
 * Integration test for OpportunitySignalMapper — verifies 14-field constructor order
 * and 4 Instant fields typeHandler.
 */
class OpportunitySignalMapperIT extends AbstractMapperIT {

    private static final String CASE_ID = "IT-SIG-CASE-001";

    @Test
    void insertAndFindById() {
        insertOperatingCase(CASE_ID);

        UUID signalId = UUID.randomUUID();
        Instant detectedAt = Instant.parse("2026-08-12T09:00:00Z");
        Instant confirmedAt = Instant.parse("2026-08-12T11:00:00Z");
        Instant createdAt = Instant.parse("2026-08-12T09:00:00Z");
        Instant updatedAt = Instant.parse("2026-08-12T09:00:00Z");

        OpportunitySignal signal = new OpportunitySignal(
                signalId,                               // signalId
                CASE_ID,                                // operatingCaseId
                "IT-JNY-001",                           // journeyId
                SignalType.FINANCING_NEED,              // signalType
                "客户有跨境业务需求",                     // content
                SignalSourceType.INTERACTION,           // sourceType
                "IT-INTER-001",                         // sourceRef
                BigDecimal.valueOf(0.85),               // confidence
                SignalStatus.DETECTED,                  // status
                "IT-EVID-001",                          // evidenceRef
                detectedAt,                             // detectedAt
                confirmedAt,                            // confirmedAt
                createdAt,                              // createdAt
                updatedAt                               // updatedAt
        );

        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            OpportunitySignalMapper mapper = session.getMapper(OpportunitySignalMapper.class);
            mapper.insert(signal);

            Optional<OpportunitySignal> found = mapper.findById(signalId);

            assertThat(found).isPresent();
            OpportunitySignal actual = found.get();
            assertThat(actual.signalId()).isEqualTo(signalId);
            assertThat(actual.operatingCaseId()).isEqualTo(CASE_ID);
            assertThat(actual.journeyId()).isEqualTo("IT-JNY-001");
            assertThat(actual.signalType()).isEqualTo(SignalType.FINANCING_NEED);
            assertThat(actual.content()).isEqualTo("客户有跨境业务需求");
            assertThat(actual.sourceType()).isEqualTo(SignalSourceType.INTERACTION);
            assertThat(actual.sourceRef()).isEqualTo("IT-INTER-001");
            assertThat(actual.confidence()).isEqualByComparingTo(BigDecimal.valueOf(0.85));
            assertThat(actual.status()).isEqualTo(SignalStatus.DETECTED);
            assertThat(actual.evidenceRef()).isEqualTo("IT-EVID-001");
            assertThat(actual.detectedAt()).isEqualTo(detectedAt);
            assertThat(actual.confirmedAt()).isEqualTo(confirmedAt);
        }
    }

    @Test
    void instantFieldsPrecision() {
        insertOperatingCase(CASE_ID);

        UUID signalId = UUID.randomUUID();
        // Use microsecond precision — H2 TIMESTAMP supports up to 6 decimal places
        Instant detectedAt = Instant.parse("2026-08-12T09:00:00.123456Z");
        Instant confirmedAt = Instant.parse("2026-08-12T10:00:00.987654Z");
        Instant createdAt = Instant.parse("2026-08-12T09:00:00.111222Z");
        Instant updatedAt = Instant.parse("2026-08-12T09:00:00.444555Z");

        OpportunitySignal signal = new OpportunitySignal(
                signalId, CASE_ID, "IT-JNY-002",
                SignalType.PRODUCT_OPPORTUNITY, "描述", SignalSourceType.ANALYSIS,
                "REF-002", BigDecimal.valueOf(0.5), SignalStatus.CONFIRMED, "EVID-002",
                detectedAt, confirmedAt, createdAt, updatedAt
        );

        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            OpportunitySignalMapper mapper = session.getMapper(OpportunitySignalMapper.class);
            mapper.insert(signal);

            Optional<OpportunitySignal> found = mapper.findById(signalId);

            assertThat(found).isPresent();
            OpportunitySignal actual = found.get();
            assertThat(actual.detectedAt()).isEqualTo(detectedAt);
            assertThat(actual.confirmedAt()).isEqualTo(confirmedAt);
            assertThat(actual.createdAt()).isEqualTo(createdAt);
            assertThat(actual.updatedAt()).isEqualTo(updatedAt);
        }
    }
}
