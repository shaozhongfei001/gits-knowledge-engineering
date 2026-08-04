package com.gien.gits.adapter.persistence;

import com.gien.gits.ontology.FactReconciliationCase;
import com.gien.gits.ontology.port.WritableFactReconciliationRepository;
import com.gien.gits.ontology.ReconciliationStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class JdbcFactReconciliationRepository implements WritableFactReconciliationRepository {

    private static final String INSERT_SQL = """
        INSERT INTO fact_reconciliation_case (reconciliation_id, case_id, topic,
            structured_fact, interaction_claim, external_fact, ontology_distinction,
            correct_judgment, wrong_output_examples, next_action, status, resolved_at,
            created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        """;

    private static final String FIND_BY_ID_SQL = """
        SELECT reconciliation_id, case_id, topic, structured_fact, interaction_claim,
            external_fact, ontology_distinction, correct_judgment, wrong_output_examples,
            next_action, status, resolved_at
        FROM fact_reconciliation_case WHERE reconciliation_id = ?
        """;

    private static final String FIND_BY_CASE_SQL = FIND_BY_ID_SQL.replace("WHERE reconciliation_id = ?", "WHERE case_id = ?");

    private static final String UPDATE_STATUS_SQL = """
        UPDATE fact_reconciliation_case SET status = ?, resolved_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
        WHERE reconciliation_id = ?
        """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcFactReconciliationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate");
    }

    public void save(FactReconciliationCase rec) {
        jdbcTemplate.update(INSERT_SQL,
            rec.reconciliationId(), rec.caseId(), rec.topic(),
            rec.structuredFact(), rec.interactionClaim(), rec.externalFact(),
            JsonHelper.toJsonArray(rec.ontologyDistinction()), rec.correctJudgment(),
            JsonHelper.toJsonArray(rec.wrongOutputExamples()), rec.nextAction(),
            rec.status().name(), null);
    }

    public Optional<FactReconciliationCase> findByReconciliationId(String reconciliationId) {
        return jdbcTemplate.query(FIND_BY_ID_SQL, new FactRecRowMapper(), reconciliationId).stream().findFirst();
    }

    public List<FactReconciliationCase> findByCaseId(String caseId) {
        return jdbcTemplate.query(FIND_BY_CASE_SQL, new FactRecRowMapper(), caseId);
    }

    public void updateStatus(String reconciliationId, ReconciliationStatus status) {
        jdbcTemplate.update(UPDATE_STATUS_SQL, status.name(), reconciliationId);
    }

    private static FactReconciliationCase toCase(ResultSet rs) throws SQLException {
        return new FactReconciliationCase(
            rs.getString("reconciliation_id"), rs.getString("case_id"), rs.getString("topic"),
            rs.getString("structured_fact"), rs.getString("interaction_claim"), rs.getString("external_fact"),
            JsonHelper.parseStringList(rs.getString("ontology_distinction")),
            rs.getString("correct_judgment"),
            JsonHelper.parseStringList(rs.getString("wrong_output_examples")),
            rs.getString("next_action"),
            ReconciliationStatus.valueOf(rs.getString("status")));
    }

    private static final class FactRecRowMapper implements RowMapper<FactReconciliationCase> {
        @Override
        public FactReconciliationCase mapRow(ResultSet rs, int rowNum) throws SQLException {
            return toCase(rs);
        }
    }
}
