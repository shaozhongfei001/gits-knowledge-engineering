package com.gien.gits.ontology.port;

import com.gien.gits.ontology.Transaction;

import java.util.List;

/**
 * 交易流水可写Port — 扩展只读Port，增加写入能力
 */
public interface WritableTransactionRepository extends TransactionRepository {

    Transaction save(Transaction transaction);

    List<Transaction> saveAll(List<Transaction> transactions);
}
