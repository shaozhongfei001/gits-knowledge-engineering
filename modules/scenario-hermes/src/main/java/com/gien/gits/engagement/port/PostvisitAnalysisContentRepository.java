package com.gien.gits.engagement.port;

import com.gien.gits.engagement.PostvisitAnalysisContent;

import java.util.Optional;

/**
 * 访后分析内容仓储端口 — 只读操作。
 *
 * <p>定义对 {@link PostvisitAnalysisContent} 的查询契约，由适配器层实现。
 * 写操作见 {@link WritablePostvisitAnalysisContentRepository}。</p>
 */
public interface PostvisitAnalysisContentRepository {

    /**
     * 根据analysisId查找访后分析内容。
     *
     * @param analysisId 分析唯一标识
     * @return 找到的访后分析内容，若不存在则返回空
     */
    Optional<PostvisitAnalysisContent> findByAnalysisId(String analysisId);

    /**
     * 根据运营案例ID查找最近一次访后分析内容。
     *
     * @param operatingCaseId 运营案例唯一标识
     * @return 最近的访后分析内容，若不存在则返回空
     */
    Optional<PostvisitAnalysisContent> findLatestByOperatingCaseId(String operatingCaseId);

    /**
     * 根据journeyId查找访后分析内容。
     *
     * @param journeyId 客户旅程唯一标识
     * @return 找到的访后分析内容，若不存在则返回空
     */
    Optional<PostvisitAnalysisContent> findByJourneyId(String journeyId);
}
