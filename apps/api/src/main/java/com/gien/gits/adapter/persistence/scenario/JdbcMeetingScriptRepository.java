package com.gien.gits.adapter.persistence.scenario;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gien.gits.engagement.MeetingScript;
import com.gien.gits.engagement.MeetingScript.AgendaItem;
import com.gien.gits.engagement.MeetingScript.KycQuestionItem;
import com.gien.gits.engagement.MeetingScript.ProductDiscussionItem;
import com.gien.gits.engagement.port.WritableMeetingScriptRepository;
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
 * JDBC persistence adapter for MeetingScript.
 *
 * <p>Placed in the API app's adapter package because persistence-relational
 * does not depend on scenario-hermes module.</p>
 */
public class JdbcMeetingScriptRepository implements WritableMeetingScriptRepository {

    private static final String MERGE_SQL = """
        MERGE INTO meeting_script (id, script_id, customer_id, rm_id, operating_case_id,
            journey_id, meeting_objective, previsit_summary, agenda_items, kyc_questions,
            product_discussions, risk_points, closing_summary, created_at, updated_at)
        KEY (script_id)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
        """;

    private static final String FIND_BY_SCRIPT_ID_SQL = """
        SELECT script_id, customer_id, rm_id, operating_case_id, journey_id,
            meeting_objective, previsit_summary, agenda_items, kyc_questions,
            product_discussions, risk_points, closing_summary, created_at
        FROM meeting_script WHERE script_id = ?
        """;

    private static final String FIND_BY_CUSTOMER_ID_SQL = """
        SELECT script_id, customer_id, rm_id, operating_case_id, journey_id,
            meeting_objective, previsit_summary, agenda_items, kyc_questions,
            product_discussions, risk_points, closing_summary, created_at
        FROM meeting_script WHERE customer_id = ?
        ORDER BY created_at DESC
        """;

    private static final String FIND_BY_CASE_ID_SQL = """
        SELECT script_id, customer_id, rm_id, operating_case_id, journey_id,
            meeting_objective, previsit_summary, agenda_items, kyc_questions,
            product_discussions, risk_points, closing_summary, created_at
        FROM meeting_script WHERE operating_case_id = ?
        ORDER BY created_at DESC
        """;

    private static final String FIND_BY_JOURNEY_ID_SQL = """
        SELECT script_id, customer_id, rm_id, operating_case_id, journey_id,
            meeting_objective, previsit_summary, agenda_items, kyc_questions,
            product_discussions, risk_points, closing_summary, created_at
        FROM meeting_script WHERE journey_id = ?
        ORDER BY created_at DESC
        """;

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JdbcMeetingScriptRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    }

    @Override
    public void save(MeetingScript script) {
        String id = UUID.randomUUID().toString();
        jdbcTemplate.update(MERGE_SQL,
            id,
            script.scriptId(),
            script.customerId(),
            script.rmId(),
            script.operatingCaseId(),
            script.journeyId(),
            script.meetingObjective(),
            script.previsitSummary(),
            writeJson(script.agendaItems()),
            writeJson(script.kycQuestions()),
            writeJson(script.productDiscussions()),
            writeJson(script.riskPoints()),
            script.closingSummary(),
            script.createdAt() != null ? Timestamp.from(script.createdAt()) : Timestamp.from(Instant.now()));
    }

    @Override
    public Optional<MeetingScript> findByScriptId(String scriptId) {
        return jdbcTemplate.query(FIND_BY_SCRIPT_ID_SQL, new ScriptRowMapper(), scriptId)
            .stream().findFirst();
    }

    @Override
    public List<MeetingScript> findByCustomerId(String customerId) {
        return jdbcTemplate.query(FIND_BY_CUSTOMER_ID_SQL, new ScriptRowMapper(), customerId);
    }

    @Override
    public List<MeetingScript> findByOperatingCaseId(String operatingCaseId) {
        return jdbcTemplate.query(FIND_BY_CASE_ID_SQL, new ScriptRowMapper(), operatingCaseId);
    }

    @Override
    public List<MeetingScript> findByJourneyId(String journeyId) {
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

    private MeetingScript mapRow(ResultSet rs) throws SQLException {
        return new MeetingScript(
            rs.getString("script_id"),
            rs.getString("customer_id"),
            rs.getString("rm_id"),
            rs.getString("operating_case_id"),
            rs.getString("journey_id"),
            rs.getString("meeting_objective"),
            rs.getString("previsit_summary"),
            readJson(rs.getString("agenda_items"), new TypeReference<List<AgendaItem>>() {}),
            readJson(rs.getString("kyc_questions"), new TypeReference<List<KycQuestionItem>>() {}),
            readJson(rs.getString("product_discussions"), new TypeReference<List<ProductDiscussionItem>>() {}),
            readJson(rs.getString("risk_points"), new TypeReference<List<String>>() {}),
            rs.getString("closing_summary"),
            rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toInstant() : null);
    }

    private final class ScriptRowMapper implements RowMapper<MeetingScript> {
        @Override
        public MeetingScript mapRow(ResultSet rs, int rowNum) throws SQLException {
            return JdbcMeetingScriptRepository.this.mapRow(rs);
        }
    }
}
