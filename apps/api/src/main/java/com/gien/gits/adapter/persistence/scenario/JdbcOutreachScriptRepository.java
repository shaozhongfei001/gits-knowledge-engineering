package com.gien.gits.adapter.persistence.scenario;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gien.gits.engagement.OutreachScript;
import com.gien.gits.engagement.OutreachScript.TalkingPoint;
import com.gien.gits.engagement.port.WritableOutreachScriptRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * JDBC persistence adapter for OutreachScript.
 *
 * <p>Placed in the API app's adapter package because persistence-relational
 * does not depend on scenario-execute module.</p>
 */
public class JdbcOutreachScriptRepository implements WritableOutreachScriptRepository {

    private static final String MERGE_SQL = """
        MERGE INTO outreach_script (id, script_id, customer_id, rm_id, operating_case_id,
            journey_id, channel, objective, opening_line, talking_points, risk_reminders,
            closing_line, follow_up_action, created_at, updated_at)
        KEY (script_id)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
        """;

    private static final String FIND_BY_SCRIPT_ID_SQL = """
        SELECT script_id, customer_id, rm_id, operating_case_id, journey_id, channel,
            objective, opening_line, talking_points, risk_reminders,
            closing_line, follow_up_action, created_at
        FROM outreach_script WHERE script_id = ?
        """;

    private static final String FIND_BY_CUSTOMER_ID_SQL = """
        SELECT script_id, customer_id, rm_id, operating_case_id, journey_id, channel,
            objective, opening_line, talking_points, risk_reminders,
            closing_line, follow_up_action, created_at
        FROM outreach_script WHERE customer_id = ?
        ORDER BY created_at DESC
        """;

    private static final String FIND_BY_CASE_ID_SQL = """
        SELECT script_id, customer_id, rm_id, operating_case_id, journey_id, channel,
            objective, opening_line, talking_points, risk_reminders,
            closing_line, follow_up_action, created_at
        FROM outreach_script WHERE operating_case_id = ?
        ORDER BY created_at DESC
        """;

    private static final String FIND_BY_JOURNEY_ID_SQL = """
        SELECT script_id, customer_id, rm_id, operating_case_id, journey_id, channel,
            objective, opening_line, talking_points, risk_reminders,
            closing_line, follow_up_action, created_at
        FROM outreach_script WHERE journey_id = ?
        ORDER BY created_at DESC
        """;

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JdbcOutreachScriptRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    }

    @Override
    public void save(OutreachScript script) {
        String id = UUID.randomUUID().toString();
        jdbcTemplate.update(MERGE_SQL,
            id,
            script.scriptId(),
            script.customerId(),
            script.rmId(),
            script.operatingCaseId(),
            script.journeyId(),
            script.channel().name(),
            script.objective(),
            script.openingLine(),
            writeJson(script.talkingPoints()),
            writeJson(script.riskReminders()),
            script.closingLine(),
            script.followUpAction(),
            script.createdAt() != null ? Timestamp.from(script.createdAt()) : Timestamp.from(Instant.now()));
    }

    @Override
    public Optional<OutreachScript> findByScriptId(String scriptId) {
        return jdbcTemplate.query(FIND_BY_SCRIPT_ID_SQL, new ScriptRowMapper(), scriptId)
            .stream().findFirst();
    }

    @Override
    public List<OutreachScript> findByCustomerId(String customerId) {
        return jdbcTemplate.query(FIND_BY_CUSTOMER_ID_SQL, new ScriptRowMapper(), customerId);
    }

    @Override
    public List<OutreachScript> findByOperatingCaseId(String operatingCaseId) {
        return jdbcTemplate.query(FIND_BY_CASE_ID_SQL, new ScriptRowMapper(), operatingCaseId);
    }

    @Override
    public List<OutreachScript> findByJourneyId(String journeyId) {
        return jdbcTemplate.query(FIND_BY_JOURNEY_ID_SQL, new ScriptRowMapper(), journeyId);
    }

    private String writeJson(Object value) {
        if (value == null) return null;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize JSON: " + e.getMessage(), e);
        }
    }

    private <T> T readJson(String json, TypeReference<T> typeRef) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize JSON: " + e.getMessage(), e);
        }
    }

    private OutreachScript mapRow(ResultSet rs) throws SQLException {
        return new OutreachScript(
            rs.getString("script_id"),
            rs.getString("customer_id"),
            rs.getString("rm_id"),
            rs.getString("operating_case_id"),
            rs.getString("journey_id"),
            OutreachScript.OutreachChannel.valueOf(rs.getString("channel")),
            rs.getString("objective"),
            rs.getString("opening_line"),
            readJson(rs.getString("talking_points"), new TypeReference<List<TalkingPoint>>() {}),
            readJson(rs.getString("risk_reminders"), new TypeReference<List<String>>() {}),
            rs.getString("closing_line"),
            rs.getString("follow_up_action"),
            rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toInstant() : null);
    }

    private final class ScriptRowMapper implements RowMapper<OutreachScript> {
        @Override
        public OutreachScript mapRow(ResultSet rs, int rowNum) throws SQLException {
            return JdbcOutreachScriptRepository.this.mapRow(rs);
        }
    }
}
