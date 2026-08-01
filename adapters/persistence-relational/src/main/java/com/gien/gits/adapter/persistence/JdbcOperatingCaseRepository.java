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
 * JDBC persistence adapter for {@link OperatingCase} against the operating_case table
 * defined by Flyway migration V001.
 *
 * <p>Maps only the domain fields that exist as columns in V001:
 * case_id, case_type, status, purpose, valid_from, valid_to, recorded_at.
 * The remaining V001 columns (record_version, created_by) are not represented in the
 * domain record and are left to their schema defaults (record_version DEFAULT 0;
 * created_by requires a default supplied by the environment/test DDL).
 */
public class JdbcOperatingCaseRepository {

    private static final String INSERT_SQL =
            "INSERT INTO operating_case " +
            "(case_id, case_type, status, purpose, valid_from, valid_to, recorded_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String FIND_BY_ID_SQL =
            "SELECT case_id, case_type, status, purpose, valid_from, valid_to, recorded_at " +
            "FROM operating_case WHERE case_id = ?";

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
                Timestamp.from(operatingCase.recordedAt()));
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

    private static OperatingCase toOperatingCase(ResultSet rs) throws SQLException {
        return new OperatingCase(
                UUID.fromString(rs.getString("case_id")),
                rs.getString("case_type"),
                CaseStatus.valueOf(rs.getString("status")),
                rs.getString("purpose"),
                rs.getTimestamp("valid_from").toInstant(),
                rs.getTimestamp("valid_to") == null ? null : rs.getTimestamp("valid_to").toInstant(),
                rs.getTimestamp("recorded_at").toInstant());
    }

    private static final class OperatingCaseRowMapper implements RowMapper<OperatingCase> {
        @Override
        public OperatingCase mapRow(ResultSet rs, int rowNum) throws SQLException {
            return toOperatingCase(rs);
        }
    }
}
