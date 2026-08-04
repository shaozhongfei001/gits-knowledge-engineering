package com.gien.gits.api.dto;

/**
 * 信号驳回响应 — 返回已驳回信号的标识与状态
 *
 * @param signalId 信号唯一标识
 * @param status   驳回状态
 */
public record SignalDismissResponse(
        String signalId,
        String status) {}
