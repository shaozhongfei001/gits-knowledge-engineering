package com.gien.gits.api.dto;

/**
 * 种子数据加载响应 — 返回加载状态与消息
 *
 * @param status  加载状态（LOADED 或 ALREADY_LOADED）
 * @param message 状态描述消息
 */
public record SeedDataLoadResponse(
        String status,
        String message) {}
