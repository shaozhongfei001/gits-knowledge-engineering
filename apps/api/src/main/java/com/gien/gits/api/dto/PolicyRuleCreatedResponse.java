package com.gien.gits.api.dto;

/**
 * 政策规则创建响应 — 返回新创建规则的标识与状态
 *
 * @param ruleId 规则唯一标识
 * @param status 创建状态
 */
public record PolicyRuleCreatedResponse(
        String ruleId,
        String status) {}
