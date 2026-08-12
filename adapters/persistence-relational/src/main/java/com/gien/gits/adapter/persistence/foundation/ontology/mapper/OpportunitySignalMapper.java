package com.gien.gits.adapter.persistence.foundation.ontology.mapper;

import com.gien.gits.ontology.OpportunitySignal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 机会信号 Mapper — foundation/ontology 层
 */
@Mapper
public interface OpportunitySignalMapper {

    void insert(OpportunitySignal signal);

    Optional<OpportunitySignal> findById(@Param("signalId") UUID signalId);

    List<OpportunitySignal> findByOperatingCaseId(@Param("operatingCaseId") String operatingCaseId);

    void updateStatus(@Param("signalId") UUID signalId,
                      @Param("status") OpportunitySignal.SignalStatus status);
}
