package com.gien.gits.adapter.persistence.v11;

import com.gien.gits.adapter.persistence.JsonHelper;
import com.gien.gits.ontology.KycGapProfile;
import com.gien.gits.ontology.port.KycGapProfileRepository;
import com.gien.gits.ontology.port.WritableKycGapProfileRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * V1.1 KYC差距画像JDBC适配器 — 列名对齐V005+V011迁移
 */
public class JdbcKycGapProfileRepository implements WritableKycGapProfileRepository {

    private static final String INSERT_SQL = """
        INSERT INTO kyc_gap_profile (profile_id, customer_id, as_of,
            known_items, partial_known_items, stale_items,
            conflicting_or_ambiguous_items, unknown_items, priority_questions,
            created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

    private static final String FIND_BY_ID = "SELECT * FROM kyc_gap_profile WHERE profile_id = ?";
    private static final String FIND_BY_CUSTOMER = "SELECT * FROM kyc_gap_profile WHERE customer_id = ? ORDER BY as_of DESC";

    private final JdbcTemplate jdbc;

    public JdbcKycGapProfileRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void save(KycGapProfile p) {
        jdbc.update(INSERT_SQL,
                p.profileId(),
                p.customerId(),
                p.asOf(),
                JsonHelper.toJsonArray(p.knownItems()),
                JsonHelper.toJsonArray(p.partialKnownItems()),
                JsonHelper.toJsonArray(p.staleItems()),
                JsonHelper.toJsonArray(p.conflictingOrAmbiguousItems()),
                JsonHelper.toJsonArray(p.unknownItems()),
                JsonHelper.toJsonArray(p.priorityQuestions()),
                p.createdAt() != null ? Timestamp.from(p.createdAt()) : Timestamp.from(Instant.now()),
                p.updatedAt() != null ? Timestamp.from(p.updatedAt()) : Timestamp.from(Instant.now()));
    }

    @Override
    public Optional<KycGapProfile> findByProfileId(String profileId) {
        return jdbc.query(FIND_BY_ID, rowMapper(), profileId).stream().findFirst();
    }

    @Override
    public Optional<KycGapProfile> findByCustomerId(String customerId) {
        return jdbc.query(FIND_BY_CUSTOMER, rowMapper(), customerId).stream().findFirst();
    }

    @Override
    public Optional<KycGapProfile> findLatestByCustomerId(String customerId) {
        return jdbc.query(FIND_BY_CUSTOMER, rowMapper(), customerId).stream().findFirst();
    }

    @Override
    public List<KycGapProfile> findByRiskImpact(String riskImpact) {
        // V011 added risk_impact column
        return jdbc.query("SELECT * FROM kyc_gap_profile WHERE risk_impact = ? ORDER BY as_of DESC",
                rowMapper(), riskImpact);
    }

    @Override
    public List<KycGapProfile> findByEntity(String entity) {
        // Search by customer_id as entity identifier
        return jdbc.query(FIND_BY_CUSTOMER, rowMapper(), entity);
    }

    @Override
    public List<KycGapProfile> findStale(int daysSinceLastAssessment) {
        return jdbc.query("""
            SELECT * FROM kyc_gap_profile
            WHERE as_of < DATEADD(DAY, -?, CURRENT_DATE)
            ORDER BY as_of DESC
            """, rowMapper(), daysSinceLastAssessment);
    }

    private RowMapper<KycGapProfile> rowMapper() {
        return (rs, rowNum) -> mapRow(rs);
    }

    private KycGapProfile mapRow(ResultSet rs) throws SQLException {
        Timestamp createdTs = rs.getTimestamp("created_at");
        Timestamp updatedTs = rs.getTimestamp("updated_at");
        return new KycGapProfile(
                rs.getString("profile_id"),
                rs.getString("customer_id"),
                rs.getObject("as_of", LocalDate.class),
                JsonHelper.parseStringList(rs.getString("known_items")),
                JsonHelper.parseStringList(rs.getString("partial_known_items")),
                JsonHelper.parseStringList(rs.getString("stale_items")),
                JsonHelper.parseStringList(rs.getString("conflicting_or_ambiguous_items")),
                JsonHelper.parseStringList(rs.getString("unknown_items")),
                JsonHelper.parseStringList(rs.getString("priority_questions")),
                createdTs != null ? createdTs.toInstant() : null,
                updatedTs != null ? updatedTs.toInstant() : null
        );
    }
}
