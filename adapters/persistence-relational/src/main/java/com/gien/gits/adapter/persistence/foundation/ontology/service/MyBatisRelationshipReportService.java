package com.gien.gits.adapter.persistence.foundation.ontology.service;

import com.gien.gits.adapter.persistence.foundation.ontology.mapper.RelationshipReportMapper;
import com.gien.gits.ontology.RelationshipReport;
import com.gien.gits.ontology.port.WritableRelationshipReportRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * MyBatis 关系报告仓储实现 — foundation/ontology 层
 */
public class MyBatisRelationshipReportService implements WritableRelationshipReportRepository {

    private final RelationshipReportMapper mapper;

    public MyBatisRelationshipReportService(RelationshipReportMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(RelationshipReport relationshipReport) {
        mapper.insert(relationshipReport);
    }

    @Override
    public Optional<RelationshipReport> findById(UUID reportId) {
        return mapper.findById(reportId);
    }

    @Override
    public List<RelationshipReport> findByOperatingCaseId(String operatingCaseId) {
        return mapper.findByOperatingCaseId(operatingCaseId);
    }

    @Override
    public Optional<RelationshipReport> findLatestByJourneyId(String journeyId) {
        return mapper.findLatestByJourneyId(journeyId);
    }
}
