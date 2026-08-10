package com.gien.gits.adapter.persistence.v11;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gien.gits.ontology.domain.Opportunity;
import com.gien.gits.ontology.port.WritableOpportunityRepository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * JDBC persistence adapter for {@link Opportunity}
 */
public class JdbcOpportunityRepository implements WritableOpportunityRepository {

    private static final String INSERT_SQL =
        "INSERT INTO opportunity (opportunity_id, customer_id, interaction_id, operating_case_id, " +
        "opportunity_type, product_id, product_name, description, status, estimated_amount, " +
        "probability, assigned_to, source, next_steps, expected_close_date, created_at, updated_at) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String FIND_BY_ID =
        "SELECT * FROM opportunity WHERE opportunity_id = ?";

    private static final String FIND_BY_CUSTOMER =
        "SELECT * FROM opportunity WHERE customer_id = ? ORDER BY created_at DESC";

    private static final String FIND_BY_STATUS =
        "SELECT * FROM opportunity WHERE status = ? ORDER BY created_at DESC";

    private static final String FIND_BY_TYPE =
        "SELECT * FROM opportunity WHERE opportunity_type = ? ORDER BY created_at DESC";

    private static final String FIND_BY_ASSIGNED =
        "SELECT * FROM opportunity WHERE assigned_to = ? ORDER BY created_at DESC";

    private static final String FIND_ACTIVE_BY_CUSTOMER =
        "SELECT * FROM opportunity WHERE customer_id = ? AND status IN ('IDENTIFIED','QUALIFIED','PROPOSAL','NEGOTIATION') ORDER BY created_at DESC";

    private static final String UPDATE_STATUS =
        "UPDATE opportunity SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE opportunity_id = ?";

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public JdbcOpportunityRepository(JdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.jdbc = Objects.requireNonNull(jdbc);
        this.objectMapper = Objects.requireNonNull(objectMapper);
    }

    @Override
    public void save(Opportunity o) {
        try {
            jdbc.update(INSERT_SQL,
                o.opportunityId(), o.customerId(), o.interactionId(), o.operatingCaseId(),
                o.opportunityType(), o.productId(), o.productName(), o.description(),
                o.status(), o.estimatedAmount(), o.probability(), o.assignedTo(),
                o.source(), objectMapper.writeValueAsString(o.nextSteps()),
                o.expectedCloseDate(), Timestamp.from(o.createdAt()), Timestamp.from(o.updatedAt()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize opportunity", e);
        }
    }

    @Override
    public void updateStatus(String opportunityId, String status) {
        jdbc.update(UPDATE_STATUS, status, opportunityId);
    }

    @Override
    public Optional<Opportunity> findByOpportunityId(String opportunityId) {
        List<Opportunity> results = jdbc.query(FIND_BY_ID, new OpportunityRowMapper(objectMapper), opportunityId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<Opportunity> findByCustomerId(String customerId) {
        return jdbc.query(FIND_BY_CUSTOMER, new OpportunityRowMapper(objectMapper), customerId);
    }

    @Override
    public List<Opportunity> findByStatus(String status) {
        return jdbc.query(FIND_BY_STATUS, new OpportunityRowMapper(objectMapper), status);
    }

    @Override
    public List<Opportunity> findByOpportunityType(String opportunityType) {
        return jdbc.query(FIND_BY_TYPE, new OpportunityRowMapper(objectMapper), opportunityType);
    }

    @Override
    public List<Opportunity> findByAssignedTo(String assignedTo) {
        return jdbc.query(FIND_BY_ASSIGNED, new OpportunityRowMapper(objectMapper), assignedTo);
    }

    @Override
    public List<Opportunity> findActiveByCustomerId(String customerId) {
        return jdbc.query(FIND_ACTIVE_BY_CUSTOMER, new OpportunityRowMapper(objectMapper), customerId);
    }

    @Override
    public List<Opportunity> findAll() {
        return jdbc.query("SELECT * FROM opportunity ORDER BY created_at DESC", new OpportunityRowMapper(objectMapper));
    }

    private static List<String> parseJsonStringList(ObjectMapper om, String json) {
        try {
            return om.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            String trimmed = json.trim();
            if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
                try {
                    String unquoted = om.readValue(trimmed, String.class);
                    return om.readValue(unquoted, new TypeReference<List<String>>() {});
                } catch (Exception ignored) {}
            }
            return List.of();
        }
    }

    private static final class OpportunityRowMapper implements RowMapper<Opportunity> {
        private final ObjectMapper objectMapper;

        OpportunityRowMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Opportunity mapRow(ResultSet rs, int rowNum) throws SQLException {
            try {
                String nextStepsJson = rs.getString("next_steps");
                List<String> nextSteps = nextStepsJson != null
                    ? parseJsonStringList(objectMapper, nextStepsJson) : List.of();
                return new Opportunity(
                    rs.getString("opportunity_id"), rs.getString("customer_id"),
                    rs.getString("interaction_id"), rs.getString("operating_case_id"),
                    rs.getString("opportunity_type"), rs.getString("product_id"),
                    rs.getString("product_name"), rs.getString("description"),
                    rs.getString("status"), rs.getString("estimated_amount"),
                    rs.getString("probability"), rs.getString("assigned_to"),
                    rs.getString("source"), nextSteps,
                    rs.getString("expected_close_date"),
                    rs.getTimestamp("created_at").toInstant(),
                    rs.getTimestamp("updated_at").toInstant());
            } catch (Exception e) {
                throw new SQLException("Failed to map Opportunity", e);
            }
        }
    }
}
