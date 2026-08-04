package com.gien.gits.adapter.persistence;

import com.gien.gits.ontology.BankRelationshipSnapshot;
import com.gien.gits.ontology.port.WritableBankRelationshipSnapshotRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class JdbcBankRelationshipSnapshotRepository implements WritableBankRelationshipSnapshotRepository {

    private static final String INSERT_SQL = """
        INSERT INTO bank_relationship_snapshot (id, customer_id, snapshot_month,
            avg_daily_deposit_cny, monthly_settlement_cny, loan_balance_cny,
            credit_total_cny, used_credit_cny, available_credit_cny,
            bank_acceptance_bill_balance_cny, guarantee_balance_cny, payroll_employees,
            cash_management_opened, supply_chain_finance_opened, cross_border_settlement_cny,
            product_count, customer_contribution_level, anomaly_flags, created_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
        """;

    private static final String FIND_LATEST_SQL = """
        SELECT id, customer_id, snapshot_month, avg_daily_deposit_cny, monthly_settlement_cny,
            loan_balance_cny, credit_total_cny, used_credit_cny, available_credit_cny,
            bank_acceptance_bill_balance_cny, guarantee_balance_cny, payroll_employees,
            cash_management_opened, supply_chain_finance_opened, cross_border_settlement_cny,
            product_count, customer_contribution_level, anomaly_flags
        FROM bank_relationship_snapshot WHERE customer_id = ? ORDER BY snapshot_month DESC LIMIT 1
        """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcBankRelationshipSnapshotRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate");
    }

    public void save(BankRelationshipSnapshot snapshot) {
        jdbcTemplate.update(INSERT_SQL,
            snapshot.id().toString(), snapshot.customerId(), snapshot.snapshotMonth(),
            snapshot.avgDailyDepositCny(), snapshot.monthlySettlementCny(), snapshot.loanBalanceCny(),
            snapshot.creditTotalCny(), snapshot.usedCreditCny(), snapshot.availableCreditCny(),
            snapshot.bankAcceptanceBillBalanceCny(), snapshot.guaranteeBalanceCny(), snapshot.payrollEmployees(),
            snapshot.cashManagementOpened(), snapshot.supplyChainFinanceOpened(), snapshot.crossBorderSettlementCny(),
            snapshot.productCount(), snapshot.customerContributionLevel(), snapshot.anomalyFlags());
    }

    public Optional<BankRelationshipSnapshot> findLatestByCustomerId(String customerId) {
        return jdbcTemplate.query(FIND_LATEST_SQL, new SnapshotRowMapper(), customerId).stream().findFirst();
    }

    private static BankRelationshipSnapshot toSnapshot(ResultSet rs) throws SQLException {
        return new BankRelationshipSnapshot(
            UUID.fromString(rs.getString("id")), rs.getString("customer_id"),
            rs.getString("snapshot_month"), rs.getLong("avg_daily_deposit_cny"),
            rs.getLong("monthly_settlement_cny"), rs.getLong("loan_balance_cny"),
            rs.getLong("credit_total_cny"), rs.getLong("used_credit_cny"),
            rs.getLong("available_credit_cny"), rs.getLong("bank_acceptance_bill_balance_cny"),
            rs.getLong("guarantee_balance_cny"), rs.getInt("payroll_employees"),
            rs.getBoolean("cash_management_opened"), rs.getBoolean("supply_chain_finance_opened"),
            rs.getLong("cross_border_settlement_cny"), rs.getInt("product_count"),
            rs.getString("customer_contribution_level"), rs.getString("anomaly_flags"));
    }

    private static final class SnapshotRowMapper implements RowMapper<BankRelationshipSnapshot> {
        @Override
        public BankRelationshipSnapshot mapRow(ResultSet rs, int rowNum) throws SQLException {
            return toSnapshot(rs);
        }
    }
}
