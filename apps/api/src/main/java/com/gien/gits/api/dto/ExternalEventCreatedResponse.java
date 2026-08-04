package com.gien.gits.api.dto;

/**
 * 外部事件创建响应 — 返回新创建事件的标识与状态
 *
 * @param eventId 事件唯一标识
 * @param status  创建状态
 */
public record ExternalEventCreatedResponse(
        String eventId,
        String status) {}
