package com.gien.gits.ontology.port;

import com.gien.gits.ontology.TransactionRecord;

/**
 * 可写交易记录仓储端口 — 在 {@link TransactionRecordRepository} 只读契约基础上增加写操作。
 *
 * <p>适配器层的 JDBC 实现类应实现此接口，从而同时满足读/写契约。</p>
 */
public interface WritableTransactionRecordRepository extends TransactionRecordRepository {

    /**
     * 保存交易记录聚合。
     *
     * @param transactionRecord 待保存的交易记录
     */
    void save(TransactionRecord transactionRecord);
}
