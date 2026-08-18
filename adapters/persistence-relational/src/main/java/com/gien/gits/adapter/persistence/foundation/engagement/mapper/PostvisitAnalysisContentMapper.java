package com.gien.gits.adapter.persistence.foundation.engagement.mapper;

import com.gien.gits.engagement.PostvisitAnalysisContent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

/**
 * 访后分析内容 Mapper — foundation/engagement 层
 */
@Mapper
public interface PostvisitAnalysisContentMapper {

    void insert(@Param("content") PostvisitAnalysisContent content,
                @Param("operatingCaseId") String operatingCaseId);

    Optional<PostvisitAnalysisContent> findRowByAnalysisId(@Param("analysisId") String analysisId);

    Optional<PostvisitAnalysisContent> findRowLatestByOperatingCaseId(@Param("operatingCaseId") String operatingCaseId);

    Optional<PostvisitAnalysisContent> findRowByJourneyId(@Param("journeyId") String journeyId);
}
