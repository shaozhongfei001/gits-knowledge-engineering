package com.gien.gits.adapter.persistence.v11;

import com.gien.gits.adapter.persistence.JsonHelper;
import com.gien.gits.ontology.Commitment;
import com.gien.gits.ontology.port.CommitmentRepository;
import com.gien.gits.ontology.port.WritableCommitmentRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * V1.1 承诺JDBC适配器 — 列名对齐V005+V011迁移
 */
public class JdbcCommitmentRepository implements WritableCommitmentRepository {

    private static final String INSERT_SQL = """
        INSERT INTO commitment (commitment_id, operating_case_id, journey_id, commitment_type,
            content, owner, due_date, status, evidence_ref, created_at, fulfilled_at,
            interaction_id, customer_id, fulfilled_date, assigned_to, verified_by, recorded_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

    private static final String UPDATE_STATUS_SQL = """
        UPDATE commitment SET status = ?, verified_by = ?, updated_at = ? WHERE commitment_id = ?
        """;

    private static final String FIND_BY_ID = "SELECT * FROM commitment WHERE commitment_id = ?";
    private static final String FIND_BY_STATUS = "SELECT * FROM commitment WHERE status = ? ORDER BY due_date";
    private static final String FIND_BY_TYPE = "SELECT * FROM commitment WHERE commitment_type = ? ORDER BY due_date";
    private static final String FIND_OVERDUE = """
        SELECT * FROM commitment WHERE status = 'OPEN' AND due_date < CURRENT_DATE ORDER BY due_date
        """;

    private final JdbcTemplate jdbc;

    public JdbcCommitmentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void save(Commitment c) {
        jdbc.update(INSERT_SQL,
                c.commitmentId() != null ? c.commitmentId().toString() : null,
                c.operatingCaseId(),
                c.journeyId(),
                c.commitmentType() != null ? c.commitmentType().name() : null,
                c.content(),
                c.owner(),
                c.dueDate(),
                c.status() != null ? c.status().name() : null,
                c.evidenceRef(),
                c.createdAt() != null ? Timestamp.from(c.createdAt()) : null,
                c.fulfilledAt() != null ? Timestamp.from(c.fulfilledAt()) : null,
                c.interactionId() != null ? c.interactionId().toString() : null,
                c.customerId() != null ? c.customerId().toString() : null,
                c.fulfilledDate(),
                c.assignedTo(),
                c.verifiedBy(),
                c.recordedAt() != null ? Timestamp.from(c.recordedAt()) : null,
                c.updatedAt() != null ? Timestamp.from(c.updatedAt()) : null);
    }

    @Override
    public void updateStatus(String commitmentId, String status, String verifiedBy) {
        jdbc.update(UPDATE_STATUS_SQL, status, verifiedBy, Timestamp.from(Instant.now()), commitmentId);
    }

    @Override
    public Optional<Commitment> findByCommitmentId(String commitmentId) {
        return jdbc.query(FIND_BY_ID, rowMapper(), commitmentId).stream().findFirst();
    }

    @Override
    public List<Commitment> findByInteractionId(String interactionId) {
        // V011 added interaction_id column
        return jdbc.query("SELECT * FROM commitment WHERE interaction_id = ? ORDER BY due_date",
                rowMapper(), interactionId);
    }

    @Override
    public List<Commitment> findByCustomerId(String customerId) {
        // V011 added customer_id column
        return jdbc.query("SELECT * FROM commitment WHERE customer_id = ? ORDER BY due_date",
                rowMapper(), customerId);
    }

    @Override
    public List<Commitment> findByStatus(String status) {
        return jdbc.query(FIND_BY_STATUS, rowMapper(), status);
    }

    @Override
    public List<Commitment> findByCommitmentType(String commitmentType) {
        return jdbc.query(FIND_BY_TYPE, rowMapper(), commitmentType);
    }

    @Override
    public List<Commitment> findByOperatingCaseId(String operatingCaseId) {
        return jdbc.query("SELECT * FROM commitment WHERE operating_case_id = ? ORDER BY due_date",
                rowMapper(), operatingCaseId);
    }

    @Override
    public List<Commitment> findOverdue() {
        return jdbc.query(FIND_OVERDUE, rowMapper());
    }

    @Override
    public List<Commitment> findAll() {
        return jdbc.query("SELECT * FROM commitment ORDER BY due_date", rowMapper());
    }

    private RowMapper<Commitment> rowMapper() {
        return (rs, rowNum) -> mapRow(rs);
    }

    private Commitment mapRow(ResultSet rs) throws SQLException {
        Timestamp createdTs = rs.getTimestamp("created_at");
        Timestamp fulfilledTs = rs.getTimestamp("fulfilled_at");
        return new Commitment(
                UUID.fromString(rs.getString("commitment_id")),
                rs.getString("operating_case_id"),
                rs.getString("journey_id"),
                Commitment.CommitmentType.valueOf(rs.getString("commitment_type")),
                rs.getString("content"),
                rs.getString("owner"),
                rs.getObject("due_date", LocalDate.class),
                Commitment.CommitmentStatus.valueOf(rs.getString("status")),
                rs.getString("evidence_ref"),
                createdTs != null ? createdTs.toInstant() : null,
                fulfilledTs != null ? fulfilledTs.toInstant() : null
        );
    }
}
