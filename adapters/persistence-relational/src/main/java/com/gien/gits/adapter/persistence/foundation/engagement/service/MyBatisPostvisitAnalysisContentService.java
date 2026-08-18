package com.gien.gits.adapter.persistence.foundation.engagement.service;

import com.gien.gits.adapter.persistence.foundation.engagement.mapper.PostvisitAnalysisContentMapper;
import com.gien.gits.engagement.PostvisitAnalysisContent;
import com.gien.gits.engagement.port.WritablePostvisitAnalysisContentRepository;

import java.util.Optional;

/**
 * MyBatis 访后分析内容仓储实现 — foundation/engagement 层
 */
public class MyBatisPostvisitAnalysisContentService implements WritablePostvisitAnalysisContentRepository {

    private final PostvisitAnalysisContentMapper mapper;

    public MyBatisPostvisitAnalysisContentService(PostvisitAnalysisContentMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(PostvisitAnalysisContent content, String operatingCaseId) {
        mapper.insert(content, operatingCaseId);
    }

    @Override
    public Optional<PostvisitAnalysisContent> findByAnalysisId(String analysisId) {
        return mapper.findRowByAnalysisId(analysisId);
    }

    @Override
    public Optional<PostvisitAnalysisContent> findLatestByOperatingCaseId(String operatingCaseId) {
        return mapper.findRowLatestByOperatingCaseId(operatingCaseId);
    }

    @Override
    public Optional<PostvisitAnalysisContent> findByJourneyId(String journeyId) {
        return mapper.findRowByJourneyId(journeyId);
    }
}
