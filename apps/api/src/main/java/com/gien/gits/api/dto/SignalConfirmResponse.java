package com.gien.gits.api.dto;

/**
 * 信号确认响应 — 返回已确认信号的标识与状态
 *
 * @param signalId 信号唯一标识
 * @param status   确认状态
 */
public record SignalConfirmResponse(
        String signalId,
        String status) {}
