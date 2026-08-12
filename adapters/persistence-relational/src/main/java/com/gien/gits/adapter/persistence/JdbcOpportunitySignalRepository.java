package com.gien.gits.adapter.persistence;

import com.gien.gits.ontology.OpportunitySignal;
import com.gien.gits.ontology.OpportunitySignal.SignalStatus;
import com.gien.gits.ontology.OpportunitySignal.SignalType;
import com.gien.gits.ontology.OpportunitySignal.SignalSourceType;
import com.gien.gits.ontology.port.WritableOpportunitySignalRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class JdbcOpportunitySignalRepository implements WritableOpportunitySignalRepository {

    private static final String INSERT_SQL = """
        INSERT INTO opportunity_signal (signal_id, operating_case_id, journey_id, signal_type,
            content, source_type, source_ref, confidence, status, evidence_ref,
            detected_at, confirmed_at, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        """;

    private static final String FIND_BY_ID_SQL = """
        SELECT signal_id, operating_case_id, journey_id, signal_type, content, source_type,
            source_ref, confidence, status, evidence_ref, detected_at, confirmed_at
        FROM opportunity_signal WHERE signal_id = ?
        """;

    private static final String FIND_BY_CASE_SQL = FIND_BY_ID_SQL.replace("WHERE signal_id = ?", "WHERE operating_case_id = ?");

    private static final String UPDATE_STATUS_CONFIRM_SQL = """
        UPDATE opportunity_signal SET status = ?, confirmed_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP WHERE signal_id = ?
        """;

    private static final String UPDATE_STATUS_DISMISS_SQL = """
        UPDATE opportunity_signal SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE signal_id = ?
        """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcOpportunitySignalRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate");
    }

    public void save(OpportunitySignal signal) {
        jdbcTemplate.update(INSERT_SQL,
            signal.signalId().toString(), signal.operatingCaseId(), signal.journeyId(),
            signal.signalType().name(), signal.content(), signal.sourceType().name(),
            signal.sourceRef(), signal.confidence(), signal.status().name(),
            signal.evidenceRef(),
            signal.detectedAt() != null ? Timestamp.from(signal.detectedAt()) : null,
            signal.confirmedAt() != null ? Timestamp.from(signal.confirmedAt()) : null);
    }

    public Optional<OpportunitySignal> findById(UUID signalId) {
        return jdbcTemplate.query(FIND_BY_ID_SQL, new SignalRowMapper(), signalId.toString()).stream().findFirst();
    }

    public List<OpportunitySignal> findByOperatingCaseId(String operatingCaseId) {
        return jdbcTemplate.query(FIND_BY_CASE_SQL, new SignalRowMapper(), operatingCaseId);
    }

    public void updateStatus(UUID signalId, SignalStatus status) {
        String sql = (status == SignalStatus.CONFIRMED) ? UPDATE_STATUS_CONFIRM_SQL : UPDATE_STATUS_DISMISS_SQL;
        int rows = jdbcTemplate.update(sql, status.name(), signalId.toString());
        if (rows == 0) {
            throw new java.util.NoSuchElementException("Signal not found: " + signalId);
        }
    }

    private static OpportunitySignal toSignal(ResultSet rs) throws SQLException {
        Timestamp detectedAt = rs.getTimestamp("detected_at");
        Timestamp confirmedAt = rs.getTimestamp("confirmed_at");
        BigDecimal confidence = rs.getBigDecimal("confidence");
        return new OpportunitySignal(
            UUID.fromString(rs.getString("signal_id")), rs.getString("operating_case_id"),
            rs.getString("journey_id"), SignalType.valueOf(rs.getString("signal_type")),
            rs.getString("content"), SignalSourceType.valueOf(rs.getString("source_type")),
            rs.getString("source_ref"), confidence,
            SignalStatus.valueOf(rs.getString("status")), rs.getString("evidence_ref"),
            detectedAt != null ? detectedAt.toInstant() : null,
            confirmedAt != null ? confirmedAt.toInstant() : null);
    }

    private static final class SignalRowMapper implements RowMapper<OpportunitySignal> {
        @Override
        public OpportunitySignal mapRow(ResultSet rs, int rowNum) throws SQLException {
            return toSignal(rs);
        }
    }
}
