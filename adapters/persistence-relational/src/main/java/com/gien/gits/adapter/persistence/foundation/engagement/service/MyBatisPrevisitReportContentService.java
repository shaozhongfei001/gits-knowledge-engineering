package com.gien.gits.adapter.persistence.foundation.engagement.service;

import com.gien.gits.adapter.persistence.foundation.engagement.mapper.PrevisitReportContentMapper;
import com.gien.gits.engagement.PrevisitReportContent;
import com.gien.gits.engagement.port.WritablePrevisitReportContentRepository;

import java.util.Optional;

/**
 * MyBatis 访前报告内容仓储实现 — foundation/engagement 层
 */
public class MyBatisPrevisitReportContentService implements WritablePrevisitReportContentRepository {

    private final PrevisitReportContentMapper mapper;

    public MyBatisPrevisitReportContentService(PrevisitReportContentMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(PrevisitReportContent content, String journeyId, String operatingCaseId) {
        mapper.insert(content, journeyId, operatingCaseId);
    }

    @Override
    public Optional<PrevisitReportContent> findByReportId(String reportId) {
        return mapper.findByReportId(reportId);
    }

    @Override
    public Optional<PrevisitReportContent> findLatestByOperatingCaseId(String operatingCaseId) {
        return mapper.findLatestByOperatingCaseId(operatingCaseId);
    }

    @Override
    public Optional<PrevisitReportContent> findByJourneyId(String journeyId) {
        return mapper.findByJourneyId(journeyId);
    }
}
