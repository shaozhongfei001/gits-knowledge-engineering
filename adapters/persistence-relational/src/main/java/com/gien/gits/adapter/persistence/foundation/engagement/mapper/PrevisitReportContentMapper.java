package com.gien.gits.adapter.persistence.foundation.engagement.mapper;

import com.gien.gits.engagement.PrevisitReportContent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

/**
 * 访前报告内容 Mapper — foundation/engagement 层
 */
@Mapper
public interface PrevisitReportContentMapper {

    void insert(@Param("content") PrevisitReportContent content,
                @Param("journeyId") String journeyId,
                @Param("operatingCaseId") String operatingCaseId);

    Optional<PrevisitReportContent> findByReportId(@Param("reportId") String reportId);

    Optional<PrevisitReportContent> findLatestByOperatingCaseId(@Param("operatingCaseId") String operatingCaseId);

    Optional<PrevisitReportContent> findByJourneyId(@Param("journeyId") String journeyId);
}
