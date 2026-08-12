package com.gien.gits.adapter.persistence.foundation.ontology.mapper;

import com.gien.gits.adapter.persistence.foundation.ontology.dto.BankRelationshipSnapshotRow;
import com.gien.gits.ontology.BankRelationshipSnapshot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

/**
 * 银行关系快照 Mapper — foundation/ontology 层
 */
@Mapper
public interface BankRelationshipSnapshotMapper {

    void insert(BankRelationshipSnapshot snapshot);

    Optional<BankRelationshipSnapshotRow> findRowByCustomerId(@Param("customerId") String customerId);
}
