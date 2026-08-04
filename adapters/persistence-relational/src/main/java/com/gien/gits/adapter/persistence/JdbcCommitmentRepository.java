package com.gien.gits.adapter.persistence;

import com.gien.gits.ontology.Commitment;
import com.gien.gits.ontology.Commitment.CommitmentType;
import com.gien.gits.ontology.Commitment.CommitmentStatus;
import com.gien.gits.ontology.port.WritableCommitmentRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class JdbcCommitmentRepository implements WritableCommitmentRepository {

    private static final String INSERT_SQL = """
        INSERT INTO commitment (commitment_id, operating_case_id, journey_id, commitment_type,
            content, owner, due_date, status, evidence_ref, created_at, fulfilled_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
        """;

    private static final String FIND_BY_ID_SQL = """
        SELECT commitment_id, operating_case_id, journey_id, commitment_type, content,
            owner, due_date, status, evidence_ref, created_at, fulfilled_at
        FROM commitment WHERE commitment_id = ?
        """;

    private static final String FIND_BY_CASE_SQL = FIND_BY_ID_SQL.replace("WHERE commitment_id = ?", "WHERE operating_case_id = ?");

    private static final String UPDATE_STATUS_SQL = """
        UPDATE commitment SET status = ?, fulfilled_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP WHERE commitment_id = ?
        """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcCommitmentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate");
    }

    public void save(Commitment commitment) {
        jdbcTemplate.update(INSERT_SQL,
            commitment.commitmentId().toString(), commitment.operatingCaseId(), commitment.journeyId(),
            commitment.commitmentType().name(), commitment.content(), commitment.owner(),
            commitment.dueDate(), commitment.status().name(), commitment.evidenceRef(),
            commitment.createdAt() != null ? Timestamp.from(commitment.createdAt()) : null,
            commitment.fulfilledAt() != null ? Timestamp.from(commitment.fulfilledAt()) : null);
    }

    public Optional<Commitment> findById(UUID commitmentId) {
        return jdbcTemplate.query(FIND_BY_ID_SQL, new CommitmentRowMapper(), commitmentId.toString()).stream().findFirst();
    }

    public List<Commitment> findByOperatingCaseId(String operatingCaseId) {
        return jdbcTemplate.query(FIND_BY_CASE_SQL, new CommitmentRowMapper(), operatingCaseId);
    }

    public void updateStatus(UUID commitmentId, CommitmentStatus status) {
        jdbcTemplate.update(UPDATE_STATUS_SQL, status.name(), commitmentId.toString());
    }

    private static Commitment toCommitment(ResultSet rs) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp fulfilledAt = rs.getTimestamp("fulfilled_at");
        return new Commitment(
            UUID.fromString(rs.getString("commitment_id")), rs.getString("operating_case_id"),
            rs.getString("journey_id"), CommitmentType.valueOf(rs.getString("commitment_type")),
            rs.getString("content"), rs.getString("owner"),
            rs.getObject("due_date", LocalDate.class),
            CommitmentStatus.valueOf(rs.getString("status")), rs.getString("evidence_ref"),
            createdAt != null ? createdAt.toInstant() : null,
            fulfilledAt != null ? fulfilledAt.toInstant() : null);
    }

    private static final class CommitmentRowMapper implements RowMapper<Commitment> {
        @Override
        public Commitment mapRow(ResultSet rs, int rowNum) throws SQLException {
            return toCommitment(rs);
        }
    }
}
