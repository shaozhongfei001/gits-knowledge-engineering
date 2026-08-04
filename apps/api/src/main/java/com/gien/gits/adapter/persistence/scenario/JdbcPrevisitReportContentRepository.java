package com.gien.gits.adapter.persistence.scenario;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gien.gits.engagement.PrevisitReportContent;
import com.gien.gits.engagement.port.WritablePrevisitReportContentRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * JDBC persistence adapter for PrevisitReportContent.
 *
 * <p>Placed in the API app's adapter package because persistence-relational
 * does not depend on scenario-hermes module.</p>
 */
public class JdbcPrevisitReportContentRepository implements WritablePrevisitReportContentRepository {

    private static final String INSERT_SQL = """
        INSERT INTO previsit_report_content (id, report_id, journey_id, operating_case_id,
            customer_id, rm_id, visit_objective, content_json,
            created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        """;

    private static final String FIND_BY_REPORT_ID_SQL = """
        SELECT id, report_id, journey_id, operating_case_id,
            customer_id, rm_id, visit_objective, content_json
        FROM previsit_report_content WHERE report_id = ?
        """;

    private static final String FIND_LATEST_BY_CASE_SQL = """
        SELECT id, report_id, journey_id, operating_case_id,
            customer_id, rm_id, visit_objective, content_json
        FROM previsit_report_content WHERE operating_case_id = ?
        ORDER BY created_at DESC LIMIT 1
        """;

    private static final String FIND_BY_JOURNEY_SQL = """
        SELECT id, report_id, journey_id, operating_case_id,
            customer_id, rm_id, visit_objective, content_json
        FROM previsit_report_content WHERE journey_id = ?
        ORDER BY created_at DESC LIMIT 1
        """;

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JdbcPrevisitReportContentRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    }

    @Override
    public void save(PrevisitReportContent content, String journeyId, String operatingCaseId) {
        String id = UUID.randomUUID().toString();
        jdbcTemplate.update(INSERT_SQL,
            id,
            content.reportId(),
            journeyId,
            operatingCaseId,
            content.customerId(),
            content.rmName(),
            content.visitObjective(),
            writeJson(content));
    }

    @Override
    public Optional<PrevisitReportContent> findByReportId(String reportId) {
        return jdbcTemplate.query(FIND_BY_REPORT_ID_SQL, new ContentRowMapper(), reportId)
            .stream().findFirst();
    }

    @Override
    public Optional<PrevisitReportContent> findLatestByOperatingCaseId(String operatingCaseId) {
        return jdbcTemplate.query(FIND_LATEST_BY_CASE_SQL, new ContentRowMapper(), operatingCaseId)
            .stream().findFirst();
    }

    @Override
    public Optional<PrevisitReportContent> findByJourneyId(String journeyId) {
        return jdbcTemplate.query(FIND_BY_JOURNEY_SQL, new ContentRowMapper(), journeyId)
            .stream().findFirst();
    }

    private String writeJson(Object value) {
        if (value == null) return null;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize JSON: " + e.getMessage(), e);
        }
    }

    private PrevisitReportContent readContentJson(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, PrevisitReportContent.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize PrevisitReportContent: " + e.getMessage(), e);
        }
    }

    private PrevisitReportContent mapRow(ResultSet rs) throws SQLException {
        return readContentJson(rs.getString("content_json"));
    }

    private final class ContentRowMapper implements RowMapper<PrevisitReportContent> {
        @Override
        public PrevisitReportContent mapRow(ResultSet rs, int rowNum) throws SQLException {
            return JdbcPrevisitReportContentRepository.this.mapRow(rs);
        }
    }
}
