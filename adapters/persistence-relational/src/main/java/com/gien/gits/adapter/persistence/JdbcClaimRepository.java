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

import com.gien.gits.ontology.Claim;
import com.gien.gits.ontology.ClaimStatus;

/**
 * JDBC persistence adapter for {@link Claim} against Flyway V001/V002 claim table.
 */
public class JdbcClaimRepository {

    private static final String INSERT_SQL =
            "INSERT INTO claim " +
            "(claim_id, case_id, interaction_id, claim_type, claim_status, statement_text, " +
            " valid_from, valid_to, recorded_at, supersedes_claim_id, model_run_id) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String FIND_BY_ID_SQL =
            "SELECT claim_id, case_id, interaction_id, claim_type, claim_status, statement_text, " +
            " valid_from, valid_to, recorded_at, supersedes_claim_id, model_run_id " +
            "FROM claim WHERE claim_id = ?";

    private static final String FIND_BY_CASE_SQL =
            "SELECT claim_id, case_id, interaction_id, claim_type, claim_status, statement_text, " +
            " valid_from, valid_to, recorded_at, supersedes_claim_id, model_run_id " +
            "FROM claim WHERE case_id = ? ORDER BY recorded_at";

    private static final String UPDATE_STATUS_SQL =
            "UPDATE claim SET claim_status = ? WHERE claim_id = ?";

    private final JdbcTemplate jdbcTemplate;

    public JdbcClaimRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate");
    }

    public void save(Claim claim) {
        if (claim == null) {
            throw new IllegalArgumentException("claim must not be null");
        }
        jdbcTemplate.update(INSERT_SQL,
                claim.claimId().toString(),
                claim.caseId().toString(),
                null, // interaction_id — not set at creation time
                claim.claimType(),
                claim.status().name(),
                claim.statement(),
                claim.validFrom() == null ? null : Timestamp.from(claim.validFrom()),
                claim.validTo() == null ? null : Timestamp.from(claim.validTo()),
                Timestamp.from(claim.recordedAt()),
                claim.supersedesClaimId() == null ? null : claim.supersedesClaimId().toString(),
                null); // model_run_id
    }

    public Optional<Claim> findById(UUID claimId) {
        if (claimId == null) {
            throw new IllegalArgumentException("claimId must not be null");
        }
        List<Claim> results = jdbcTemplate.query(FIND_BY_ID_SQL, new ClaimRowMapper(), claimId.toString());
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<Claim> findByCaseId(UUID caseId) {
        if (caseId == null) {
            throw new IllegalArgumentException("caseId must not be null");
        }
        return jdbcTemplate.query(FIND_BY_CASE_SQL, new ClaimRowMapper(), caseId.toString());
    }

    public void updateStatus(UUID claimId, ClaimStatus newStatus) {
        Objects.requireNonNull(claimId, "claimId");
        Objects.requireNonNull(newStatus, "newStatus");
        jdbcTemplate.update(UPDATE_STATUS_SQL, newStatus.name(), claimId.toString());
    }

    private static Claim toClaim(ResultSet rs) throws SQLException {
        String interactionIdStr = rs.getString("interaction_id");
        String supersedesStr = rs.getString("supersedes_claim_id");
        Timestamp validFromTs = rs.getTimestamp("valid_from");
        Timestamp validToTs = rs.getTimestamp("valid_to");
        return new Claim(
                UUID.fromString(rs.getString("claim_id")),
                UUID.fromString(rs.getString("case_id")),
                rs.getString("claim_type"),
                ClaimStatus.valueOf(rs.getString("claim_status")),
                rs.getString("statement_text"),
                validFromTs == null ? null : validFromTs.toInstant(),
                validToTs == null ? null : validToTs.toInstant(),
                rs.getTimestamp("recorded_at").toInstant(),
                supersedesStr == null ? null : UUID.fromString(supersedesStr));
    }

    private static final class ClaimRowMapper implements RowMapper<Claim> {
        @Override
        public Claim mapRow(ResultSet rs, int rowNum) throws SQLException {
            return toClaim(rs);
        }
    }
}
