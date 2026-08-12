package com.gien.gits.ontology;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 一次交互事件的完整记录。
 *
 * 交互是业务链的核心——客户经理与客户之间的每次接触都是一个Interaction。
 * 它是连接 M17(开户)→M18(洞察)→M20(产品匹配)→M21(访前)→M22(访后) 的纽带。
 *
 * 设计原则：
 * - 交互可由人发起（客户经理拜访），也可由系统发起（AI信号推送）
 * - 交互产出可追溯：每次交互可能产生新的 Claim 或触发状态转换
 * - 交互有方向：出站（主动接触客户）和入站（客户主动联系）
 * - 交互有上下文：关联到具体的 OperatingCase 和 CustomerJourney
 */
public record Interaction(
        UUID interactionId,
        UUID caseId,
        UUID journeyId,
        InteractionType type,
        Direction direction,
        Channel channel,
        Participant initiator,
        List<Participant> participants,
        String contentSummary,
        List<UUID> producedClaimIds,
        InteractionOutcome outcome,
        Instant occurredAt,
        Instant endedAt,
        String sourceHash,
        String sourceUri,
        String sourceVersion,
        Instant recordedAt) {

    /** 交互类型——对应业务场景中的不同接触方式 */
    public enum InteractionType {
        /** 信号触发——系统检测到客户行为变化，自动推送 */
        SIGNAL_TRIGGER,
        /** AI洞察推送——系统将分析结果推送给客户经理 */
        AI_INSIGHT_PUSH,
        /** 电话沟通 */
        PHONE_CALL,
        /** 面对面拜访——客户经理实地拜访客户 */
        FACE_TO_FACE_VISIT,
        /** 远程视频会议 */
        VIDEO_CONFERENCE,
        /** 即时消息——微信/企业微信/钉钉 */
        INSTANT_MESSAGE,
        /** 邮件往来 */
        EMAIL,
        /** 产品推介——正式的产品推荐会议 */
        PRODUCT_PRESENTATION,
        /** 客户投诉 */
        CUSTOMER_COMPLAINT,
        /** 回访跟进——签约后的定期回访 */
        FOLLOW_UP
    }

    /** 交互方向 */
    public enum Direction {
        /** 出站：客户经理/系统主动接触客户 */
        OUTBOUND,
        /** 入站：客户主动联系银行 */
        INBOUND
    }

    /** 交互参与者 */
    public record Participant(
            String participantId,
            Role role,
            String displayName) {

        public enum Role {
            /** 客户经理——银行员工 */
            RELATIONSHIP_MANAGER,
            /** 客户——企业/个人 */
            CUSTOMER,
            /** AI智能体——系统自动 */
            AI_AGENT,
            /** 合规审核员 */
            COMPLIANCE_OFFICER,
            /** 产品专家 */
            PRODUCT_SPECIALIST
        }

        public Participant {
            if (participantId == null || participantId.isBlank()) {
                throw new IllegalArgumentException("participantId is required");
            }
            Objects.requireNonNull(role, "role");
            if (displayName == null || displayName.isBlank()) {
                throw new IllegalArgumentException("displayName is required");
            }
        }
    }

    /** 交互结果 */
    public enum InteractionOutcome {
        /** 成功完成——达成预期目标 */
        COMPLETED,
        /** 客户同意——接受推荐/签约 */
        CUSTOMER_AGREED,
        /** 客户拒绝——不接受推荐 */
        CUSTOMER_DECLINED,
        /** 客户需考虑——未当场决定 */
        CUSTOMER_DEFERRED,
        /** 需要跟进——交互中发现新问题 */
        FOLLOW_UP_REQUIRED,
        /** 交互中断——技术/时间原因 */
        INTERRUPTED,
        /** 信息收集——仅收集信息，未推进决策 */
        INFORMATION_GATHERED
    }

    public Interaction {
        Objects.requireNonNull(interactionId, "interactionId");
        Objects.requireNonNull(caseId, "caseId");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(direction, "direction");
        Objects.requireNonNull(channel, "channel");
        Objects.requireNonNull(initiator, "initiator");
        participants = List.copyOf(Objects.requireNonNullElse(participants, List.of()));
        // contentSummary 可以为空（信号触发类交互可能没有自然语言摘要）
        producedClaimIds = List.copyOf(Objects.requireNonNullElse(producedClaimIds, List.of()));
        Objects.requireNonNull(outcome, "outcome");
        Objects.requireNonNull(occurredAt, "occurredAt");
        if (sourceHash == null || sourceHash.isBlank()) {
            throw new IllegalArgumentException("sourceHash is required");
        }
    }

    /**
     * 兼容旧构造器 — V1.0 数据不包含 sourceUri/sourceVersion/recordedAt
     */
    public Interaction(UUID interactionId, UUID caseId, UUID journeyId,
                       InteractionType type, Direction direction, Channel channel,
                       Participant initiator, List<Participant> participants,
                       String contentSummary, List<UUID> producedClaimIds,
                       InteractionOutcome outcome, Instant occurredAt, Instant endedAt,
                       String sourceHash) {
        this(interactionId, caseId, journeyId, type, direction, channel,
             initiator, participants, contentSummary, producedClaimIds,
             outcome, occurredAt, endedAt, sourceHash,
             null, null, null);
    }

    /**
     * 判断此交互是否涉及人工参与（非纯AI自动交互）。
     * 用于决定是否需要 HumanConfirmation 才能推进 OperatingCase 状态。
     */
    public boolean involvesHuman() {
        if (initiator.role() == Participant.Role.RELATIONSHIP_MANAGER
                || initiator.role() == Participant.Role.COMPLIANCE_OFFICER
                || initiator.role() == Participant.Role.PRODUCT_SPECIALIST) {
            return true;
        }
        return participants.stream().anyMatch(p ->
                p.role() == Participant.Role.RELATIONSHIP_MANAGER
                || p.role() == Participant.Role.COMPLIANCE_OFFICER
                || p.role() == Participant.Role.PRODUCT_SPECIALIST);
    }

    /**
     * 判断此交互是否由AI自动发起。
     * AI发起的交互产出只能是CANDIDATE Claim，不能直接作为VERIFIED_FACT。
     */
    public boolean isAiInitiated() {
        return initiator.role() == Participant.Role.AI_AGENT;
    }
}
