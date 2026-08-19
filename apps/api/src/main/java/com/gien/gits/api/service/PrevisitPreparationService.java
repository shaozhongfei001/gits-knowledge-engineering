package com.gien.gits.api.service;

import com.gien.gits.engagement.MeetingScript;
import com.gien.gits.engagement.OutreachScript;
import com.gien.gits.engagement.OutreachScript.OutreachChannel;
import com.gien.gits.engagement.QuickBattleCard;
import java.util.Objects;

/**
 * 访前自动准备编排（P23/G6）：进入访前准备阶段后，按知识地图任务映射一键生成完整访前包。
 *
 * <p>合并"生成外联脚本 / 生成会面脚本 / 执行访前准备"三个动作为一次调用：
 * <ol>
 *   <li>外联脚本（知识地图 KI-FRONT-004 沟通话术驱动）</li>
 *   <li>会面脚本（KI-FRONT-002/003/004 经营/对账/话术驱动）</li>
 *   <li>访前报告 R1 + 速战卡 R2（KI-009 客户信息 + KI-FRONT-001~006 全条目驱动）</li>
 * </ol>
 * 任一环节失败按 fail-closed 抛出，不返回部分结果。</p>
 */
public final class PrevisitPreparationService {

    private final OutreachScriptService outreachScriptService;
    private final MeetingScriptService meetingScriptService;
    private final EngagementOrchestrator orchestrator;

    public PrevisitPreparationService(
            OutreachScriptService outreachScriptService,
            MeetingScriptService meetingScriptService,
            EngagementOrchestrator orchestrator) {
        this.outreachScriptService = Objects.requireNonNull(outreachScriptService);
        this.meetingScriptService = Objects.requireNonNull(meetingScriptService);
        this.orchestrator = Objects.requireNonNull(orchestrator);
    }

    /**
     * 一键生成完整访前包。
     *
     * @return 外联脚本 + 会面脚本 + 访前报告 + 速战卡
     */
    public PreparedPrevisit prepare(
            String journeyId, String customerId, String rmId,
            String operatingCaseId, String visitObjective, OutreachChannel channel) {

        Objects.requireNonNull(journeyId, "journeyId");
        Objects.requireNonNull(customerId, "customerId");
        Objects.requireNonNull(operatingCaseId, "operatingCaseId");

        // 1. 外联脚本（知识地图 KI-FRONT-004 沟通话术驱动）
        OutreachScript outreach = outreachScriptService.generateScript(
                customerId, rmId, operatingCaseId, journeyId, channel);

        // 2. 会面脚本（KI-FRONT-002/003/004 驱动）
        MeetingScript meeting = meetingScriptService.generateScript(
                customerId, rmId, operatingCaseId, journeyId);

        // 3. 访前报告 R1 + 速战卡 R2（KI-009 + KI-FRONT-001~006 驱动）
        EngagementOrchestrator.PrevisitWorkflowResult previsit = orchestrator.executePrevisitPhase(
                journeyId, customerId, operatingCaseId, visitObjective);

        return new PreparedPrevisit(outreach, meeting, previsit.previsitReport(), previsit.battleCard());
    }

    /** 完整访前包（外联 + 会面 + R1 + R2）。 */
    public record PreparedPrevisit(
            OutreachScript outreachScript,
            MeetingScript meetingScript,
            com.gien.gits.engagement.PrevisitReportContent previsitReport,
            QuickBattleCard battleCard) {}
}
