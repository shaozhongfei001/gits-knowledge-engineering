package com.gien.gits.engagement.port;

import com.gien.gits.engagement.PrevisitReportContent;

/**
 * 可写访前报告内容仓储端口 — 在 {@link PrevisitReportContentRepository} 只读契约基础上增加写操作。
 *
 * <p>适配器层的 JDBC 实现类应实现此接口，从而同时满足读/写契约。</p>
 */
public interface WritablePrevisitReportContentRepository extends PrevisitReportContentRepository {

    /**
     * 保存访前报告内容。
     *
     * @param content          待保存的访前报告内容
     * @param journeyId        客户旅程唯一标识
     * @param operatingCaseId  运营案例唯一标识
     */
    void save(PrevisitReportContent content, String journeyId, String operatingCaseId);
}
