package com.gien.gits.engagement;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * 客户经营视图 — 聚合客户全维度经营信息的只读视图
 * 用于报告数据驱动化，从多数据源动态组装
 */
public record CustomerOperatingView(
        String customerId,
        String customerName,
        String industry,
        String enterpriseScale,
        String customerTier,
        String riskLevel,
        String rmId,
        String rmName,
        // KYC缺口
        List<String> knownKycItems,
        List<String> partialKycItems,
        List<String> unknownKycItems,
        // 机会信号
        List<OpportunitySignalSummary> activeSignals,
        // 交互历史
        int totalInteractions,
        Instant lastInteractionTime,
        // 旅程状态
        int activeJourneyCount,
        String currentJourneyPhase,
        // 承诺跟踪
        List<CommitmentSummary> pendingCommitments,
        // 事实对账
        int openReconciliationCount,
        // 风险指标
        List<String> riskIndicators) {

    public CustomerOperatingView {
        Objects.requireNonNull(customerId, "customerId");
        knownKycItems = List.copyOf(knownKycItems != null ? knownKycItems : List.of());
        partialKycItems = List.copyOf(partialKycItems != null ? partialKycItems : List.of());
        unknownKycItems = List.copyOf(unknownKycItems != null ? unknownKycItems : List.of());
        activeSignals = List.copyOf(activeSignals != null ? activeSignals : List.of());
        pendingCommitments = List.copyOf(pendingCommitments != null ? pendingCommitments : List.of());
        riskIndicators = List.copyOf(riskIndicators != null ? riskIndicators : List.of());
    }

    /**
     * 机会信号摘要
     */
    public record OpportunitySignalSummary(
            String signalType,
            String content,
            BigDecimal confidence,
            String status) {}

    /**
     * 承诺摘要
     */
    public record CommitmentSummary(
            String commitmentType,
            String content,
            String owner,
            String dueDate,
            boolean fulfilled) {}

    /**
     * 判断是否有活跃的机会信号
     */
    public boolean hasActiveSignals() {
        return !activeSignals.isEmpty();
    }

    /**
     * 判断是否有未完成的事实对账
     */
    public boolean hasOpenReconciliations() {
        return openReconciliationCount > 0;
    }

    /**
     * 判断是否有KYC缺口
     */
    public boolean hasKycGaps() {
        return !unknownKycItems.isEmpty() || !partialKycItems.isEmpty();
    }

    /**
     * 获取风险等级描述
     */
    public String riskLevelDescription() {
        return switch (riskLevel) {
            case "HIGH" -> "高风险";
            case "MEDIUM" -> "中风险";
            case "LOW" -> "低风险";
            default -> "未知";
        };
    }
}
