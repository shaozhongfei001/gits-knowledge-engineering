package com.gien.gits.adapter.persistence;

import com.gien.gits.ontology.PolicyRule;
import com.gien.gits.ontology.PolicyRule.Severity;
import com.gien.gits.ontology.port.WritablePolicyRuleRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class JdbcPolicyRuleRepository implements WritablePolicyRuleRepository {

    private static final String INSERT_SQL = """
        INSERT INTO policy_rule (rule_id, name, severity, logic, required_output, created_at)
        VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
        """;

    private static final String FIND_BY_ID_SQL = """
        SELECT rule_id, name, severity, logic, required_output
        FROM policy_rule WHERE rule_id = ?
        """;

    private static final String FIND_BY_SEVERITY_SQL = FIND_BY_ID_SQL.replace("WHERE rule_id = ?", "WHERE severity = ?");
    private static final String FIND_ALL_SQL = "SELECT rule_id, name, severity, logic, required_output FROM policy_rule";

    private final JdbcTemplate jdbcTemplate;

    public JdbcPolicyRuleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate");
    }

    public void save(PolicyRule rule) {
        jdbcTemplate.update(INSERT_SQL,
            rule.ruleId(), rule.name(), rule.severity().name(), rule.logic(), rule.requiredOutput());
    }

    public Optional<PolicyRule> findByRuleId(String ruleId) {
        return jdbcTemplate.query(FIND_BY_ID_SQL, new PolicyRuleRowMapper(), ruleId).stream().findFirst();
    }

    public List<PolicyRule> findBySeverity(Severity severity) {
        return jdbcTemplate.query(FIND_BY_SEVERITY_SQL, new PolicyRuleRowMapper(), severity.name());
    }

    /** 兼容String参数 */
    public List<PolicyRule> findBySeverity(String severity) {
        return jdbcTemplate.query(FIND_BY_SEVERITY_SQL, new PolicyRuleRowMapper(), severity);
    }

    public List<PolicyRule> findAll() {
        return jdbcTemplate.query(FIND_ALL_SQL, new PolicyRuleRowMapper());
    }

    private static PolicyRule toPolicyRule(ResultSet rs) throws SQLException {
        return new PolicyRule(
            rs.getString("rule_id"), rs.getString("name"),
            Severity.valueOf(rs.getString("severity")),
            rs.getString("logic"), rs.getString("required_output"));
    }

    private static final class PolicyRuleRowMapper implements RowMapper<PolicyRule> {
        @Override
        public PolicyRule mapRow(ResultSet rs, int rowNum) throws SQLException {
            return toPolicyRule(rs);
        }
    }
}
