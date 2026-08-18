package com.gien.gits.engagement.port;

import com.gien.gits.engagement.PostvisitAnalysisContent;

/**
 * 可写访后分析内容仓储端口 — 在 {@link PostvisitAnalysisContentRepository} 只读契约基础上增加写操作。
 *
 * <p>适配器层的 JDBC 实现类应实现此接口，从而同时满足读/写契约。</p>
 */
public interface WritablePostvisitAnalysisContentRepository extends PostvisitAnalysisContentRepository {

    /**
     * 保存访后分析内容。
     *
     * @param content 待保存的访后分析内容
     * @param operatingCaseId 运营案例唯一标识
     */
    void save(PostvisitAnalysisContent content, String operatingCaseId);
}
