package com.gien.gits.adapter.persistence;

import com.gien.gits.ontology.LegalEntity;
import com.gien.gits.ontology.port.WritableLegalEntityRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class JdbcLegalEntityRepository implements WritableLegalEntityRepository {

    private static final String INSERT_SQL = """
        INSERT INTO legal_entity (entity_id, group_id, name, role, ownership,
            bank_customer_id, relationship_status, evidence_ref, created_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
        """;

    private static final String FIND_BY_ID_SQL = """
        SELECT entity_id, group_id, name, role, ownership,
            bank_customer_id, relationship_status, evidence_ref
        FROM legal_entity WHERE entity_id = ?
        """;

    private static final String FIND_BY_GROUP_SQL = FIND_BY_ID_SQL.replace("WHERE entity_id = ?", "WHERE group_id = ?");

    private final JdbcTemplate jdbcTemplate;

    public JdbcLegalEntityRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate");
    }

    public void save(LegalEntity entity) {
        jdbcTemplate.update(INSERT_SQL,
            entity.entityId(), entity.groupId(), entity.name(), entity.role(),
            entity.ownership(), entity.bankCustomerId(), entity.relationshipStatus(),
            entity.evidenceRef());
    }

    public Optional<LegalEntity> findByEntityId(String entityId) {
        return jdbcTemplate.query(FIND_BY_ID_SQL, new LegalEntityRowMapper(), entityId).stream().findFirst();
    }

    public List<LegalEntity> findByGroupId(String groupId) {
        return jdbcTemplate.query(FIND_BY_GROUP_SQL, new LegalEntityRowMapper(), groupId);
    }

    private static LegalEntity toLegalEntity(ResultSet rs) throws SQLException {
        return new LegalEntity(
            rs.getString("entity_id"), rs.getString("group_id"), rs.getString("name"),
            rs.getString("role"), rs.getString("ownership"), rs.getString("bank_customer_id"),
            rs.getString("relationship_status"), rs.getString("evidence_ref"));
    }

    private static final class LegalEntityRowMapper implements RowMapper<LegalEntity> {
        @Override
        public LegalEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
            return toLegalEntity(rs);
        }
    }
}
