package com.gien.gits.adapter.persistence.scenario;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gien.gits.engagement.InteractionExtraction;
import com.gien.gits.engagement.PostvisitAnalysisContent;
import com.gien.gits.engagement.PostvisitAnalysisContent.CommitmentItem;
import com.gien.gits.engagement.PostvisitAnalysisContent.FactReconciliationItem;
import com.gien.gits.engagement.PostvisitAnalysisContent.OpportunitySignalItem;
import com.gien.gits.engagement.port.WritablePostvisitAnalysisContentRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * JDBC persistence adapter for PostvisitAnalysisContent.
 *
 * <p>Placed in the API app's adapter package because persistence-relational
 * does not depend on scenario-execute module.</p>
 */
public class JdbcPostvisitAnalysisContentRepository implements WritablePostvisitAnalysisContentRepository {

    private static final String INSERT_SQL = """
        INSERT INTO postvisit_analysis_content (id, analysis_id, journey_id, operating_case_id,
            visit_summary, key_findings_json, opportunity_signals_json, commitments_json,
            reconciliation_items_json, follow_up_actions_json, next_step_recommendation,
            created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        """;

    private static final String FIND_BY_ANALYSIS_ID_SQL = """
        SELECT id, analysis_id, journey_id, operating_case_id, visit_summary,
            key_findings_json, opportunity_signals_json, commitments_json,
            reconciliation_items_json, follow_up_actions_json, next_step_recommendation
        FROM postvisit_analysis_content WHERE analysis_id = ?
        """;

    private static final String FIND_LATEST_BY_CASE_SQL = """
        SELECT id, analysis_id, journey_id, operating_case_id, visit_summary,
            key_findings_json, opportunity_signals_json, commitments_json,
            reconciliation_items_json, follow_up_actions_json, next_step_recommendation
        FROM postvisit_analysis_content WHERE operating_case_id = ?
        ORDER BY created_at DESC LIMIT 1
        """;

    private static final String FIND_BY_JOURNEY_SQL = """
        SELECT id, analysis_id, journey_id, operating_case_id, visit_summary,
            key_findings_json, opportunity_signals_json, commitments_json,
            reconciliation_items_json, follow_up_actions_json, next_step_recommendation
        FROM postvisit_analysis_content WHERE journey_id = ?
        ORDER BY created_at DESC LIMIT 1
        """;

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JdbcPostvisitAnalysisContentRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    }

    @Override
    public void save(PostvisitAnalysisContent content, String operatingCaseId) {
        String id = UUID.randomUUID().toString();
        jdbcTemplate.update(INSERT_SQL,
            id,
            content.analysisId(),
            content.journeyId(),
            operatingCaseId,
            content.visitSummary(),
            writeJson(content.keyFindings()),
            writeJson(content.opportunitySignals()),
            writeJson(content.commitments()),
            writeJson(content.reconciliationItems()),
            writeJson(content.followUpActions()),
            content.nextStepRecommendation());
    }

    @Override
    public Optional<PostvisitAnalysisContent> findByAnalysisId(String analysisId) {
        return jdbcTemplate.query(FIND_BY_ANALYSIS_ID_SQL, new ContentRowMapper(), analysisId)
            .stream().findFirst();
    }

    @Override
    public Optional<PostvisitAnalysisContent> findLatestByOperatingCaseId(String operatingCaseId) {
        return jdbcTemplate.query(FIND_LATEST_BY_CASE_SQL, new ContentRowMapper(), operatingCaseId)
            .stream().findFirst();
    }

    @Override
    public Optional<PostvisitAnalysisContent> findByJourneyId(String journeyId) {
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

    private <T> T readJson(String json, TypeReference<T> typeRef) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize JSON: " + e.getMessage(), e);
        }
    }

    private PostvisitAnalysisContent mapRow(ResultSet rs) throws SQLException {
        return new PostvisitAnalysisContent(
            rs.getString("analysis_id"),
            rs.getString("journey_id"),
            rs.getString("visit_summary"),
            readJson(rs.getString("key_findings_json"), new TypeReference<List<InteractionExtraction>>() {}),
            readJson(rs.getString("opportunity_signals_json"), new TypeReference<List<OpportunitySignalItem>>() {}),
            readJson(rs.getString("commitments_json"), new TypeReference<List<CommitmentItem>>() {}),
            readJson(rs.getString("reconciliation_items_json"), new TypeReference<List<FactReconciliationItem>>() {}),
            readJson(rs.getString("follow_up_actions_json"), new TypeReference<List<String>>() {}),
            rs.getString("next_step_recommendation"));
    }

    private final class ContentRowMapper implements RowMapper<PostvisitAnalysisContent> {
        @Override
        public PostvisitAnalysisContent mapRow(ResultSet rs, int rowNum) throws SQLException {
            return JdbcPostvisitAnalysisContentRepository.this.mapRow(rs);
        }
    }
}
