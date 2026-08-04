package com.gien.gits.adapter.oracle;

import com.gien.gits.ontology.model.OracleClaim;
import com.gien.gits.ontology.port.OracleSourcePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JdbcOracleSourceAdapter测试 — 使用H2内存数据库(Oracle兼容模式)验证。
 */
class JdbcOracleSourceAdapterTest {

    private OracleSourcePort oracleSourcePort;
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // 使用H2 Oracle兼容模式模拟Oracle数据库
        org.springframework.jdbc.datasource.DriverManagerDataSource ds =
                new org.springframework.jdbc.datasource.DriverManagerDataSource();
        ds.setUrl("jdbc:h2:mem:oracle_test;MODE=Oracle;DB_CLOSE_DELAY=-1");
        ds.setUsername("sa");
        ds.setPassword("");

        jdbcTemplate = new org.springframework.jdbc.core.JdbcTemplate(ds);

        // 创建测试表
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS claim (
                    claim_id VARCHAR(64) PRIMARY KEY,
                    customer_name VARCHAR(128),
                    claim_type VARCHAR(64),
                    status VARCHAR(32),
                    claim_date TIMESTAMP,
                    last_updated TIMESTAMP,
                    source_system VARCHAR(64)
                )
                """);

        // 清空并插入测试数据
        jdbcTemplate.execute("TRUNCATE TABLE claim");
        jdbcTemplate.update(
                "INSERT INTO claim (claim_id, customer_name, claim_type, status, claim_date, last_updated, source_system) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)",
                "CLM-001", "张三", "CREDIT", "OPEN",
                java.sql.Timestamp.valueOf("2025-01-01 10:00:00"),
                java.sql.Timestamp.valueOf("2025-06-15 14:30:00"),
                "ORACLE_ERP"
        );
        jdbcTemplate.update(
                "INSERT INTO claim (claim_id, customer_name, claim_type, status, claim_date, last_updated, source_system) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)",
                "CLM-002", "李四", "TRADE", "CLOSED",
                java.sql.Timestamp.valueOf("2025-02-01 10:00:00"),
                java.sql.Timestamp.valueOf("2025-07-20 09:00:00"),
                "ORACLE_ERP"
        );

        oracleSourcePort = new JdbcOracleSourceAdapter(ds, true);
    }

    @Test
    @DisplayName("读取指定时间后的索赔记录")
    void readClaims_sinceFilter_returnsCorrectClaims() {
        LocalDateTime since = LocalDateTime.of(2025, 7, 1, 0, 0);
        List<OracleClaim> claims = oracleSourcePort.readClaims(since);

        assertEquals(1, claims.size());
        assertEquals("CLM-002", claims.get(0).claimId());
        assertEquals("李四", claims.get(0).customerName());
        assertEquals("TRADE", claims.get(0).claimType());
        assertEquals("CLOSED", claims.get(0).status());
        assertEquals("ORACLE_ERP", claims.get(0).sourceSystem());
    }

    @Test
    @DisplayName("读取所有索赔记录")
    void readClaims_allRecords_returnsAllClaims() {
        LocalDateTime since = LocalDateTime.of(2025, 1, 1, 0, 0);
        List<OracleClaim> claims = oracleSourcePort.readClaims(since);

        assertEquals(2, claims.size());
    }

    @Test
    @DisplayName("无匹配记录时返回空列表")
    void readClaims_noMatches_returnsEmptyList() {
        LocalDateTime since = LocalDateTime.of(2026, 1, 1, 0, 0);
        List<OracleClaim> claims = oracleSourcePort.readClaims(since);

        assertTrue(claims.isEmpty());
    }

    @Test
    @DisplayName("isAvailable返回true")
    void isAvailable_returnsTrue() {
        assertTrue(oracleSourcePort.isAvailable());
    }

    @Test
    @DisplayName("不可用时isAvailable返回false")
    void isAvailable_whenNotAvailable_returnsFalse() {
        org.springframework.jdbc.datasource.DriverManagerDataSource ds =
                new org.springframework.jdbc.datasource.DriverManagerDataSource();
        ds.setUrl("jdbc:h2:mem:oracle_test2;MODE=Oracle");
        ds.setUsername("sa");
        ds.setPassword("");

        OracleSourcePort unavailable = new JdbcOracleSourceAdapter(ds, false);
        assertFalse(unavailable.isAvailable());
    }
}
