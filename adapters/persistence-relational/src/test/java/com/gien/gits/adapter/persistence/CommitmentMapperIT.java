package com.gien.gits.adapter.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.Test;

import com.gien.gits.adapter.persistence.foundation.ontology.mapper.CommitmentMapper;
import com.gien.gits.ontology.Commitment;
import com.gien.gits.ontology.Commitment.CommitmentType;
import com.gien.gits.ontology.Commitment.CommitmentStatus;

/**
 * Integration test for CommitmentMapper — verifies 18-field constructor order
 * and Instant fields typeHandler.
 */
class CommitmentMapperIT extends AbstractMapperIT {

    private static final String CASE_ID = "IT-COM-CASE-001";

    @Test
    void insertAndFindByCommitmentId() {
        insertOperatingCase(CASE_ID);

        UUID commitmentId = UUID.randomUUID();
        UUID interactionId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-08-12T12:00:00Z");
        Instant fulfilledAt = Instant.parse("2026-08-20T15:30:00Z");
        Instant recordedAt = Instant.parse("2026-08-12T12:01:00Z");
        Instant updatedAt = Instant.parse("2026-08-12T12:01:00Z");

        Commitment commitment = new Commitment(
                commitmentId,                           // commitmentId
                CASE_ID,                                // operatingCaseId
                "IT-JNY-001",                           // journeyId
                CommitmentType.CUSTOMER_COMMITMENT,     // commitmentType
                "提供贸易合同和发票",                     // content
                "客户",                                 // owner
                LocalDate.of(2026, 9, 1),              // dueDate
                CommitmentStatus.OPEN,                  // status
                "IT-EVID-001",                          // evidenceRef
                createdAt,                              // createdAt
                fulfilledAt,                            // fulfilledAt
                interactionId,                          // interactionId
                customerId,                             // customerId
                "2026-08-20",                           // fulfilledDate
                "张经理",                                // assignedTo
                "李审核员",                               // verifiedBy
                recordedAt,                             // recordedAt
                updatedAt                               // updatedAt
        );

        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            CommitmentMapper mapper = session.getMapper(CommitmentMapper.class);
            mapper.insert(commitment);

            Optional<Commitment> found = mapper.findByCommitmentId(commitmentId.toString());

            assertThat(found).isPresent();
            Commitment actual = found.get();
            assertThat(actual.commitmentId()).isEqualTo(commitmentId);
            assertThat(actual.operatingCaseId()).isEqualTo(CASE_ID);
            assertThat(actual.journeyId()).isEqualTo("IT-JNY-001");
            assertThat(actual.commitmentType()).isEqualTo(CommitmentType.CUSTOMER_COMMITMENT);
            assertThat(actual.content()).isEqualTo("提供贸易合同和发票");
            assertThat(actual.owner()).isEqualTo("客户");
            assertThat(actual.dueDate()).isEqualTo(LocalDate.of(2026, 9, 1));
            assertThat(actual.status()).isEqualTo(CommitmentStatus.OPEN);
            assertThat(actual.evidenceRef()).isEqualTo("IT-EVID-001");
            assertThat(actual.interactionId()).isEqualTo(interactionId);
            assertThat(actual.customerId()).isEqualTo(customerId);
            assertThat(actual.fulfilledDate()).isEqualTo("2026-08-20");
            assertThat(actual.assignedTo()).isEqualTo("张经理");
            assertThat(actual.verifiedBy()).isEqualTo("李审核员");
        }
    }

    @Test
    void instantFieldsPrecision() {
        insertOperatingCase(CASE_ID);

        UUID commitmentId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-08-12T12:00:00.123456Z");
        Instant fulfilledAt = Instant.parse("2026-08-20T15:30:00.111222Z");
        Instant recordedAt = Instant.parse("2026-08-12T12:01:00.444555Z");
        Instant updatedAt = Instant.parse("2026-08-12T12:01:00.987654Z");

        Commitment commitment = new Commitment(
                commitmentId, CASE_ID, "IT-JNY-002",
                CommitmentType.BANK_COMMITMENT, "内容", "RM",
                LocalDate.of(2026, 10, 1), CommitmentStatus.FULFILLED, "EVID-002",
                createdAt, fulfilledAt, null, null, null, null, null, recordedAt, updatedAt
        );

        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            CommitmentMapper mapper = session.getMapper(CommitmentMapper.class);
            mapper.insert(commitment);

            Optional<Commitment> found = mapper.findByCommitmentId(commitmentId.toString());

            assertThat(found).isPresent();
            Commitment actual = found.get();
            assertThat(actual.createdAt()).isEqualTo(createdAt);
            assertThat(actual.fulfilledAt()).isEqualTo(fulfilledAt);
            assertThat(actual.recordedAt()).isEqualTo(recordedAt);
            assertThat(actual.updatedAt()).isEqualTo(updatedAt);
        }
    }
}
