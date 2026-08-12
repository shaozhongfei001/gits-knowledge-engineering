package com.gien.gits.adapter.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gien.gits.adapter.persistence.entity.CrmWritebackCommandEntity;
import com.gien.gits.adapter.persistence.entity.CrmWritebackCommandEntity.CrmWritebackStatus;
import com.gien.gits.ontology.GateDecision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;


import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JdbcCrmWritebackCommandRepository {

    private static final Logger log = LoggerFactory.getLogger(JdbcCrmWritebackCommandRepository.class);
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};
    private static final TypeReference<List<Map<String, Object>>> LIST_MOD_TYPE = new TypeReference<>() {};

    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public JdbcCrmWritebackCommandRepository(NamedParameterJdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    public CrmWritebackCommandEntity save(CrmWritebackCommandEntity cmd) {
        var params = new MapSqlParameterSource()
                .addValue("commandId", cmd.commandId())
                .addValue("journeyId", cmd.journeyId())
                .addValue("customerId", cmd.customerId())
                .addValue("operatingCaseId", cmd.operatingCaseId())
                .addValue("operation", cmd.operation())
                .addValue("targetEntity", cmd.targetEntity())
                .addValue("payload", toJson(cmd.payload()))
                .addValue("status", cmd.status().name())
                .addValue("humanConfirmationRequired", cmd.humanConfirmationRequired())
                .addValue("decision", cmd.decision() != null ? cmd.decision().name() : null)
                .addValue("modifications", toJson(cmd.modifications()))
                .addValue("decisionReason", cmd.decisionReason())
                .addValue("actorId", cmd.actorId())
                .addValue("createdAt", cmd.createdAt())
                .addValue("decidedAt", cmd.decidedAt())
                .addValue("sentAt", cmd.sentAt())
                .addValue("errorMessage", cmd.errorMessage());

        jdbc.update("""
                INSERT INTO crm_writeback_command (command_id, journey_id, customer_id, operating_case_id,
                    operation, target_entity, payload, status, human_confirmation_required,
                    decision, modifications, decision_reason, actor_id,
                    created_at, decided_at, sent_at, error_message)
                VALUES (:commandId, :journeyId, :customerId, :operatingCaseId,
                    :operation, :targetEntity, :payload, :status, :humanConfirmationRequired,
                    :decision, :modifications, :decisionReason, :actorId,
                    :createdAt, :decidedAt, :sentAt, :errorMessage)
                """, params);

        return cmd;
    }

    public Optional<CrmWritebackCommandEntity> findById(String commandId) {
        try {
            var cmd = jdbc.queryForObject(
                    "SELECT * FROM crm_writeback_command WHERE command_id = :commandId",
                    Map.of("commandId", commandId),
                    this::mapRow);
            return Optional.ofNullable(cmd);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<CrmWritebackCommandEntity> findByStatus(String status) {
        return jdbc.query("SELECT * FROM crm_writeback_command WHERE status = :status ORDER BY created_at DESC",
                Map.of("status", status), this::mapRow);
    }

    public List<CrmWritebackCommandEntity> findByJourneyId(String journeyId) {
        return jdbc.query("SELECT * FROM crm_writeback_command WHERE journey_id = :journeyId ORDER BY created_at DESC",
                Map.of("journeyId", journeyId), this::mapRow);
    }

    public List<CrmWritebackCommandEntity> findByCustomerId(String customerId) {
        return jdbc.query("SELECT * FROM crm_writeback_command WHERE customer_id = :customerId ORDER BY created_at DESC",
                Map.of("customerId", customerId), this::mapRow);
    }

    public List<CrmWritebackCommandEntity> findAll() {
        return jdbc.query("SELECT * FROM crm_writeback_command ORDER BY created_at DESC", this::mapRow);
    }

    public CrmWritebackCommandEntity decide(String commandId, GateDecision decision,
                                              List<Map<String, Object>> modifications,
                                              String reason, String actorId) {
        var cmd = findById(commandId)
                .orElseThrow(() -> new IllegalArgumentException("CrmWritebackCommand not found: " + commandId));

        var updated = cmd.withDecision(decision, modifications, reason, actorId);

        var params = new MapSqlParameterSource()
                .addValue("commandId", commandId)
                .addValue("status", updated.status().name())
                .addValue("decision", decision.name())
                .addValue("modifications", toJson(modifications))
                .addValue("decisionReason", reason)
                .addValue("actorId", actorId)
                .addValue("decidedAt", updated.decidedAt());

        jdbc.update("""
                UPDATE crm_writeback_command SET status = :status, decision = :decision,
                    modifications = :modifications, decision_reason = :decisionReason,
                    actor_id = :actorId, decided_at = :decidedAt
                WHERE command_id = :commandId
                """, params);

        log.info("CrmWritebackCommand decided: commandId={}, decision={}, actor={}", commandId, decision, actorId);
        return findById(commandId).orElseThrow();
    }

    private CrmWritebackCommandEntity mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new CrmWritebackCommandEntity(
                rs.getString("command_id"),
                rs.getString("journey_id"),
                rs.getString("customer_id"),
                rs.getString("operating_case_id"),
                rs.getString("operation"),
                rs.getString("target_entity"),
                fromJson(rs.getString("payload"), MAP_TYPE),
                CrmWritebackStatus.valueOf(rs.getString("status")),
                rs.getBoolean("human_confirmation_required"),
                rs.getString("decision") != null ? GateDecision.valueOf(rs.getString("decision")) : null,
                fromJson(rs.getString("modifications"), LIST_MOD_TYPE),
                rs.getString("decision_reason"),
                rs.getString("actor_id"),
                rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toInstant() : null,
                rs.getTimestamp("decided_at") != null ? rs.getTimestamp("decided_at").toInstant() : null,
                rs.getTimestamp("sent_at") != null ? rs.getTimestamp("sent_at").toInstant() : null,
                rs.getString("error_message")
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
