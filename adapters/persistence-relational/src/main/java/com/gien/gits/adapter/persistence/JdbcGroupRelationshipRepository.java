package com.gien.gits.adapter.persistence;

import com.gien.gits.ontology.GroupRelationship;
import com.gien.gits.ontology.port.WritableGroupRelationshipRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class JdbcGroupRelationshipRepository implements WritableGroupRelationshipRepository {

    private static final String INSERT_SQL = """
        INSERT INTO group_relationship (id, group_id, from_entity_id, to_entity_id,
            relationship_type, ownership_ratio, created_at)
        VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
        """;

    private static final String FIND_BY_GROUP_SQL = """
        SELECT id, group_id, from_entity_id, to_entity_id, relationship_type, ownership_ratio
        FROM group_relationship WHERE group_id = ?
        """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcGroupRelationshipRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate");
    }

    public void save(GroupRelationship rel) {
        jdbcTemplate.update(INSERT_SQL,
            rel.id().toString(), rel.groupId(), rel.fromEntityId(), rel.toEntityId(),
            rel.relationshipType(), rel.ownershipRatio());
    }

    public List<GroupRelationship> findByGroupId(String groupId) {
        return jdbcTemplate.query(FIND_BY_GROUP_SQL, new GroupRelationshipRowMapper(), groupId);
    }

    private static GroupRelationship toGroupRelationship(ResultSet rs) throws SQLException {
        return new GroupRelationship(
            UUID.fromString(rs.getString("id")), rs.getString("group_id"),
            rs.getString("from_entity_id"), rs.getString("to_entity_id"),
            rs.getString("relationship_type"), rs.getInt("ownership_ratio"));
    }

    private static final class GroupRelationshipRowMapper implements RowMapper<GroupRelationship> {
        @Override
        public GroupRelationship mapRow(ResultSet rs, int rowNum) throws SQLException {
            return toGroupRelationship(rs);
        }
    }
}
