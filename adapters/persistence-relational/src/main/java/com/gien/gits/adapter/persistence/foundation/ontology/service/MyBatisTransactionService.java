package com.gien.gits.adapter.persistence.foundation.ontology.service;

import com.gien.gits.adapter.persistence.foundation.ontology.mapper.TransactionMapper;
import com.gien.gits.ontology.Transaction;
import com.gien.gits.ontology.port.WritableTransactionRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * MyBatis 交易流水仓储实现 — foundation/ontology 层
 */
public class MyBatisTransactionService implements WritableTransactionRepository {

    private final TransactionMapper mapper;

    public MyBatisTransactionService(TransactionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Transaction save(Transaction transaction) {
        mapper.insert(transaction);
        return transaction;
    }

    @Override
    public List<Transaction> saveAll(List<Transaction> transactions) {
        mapper.insertBatch(transactions);
        return transactions;
    }

    @Override
    public Optional<Transaction> findById(String id) {
        return mapper.findById(id);
    }

    @Override
    public List<Transaction> findByCustomerId(String customerId) {
        return mapper.findByCustomerId(customerId);
    }

    @Override
    public List<Transaction> findByCustomerIdAndDateRange(String customerId,
                                                           LocalDate startDate,
                                                           LocalDate endDate) {
        return mapper.findByCustomerIdAndDateRange(customerId, startDate, endDate);
    }

    @Override
    public List<Transaction> findRecentByCustomerId(String customerId, int limit) {
        return mapper.findRecentByCustomerId(customerId, limit);
    }
}
