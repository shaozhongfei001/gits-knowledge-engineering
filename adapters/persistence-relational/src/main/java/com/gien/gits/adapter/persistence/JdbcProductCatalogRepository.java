package com.gien.gits.adapter.persistence;

import com.gien.gits.ontology.ProductKnowledgeCard;
import com.gien.gits.ontology.port.WritableProductCatalogRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class JdbcProductCatalogRepository implements WritableProductCatalogRepository {

    private static final String INSERT_SQL = """
        INSERT INTO product_catalog (product_id, name, definition, key_conditions,
            required_materials, risk_points, trigger, prohibited_phrases, evidence_source, created_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
        """;

    private static final String FIND_BY_ID_SQL = """
        SELECT product_id, name, definition, key_conditions, required_materials,
            risk_points, trigger, prohibited_phrases, evidence_source
        FROM product_catalog WHERE product_id = ?
        """;

    private static final String FIND_ALL_SQL = """
        SELECT product_id, name, definition, key_conditions, required_materials,
            risk_points, trigger, prohibited_phrases, evidence_source
        FROM product_catalog
        """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcProductCatalogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate");
    }

    public void save(ProductKnowledgeCard card) {
        jdbcTemplate.update(INSERT_SQL,
            card.productId(), card.name(), card.definition(),
            JsonHelper.toJsonArray(card.keyConditions()), JsonHelper.toJsonArray(card.requiredMaterials()),
            JsonHelper.toJsonArray(card.riskPoints()), card.trigger(),
            JsonHelper.toJsonArray(card.prohibitedPhrases()), card.evidenceSource());
    }

    public Optional<ProductKnowledgeCard> findByProductId(String productId) {
        return jdbcTemplate.query(FIND_BY_ID_SQL, new ProductCatalogRowMapper(), productId).stream().findFirst();
    }

    public List<ProductKnowledgeCard> findAll() {
        return jdbcTemplate.query(FIND_ALL_SQL, new ProductCatalogRowMapper());
    }

    private static ProductKnowledgeCard toCard(ResultSet rs) throws SQLException {
        return new ProductKnowledgeCard(
            rs.getString("product_id"), rs.getString("name"), rs.getString("definition"),
            JsonHelper.parseStringList(rs.getString("key_conditions")),
            JsonHelper.parseStringList(rs.getString("required_materials")),
            JsonHelper.parseStringList(rs.getString("risk_points")),
            rs.getString("trigger"),
            JsonHelper.parseStringList(rs.getString("prohibited_phrases")),
            rs.getString("evidence_source"));
    }

    private static final class ProductCatalogRowMapper implements RowMapper<ProductKnowledgeCard> {
        @Override
        public ProductKnowledgeCard mapRow(ResultSet rs, int rowNum) throws SQLException {
            return toCard(rs);
        }
    }
}
