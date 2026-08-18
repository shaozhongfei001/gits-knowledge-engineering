package com.gien.gits.adapter.persistence.foundation.ontology.service;

import com.gien.gits.adapter.persistence.foundation.ontology.dto.TransactionRecordRow;
import com.gien.gits.adapter.persistence.foundation.ontology.mapper.TransactionRecordMapper;
import com.gien.gits.ontology.TransactionRecord;
import com.gien.gits.ontology.port.WritableTransactionRecordRepository;

import java.util.List;

/**
 * MyBatis 交易记录仓储实现 — foundation/ontology 层
 */
public class MyBatisTransactionRecordService implements WritableTransactionRecordRepository {

    private final TransactionRecordMapper mapper;

    public MyBatisTransactionRecordService(TransactionRecordMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(TransactionRecord transactionRecord) {
        mapper.insert(transactionRecord);
    }

    @Override
    public List<TransactionRecord> findByCustomerId(String customerId) {
        return mapper.findRowsByCustomerId(customerId).stream()
                .map(TransactionRecordRow::toTransactionRecord)
                .toList();
    }
}
