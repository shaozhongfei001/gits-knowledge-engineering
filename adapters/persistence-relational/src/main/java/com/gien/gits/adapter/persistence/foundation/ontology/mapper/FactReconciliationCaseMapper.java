package com.gien.gits.adapter.persistence.foundation.ontology.mapper;

import com.gien.gits.ontology.FactReconciliationCase;
import com.gien.gits.ontology.ReconciliationStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 事实对账案例 Mapper — foundation/ontology 层
 */
@Mapper
public interface FactReconciliationCaseMapper {

    void insert(FactReconciliationCase factReconciliationCase);

    Optional<FactReconciliationCase> findByReconciliationId(@Param("reconciliationId") String reconciliationId);

    List<FactReconciliationCase> findByCaseId(@Param("caseId") String caseId);

    void updateStatus(@Param("reconciliationId") String reconciliationId,
                      @Param("status") ReconciliationStatus status);
}
