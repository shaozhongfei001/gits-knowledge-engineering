package com.gien.gits.api.dto;

/**
 * 客户创建响应 — 返回新创建客户的标识与状态
 *
 * @param customerId 客户唯一标识
 * @param status     创建状态
 */
public record CustomerCreatedResponse(
        String customerId,
        String status) {}
