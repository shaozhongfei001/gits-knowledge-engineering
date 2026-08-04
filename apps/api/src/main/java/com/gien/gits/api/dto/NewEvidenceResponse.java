package com.gien.gits.api.dto;

/**
 * 新证据处理响应 — 返回更新后的报告与下次访前报告
 *
 * @param updatedReportId     更新后的关系报告唯一标识（R7）
 * @param nextPrevisitReportId 下次访前报告唯一标识（R8）
 */
public record NewEvidenceResponse(
        String updatedReportId,
        String nextPrevisitReportId) {}
