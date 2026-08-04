package com.gien.gits.api.dto;

/**
 * 产品知识卡创建响应 — 返回新创建产品的标识与状态
 *
 * @param productId 产品唯一标识
 * @param status    创建状态
 */
public record ProductCreatedResponse(
        String productId,
        String status) {}
