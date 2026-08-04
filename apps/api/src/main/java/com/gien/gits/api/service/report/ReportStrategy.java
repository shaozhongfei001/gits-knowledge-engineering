package com.gien.gits.api.service.report;

import com.gien.gits.engagement.PostvisitAnalysisContent;
import com.gien.gits.ontology.RelationshipReport;

import java.util.Optional;

/**
 * 报告生成策略接口 — 不同类型的关系报告采用不同的生成策略
 * 实现: R5A(内部关系), R5B(CRM通话), R7(更新关系), R8(下次访前)
 */
public interface ReportStrategy {

    /**
     * 生成报告
     * @param context 报告生成上下文
     * @return 生成的关系报告
     */
    RelationshipReport generate(ReportContext context);

    /**
     * 支持的报告类型
     */
    RelationshipReport.ReportType supportedType();
}
