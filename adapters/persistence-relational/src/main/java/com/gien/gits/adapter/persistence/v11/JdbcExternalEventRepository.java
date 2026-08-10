package com.gien.gits.adapter.persistence.v11;

import com.gien.gits.adapter.persistence.JsonHelper;
import com.gien.gits.ontology.ExternalEvent;
import com.gien.gits.ontology.port.ExternalEventRepository;
import com.gien.gits.ontology.port.WritableExternalEventRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * V1.1 外部事件JDBC适配器 — 列名对齐V004+V011迁移
 */
public class JdbcExternalEventRepository implements WritableExternalEventRepository {

    private static final String INSERT_SQL = """
        INSERT INTO external_event (event_id, event_date, source_type, source_name, entity,
            title, content, confidence, reliability, bank_use_allowed,
            linked_themes, possible_business_signal, no_go_statement, evidence_ref)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

    private static final String FIND_BY_ID = "SELECT * FROM external_event WHERE event_id = ?";
    private static final String FIND_BY_SOURCE_TYPE = "SELECT * FROM external_event WHERE source_type = ? ORDER BY event_date DESC";
    private static final String FIND_BY_ENTITY = "SELECT * FROM external_event WHERE entity = ? ORDER BY event_date DESC";
    private static final String FIND_RECENT = "SELECT * FROM external_event ORDER BY event_date DESC LIMIT ?";

    private final JdbcTemplate jdbc;

    public JdbcExternalEventRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void save(ExternalEvent e) {
        jdbc.update(INSERT_SQL,
                e.eventId(),
                e.eventDate(),
                e.sourceType() != null ? e.sourceType().name() : null,
                e.sourceName(),
                e.entity(),
                e.title(),
                e.content(),
                e.confidence() != null ? e.confidence().name() : null,
                e.reliability() != null ? e.reliability().name() : null,
                e.bankUseAllowed(),
                JsonHelper.toJsonArray(e.linkedThemes()),
                e.possibleBusinessSignal(),
                e.noGoStatement(),
                e.evidenceRef());
    }

    @Override
    public Optional<ExternalEvent> findByEventId(String eventId) {
        return jdbc.query(FIND_BY_ID, rowMapper(), eventId).stream().findFirst();
    }

    @Override
    public List<ExternalEvent> findByEventType(String eventType) {
        return jdbc.query(FIND_BY_SOURCE_TYPE, rowMapper(), eventType);
    }

    @Override
    public List<ExternalEvent> findByAffectedCustomerId(String customerId) {
        // V004 schema doesn't have affected_customer_ids as a proper FK; search via entity
        return jdbc.query(FIND_BY_ENTITY, rowMapper(), customerId);
    }

    @Override
    public List<ExternalEvent> findByAffectedIndustry(String industry) {
        // V004 schema uses linked_themes JSON array; search via LIKE
        return jdbc.query("SELECT * FROM external_event WHERE linked_themes LIKE ? ORDER BY event_date DESC",
                rowMapper(), "%" + industry + "%");
    }

    @Override
    public List<ExternalEvent> findBySeverity(String severity) {
        // V011 added severity column
        return jdbc.query("SELECT * FROM external_event WHERE severity = ? ORDER BY event_date DESC",
                rowMapper(), severity);
    }

    @Override
    public List<ExternalEvent> findByEntity(String entity) {
        return jdbc.query(FIND_BY_ENTITY, rowMapper(), entity);
    }

    @Override
    public List<ExternalEvent> findRecent(int limit) {
        return jdbc.query(FIND_RECENT, rowMapper(), limit);
    }

    @Override
    public List<ExternalEvent> findAll() {
        return jdbc.query("SELECT * FROM external_event ORDER BY event_date DESC", rowMapper());
    }

    private RowMapper<ExternalEvent> rowMapper() {
        return (rs, rowNum) -> mapRow(rs);
    }

    private ExternalEvent mapRow(ResultSet rs) throws SQLException {
        return new ExternalEvent(
                rs.getString("event_id"),
                rs.getObject("event_date", LocalDate.class),
                rs.getString("source_type"),
                rs.getString("source_name"),
                rs.getString("entity"),
                rs.getString("title"),
                rs.getString("content"),
                rs.getString("confidence"),
                rs.getString("reliability"),
                rs.getBoolean("bank_use_allowed"),
                JsonHelper.parseStringList(rs.getString("linked_themes")),
                rs.getString("possible_business_signal"),
                rs.getString("no_go_statement"),
                rs.getString("evidence_ref")
        );
    }
}
