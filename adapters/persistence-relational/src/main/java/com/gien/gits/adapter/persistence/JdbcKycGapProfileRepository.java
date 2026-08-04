package com.gien.gits.adapter.persistence;

import com.gien.gits.ontology.KycGapProfile;
import com.gien.gits.ontology.port.WritableKycGapProfileRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class JdbcKycGapProfileRepository implements WritableKycGapProfileRepository {

    private static final String INSERT_SQL = """
        INSERT INTO kyc_gap_profile (profile_id, customer_id, as_of, known_items,
            partial_known_items, stale_items, conflicting_or_ambiguous_items,
            unknown_items, priority_questions, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        """;

    private static final String FIND_BY_ID_SQL = """
        SELECT profile_id, customer_id, as_of, known_items, partial_known_items,
            stale_items, conflicting_or_ambiguous_items, unknown_items, priority_questions
        FROM kyc_gap_profile WHERE profile_id = ?
        """;

    private static final String FIND_LATEST_SQL = FIND_BY_ID_SQL.replace("WHERE profile_id = ?",
        "WHERE customer_id = ? ORDER BY as_of DESC LIMIT 1");

    private final JdbcTemplate jdbcTemplate;

    public JdbcKycGapProfileRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate");
    }

    public void save(KycGapProfile profile) {
        jdbcTemplate.update(INSERT_SQL,
            profile.profileId(), profile.customerId(), profile.asOf(),
            JsonHelper.toJsonArray(profile.knownItems()), JsonHelper.toJsonArray(profile.partialKnownItems()),
            JsonHelper.toJsonArray(profile.staleItems()), JsonHelper.toJsonArray(profile.conflictingOrAmbiguousItems()),
            JsonHelper.toJsonArray(profile.unknownItems()), JsonHelper.toJsonArray(profile.priorityQuestions()));
    }

    public Optional<KycGapProfile> findByProfileId(String profileId) {
        return jdbcTemplate.query(FIND_BY_ID_SQL, new KycGapRowMapper(), profileId).stream().findFirst();
    }

    public Optional<KycGapProfile> findLatestByCustomerId(String customerId) {
        return jdbcTemplate.query(FIND_LATEST_SQL, new KycGapRowMapper(), customerId).stream().findFirst();
    }

    private static KycGapProfile toProfile(ResultSet rs) throws SQLException {
        return new KycGapProfile(
            rs.getString("profile_id"), rs.getString("customer_id"),
            rs.getObject("as_of", LocalDate.class),
            JsonHelper.parseStringList(rs.getString("known_items")),
            JsonHelper.parseStringList(rs.getString("partial_known_items")),
            JsonHelper.parseStringList(rs.getString("stale_items")),
            JsonHelper.parseStringList(rs.getString("conflicting_or_ambiguous_items")),
            JsonHelper.parseStringList(rs.getString("unknown_items")),
            JsonHelper.parseStringList(rs.getString("priority_questions")));
    }

    private static final class KycGapRowMapper implements RowMapper<KycGapProfile> {
        @Override
        public KycGapProfile mapRow(ResultSet rs, int rowNum) throws SQLException {
            return toProfile(rs);
        }
    }
}
