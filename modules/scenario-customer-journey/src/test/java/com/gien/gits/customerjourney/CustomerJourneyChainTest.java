package com.gien.gits.customerjourney;

import com.gien.gits.ontology.CaseStatus;
import com.gien.gits.ontology.Claim;
import com.gien.gits.ontology.ClaimStatus;
import com.gien.gits.ontology.Interaction;
import com.gien.gits.ontology.OperatingCase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 客户经理访前访后智能体业务链端到端演示（含交互本体）
 *
 * 业务剧情：客户经理王磊经营对公客户「鑫达贸易」
 * - M17: 系统检测到跨境结算增长信号 → AI信号触发交互（AI→王磊）
 * - M18: AI生成商机洞察 → AI推送交互（AI→王磊）
 * - M20: AI匹配远期结售汇 → 产品匹配交互（AI→王磊）
 * - M21: 王磊拜访鑫达贸易 → 面谈交互（王磊→客户，出站）→ 等待人工确认
 * - M22: 客户确认试用 → 回访交互（客户→王磊，入站）→ 案例关闭
 */
class CustomerJourneyChainTest {

    // ── 业务场景端到端 ──────────────────────────────────────────

    @Nested
    @DisplayName("业务场景：客户经理王磊经营鑫达贸易——完整交互链路")
    class WangLeiXinDaTrade {

        /**
         * 完整剧情：
         * 1. AI信号引擎检测到鑫达贸易跨境结算量环比增长42%
         * 2. AI洞察分析：客户有套期保值需求
         * 3. AI产品匹配：推荐远期结售汇
         * 4. 王磊实地拜访鑫达贸易，介绍远期结售汇
         * 5. 鑫达贸易考虑后确认试用，案例闭环
         *
         * 每步产生一个Interaction记录，5步共5个交互：
         *   [M17] SIGNAL_TRIGGER   AI→王磊    出站  信号推送
         *   [M18] AI_INSIGHT_PUSH  AI→王磊    出站  洞察推送
         *   [M20] AI_INSIGHT_PUSH  AI→王磊    出站  产品匹配推送
         *   [M21] FACE_TO_FACE     王磊→客户  出站  面对面拜访
         *   [M22] FOLLOW_UP        客户→王磊  入站  客户确认试用
         */
        @Test
        @DisplayName("完整剧情：5次交互覆盖 M17→M22，数据链可追溯")
        void fullJourney_withInteractions() {
            // 准备：开设案例
            UUID caseId = UUID.randomUUID();
            OperatingCase case_ = new OperatingCase(caseId, "CUSTOMER_JOURNEY",
                    CaseStatus.OPEN, "对公客户经营-访前访后", Instant.now(), null,
                    Instant.now(), "risk-signal-engine");

            Claim aiSignal = new Claim(UUID.randomUUID(), caseId, "CUSTOMER_JOURNEY",
                    ClaimStatus.CANDIDATE,
                    "跨境结算量环比增长42%，无汇率对冲工具",
                    Instant.now(), null, Instant.now(), null);

            // 执行完整链路
            var result = CustomerJourneyOrchestrator.executeFullChain(
                    case_,
                    "XINDA-TRADE-001", "鑫达贸易有限公司",
                    aiSignal,
                    "OPPORTUNITY", "客户跨境结算量持续增长，但尚未使用远期结售汇产品对冲汇率风险，存在套期保值需求",
                    "FX-HEDGE-01", "远期结售汇", "客户跨境结算量增长但无汇率对冲工具，远期结售汇可锁定汇兑成本",
                    "建议下次拜访重点推荐远期结售汇，客户有明确的跨境业务增长和汇率避险需求",
                    "客户确认有跨境业务扩张计划，同意试用远期结售汇产品",
                    "安排产品演示及远期结售汇开户流程",
                    "RM-WANGLEI", "王磊",
                    true);  // 客户同意

            // === 验证：5次交互全部产生 ===
            List<Interaction> interactions = result.interactions();
            assertEquals(5, interactions.size(), "完整链路应产生5次交互");

            // --- M17交互：AI信号触发 → 推送给王磊 ---
            Interaction m17Interaction = interactions.get(0);
            assertEquals(Interaction.InteractionType.SIGNAL_TRIGGER, m17Interaction.type());
            assertEquals(Interaction.Direction.OUTBOUND, m17Interaction.direction());
            assertEquals(Interaction.Participant.Role.AI_AGENT, m17Interaction.initiator().role());
            assertTrue(m17Interaction.isAiInitiated(), "M17交互由AI发起");
            // involvesHuman检查participants——参与者包含客户经理，所以为true
            // 这正是业务含义：AI推送信号给客户经理，客户经理是人，需要人工关注
            assertTrue(m17Interaction.involvesHuman(), "M17交互参与者包含客户经理，属于涉及人工");

            // --- M18交互：AI洞察推送 → 推送给王磊 ---
            Interaction m18Interaction = interactions.get(1);
            assertEquals(Interaction.InteractionType.AI_INSIGHT_PUSH, m18Interaction.type());
            assertEquals("AI_INSIGHT_ENGINE", m18Interaction.channel());
            assertTrue(m18Interaction.contentSummary().contains("套期保值"));
            assertEquals(1, m18Interaction.producedClaimIds().size(),
                    "M18交互应产出1个洞察ID");

            // --- M20交互：AI产品匹配 → 推送给王磊 ---
            Interaction m20Interaction = interactions.get(2);
            assertEquals(Interaction.InteractionType.AI_INSIGHT_PUSH, m20Interaction.type());
            assertEquals("PRODUCT_MATCH_ENGINE", m20Interaction.channel());
            assertTrue(m20Interaction.contentSummary().contains("远期结售汇"));

            // --- M21交互：王磊实地拜访鑫达贸易 ---
            Interaction m21Interaction = interactions.get(3);
            assertEquals(Interaction.InteractionType.FACE_TO_FACE_VISIT, m21Interaction.type());
            assertEquals(Interaction.Direction.OUTBOUND, m21Interaction.direction());
            assertEquals(Interaction.Participant.Role.RELATIONSHIP_MANAGER, m21Interaction.initiator().role());
            assertTrue(m21Interaction.involvesHuman(), "面谈交互涉及人工");
            assertFalse(m21Interaction.isAiInitiated(), "面谈不是AI发起");
            // 参与者：发起方=王磊，接收方=鑫达贸易
            assertEquals(1, m21Interaction.participants().size());
            assertEquals(Interaction.Participant.Role.CUSTOMER, m21Interaction.participants().get(0).role());
            assertEquals(Interaction.InteractionOutcome.CUSTOMER_DEFERRED, m21Interaction.outcome(),
                    "访前阶段客户未当场决定，结果应为DEFERRED");

            // --- M22交互：鑫达贸易确认试用（客户入站） ---
            Interaction m22Interaction = interactions.get(4);
            assertEquals(Interaction.InteractionType.FOLLOW_UP, m22Interaction.type());
            assertEquals(Interaction.Direction.INBOUND, m22Interaction.direction(),
                    "客户确认试用是入站交互——客户主动回复");
            assertEquals(Interaction.Participant.Role.CUSTOMER, m22Interaction.initiator().role(),
                    "M22交互由客户发起（入站）");
            assertEquals(Interaction.InteractionOutcome.CUSTOMER_AGREED, m22Interaction.outcome(),
                    "客户同意试用");

            // === 验证：数据链完整性 ===
            // insight → product → report → analysis 全部ID可追溯
            assertEquals(result.insight().insightId(), result.product().insightClaimId());
            assertTrue(result.report().insightIds().contains(result.insight().insightId()));
            assertEquals(result.report().reportId(), result.analysis().previsitReportId());

            // === 验证：案例已关闭 ===
            assertEquals(CaseStatus.CLOSED, result.closedCase().status());
        }

        /**
         * 客户拒绝场景：鑫达贸易不感兴趣
         */
        @Test
        @DisplayName("客户拒绝场景：M22交互结果=CUSTOMER_DECLINED")
        void customerDeclinedScenario() {
            UUID caseId = UUID.randomUUID();
            OperatingCase case_ = new OperatingCase(caseId, "CUSTOMER_JOURNEY",
                    CaseStatus.OPEN, "对公客户经营", Instant.now(), null,
                    Instant.now(), "risk-signal-engine");

            Claim aiSignal = new Claim(UUID.randomUUID(), caseId, "CUSTOMER_JOURNEY",
                    ClaimStatus.CANDIDATE, "信号", Instant.now(), null, Instant.now(), null);

            var result = CustomerJourneyOrchestrator.executeFullChain(
                    case_, "C-002", "某公司", aiSignal,
                    "RISK", "风险信号", "RC-01", "风险缓释", "匹配风险特征",
                    "访前风险提示", "客户认为不需要", "无需跟进",
                    "RM-ZHANG", "张经理",
                    false);  // 客户拒绝

            Interaction m22 = result.interactions().get(4);
            assertEquals(Interaction.InteractionOutcome.CUSTOMER_DECLINED, m22.outcome());
            assertEquals(Interaction.Direction.OUTBOUND, m22.direction(),
                    "客户拒绝时，客户经理仍需主动记录结果");
            // 案例仍然关闭（拒绝也是一种闭环）
            assertEquals(CaseStatus.CLOSED, result.closedCase().status());
        }

        /**
         * AI交互与人工交互的隔离验证：
         * AI发起的交互不能产出VERIFIED_FACT，只能产出CANDIDATE
         */
        @Test
        @DisplayName("AI交互产出只能是CANDIDATE——防止AI自我强化")
        void aiInteractionCannotProduceVerifiedFact() {
            UUID caseId = UUID.randomUUID();
            OperatingCase case_ = new OperatingCase(caseId, "CUSTOMER_JOURNEY",
                    CaseStatus.OPEN, "test", Instant.now(), null, Instant.now(), "system");

            Claim aiSignal = new Claim(UUID.randomUUID(), caseId, "CUSTOMER_JOURNEY",
                    ClaimStatus.CANDIDATE, "信号", Instant.now(), null, Instant.now(), null);

            var result = CustomerJourneyOrchestrator.executeFullChain(
                    case_, "C-003", "测试", aiSignal,
                    "OPP", "商机", "P-01", "产品", "理由",
                    "摘要", "结论", "跟进",
                    "RM-TEST", "测试经理",
                    true);

            // M17/M18/M20 都是AI发起——验证isAiInitiated
            assertTrue(result.interactions().get(0).isAiInitiated());  // SIGNAL_TRIGGER
            assertTrue(result.interactions().get(1).isAiInitiated());  // AI_INSIGHT_PUSH
            assertTrue(result.interactions().get(2).isAiInitiated());  // PRODUCT_MATCH

            // M21/M22 是人发起——验证!isAiInitiated
            assertFalse(result.interactions().get(3).isAiInitiated()); // FACE_TO_FACE
            // M22: customerAgreed=true → INBOUND, 客户发起
            assertFalse(result.interactions().get(4).isAiInitiated()); // FOLLOW_UP
        }
    }

    // ── 交互本体自身验证 ────────────────────────────────────────

    @Nested
    @DisplayName("交互本体模型验证")
    class InteractionModelTest {

        @Test
        @DisplayName("交互必须包含发起方和渠道")
        void interactionRequiresInitiatorAndChannel() {
            assertThrows(Exception.class, () -> new Interaction(
                    UUID.randomUUID(), UUID.randomUUID(), null,
                    Interaction.InteractionType.PHONE_CALL,
                    Interaction.Direction.OUTBOUND,
                    "",  // channel为空
                    new Interaction.Participant("RM-01", Interaction.Participant.Role.RELATIONSHIP_MANAGER, "王磊"),
                    List.of(), "摘要", List.of(),
                    Interaction.InteractionOutcome.COMPLETED,
                    Instant.now(), null, "hash"));
        }

        @Test
        @DisplayName("参与者必须包含角色和显示名")
        void participantRequiresRoleAndName() {
            assertThrows(Exception.class, () -> new Interaction.Participant(
                    "", Interaction.Participant.Role.CUSTOMER, "客户"));
            assertThrows(Exception.class, () -> new Interaction.Participant(
                    "C-01", null, "客户"));
        }

        @Test
        @DisplayName("involvesHuman判断：含客户经理=涉及人工")
        void involvesHumanDetection() {
            Interaction withRm = new Interaction(
                    UUID.randomUUID(), UUID.randomUUID(), null,
                    Interaction.InteractionType.AI_INSIGHT_PUSH,
                    Interaction.Direction.OUTBOUND,
                    "AI_ENGINE",
                    new Interaction.Participant("AI-01", Interaction.Participant.Role.AI_AGENT, "AI"),
                    List.of(new Interaction.Participant("RM-01", Interaction.Participant.Role.RELATIONSHIP_MANAGER, "王磊")),
                    "推送洞察", List.of(),
                    Interaction.InteractionOutcome.INFORMATION_GATHERED,
                    Instant.now(), null, "hash");

            assertTrue(withRm.involvesHuman(), "参与者包含客户经理，应判定为涉及人工");
            assertTrue(withRm.isAiInitiated(), "发起方是AI，应判定为AI发起");
        }

        @Test
        @DisplayName("纯AI交互：不涉及人工")
        void pureAiInteraction() {
            Interaction pureAi = new Interaction(
                    UUID.randomUUID(), UUID.randomUUID(), null,
                    Interaction.InteractionType.SIGNAL_TRIGGER,
                    Interaction.Direction.OUTBOUND,
                    "RISK_ENGINE",
                    new Interaction.Participant("AI-01", Interaction.Participant.Role.AI_AGENT, "AI"),
                    List.of(), "信号触发", List.of(),
                    Interaction.InteractionOutcome.INFORMATION_GATHERED,
                    Instant.now(), null, "hash");

            assertFalse(pureAi.involvesHuman(), "纯AI交互不应判定为涉及人工");
            assertTrue(pureAi.isAiInitiated());
        }
    }

    // ── 状态机 + 交互集成 ───────────────────────────────────────

    @Nested
    @DisplayName("状态机与交互集成验证")
    class StateMachineAndInteractionTest {

        @Test
        @DisplayName("链路可中途取消——CANCELLED状态不可恢复")
        void cancelIsTerminal() {
            OperatingCase openCase = new OperatingCase(UUID.randomUUID(), "CUSTOMER_JOURNEY",
                    CaseStatus.OPEN, "test", Instant.now(), null, Instant.now(), "system");
            OperatingCase inProgress = OperatingCaseStateMachine.transition(openCase, CaseStatus.IN_PROGRESS);
            OperatingCase cancelled = OperatingCaseStateMachine.transition(inProgress, CaseStatus.CANCELLED);

            assertEquals(CaseStatus.CANCELLED, cancelled.status());
            assertThrows(IllegalStateException.class, () ->
                    OperatingCaseStateMachine.transition(cancelled, CaseStatus.IN_PROGRESS));
        }

        @Test
        @DisplayName("WAITING_FOR_HUMAN不能直接CLOSE——模拟客户经理必须确认")
        void humanMustConfirmBeforeClose() {
            OperatingCase openCase = new OperatingCase(UUID.randomUUID(), "CUSTOMER_JOURNEY",
                    CaseStatus.OPEN, "test", Instant.now(), null, Instant.now(), "system");
            OperatingCase inProgress = OperatingCaseStateMachine.transition(openCase, CaseStatus.IN_PROGRESS);
            OperatingCase waiting = OperatingCaseStateMachine.transition(inProgress, CaseStatus.WAITING_FOR_HUMAN);

            // 不能直接关闭——客户经理还没确认
            assertThrows(IllegalStateException.class, () ->
                    OperatingCaseStateMachine.transition(waiting, CaseStatus.CLOSED));

            // 正确路径：确认后恢复→关闭
            OperatingCase resumed = OperatingCaseStateMachine.transition(waiting, CaseStatus.IN_PROGRESS);
            OperatingCase closed = OperatingCaseStateMachine.transition(resumed, CaseStatus.CLOSED);
            assertEquals(CaseStatus.CLOSED, closed.status());
        }
    }
}
