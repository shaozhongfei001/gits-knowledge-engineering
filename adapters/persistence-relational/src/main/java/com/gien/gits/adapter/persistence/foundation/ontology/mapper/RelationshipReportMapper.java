package com.gien.gits.adapter.persistence.foundation.ontology.mapper;

import com.gien.gits.ontology.RelationshipReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 关系报告 Mapper — foundation/ontology 层
 */
@Mapper
public interface RelationshipReportMapper {

    void insert(RelationshipReport relationshipReport);

    Optional<RelationshipReport> findById(@Param("reportId") UUID reportId);

    List<RelationshipReport> findByOperatingCaseId(@Param("operatingCaseId") String operatingCaseId);

    Optional<RelationshipReport> findLatestByJourneyId(@Param("journeyId") String journeyId);
}
