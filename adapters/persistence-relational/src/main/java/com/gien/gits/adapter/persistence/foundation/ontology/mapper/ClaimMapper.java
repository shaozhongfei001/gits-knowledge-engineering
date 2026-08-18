package com.gien.gits.adapter.persistence.foundation.ontology.mapper;

import com.gien.gits.ontology.Claim;
import com.gien.gits.ontology.ClaimStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 声明 Mapper — foundation/ontology 层
 */
@Mapper
public interface ClaimMapper {

    void insert(Claim claim);

    Optional<Claim> findById(@Param("claimId") UUID claimId);

    List<Claim> findByCaseId(@Param("caseId") UUID caseId);

    void updateStatus(@Param("claimId") UUID claimId,
                      @Param("status") ClaimStatus status);
}
