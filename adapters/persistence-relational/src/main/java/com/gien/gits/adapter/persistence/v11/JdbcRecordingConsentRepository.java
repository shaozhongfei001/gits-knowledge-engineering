package com.gien.gits.adapter.persistence.v11;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gien.gits.action.domain.RecordingConsent;
import com.gien.gits.action.port.WritableRecordingConsentRepository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * JDBC persistence adapter for {@link RecordingConsent}
 */
public class JdbcRecordingConsentRepository implements WritableRecordingConsentRepository {

    private static final String INSERT_SQL =
        "INSERT INTO recording_consent (consent_id, interaction_id, customer_id, consent_type, " +
        "status, granted_by, granted_role, granted_at, withdrawal_reason, expires_at, legal_basis) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String FIND_BY_ID =
        "SELECT * FROM recording_consent WHERE consent_id = ?";

    private static final String FIND_BY_INTERACTION =
        "SELECT * FROM recording_consent WHERE interaction_id = ? ORDER BY granted_at DESC";

    private static final String FIND_LATEST_BY_INTERACTION =
        "SELECT * FROM recording_consent WHERE interaction_id = ? ORDER BY granted_at DESC LIMIT 1";

    private static final String FIND_BY_CUSTOMER =
        "SELECT * FROM recording_consent WHERE customer_id = ? ORDER BY granted_at DESC";

    private static final String FIND_BY_STATUS =
        "SELECT * FROM recording_consent WHERE status = ? ORDER BY granted_at DESC";

    private static final String UPDATE_STATUS =
        "UPDATE recording_consent SET status = ?, withdrawal_reason = ? WHERE consent_id = ?";

    private final JdbcTemplate jdbc;

    public JdbcRecordingConsentRepository(JdbcTemplate jdbc) {
        this.jdbc = Objects.requireNonNull(jdbc);
    }

    @Override
    public void save(RecordingConsent c) {
        jdbc.update(INSERT_SQL,
            c.consentId(), c.interactionId(), c.customerId(), c.consentType(),
            c.status(), c.grantedBy(), c.grantedRole(),
            Timestamp.from(c.grantedAt()), c.withdrawalReason(),
            c.expiresAt() != null ? Timestamp.from(c.expiresAt()) : null,
            c.legalBasis());
    }

    @Override
    public void updateStatus(String consentId, String status, String withdrawalReason) {
        jdbc.update(UPDATE_STATUS, status, withdrawalReason, consentId);
    }

    @Override
    public Optional<RecordingConsent> findByConsentId(String consentId) {
        List<RecordingConsent> results = jdbc.query(FIND_BY_ID, new ConsentRowMapper(), consentId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<RecordingConsent> findByInteractionId(String interactionId) {
        return jdbc.query(FIND_BY_INTERACTION, new ConsentRowMapper(), interactionId);
    }

    @Override
    public Optional<RecordingConsent> findLatestByInteractionId(String interactionId) {
        List<RecordingConsent> results = jdbc.query(FIND_LATEST_BY_INTERACTION, new ConsentRowMapper(), interactionId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<RecordingConsent> findByCustomerId(String customerId) {
        return jdbc.query(FIND_BY_CUSTOMER, new ConsentRowMapper(), customerId);
    }

    @Override
    public List<RecordingConsent> findByStatus(String status) {
        return jdbc.query(FIND_BY_STATUS, new ConsentRowMapper(), status);
    }

    private static final class ConsentRowMapper implements RowMapper<RecordingConsent> {
        @Override
        public RecordingConsent mapRow(ResultSet rs, int rowNum) throws SQLException {
            Timestamp expiresAtTs = rs.getTimestamp("expires_at");
            return new RecordingConsent(
                rs.getString("consent_id"), rs.getString("interaction_id"),
                rs.getString("customer_id"), rs.getString("consent_type"),
                rs.getString("status"), rs.getString("granted_by"),
                rs.getString("granted_role"), rs.getTimestamp("granted_at").toInstant(),
                rs.getString("withdrawal_reason"),
                expiresAtTs != null ? expiresAtTs.toInstant() : null,
                rs.getString("legal_basis"));
        }
    }
}
