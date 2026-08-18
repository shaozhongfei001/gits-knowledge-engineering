package com.gien.gits.adapter.persistence;

import com.gien.gits.ontology.domain.EvidenceVersionLink;
import com.gien.gits.ontology.port.EvidenceVersionLinkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of EvidenceVersionLinkRepository.
 * Registered as a bean in RepositoryConfig (no @Repository annotation).
 */
public class JdbcEvidenceVersionLinkRepository implements EvidenceVersionLinkRepository {

    private static final Logger log = LoggerFactory.getLogger(JdbcEvidenceVersionLinkRepository.class);

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcEvidenceVersionLinkRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<EvidenceVersionLink> findByLinkId(String linkId) {
        var sql = "SELECT link_id, evidence_id, previous_version_id, next_version_id, " +
                  "version_number, change_type, change_reason, changed_by, changed_at " +
                  "FROM evidence_version_link WHERE link_id = :linkId";
        try {
            var result = jdbc.queryForObject(sql, new MapSqlParameterSource("linkId", linkId),
                    this::mapRow);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<EvidenceVersionLink> findByEvidenceId(String evidenceId) {
        var sql = "SELECT link_id, evidence_id, previous_version_id, next_version_id, " +
                  "version_number, change_type, change_reason, changed_by, changed_at " +
                  "FROM evidence_version_link WHERE evidence_id = :evidenceId " +
                  "ORDER BY version_number DESC LIMIT 1";
        try {
            var result = jdbc.queryForObject(sql, new MapSqlParameterSource("evidenceId", evidenceId),
                    this::mapRow);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<EvidenceVersionLink> findVersionChain(String evidenceId) {
        var sql = "SELECT link_id, evidence_id, previous_version_id, next_version_id, " +
                  "version_number, change_type, change_reason, changed_by, changed_at " +
                  "FROM evidence_version_link WHERE evidence_id = :evidenceId " +
                  "ORDER BY version_number ASC";
        return jdbc.query(sql, new MapSqlParameterSource("evidenceId", evidenceId), this::mapRow);
    }

    @Override
    public Optional<EvidenceVersionLink> findLatestVersion(String evidenceId) {
        return findByEvidenceId(evidenceId);
    }

    private EvidenceVersionLink mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new EvidenceVersionLink(
                rs.getString("link_id"),
                rs.getString("evidence_id"),
                rs.getString("previous_version_id"),
                rs.getString("next_version_id"),
                rs.getInt("version_number"),
                rs.getString("change_type"),
                rs.getString("change_reason"),
                rs.getString("changed_by"),
                rs.getTimestamp("changed_at").toInstant()
        );
    }
}
