package com.gien.gits.ontology.port;

import com.gien.gits.ontology.TransactionRecord;

import java.util.List;

/**
 * 交易记录仓储端口 — 只读操作。
 *
 * <p>定义对 {@link TransactionRecord} 聚合的查询契约，由适配器层实现。
 * 写操作见 {@link WritableTransactionRecordRepository}。</p>
 */
public interface TransactionRecordRepository {

    /**
     * 根据客户ID查找其所有交易记录。
     *
     * @param customerId 客户唯一标识
     * @return 该客户的交易记录列表
     */
    List<TransactionRecord> findByCustomerId(String customerId);
}
