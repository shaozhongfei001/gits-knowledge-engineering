package com.gien.gits.adapter.persistence;

import com.gien.gits.ontology.Transaction;
import com.gien.gits.ontology.Transaction.TransactionType;
import com.gien.gits.ontology.port.WritableTransactionRepository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 交易流水JDBC持久化适配器
 */
public class JdbcTransactionRepository implements WritableTransactionRepository {

    private final DataSource dataSource;

    public JdbcTransactionRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Transaction> findById(String id) {
        try (var conn = dataSource.getConnection();
             var ps = conn.prepareStatement(
                     "SELECT * FROM transaction WHERE id = ?")) {
            ps.setString(1, id);
            try (var rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find transaction by id: " + id, e);
        }
    }

    @Override
    public List<Transaction> findByCustomerId(String customerId) {
        try (var conn = dataSource.getConnection();
             var ps = conn.prepareStatement(
                     "SELECT * FROM transaction WHERE customer_id = ? ORDER BY transaction_date DESC")) {
            ps.setString(1, customerId);
            return mapList(ps);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find transactions by customer: " + customerId, e);
        }
    }

    @Override
    public List<Transaction> findByCustomerIdAndDateRange(String customerId,
                                                           LocalDate startDate,
                                                           LocalDate endDate) {
        try (var conn = dataSource.getConnection();
             var ps = conn.prepareStatement(
                     "SELECT * FROM transaction WHERE customer_id = ? AND transaction_date >= ? AND transaction_date <= ? ORDER BY transaction_date DESC")) {
            ps.setString(1, customerId);
            ps.setDate(2, java.sql.Date.valueOf(startDate));
            ps.setDate(3, java.sql.Date.valueOf(endDate));
            return mapList(ps);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find transactions by customer and date range", e);
        }
    }

    @Override
    public List<Transaction> findRecentByCustomerId(String customerId, int limit) {
        try (var conn = dataSource.getConnection();
             var ps = conn.prepareStatement(
                     "SELECT * FROM transaction WHERE customer_id = ? ORDER BY transaction_date DESC LIMIT ?")) {
            ps.setString(1, customerId);
            ps.setInt(2, limit);
            return mapList(ps);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find recent transactions for customer: " + customerId, e);
        }
    }

    @Override
    public Transaction save(Transaction transaction) {
        try (var conn = dataSource.getConnection();
             var ps = conn.prepareStatement(
                     "MERGE INTO transaction (id, transaction_id, customer_id, account_id, transaction_type, amount, currency, counterparty, counterparty_industry, description, transaction_date, created_at) " +
                     "KEY (id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            setParameters(ps, transaction);
            ps.executeUpdate();
            return transaction;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save transaction: " + transaction.transactionId(), e);
        }
    }

    @Override
    public List<Transaction> saveAll(List<Transaction> transactions) {
        List<Transaction> saved = new ArrayList<>();
        for (Transaction t : transactions) {
            saved.add(save(t));
        }
        return saved;
    }

    private void setParameters(java.sql.PreparedStatement ps, Transaction t) throws SQLException {
        ps.setString(1, t.id().toString());
        ps.setString(2, t.transactionId());
        ps.setString(3, t.customerId());
        ps.setString(4, t.accountId());
        ps.setString(5, t.transactionType().name());
        ps.setBigDecimal(6, t.amount());
        ps.setString(7, t.currency());
        ps.setString(8, t.counterparty());
        ps.setString(9, t.counterpartyIndustry());
        ps.setString(10, t.description());
        ps.setDate(11, java.sql.Date.valueOf(t.transactionDate()));
        ps.setTimestamp(12, Timestamp.from(t.createdAt()));
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        return new Transaction(
                UUID.fromString(rs.getString("id")),
                rs.getString("transaction_id"),
                rs.getString("customer_id"),
                rs.getString("account_id"),
                TransactionType.valueOf(rs.getString("transaction_type")),
                rs.getBigDecimal("amount"),
                rs.getString("currency"),
                rs.getString("counterparty"),
                rs.getString("counterparty_industry"),
                rs.getString("description"),
                rs.getDate("transaction_date").toLocalDate(),
                rs.getTimestamp("created_at").toInstant()
        );
    }

    private List<Transaction> mapList(java.sql.PreparedStatement ps) throws SQLException {
        try (var rs = ps.executeQuery()) {
            List<Transaction> results = new ArrayList<>();
            while (rs.next()) {
                results.add(mapRow(rs));
            }
            return results;
        }
    }
}
