package com.gien.gits.api.dto;

import com.gien.gits.engagement.PrevisitReportContent;
import com.gien.gits.engagement.QuickBattleCard;

/**
 * 访前准备执行响应 — 返回访前报告与60秒作战卡
 *
 * @param previsitReport 访前报告（R1格式），包含客户概览、KYC缺口、产品方案等
 * @param battleCard     60秒作战卡（R2格式），移动端快速查看要点
 */
public record PrevisitExecutionResponse(
        PrevisitReportContent previsitReport,
        QuickBattleCard battleCard) {}
