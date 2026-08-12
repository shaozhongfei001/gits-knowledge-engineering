package com.gien.gits.adapter.persistence.foundation.ontology.mapper;

import com.gien.gits.ontology.Commitment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 承诺 Mapper — foundation/ontology 层
 */
@Mapper
public interface CommitmentMapper {

    void insert(Commitment commitment);

    Optional<Commitment> findByCommitmentId(@Param("commitmentId") String commitmentId);

    List<Commitment> findByInteractionId(@Param("interactionId") String interactionId);

    List<Commitment> findByCustomerId(@Param("customerId") String customerId);

    List<Commitment> findByStatus(@Param("status") String status);

    List<Commitment> findByCommitmentType(@Param("commitmentType") String commitmentType);

    List<Commitment> findByOperatingCaseId(@Param("operatingCaseId") String operatingCaseId);

    List<Commitment> findOverdue();

    List<Commitment> findAll();

    void updateStatus(@Param("commitmentId") String commitmentId,
                      @Param("status") String status,
                      @Param("verifiedBy") String verifiedBy);
}
