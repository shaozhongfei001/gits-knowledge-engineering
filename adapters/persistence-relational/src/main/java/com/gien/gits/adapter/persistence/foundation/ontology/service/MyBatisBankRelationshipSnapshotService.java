package com.gien.gits.adapter.persistence.foundation.ontology.service;

import com.gien.gits.adapter.persistence.foundation.ontology.dto.BankRelationshipSnapshotRow;
import com.gien.gits.adapter.persistence.foundation.ontology.mapper.BankRelationshipSnapshotMapper;
import com.gien.gits.ontology.BankRelationshipSnapshot;
import com.gien.gits.ontology.port.WritableBankRelationshipSnapshotRepository;

import java.util.Optional;

/**
 * MyBatis 银行关系快照仓储实现 — foundation/ontology 层
 */
public class MyBatisBankRelationshipSnapshotService implements WritableBankRelationshipSnapshotRepository {

    private final BankRelationshipSnapshotMapper mapper;

    public MyBatisBankRelationshipSnapshotService(BankRelationshipSnapshotMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(BankRelationshipSnapshot snapshot) {
        mapper.insert(snapshot);
    }

    @Override
    public Optional<BankRelationshipSnapshot> findLatestByCustomerId(String customerId) {
        return mapper.findRowByCustomerId(customerId)
                .map(BankRelationshipSnapshotRow::toBankRelationshipSnapshot);
    }
}
