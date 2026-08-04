package com.gien.gits.adapter.persistence;

import com.gien.gits.ontology.CreditFacility;
import com.gien.gits.ontology.port.WritableCreditFacilityRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class JdbcCreditFacilityRepository implements WritableCreditFacilityRepository {

    private static final String INSERT_SQL = """
        INSERT INTO credit_facility (facility_id, customer_id, borrower_entity,
            approval_date, maturity_date, credit_total_cny, used_credit_cny, available_credit_cny,
            current_loan_balance_cny, bank_acceptance_bill_balance_cny, guarantee_balance_cny,
            collateral, purpose_allowed, purpose_restrictions, covenants,
            reconciliation_note, evidence_ref, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        """;

    private static final String FIND_BY_ID_SQL = """
        SELECT facility_id, customer_id, borrower_entity, approval_date, maturity_date,
            credit_total_cny, used_credit_cny, available_credit_cny,
            current_loan_balance_cny, bank_acceptance_bill_balance_cny, guarantee_balance_cny,
            collateral, purpose_allowed, purpose_restrictions, covenants,
            reconciliation_note, evidence_ref
        FROM credit_facility WHERE facility_id = ?
        """;

    private static final String FIND_BY_CUSTOMER_SQL = FIND_BY_ID_SQL.replace("WHERE facility_id = ?", "WHERE customer_id = ?");

    private final JdbcTemplate jdbcTemplate;

    public JdbcCreditFacilityRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate");
    }

    public void save(CreditFacility facility) {
        jdbcTemplate.update(INSERT_SQL,
            facility.facilityId(), facility.customerId(), facility.borrowerEntity(),
            facility.approvalDate(), facility.maturityDate(),
            facility.creditTotalCny(), facility.usedCreditCny(), facility.availableCreditCny(),
            facility.currentLoanBalanceCny(), facility.bankAcceptanceBillBalanceCny(), facility.guaranteeBalanceCny(),
            facility.collateral(),
            JsonHelper.toJsonArray(facility.purposeAllowed()), JsonHelper.toJsonArray(facility.purposeRestrictions()),
            JsonHelper.toJsonArray(facility.covenants()),
            facility.reconciliationNote(), facility.evidenceRef());
    }

    public Optional<CreditFacility> findByFacilityId(String facilityId) {
        return jdbcTemplate.query(FIND_BY_ID_SQL, new CreditFacilityRowMapper(), facilityId).stream().findFirst();
    }

    public List<CreditFacility> findByCustomerId(String customerId) {
        return jdbcTemplate.query(FIND_BY_CUSTOMER_SQL, new CreditFacilityRowMapper(), customerId);
    }

    private static CreditFacility toCreditFacility(ResultSet rs) throws SQLException {
        return new CreditFacility(
            rs.getString("facility_id"), rs.getString("customer_id"), rs.getString("borrower_entity"),
            rs.getObject("approval_date", LocalDate.class), rs.getObject("maturity_date", LocalDate.class),
            rs.getLong("credit_total_cny"), rs.getLong("used_credit_cny"), rs.getLong("available_credit_cny"),
            rs.getLong("current_loan_balance_cny"), rs.getLong("bank_acceptance_bill_balance_cny"),
            rs.getLong("guarantee_balance_cny"), rs.getString("collateral"),
            JsonHelper.parseStringList(rs.getString("purpose_allowed")),
            JsonHelper.parseStringList(rs.getString("purpose_restrictions")),
            JsonHelper.parseStringList(rs.getString("covenants")),
            rs.getString("reconciliation_note"), rs.getString("evidence_ref"));
    }

    private static final class CreditFacilityRowMapper implements RowMapper<CreditFacility> {
        @Override
        public CreditFacility mapRow(ResultSet rs, int rowNum) throws SQLException {
            return toCreditFacility(rs);
        }
    }
}
