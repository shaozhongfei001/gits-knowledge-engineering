package com.gien.gits.ontology;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * 人工门禁 — 对应ControlledAction的人审环节
 *
 * @param gateId         门禁唯一标识
 * @param gateType       门禁类型
 * @param journeyId      关联旅程ID
 * @param customerId     关联客户ID
 * @param operatingCaseId 关联经营案例ID
 * @param status         门禁状态
 * @param subject        门禁主题
 * @param proposal       AI提案（JSON对象）
 * @param evidenceRefs   证据引用列表
 * @param decision       决策结果
 * @param modification   修改内容（当decision=MODIFY时）
 * @param decisionReason 决策原因
 * @param actorId        决策人ID
 * @param createdAt      创建时间
 * @param decidedAt      决策时间
 */
public record HumanGate(
        String gateId,
        GateType gateType,
        String journeyId,
        String customerId,
        String operatingCaseId,
        HumanGateStatus status,
        String subject,
        Map<String, Object> proposal,
        List<String> evidenceRefs,
        GateDecision decision,
        Map<String, Object> modification,
        String decisionReason,
        String actorId,
        Instant createdAt,
        Instant decidedAt
) {
    public HumanGate withDecision(GateDecision decision, Map<String, Object> modification,
                                   String reason, String actorId) {
        return new HumanGate(
                gateId, gateType, journeyId, customerId, operatingCaseId,
                mapDecisionToStatus(decision),
                subject, proposal, evidenceRefs,
                decision, modification, reason, actorId,
                createdAt, Instant.now()
        );
    }

    private static HumanGateStatus mapDecisionToStatus(GateDecision decision) {
        return switch (decision) {
            case APPROVE -> HumanGateStatus.APPROVED;
            case REJECT, DECLINE -> HumanGateStatus.REJECTED;
            case MODIFY -> HumanGateStatus.MODIFIED;
            case HOLD -> HumanGateStatus.PENDING;
        };
    }
}
