package com.gien.gits.adapter.persistence;

import com.gien.gits.ontology.ExternalEvent;
import com.gien.gits.ontology.ExternalEvent.SourceType;
import com.gien.gits.ontology.ExternalEvent.Confidence;
import com.gien.gits.ontology.ExternalEvent.Reliability;
import com.gien.gits.ontology.port.WritableExternalEventRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class JdbcExternalEventRepository implements WritableExternalEventRepository {

    private static final String INSERT_SQL = """
        INSERT INTO external_event (event_id, event_date, source_type, source_name, entity,
            title, content, confidence, reliability, bank_use_allowed, linked_themes,
            possible_business_signal, no_go_statement, evidence_ref, created_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
        """;

    private static final String FIND_BY_ID_SQL = """
        SELECT event_id, event_date, source_type, source_name, entity, title, content,
            confidence, reliability, bank_use_allowed, linked_themes,
            possible_business_signal, no_go_statement, evidence_ref
        FROM external_event WHERE event_id = ?
        """;

    private static final String FIND_BY_ENTITY_SQL = FIND_BY_ID_SQL.replace("WHERE event_id = ?", "WHERE entity = ?");

    private static final String FIND_RECENT_SQL = """
        SELECT event_id, event_date, source_type, source_name, entity, title, content,
            confidence, reliability, bank_use_allowed, linked_themes,
            possible_business_signal, no_go_statement, evidence_ref
        FROM external_event ORDER BY event_date DESC LIMIT ?
        """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcExternalEventRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate");
    }

    public void save(ExternalEvent event) {
        jdbcTemplate.update(INSERT_SQL,
            event.eventId(), event.eventDate(), event.sourceType().name(), event.sourceName(),
            event.entity(), event.title(), event.content(), event.confidence().name(),
            event.reliability().name(), event.bankUseAllowed(),
            JsonHelper.toJsonArray(event.linkedThemes()),
            event.possibleBusinessSignal(), event.noGoStatement(), event.evidenceRef());
    }

    public Optional<ExternalEvent> findByEventId(String eventId) {
        return jdbcTemplate.query(FIND_BY_ID_SQL, new ExternalEventRowMapper(), eventId).stream().findFirst();
    }

    public List<ExternalEvent> findByEntity(String entity) {
        return jdbcTemplate.query(FIND_BY_ENTITY_SQL, new ExternalEventRowMapper(), entity);
    }

    public List<ExternalEvent> findRecent(int limit) {
        return jdbcTemplate.query(FIND_RECENT_SQL, new ExternalEventRowMapper(), limit);
    }

    private static ExternalEvent toExternalEvent(ResultSet rs) throws SQLException {
        return new ExternalEvent(
            rs.getString("event_id"), rs.getObject("event_date", LocalDate.class),
            safeEnum(SourceType.class, rs.getString("source_type")),
            rs.getString("source_name"), rs.getString("entity"),
            rs.getString("title"), rs.getString("content"),
            safeEnum(Confidence.class, rs.getString("confidence")),
            safeEnum(Reliability.class, rs.getString("reliability")),
            rs.getBoolean("bank_use_allowed"),
            JsonHelper.parseStringList(rs.getString("linked_themes")),
            rs.getString("possible_business_signal"), rs.getString("no_go_statement"),
            rs.getString("evidence_ref"));
    }

    private static <E extends Enum<E>> E safeEnum(Class<E> enumClass, String value) {
        if (value == null) return null;
        try {
            return Enum.valueOf(enumClass, value.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static final class ExternalEventRowMapper implements RowMapper<ExternalEvent> {
        @Override
        public ExternalEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
            return toExternalEvent(rs);
        }
    }
}
