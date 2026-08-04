package com.gien.gits.ontology.port;

import com.gien.gits.ontology.Transaction;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 交易流水只读Port — 查询交易流水
 */
public interface TransactionRepository {

    Optional<Transaction> findById(String id);

    List<Transaction> findByCustomerId(String customerId);

    List<Transaction> findByCustomerIdAndDateRange(String customerId,
                                                    LocalDate startDate,
                                                    LocalDate endDate);

    List<Transaction> findRecentByCustomerId(String customerId, int limit);
}
