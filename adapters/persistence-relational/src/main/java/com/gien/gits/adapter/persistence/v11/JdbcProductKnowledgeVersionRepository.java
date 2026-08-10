package com.gien.gits.adapter.persistence.v11;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gien.gits.ontology.domain.ProductKnowledgeVersion;
import com.gien.gits.ontology.port.WritableProductKnowledgeVersionRepository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * JDBC persistence adapter for {@link ProductKnowledgeVersion}
 */
public class JdbcProductKnowledgeVersionRepository implements WritableProductKnowledgeVersionRepository {

    private static final String INSERT_SQL =
        "INSERT INTO product_knowledge_version (version_id, product_id, version_number, product_name, " +
        "category, description, key_features, target_industries, risk_level, required_materials, " +
        "pricing_basis, previous_version_id, change_summary, changed_by, changed_at) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String FIND_BY_VERSION_ID =
        "SELECT * FROM product_knowledge_version WHERE version_id = ?";

    private static final String FIND_BY_PRODUCT_ID =
        "SELECT * FROM product_knowledge_version WHERE product_id = ? ORDER BY version_number DESC";

    private static final String FIND_LATEST_BY_PRODUCT_ID =
        "SELECT * FROM product_knowledge_version WHERE product_id = ? ORDER BY version_number DESC LIMIT 1";

    private static final String FIND_BY_CATEGORY =
        "SELECT * FROM product_knowledge_version WHERE category = ? ORDER BY changed_at DESC";

    private static final String FIND_RECENT =
        "SELECT * FROM product_knowledge_version ORDER BY changed_at DESC LIMIT ?";

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public JdbcProductKnowledgeVersionRepository(JdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.jdbc = Objects.requireNonNull(jdbc);
        this.objectMapper = Objects.requireNonNull(objectMapper);
    }

    @Override
    public void save(ProductKnowledgeVersion v) {
        try {
            jdbc.update(INSERT_SQL,
                v.versionId(), v.productId(), v.versionNumber(), v.productName(),
                v.category(), v.description(),
                objectMapper.writeValueAsString(v.keyFeatures()),
                objectMapper.writeValueAsString(v.targetIndustries()),
                v.riskLevel(),
                objectMapper.writeValueAsString(v.requiredMaterials()),
                v.pricingBasis(), v.previousVersionId(), v.changeSummary(),
                v.changedBy(), Timestamp.from(v.changedAt()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize ProductKnowledgeVersion", e);
        }
    }

    @Override
    public Optional<ProductKnowledgeVersion> findByVersionId(String versionId) {
        List<ProductKnowledgeVersion> results = jdbc.query(FIND_BY_VERSION_ID, new PKVRowMapper(objectMapper), versionId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<ProductKnowledgeVersion> findByProductId(String productId) {
        return jdbc.query(FIND_BY_PRODUCT_ID, new PKVRowMapper(objectMapper), productId);
    }

    @Override
    public Optional<ProductKnowledgeVersion> findLatestByProductId(String productId) {
        List<ProductKnowledgeVersion> results = jdbc.query(FIND_LATEST_BY_PRODUCT_ID, new PKVRowMapper(objectMapper), productId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<ProductKnowledgeVersion> findByCategory(String category) {
        return jdbc.query(FIND_BY_CATEGORY, new PKVRowMapper(objectMapper), category);
    }

    @Override
    public List<ProductKnowledgeVersion> findRecentVersions(int limit) {
        return jdbc.query(FIND_RECENT, new PKVRowMapper(objectMapper), limit);
    }

    @SuppressWarnings("unchecked")
    private static List<String> readJsonList(ObjectMapper om, String json) {
        try {
            return json != null ? om.readValue(json, List.class) : List.of();
        } catch (Exception e) {
            return List.of();
        }
    }

    private static final class PKVRowMapper implements RowMapper<ProductKnowledgeVersion> {
        private final ObjectMapper objectMapper;

        PKVRowMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public ProductKnowledgeVersion mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ProductKnowledgeVersion(
                rs.getString("version_id"), rs.getString("product_id"),
                rs.getInt("version_number"), rs.getString("product_name"),
                rs.getString("category"), rs.getString("description"),
                readJsonList(objectMapper, rs.getString("key_features")),
                readJsonList(objectMapper, rs.getString("target_industries")),
                rs.getString("risk_level"),
                readJsonList(objectMapper, rs.getString("required_materials")),
                rs.getString("pricing_basis"), rs.getString("previous_version_id"),
                rs.getString("change_summary"), rs.getString("changed_by"),
                rs.getTimestamp("changed_at").toInstant());
        }
    }
}
