package com.gien.gits.adapter.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.h2.Driver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import com.gien.gits.ontology.CaseStatus;
import com.gien.gits.ontology.OperatingCase;

class JdbcOperatingCaseRepositoryTest {

    private static final String JDBC_URL =
            "jdbc:h2:mem:operating_case_test;MODE=MySQL;DB_CLOSE_DELAY=-1";

    private JdbcTemplate jdbcTemplate;
    private JdbcOperatingCaseRepository repository;

    @BeforeEach
    void setUp() {
        DataSource dataSource = new SimpleDriverDataSource(new Driver(), JDBC_URL, "sa", "");
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("DROP TABLE IF EXISTS operating_case");
        jdbcTemplate.execute(
                "CREATE TABLE operating_case (" +
                "  case_id CHAR(36) PRIMARY KEY, " +
                "  case_type VARCHAR(64) NOT NULL, " +
                "  status VARCHAR(32) NOT NULL, " +
                "  purpose VARCHAR(512) NOT NULL, " +
                "  valid_from TIMESTAMP NOT NULL, " +
                "  valid_to TIMESTAMP NULL, " +
                "  recorded_at TIMESTAMP NOT NULL, " +
                "  record_version BIGINT NOT NULL DEFAULT 0, " +
                "  created_by VARCHAR(128) NOT NULL DEFAULT 'system', " +
                "  CONSTRAINT ck_case_valid_time CHECK (valid_to IS NULL OR valid_to >= valid_from)" +
                ")");
        repository = new JdbcOperatingCaseRepository(jdbcTemplate);
    }

    @Test
    void saveAndFindByIdRoundTripsEqualOperatingCase() {
        UUID caseId = UUID.randomUUID();
        Instant validFrom = Instant.parse("2026-01-01T00:00:00Z");
        Instant recordedAt = Instant.parse("2026-01-02T00:00:00Z");
        OperatingCase original = new OperatingCase(
                caseId, "CLAIM_REVIEW", CaseStatus.OPEN, "Reconcile claim set for case",
                validFrom, null, recordedAt);

        repository.save(original);

        Optional<OperatingCase> found = repository.findById(caseId);

        assertTrue(found.isPresent());
        assertEquals(original, found.get());
    }

    @Test
    void saveNullThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> repository.save(null));
    }

    @Test
    void findByIdNullThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> repository.findById(null));
    }
}
