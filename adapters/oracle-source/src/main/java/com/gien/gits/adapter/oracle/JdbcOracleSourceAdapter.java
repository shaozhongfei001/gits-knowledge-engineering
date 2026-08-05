package com.gien.gits.adapter.oracle;

import com.gien.gits.ontology.model.OracleClaim;
import com.gien.gits.ontology.port.OracleSourcePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * JDBC Oracle数据源适配器 — 通过JDBC从Oracle数据库读取索赔数据。
 * 使用只读查询，不写入任何数据。
 */
public class JdbcOracleSourceAdapter implements OracleSourcePort {

    private static final Logger log = LoggerFactory.getLogger(JdbcOracleSourceAdapter.class);

    private final JdbcTemplate jdbcTemplate;
    private final boolean available;

    private static final String READ_CLAIMS_SQL = """
            SELECT claim_id, customer_name, claim_type, status, claim_date, last_updated, source_system
            FROM claim
            WHERE last_updated > ?
            ORDER BY last_updated
            """;

    private final RowMapper<OracleClaim> rowMapper = (rs, rowNum) -> new OracleClaim(
            rs.getString("claim_id"),
            rs.getString("customer_name"),
            rs.getString("claim_type"),
            rs.getString("status"),
            rs.getTimestamp("claim_date").toLocalDateTime(),
            rs.getTimestamp("last_updated").toLocalDateTime(),
            rs.getString("source_system")
    );

    public JdbcOracleSourceAdapter(DataSource dataSource, boolean available) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.available = available;
        log.info("JdbcOracleSourceAdapter initialized, available={}", available);
    }

    @Override
    public List<OracleClaim> readClaims(LocalDateTime since) {
        log.info("Reading claims from Oracle since: {}", since);
        try {
            List<OracleClaim> claims = jdbcTemplate.query(
                    READ_CLAIMS_SQL, rowMapper, Timestamp.valueOf(since));
            log.info("Read {} claims from Oracle since {}", claims.size(), since);
            return claims;
        } catch (Exception e) {
            log.error("Failed to read claims from Oracle: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    /**
     * 连接健康检查 — 验证Oracle数据源连接是否正常。
     * 执行简单查询(SELECT 1 FROM DUAL)确认连接可用。
     * @return HealthCheckResult 包含状态和描述信息
     */
    public HealthCheckResult checkHealth() {
        if (!available) {
            log.warn("Oracle source adapter is not available (available=false)");
            return new HealthCheckResult(false, "Oracle source adapter is disabled");
        }
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1 FROM DUAL", Integer.class);
            boolean healthy = result != null && result == 1;
            if (healthy) {
                log.debug("Oracle health check passed");
                return new HealthCheckResult(true, "Oracle connection is healthy");
            } else {
                log.warn("Oracle health check failed: unexpected query result={}", result);
                return new HealthCheckResult(false, "Oracle query returned unexpected result: " + result);
            }
        } catch (Exception e) {
            log.error("Oracle health check failed: {}", e.getMessage());
            return new HealthCheckResult(false, "Oracle connection failed: " + e.getMessage());
        }
    }

    /**
     * 健康检查结果
     */
    public record HealthCheckResult(boolean healthy, String detail) {}
}
