package com.gien.gits.engagement.port;

import com.gien.gits.engagement.MeetingScript;

import java.util.List;
import java.util.Optional;

/**
 * 会面脚本仓储端口 — 只读操作。
 *
 * <p>定义对 {@link MeetingScript} 的查询契约，由适配器层实现。
 * 写操作见 {@link WritableMeetingScriptRepository}。</p>
 */
public interface MeetingScriptRepository {

    /**
     * 根据scriptId查找会面脚本。
     *
     * @param scriptId 脚本唯一标识
     * @return 找到的会面脚本，若不存在则返回空
     */
    Optional<MeetingScript> findByScriptId(String scriptId);

    /**
     * 根据客户ID查找会面脚本列表。
     *
     * @param customerId 客户唯一标识
     * @return 该客户的会面脚本列表
     */
    List<MeetingScript> findByCustomerId(String customerId);

    /**
     * 根据运营案例ID查找会面脚本列表。
     *
     * @param operatingCaseId 运营案例唯一标识
     * @return 该运营案例的会面脚本列表
     */
    List<MeetingScript> findByOperatingCaseId(String operatingCaseId);

    /**
     * 根据journeyId查找会面脚本列表。
     *
     * @param journeyId 客户旅程唯一标识
     * @return 该旅程的会面脚本列表
     */
    List<MeetingScript> findByJourneyId(String journeyId);
}
