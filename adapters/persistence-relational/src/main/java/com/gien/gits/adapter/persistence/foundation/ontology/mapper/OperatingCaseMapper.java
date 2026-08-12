package com.gien.gits.adapter.persistence.foundation.ontology.mapper;

import com.gien.gits.ontology.CaseStatus;
import com.gien.gits.ontology.OperatingCase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * 运营案例 Mapper — foundation/ontology 层
 */
@Mapper
public interface OperatingCaseMapper {

    void insert(OperatingCase operatingCase);

    Optional<OperatingCase> findById(@Param("caseId") UUID caseId);

    void updateStatus(@Param("caseId") UUID caseId,
                      @Param("status") CaseStatus status);
}
