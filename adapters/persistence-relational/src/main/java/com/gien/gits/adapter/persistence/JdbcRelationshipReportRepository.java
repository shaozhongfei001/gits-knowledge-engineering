package com.gien.gits.adapter.persistence;

import com.gien.gits.ontology.RelationshipReport;
import com.gien.gits.ontology.RelationshipReport.ReportType;
import com.gien.gits.ontology.port.WritableRelationshipReportRepository;
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

public class JdbcRelationshipReportRepository implements WritableRelationshipReportRepository {

    private static final String INSERT_SQL = """
        INSERT INTO relationship_report (report_id, operating_case_id, journey_id, report_type,
            content, based_on_evidence, based_on_reconciliations, generated_at, supersedes_report_id,
            created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        """;

    private static final String FIND_BY_ID_SQL = """
        SELECT report_id, operating_case_id, journey_id, report_type, content,
            based_on_evidence, based_on_reconciliations, generated_at, supersedes_report_id
        FROM relationship_report WHERE report_id = ?
        """;

    private static final String FIND_BY_CASE_SQL = FIND_BY_ID_SQL.replace("WHERE report_id = ?", "WHERE operating_case_id = ?");

    private static final String FIND_LATEST_BY_JOURNEY_SQL = FIND_BY_ID_SQL.replace(
        "WHERE report_id = ?", "WHERE journey_id = ? ORDER BY generated_at DESC LIMIT 1");

    private final JdbcTemplate jdbcTemplate;

    public JdbcRelationshipReportRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate");
    }

    public void save(RelationshipReport report) {
        jdbcTemplate.update(INSERT_SQL,
            report.reportId().toString(), report.operatingCaseId(), report.journeyId(),
            report.reportType().name(), report.content(),
            JsonHelper.toJsonArray(report.basedOnEvidence()),
            JsonHelper.toJsonArray(report.basedOnReconciliations()),
            report.generatedAt() != null ? Timestamp.from(report.generatedAt()) : null,
            report.supersedesReportId() != null ? report.supersedesReportId().toString() : null);
    }

    public Optional<RelationshipReport> findById(UUID reportId) {
        return jdbcTemplate.query(FIND_BY_ID_SQL, new ReportRowMapper(), reportId.toString()).stream().findFirst();
    }

    public List<RelationshipReport> findByOperatingCaseId(String operatingCaseId) {
        return jdbcTemplate.query(FIND_BY_CASE_SQL, new ReportRowMapper(), operatingCaseId);
    }

    public Optional<RelationshipReport> findLatestByJourneyId(String journeyId) {
        return jdbcTemplate.query(FIND_LATEST_BY_JOURNEY_SQL, new ReportRowMapper(), journeyId).stream().findFirst();
    }

    private static RelationshipReport toReport(ResultSet rs) throws SQLException {
        Timestamp generatedAt = rs.getTimestamp("generated_at");
        String supersedesStr = rs.getString("supersedes_report_id");
        return new RelationshipReport(
            UUID.fromString(rs.getString("report_id")), rs.getString("operating_case_id"),
            rs.getString("journey_id"), ReportType.valueOf(rs.getString("report_type")),
            rs.getString("content"),
            JsonHelper.parseStringList(rs.getString("based_on_evidence")),
            JsonHelper.parseStringList(rs.getString("based_on_reconciliations")),
            generatedAt != null ? generatedAt.toInstant() : null,
            supersedesStr != null ? UUID.fromString(supersedesStr) : null);
    }

    private static final class ReportRowMapper implements RowMapper<RelationshipReport> {
        @Override
        public RelationshipReport mapRow(ResultSet rs, int rowNum) throws SQLException {
            return toReport(rs);
        }
    }
}
