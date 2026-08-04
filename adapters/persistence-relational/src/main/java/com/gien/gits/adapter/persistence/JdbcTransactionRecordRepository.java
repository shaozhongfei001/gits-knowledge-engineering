package com.gien.gits.adapter.persistence;

import com.gien.gits.ontology.TransactionRecord;
import com.gien.gits.ontology.port.WritableTransactionRecordRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class JdbcTransactionRecordRepository implements WritableTransactionRecordRepository {

    private static final String INSERT_SQL = """
        INSERT INTO transaction_ledger (id, customer_id, transaction_date, transaction_type,
            counterparty, amount_cny, description, evidence_ref, created_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
        """;

    private static final String FIND_BY_CUSTOMER_SQL = """
        SELECT id, customer_id, transaction_date, transaction_type, counterparty,
            amount_cny, description, evidence_ref
        FROM transaction_ledger WHERE customer_id = ? ORDER BY transaction_date
        """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcTransactionRecordRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate");
    }

    public void save(TransactionRecord record) {
        jdbcTemplate.update(INSERT_SQL,
            record.id().toString(), record.customerId(), record.transactionDate(),
            record.transactionType(), record.counterparty(), record.amountCny(),
            record.description(), record.evidenceRef());
    }

    public List<TransactionRecord> findByCustomerId(String customerId) {
        return jdbcTemplate.query(FIND_BY_CUSTOMER_SQL, new TransactionRecordRowMapper(), customerId);
    }

    private static TransactionRecord toTransactionRecord(ResultSet rs) throws SQLException {
        return new TransactionRecord(
            UUID.fromString(rs.getString("id")), rs.getString("customer_id"),
            rs.getObject("transaction_date", LocalDate.class), rs.getString("transaction_type"),
            rs.getString("counterparty"), rs.getLong("amount_cny"),
            rs.getString("description"), rs.getString("evidence_ref"));
    }

    private static final class TransactionRecordRowMapper implements RowMapper<TransactionRecord> {
        @Override
        public TransactionRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            return toTransactionRecord(rs);
        }
    }
}
