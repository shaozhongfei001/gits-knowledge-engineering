package com.gien.gits.engagement.port;

import com.gien.gits.engagement.OutreachScript;

import java.util.List;
import java.util.Optional;

/**
 * 外联脚本仓储端口 — 只读操作。
 *
 * <p>定义对 {@link OutreachScript} 的查询契约，由适配器层实现。
 * 写操作见 {@link WritableOutreachScriptRepository}。</p>
 */
public interface OutreachScriptRepository {

    /**
     * 根据scriptId查找外联脚本。
     *
     * @param scriptId 脚本唯一标识
     * @return 找到的外联脚本，若不存在则返回空
     */
    Optional<OutreachScript> findByScriptId(String scriptId);

    /**
     * 根据客户ID查找外联脚本列表。
     *
     * @param customerId 客户唯一标识
     * @return 该客户的外联脚本列表
     */
    List<OutreachScript> findByCustomerId(String customerId);

    /**
     * 根据运营案例ID查找外联脚本列表。
     *
     * @param operatingCaseId 运营案例唯一标识
     * @return 该运营案例的外联脚本列表
     */
    List<OutreachScript> findByOperatingCaseId(String operatingCaseId);

    /**
     * 根据journeyId查找外联脚本列表。
     *
     * @param journeyId 客户旅程唯一标识
     * @return 该旅程的外联脚本列表
     */
    List<OutreachScript> findByJourneyId(String journeyId);
}
