package com.gien.gits.api.dto;

/**
 * 旅程启动响应 — 返回新创建的客户旅程信息
 *
 * @param journeyId  旅程唯一标识
 * @param customerId 客户唯一标识
 * @param phase      旅程当前阶段
 * @param startedAt  旅程启动时间（ISO格式）
 */
public record JourneyStartResponse(
        String journeyId,
        String customerId,
        String phase,
        String startedAt) {}
