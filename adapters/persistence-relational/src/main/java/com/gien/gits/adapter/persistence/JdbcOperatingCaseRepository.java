package com.gien.gits.adapter.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.gien.gits.ontology.CaseStatus;
import com.gien.gits.ontology.OperatingCase;

/**
 * JDBC persistence adapter for {@link OperatingCase} against Flyway V001 operating_case.
 * Maps domain fields to columns: case_id, case_type, status, purpose, valid_from, valid_to,
 * recorded_at, created_by. record_version uses schema DEFAULT 0.
 */
public class JdbcOperatingCaseRepository {

    private static final String INSERT_SQL =
            "INSERT INTO operating_case " +
            "(case_id, case_type, status, purpose, valid_from, valid_to, recorded_at, created_by) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String FIND_BY_ID_SQL =
            "SELECT case_id, case_type, status, purpose, valid_from, valid_to, recorded_at, created_by " +
            "FROM operating_case WHERE case_id = ?";

    private static final String UPDATE_STATUS_SQL =
            "UPDATE operating_case SET status = ? WHERE case_id = ?";

    private final JdbcTemplate jdbcTemplate;

    public JdbcOperatingCaseRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate");
    }

    public void save(OperatingCase operatingCase) {
        if (operatingCase == null) {
            throw new IllegalArgumentException("operatingCase must not be null");
        }
        jdbcTemplate.update(INSERT_SQL,
                operatingCase.caseId().toString(),
                operatingCase.caseType(),
                operatingCase.status().name(),
                operatingCase.purpose(),
                Timestamp.from(operatingCase.validFrom()),
                operatingCase.validTo() == null ? null : Timestamp.from(operatingCase.validTo()),
                Timestamp.from(operatingCase.recordedAt()),
                operatingCase.createdBy());
    }

    public Optional<OperatingCase> findById(UUID caseId) {
        if (caseId == null) {
            throw new IllegalArgumentException("caseId must not be null");
        }
        List<OperatingCase> results = jdbcTemplate.query(
                FIND_BY_ID_SQL,
                new OperatingCaseRowMapper(),
                caseId.toString());
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /** 更新案例状态 */
    public void updateStatus(UUID caseId, CaseStatus newStatus) {
        if (caseId == null) {
            throw new IllegalArgumentException("caseId must not be null");
        }
        Objects.requireNonNull(newStatus, "newStatus");
        jdbcTemplate.update(UPDATE_STATUS_SQL, newStatus.name(), caseId.toString());
    }

    private static OperatingCase toOperatingCase(ResultSet rs) throws SQLException {
        return new OperatingCase(
                UUID.fromString(rs.getString("case_id")),
                rs.getString("case_type"),
                CaseStatus.valueOf(rs.getString("status")),
                rs.getString("purpose"),
                rs.getTimestamp("valid_from").toInstant(),
                rs.getTimestamp("valid_to") == null ? null : rs.getTimestamp("valid_to").toInstant(),
                rs.getTimestamp("recorded_at").toInstant(),
                rs.getString("created_by"));
    }

    private static final class OperatingCaseRowMapper implements RowMapper<OperatingCase> {
        @Override
        public OperatingCase mapRow(ResultSet rs, int rowNum) throws SQLException {
            return toOperatingCase(rs);
        }
    }
}
