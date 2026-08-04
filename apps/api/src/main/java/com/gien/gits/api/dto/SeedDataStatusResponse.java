package com.gien.gits.api.dto;

/**
 * 种子数据状态查询响应 — 返回种子数据是否已加载
 *
 * @param loaded 种子数据是否已加载
 */
public record SeedDataStatusResponse(
        boolean loaded) {}
