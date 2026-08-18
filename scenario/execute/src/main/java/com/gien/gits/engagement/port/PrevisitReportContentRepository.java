package com.gien.gits.engagement.port;

import com.gien.gits.engagement.PrevisitReportContent;

import java.util.Optional;

/**
 * 访前报告内容仓储端口 — 只读操作。
 *
 * <p>定义对 {@link PrevisitReportContent} 的查询契约，由适配器层实现。
 * 写操作见 {@link WritablePrevisitReportContentRepository}。</p>
 */
public interface PrevisitReportContentRepository {

    /**
     * 根据reportId查找访前报告内容。
     *
     * @param reportId 报告唯一标识
     * @return 找到的访前报告内容，若不存在则返回空
     */
    Optional<PrevisitReportContent> findByReportId(String reportId);

    /**
     * 根据运营案例ID查找最近一次访前报告内容。
     *
     * @param operatingCaseId 运营案例唯一标识
     * @return 最近的访前报告内容，若不存在则返回空
     */
    Optional<PrevisitReportContent> findLatestByOperatingCaseId(String operatingCaseId);

    /**
     * 根据journeyId查找访前报告内容。
     *
     * @param journeyId 客户旅程唯一标识
     * @return 找到的访前报告内容，若不存在则返回空
     */
    Optional<PrevisitReportContent> findByJourneyId(String journeyId);
}
