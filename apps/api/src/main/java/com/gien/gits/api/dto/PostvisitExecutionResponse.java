package com.gien.gits.api.dto;

/**
 * 访后处理执行响应 — 返回转录、分析、报告及CRM回写信息
 *
 * @param transcriptId               会议转录唯一标识
 * @param analysisId                 访后分析唯一标识
 * @param internalReportId           内部关系报告唯一标识（R5A）
 * @param crmReportId                CRM通话报告唯一标识（R5B）
 * @param crmCommandCount            CRM回写命令数量
 * @param allCommandsRequireHumanConfirm 是否所有命令都需要人工确认
 */
public record PostvisitExecutionResponse(
        String transcriptId,
        String analysisId,
        String internalReportId,
        String crmReportId,
        int crmCommandCount,
        boolean allCommandsRequireHumanConfirm) {}
