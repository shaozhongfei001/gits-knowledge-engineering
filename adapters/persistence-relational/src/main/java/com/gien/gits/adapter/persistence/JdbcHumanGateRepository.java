package com.gien.gits.adapter.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gien.gits.ontology.GateDecision;
import com.gien.gits.ontology.GateType;
import com.gien.gits.ontology.HumanGate;
import com.gien.gits.ontology.HumanGateStatus;
import com.gien.gits.ontology.port.HumanGateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;


import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JdbcHumanGateRepository implements HumanGateRepository {

    private static final Logger log = LoggerFactory.getLogger(JdbcHumanGateRepository.class);
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};
    private static final TypeReference<List<String>> LIST_TYPE = new TypeReference<>() {};

    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public JdbcHumanGateRepository(NamedParameterJdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    @Override
    public HumanGate save(HumanGate gate) {
        var params = new MapSqlParameterSource()
                .addValue("gateId", gate.gateId())
                .addValue("gateType", gate.gateType().name())
                .addValue("journeyId", gate.journeyId())
                .addValue("customerId", gate.customerId())
                .addValue("operatingCaseId", gate.operatingCaseId())
                .addValue("status", gate.status().name())
                .addValue("subject", gate.subject())
                .addValue("proposal", toJson(gate.proposal()))
                .addValue("evidenceRefs", toJson(gate.evidenceRefs()))
                .addValue("decision", gate.decision() != null ? gate.decision().name() : null)
                .addValue("modification", toJson(gate.modification()))
                .addValue("decisionReason", gate.decisionReason())
                .addValue("actorId", gate.actorId())
                .addValue("createdAt", gate.createdAt())
                .addValue("decidedAt", gate.decidedAt());

        jdbc.update("""
                INSERT INTO human_gate (gate_id, gate_type, journey_id, customer_id, operating_case_id,
                    status, subject, proposal, evidence_refs, decision, modification,
                    decision_reason, actor_id, created_at, decided_at)
                VALUES (:gateId, :gateType, :journeyId, :customerId, :operatingCaseId,
                    :status, :subject, :proposal, :evidenceRefs, :decision, :modification,
                    :decisionReason, :actorId, :createdAt, :decidedAt)
                """, params);

        log.info("HumanGate saved: gateId={}, type={}, status={}", gate.gateId(), gate.gateType(), gate.status());
        return gate;
    }

    @Override
    public Optional<HumanGate> findById(String gateId) {
        try {
            var gate = jdbc.queryForObject(
                    "SELECT * FROM human_gate WHERE gate_id = :gateId",
                    Map.of("gateId", gateId),
                    this::mapRow);
            return Optional.ofNullable(gate);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<HumanGate> findByStatus(HumanGateStatus status) {
        return jdbc.query("SELECT * FROM human_gate WHERE status = :status",
                Map.of("status", status.name()), this::mapRow);
    }

    @Override
    public List<HumanGate> findByGateType(GateType gateType) {
        return jdbc.query("SELECT * FROM human_gate WHERE gate_type = :gateType",
                Map.of("gateType", gateType.name()), this::mapRow);
    }

    @Override
    public List<HumanGate> findByJourneyId(String journeyId) {
        return jdbc.query("SELECT * FROM human_gate WHERE journey_id = :journeyId",
                Map.of("journeyId", journeyId), this::mapRow);
    }

    @Override
    public List<HumanGate> findByCustomerId(String customerId) {
        return jdbc.query("SELECT * FROM human_gate WHERE customer_id = :customerId",
                Map.of("customerId", customerId), this::mapRow);
    }

    @Override
    public List<HumanGate> findAll() {
        return jdbc.query("SELECT * FROM human_gate ORDER BY created_at DESC", this::mapRow);
    }

    @Override
    public HumanGate decide(String gateId, GateDecision decision, Map<String, Object> modification,
                             String reason, String actorId) {
        var gate = findById(gateId)
                .orElseThrow(() -> new IllegalArgumentException("HumanGate not found: " + gateId));

        var updated = gate.withDecision(decision, modification, reason, actorId);

        var params = new MapSqlParameterSource()
                .addValue("gateId", gateId)
                .addValue("status", updated.status().name())
                .addValue("decision", decision.name())
                .addValue("modification", toJson(modification))
                .addValue("decisionReason", reason)
                .addValue("actorId", actorId)
                .addValue("decidedAt", updated.decidedAt());

        jdbc.update("""
                UPDATE human_gate SET status = :status, decision = :decision,
                    modification = :modification, decision_reason = :decisionReason,
                    actor_id = :actorId, decided_at = :decidedAt
                WHERE gate_id = :gateId
                """, params);

        log.info("HumanGate decided: gateId={}, decision={}, actor={}", gateId, decision, actorId);
        return updated;
    }

    private HumanGate mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new HumanGate(
                rs.getString("gate_id"),
                GateType.valueOf(rs.getString("gate_type")),
                rs.getString("journey_id"),
                rs.getString("customer_id"),
                rs.getString("operating_case_id"),
                HumanGateStatus.valueOf(rs.getString("status")),
                rs.getString("subject"),
                fromJson(rs.getString("proposal"), MAP_TYPE),
                fromJson(rs.getString("evidence_refs"), LIST_TYPE),
                rs.getString("decision") != null ? GateDecision.valueOf(rs.getString("decision")) : null,
                fromJson(rs.getString("modification"), MAP_TYPE),
                rs.getString("decision_reason"),
                rs.getString("actor_id"),
                rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toInstant() : null,
                rs.getTimestamp("decided_at") != null ? rs.getTimestamp("decided_at").toInstant() : null
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
