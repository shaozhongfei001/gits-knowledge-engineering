package com.gien.gits.api.service.report;

import com.gien.gits.action.port.CrmWritebackChannel;
import com.gien.gits.action.port.CrmWritebackChannel.WritebackResult;
import com.gien.gits.engagement.CrmWritebackCommand;
import com.gien.gits.engagement.InteractionExtraction;
import com.gien.gits.engagement.PostvisitAnalysisContent;
import com.gien.gits.ontology.port.DomainEventPublisher;
import com.gien.gits.adapter.event.CloudEventFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * CRM回写服务 — 生成回写命令并通过CrmWritebackChannel执行回写。
 * 所有命令强制 require_human_confirm=true (RULE-CRM-001)。
 * 只有经过人工确认的命令才会执行回写。
 */
public class CrmWritebackService {

    private static final Logger log = LoggerFactory.getLogger(CrmWritebackService.class);

    private final CrmWritebackChannel channel;
    private final DomainEventPublisher domainEventPublisher;

    public CrmWritebackService(CrmWritebackChannel channel, DomainEventPublisher domainEventPublisher) {
        this.channel = channel;
        this.domainEventPublisher = domainEventPublisher;
    }

    /**
     * 基于访后分析生成CRM回写命令并执行回写。
     * 门控：只有 requiresHumanConfirm=true 的命令才会执行回写。
     */
    public List<WritebackResult> executeWriteback(
            String operatingCaseId, String journeyId,
            PostvisitAnalysisContent analysis) {
        List<CrmWritebackCommand> commands = generateFromAnalysis(operatingCaseId, journeyId, analysis);
        List<WritebackResult> results = new ArrayList<>();

        for (CrmWritebackCommand command : commands) {
            // 门控：只有经过人工确认的命令才执行回写 (RULE-CRM-001)
            if (!command.requiresHumanConfirm()) {
                log.warn("[CRM-WRITEBACK] Skipping commandId={}: requiresHumanConfirm is false", command.commandId());
                results.add(WritebackResult.failed("Skipped: requiresHumanConfirm is false"));
                continue;
            }
            WritebackResult result = channel.send(command);
            results.add(result);
            log.info("[CRM-WRITEBACK] commandId={} result: success={}, messageId={}, detail={}",
                     command.commandId(), result.success(), result.messageId(), result.detail());
        }

        return results;
    }

    /**
     * 基于访后分析生成CRM回写命令
     */
    public List<CrmWritebackCommand> generateFromAnalysis(
            String operatingCaseId, String journeyId,
            PostvisitAnalysisContent analysis) {
        List<CrmWritebackCommand> commands = new ArrayList<>();

        // 1. 互动记录回写
        commands.add(new CrmWritebackCommand(
            "CMD-" + UUID.randomUUID().toString().substring(0, 8),
            CrmWritebackCommand.ObjectType.INTERACTION,
            CrmWritebackCommand.Operation.CREATE,
            "N/A",
            "访后互动记录: " + analysis.visitSummary(),
            CrmWritebackCommand.RiskLevel.LOW,
            true,  // RULE-CRM-001: 强制人工确认
            journeyId, operatingCaseId, "AUDIT-" + UUID.randomUUID()));

        // 2. 关键发现回写
        for (InteractionExtraction finding : analysis.keyFindings()) {
            commands.add(new CrmWritebackCommand(
                "CMD-" + UUID.randomUUID().toString().substring(0, 8),
                mapObjectType(finding.type()),
                CrmWritebackCommand.Operation.UPDATE,
                finding.evidenceRef(),
                finding.content(),
                mapRiskLevel(finding.notFact()),
                true,  // RULE-CRM-001
                journeyId, operatingCaseId, "AUDIT-" + UUID.randomUUID()));
        }

        // 3. 机会信号回写
        for (PostvisitAnalysisContent.OpportunitySignalItem signal : analysis.opportunitySignals()) {
            commands.add(new CrmWritebackCommand(
                "CMD-" + UUID.randomUUID().toString().substring(0, 8),
                CrmWritebackCommand.ObjectType.CUSTOMER,
                CrmWritebackCommand.Operation.UPDATE,
                signal.sourceType(),
                "机会信号: " + signal.content(),
                CrmWritebackCommand.RiskLevel.MEDIUM,
                true,  // RULE-CRM-001
                journeyId, operatingCaseId, "AUDIT-" + UUID.randomUUID()));
        }

        // 发布领域事件: controlledActionRequested
        domainEventPublisher.publish(
            CloudEventFactory.controlledActionRequested(
                operatingCaseId,
                Map.of("journeyId", journeyId, "commandCount", commands.size())));

        return commands;
    }

    private CrmWritebackCommand.ObjectType mapObjectType(InteractionExtraction.ExtractionType type) {
        return switch (type) {
            case OPPORTUNITY_SIGNAL -> CrmWritebackCommand.ObjectType.CUSTOMER;
            case FACT_CLAIM -> CrmWritebackCommand.ObjectType.INTERACTION;
            case COMMITMENT -> CrmWritebackCommand.ObjectType.COMMITMENT;
            case RISK_INDICATOR -> CrmWritebackCommand.ObjectType.CREDIT_FACILITY;
            case CLAIM -> CrmWritebackCommand.ObjectType.INTERACTION;
            case INTENT, CLARIFIED_INTENT -> CrmWritebackCommand.ObjectType.CUSTOMER;
            case CUSTOMER_COMMITMENT, BANK_COMMITMENT -> CrmWritebackCommand.ObjectType.COMMITMENT;
        };
    }

    private CrmWritebackCommand.RiskLevel mapRiskLevel(boolean notFact) {
        return notFact ? CrmWritebackCommand.RiskLevel.HIGH : CrmWritebackCommand.RiskLevel.LOW;
    }
}
