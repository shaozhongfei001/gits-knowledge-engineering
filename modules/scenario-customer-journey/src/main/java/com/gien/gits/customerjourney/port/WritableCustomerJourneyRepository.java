package com.gien.gits.customerjourney.port;

import com.gien.gits.customerjourney.CustomerJourney;
import com.gien.gits.customerjourney.InsightClaim;
import com.gien.gits.customerjourney.JourneyPhase;
import com.gien.gits.customerjourney.PostvisitAnalysis;
import com.gien.gits.customerjourney.PrevisitReport;
import com.gien.gits.customerjourney.ProductCandidateClaim;

import java.util.UUID;

/**
 * 可写客户旅程仓储端口 — 在 {@link CustomerJourneyRepository} 只读契约基础上增加写操作。
 *
 * <p>适配器层的 JDBC 实现类应实现此接口，从而同时满足读/写契约。</p>
 */
public interface WritableCustomerJourneyRepository extends CustomerJourneyRepository {

    // ── CustomerJourney 写操作 ──

    /**
     * 保存客户旅程聚合。
     *
     * @param journey 待保存的客户旅程
     */
    void saveJourney(CustomerJourney journey);

    /**
     * 更新客户旅程阶段。
     *
     * @param journeyId 旅程唯一标识
     * @param phase     目标阶段
     */
    void updateJourneyPhase(UUID journeyId, JourneyPhase phase);

    // ── InsightClaim 写操作 ──

    /**
     * 保存洞察声明。
     *
     * @param insight 待保存的洞察声明
     */
    void saveInsight(InsightClaim insight);

    // ── ProductCandidateClaim 写操作 ──

    /**
     * 保存产品候选声明。
     *
     * @param candidate 待保存的产品候选声明
     */
    void saveProductCandidate(ProductCandidateClaim candidate);

    // ── PrevisitReport 写操作 ──

    /**
     * 保存访前报告。
     *
     * @param report 待保存的访前报告
     */
    void savePrevisitReport(PrevisitReport report);

    // ── PostvisitAnalysis 写操作 ──

    /**
     * 保存访后分析。
     *
     * @param analysis 待保存的访后分析
     */
    void savePostvisitAnalysis(PostvisitAnalysis analysis);
}
