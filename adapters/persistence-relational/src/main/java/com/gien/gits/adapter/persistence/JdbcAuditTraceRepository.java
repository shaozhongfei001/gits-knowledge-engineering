package com.gien.gits.adapter.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gien.gits.ontology.AuditTraceEntry;
import com.gien.gits.ontology.port.AuditTraceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;


import java.time.Instant;
import java.util.List;
import java.util.Map;

public class JdbcAuditTraceRepository implements AuditTraceRepository {

    private static final Logger log = LoggerFactory.getLogger(JdbcAuditTraceRepository.class);
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public JdbcAuditTraceRepository(NamedParameterJdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    @Override
    public AuditTraceEntry save(AuditTraceEntry entry) {
        var params = new MapSqlParameterSource()
                .addValue("traceId", entry.traceId())
                .addValue("entityType", entry.entityType())
                .addValue("entityId", entry.entityId())
                .addValue("operation", entry.operation())
                .addValue("beforeSnapshot", toJson(entry.beforeSnapshot()))
                .addValue("afterSnapshot", toJson(entry.afterSnapshot()))
                .addValue("actorId", entry.actorId())
                .addValue("actorRole", entry.actorRole())
                .addValue("occurredAt", entry.occurredAt())
                .addValue("correlationId", entry.correlationId());

        jdbc.update("""
                INSERT INTO audit_trace (trace_id, entity_type, entity_id, operation,
                    before_snapshot, after_snapshot, actor_id, actor_role, occurred_at, correlation_id)
                VALUES (:traceId, :entityType, :entityId, :operation,
                    :beforeSnapshot, :afterSnapshot, :actorId, :actorRole, :occurredAt, :correlationId)
                """, params);

        log.debug("AuditTrace saved: traceId={}, entity={}/{}", entry.traceId(), entry.entityType(), entry.entityId());
        return entry;
    }

    @Override
    public List<AuditTraceEntry> findByEntityTypeAndEntityId(String entityType, String entityId) {
        return jdbc.query(
                "SELECT * FROM audit_trace WHERE entity_type = :entityType AND entity_id = :entityId ORDER BY occurred_at DESC",
                Map.of("entityType", entityType, "entityId", entityId),
                this::mapRow);
    }

    @Override
    public List<AuditTraceEntry> findByActorId(String actorId) {
        return jdbc.query(
                "SELECT * FROM audit_trace WHERE actor_id = :actorId ORDER BY occurred_at DESC",
                Map.of("actorId", actorId),
                this::mapRow);
    }

    @Override
    public List<AuditTraceEntry> findByTimeRange(Instant from, Instant to) {
        return jdbc.query(
                "SELECT * FROM audit_trace WHERE occurred_at >= :from AND occurred_at <= :to ORDER BY occurred_at DESC",
                Map.of("from", from, "to", to),
                this::mapRow);
    }

    @Override
    public List<AuditTraceEntry> findAll() {
        return jdbc.query("SELECT * FROM audit_trace ORDER BY occurred_at DESC LIMIT 500", this::mapRow);
    }

    private AuditTraceEntry mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new AuditTraceEntry(
                rs.getString("trace_id"),
                rs.getString("entity_type"),
                rs.getString("entity_id"),
                rs.getString("operation"),
                fromJson(rs.getString("before_snapshot"), MAP_TYPE),
                fromJson(rs.getString("after_snapshot"), MAP_TYPE),
                rs.getString("actor_id"),
                rs.getString("actor_role"),
                rs.getTimestamp("occurred_at") != null ? rs.getTimestamp("occurred_at").toInstant() : null,
                rs.getString("correlation_id")
        );
    }

    private String toJson(Object value) {
        if (value == null) return null;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            log.warn("Failed to serialize to JSON", e);
            return null;
        }
    }

    private <T> T fromJson(String json, TypeReference<T> type) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            log.warn("Failed to deserialize JSON: {}", json, e);
            return null;
        }
    }
}
