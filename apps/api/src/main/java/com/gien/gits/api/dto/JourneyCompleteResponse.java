package com.gien.gits.api.dto;

/**
 * 旅程完成响应 — 返回旅程完成状态
 *
 * @param status    旅程完成状态
 * @param journeyId 旅程唯一标识
 */
public record JourneyCompleteResponse(
        String status,
        String journeyId) {}
