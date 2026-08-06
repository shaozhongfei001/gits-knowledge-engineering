package com.gien.gits.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.gien.gits.evaluation.EvaluationContext;
import com.gien.gits.evaluation.EvaluationPort;
import com.gien.gits.evaluation.EvaluationResult;
import com.gien.gits.ontology.CaseStatus;
import com.gien.gits.ontology.CaseType;
import com.gien.gits.ontology.Channel;
import com.gien.gits.ontology.Claim;
import com.gien.gits.ontology.ClaimStatus;
import com.gien.gits.ontology.ClaimType;
import com.gien.gits.ontology.Interaction;
import com.gien.gits.ontology.OperatingCase;
import com.gien.gits.ontology.PolicyRule;
import com.gien.gits.ontology.port.ClaimRepository;
import com.gien.gits.ontology.port.InteractionRepository;
import com.gien.gits.ontology.port.OperatingCaseRepository;
import com.gien.gits.ontology.port.PolicyRuleRepository;

/**
 * EvaluationController 严谨测试 — 验证评分逻辑和上下文构建
 * 
 * 核心业务规则:
 *   - caseId不存在 → 404
 *   - evidenceCompleteCount = VERIFIED_FACT状态的Claim数量
 *   - ruleHitCount = CRITICAL或HIGH severity的PolicyRule数量
 *   - lastDataUpdateAt = 最新的Interaction.occurredAt，无Interaction时用case.recordedAt
 *   - 评分结果维度映射正确
 */
class EvaluationControllerTest {

    private EvaluationPort evaluator;
    private OperatingCaseRepository caseRepo;
    private ClaimRepository claimRepo;
    private InteractionRepository interactionRepo;
    private PolicyRuleRepository ruleRepo;
    private EvaluationController controller;

    @BeforeEach
    void setUp() {
        evaluator = Mockito.mock(EvaluationPort.class);
        caseRepo = Mockito.mock(OperatingCaseRepository.class);
        claimRepo = Mockito.mock(ClaimRepository.class);
        interactionRepo = Mockito.mock(InteractionRepository.class);
        ruleRepo = Mockito.mock(PolicyRuleRepository.class);
        controller = new EvaluationController(evaluator, caseRepo, claimRepo, interactionRepo, ruleRepo);
    }

    private OperatingCase makeCase(UUID caseId) {
        Instant now = Instant.now();
        return new OperatingCase(caseId, CaseType.CONTINUOUS_ENGAGEMENT, CaseStatus.OPEN, "test purpose", now, null, now, "tester");
    }

    private Claim makeClaim(UUID caseId, ClaimStatus status) {
        Instant now = Instant.now();
        return new Claim(UUID.randomUUID(), caseId, ClaimType.RISK_SIGNAL, status, "content-" + status, now, null, now, null);
    }

    private Interaction makeInteraction(UUID caseId, Instant occurredAt) {
        return new Interaction(
            UUID.randomUUID(), caseId, null,
            Interaction.InteractionType.PHONE_CALL,
            Interaction.Direction.OUTBOUND,
            Channel.PHONE,
            new Interaction.Participant("RM-001", Interaction.Participant.Role.RELATIONSHIP_MANAGER, "张经理"),
            List.of(), "通话内容摘要", List.of(),
            Interaction.InteractionOutcome.COMPLETED,
            occurredAt, null, "hash-" + occurredAt.toString());
    }

    // ── 404场景 ────────────────────────────────────────────────

    @Nested
    @DisplayName("案例不存在场景")
    class CaseNotFoundTests {

        @Test
        @DisplayName("caseId不存在 → 返回404")
        void nonexistentCase_returns404() {
            UUID caseId = UUID.randomUUID();
            when(caseRepo.findById(caseId)).thenReturn(Optional.empty());

            ResponseEntity<EvaluationController.EvaluationResponse> response = controller.evaluate(caseId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNull();
        }

        @Test
        @DisplayName("caseId不存在时不应调用evaluator")
        void nonexistentCase_doesNotCallEvaluator() {
            UUID caseId = UUID.randomUUID();
            when(caseRepo.findById(caseId)).thenReturn(Optional.empty());

            controller.evaluate(caseId);

            verify(evaluator, Mockito.never()).score(any());
        }
    }

    // ── 上下文构建: evidenceCompleteCount ──────────────────────

    @Nested
    @DisplayName("上下文构建: evidenceCompleteCount计算")
    class EvidenceCompleteCountTests {

        @Test
        @DisplayName("仅VERIFIED_FACT状态的Claim计入evidenceCompleteCount")
        void onlyVerifiedFactsCounted() {
            UUID caseId = UUID.randomUUID();
            when(caseRepo.findById(caseId)).thenReturn(Optional.of(makeCase(caseId)));

            Claim verified1 = makeClaim(caseId, ClaimStatus.VERIFIED_FACT);
            Claim verified2 = makeClaim(caseId, ClaimStatus.VERIFIED_FACT);
            Claim candidate = makeClaim(caseId, ClaimStatus.CANDIDATE);
            when(claimRepo.findByCaseId(caseId)).thenReturn(List.of(verified1, verified2, candidate));
            when(interactionRepo.findByCaseId(caseId)).thenReturn(List.of());
            when(ruleRepo.findAll()).thenReturn(List.of());

            when(evaluator.score(any(EvaluationContext.class)))
                .thenReturn(new EvaluationResult(0.7, Map.of("evidence", 0.7, "freshness", 0.5, "ruleHit", 0.9)));

            ResponseEntity<EvaluationController.EvaluationResponse> response = controller.evaluate(caseId);

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().contextSummary().evidenceCount()).isEqualTo(3);
            assertThat(response.getBody().contextSummary().evidenceCompleteCount()).isEqualTo(2);
        }
    }

    // ── 上下文构建: lastDataUpdateAt ───────────────────────────

    @Nested
    @DisplayName("上下文构建: lastDataUpdateAt计算")
    class LastDataUpdateAtTests {

        @Test
        @DisplayName("有Interaction时，lastDataUpdateAt = 最新的Interaction.occurredAt")
        void withInteraction_usesLatestInteractionTime() {
            UUID caseId = UUID.randomUUID();
            Instant caseTime = Instant.parse("2026-01-01T00:00:00Z");
            Instant interactionTime = Instant.parse("2026-06-15T12:00:00Z");
            OperatingCase oc = new OperatingCase(caseId, CaseType.CONTINUOUS_ENGAGEMENT, CaseStatus.OPEN, "test", caseTime, null, caseTime, "user");
            when(caseRepo.findById(caseId)).thenReturn(Optional.of(oc));

            Interaction interaction = makeInteraction(caseId, interactionTime);
            when(claimRepo.findByCaseId(caseId)).thenReturn(List.of());
            when(interactionRepo.findByCaseId(caseId)).thenReturn(List.of(interaction));
            when(ruleRepo.findAll()).thenReturn(List.of());

            when(evaluator.score(any(EvaluationContext.class)))
                .thenReturn(new EvaluationResult(0.5, Map.of("evidence", 0.5, "freshness", 0.5, "ruleHit", 0.5)));

            controller.evaluate(caseId);

            verify(evaluator).score(argThat(ctx -> ctx.lastDataUpdateAt().equals(interactionTime)));
        }

        @Test
        @DisplayName("无Interaction时，lastDataUpdateAt = case.recordedAt")
        void withoutInteraction_usesCaseRecordedAt() {
            UUID caseId = UUID.randomUUID();
            Instant caseTime = Instant.parse("2026-01-01T00:00:00Z");
            OperatingCase oc = new OperatingCase(caseId, CaseType.CONTINUOUS_ENGAGEMENT, CaseStatus.OPEN, "test", caseTime, null, caseTime, "user");
            when(caseRepo.findById(caseId)).thenReturn(Optional.of(oc));
            when(claimRepo.findByCaseId(caseId)).thenReturn(List.of());
            when(interactionRepo.findByCaseId(caseId)).thenReturn(List.of());
            when(ruleRepo.findAll()).thenReturn(List.of());

            when(evaluator.score(any(EvaluationContext.class)))
                .thenReturn(new EvaluationResult(0.5, Map.of("evidence", 0.5, "freshness", 0.5, "ruleHit", 0.5)));

            controller.evaluate(caseId);

            verify(evaluator).score(argThat(ctx -> ctx.lastDataUpdateAt().equals(caseTime)));
        }
    }

    // ── 上下文构建: ruleHitCount ───────────────────────────────

    @Nested
    @DisplayName("上下文构建: ruleHitCount计算")
    class RuleHitCountTests {

        @Test
        @DisplayName("仅CRITICAL和HIGH severity的规则计入ruleHitCount")
        void onlyCriticalAndHighSeverityCounted() {
            UUID caseId = UUID.randomUUID();
            when(caseRepo.findById(caseId)).thenReturn(Optional.of(makeCase(caseId)));
            when(claimRepo.findByCaseId(caseId)).thenReturn(List.of());
            when(interactionRepo.findByCaseId(caseId)).thenReturn(List.of());

            PolicyRule critical = new PolicyRule("R001", "CR规则", PolicyRule.Severity.CRITICAL, "cond", "act");
            PolicyRule high = new PolicyRule("R002", "HIGH规则", PolicyRule.Severity.HIGH, "cond", "act");
            PolicyRule medium = new PolicyRule("R003", "MED规则", PolicyRule.Severity.MEDIUM, "cond", "act");
            when(ruleRepo.findAll()).thenReturn(List.of(critical, high, medium));

            when(evaluator.score(any(EvaluationContext.class)))
                .thenReturn(new EvaluationResult(0.5, Map.of("evidence", 0.5, "freshness", 0.5, "ruleHit", 0.5)));

            ResponseEntity<EvaluationController.EvaluationResponse> response = controller.evaluate(caseId);

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().contextSummary().ruleHitCount()).isEqualTo(2); // CRITICAL + HIGH
            assertThat(response.getBody().contextSummary().totalRuleCount()).isEqualTo(3);
        }
    }

    // ── 评分结果映射 ────────────────────────────────────────────

    @Nested
    @DisplayName("评分结果映射")
    class ResultMappingTests {

        @Test
        @DisplayName("EvaluationResult正确映射到EvaluationResponse")
        void resultMapping_correct() {
            UUID caseId = UUID.randomUUID();
            when(caseRepo.findById(caseId)).thenReturn(Optional.of(makeCase(caseId)));
            when(claimRepo.findByCaseId(caseId)).thenReturn(List.of());
            when(interactionRepo.findByCaseId(caseId)).thenReturn(List.of());
            when(ruleRepo.findAll()).thenReturn(List.of());

            Map<String, Double> dimensions = Map.of("evidence", 0.7, "freshness", 0.5, "ruleHit", 0.75);
            when(evaluator.score(any(EvaluationContext.class)))
                .thenReturn(new EvaluationResult(0.65, dimensions));

            ResponseEntity<EvaluationController.EvaluationResponse> response = controller.evaluate(caseId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().caseId()).isEqualTo(caseId);
            assertThat(response.getBody().compositeScore()).isEqualTo(0.65);
            assertThat(response.getBody().dimensions()).containsEntry("evidence", 0.7);
            assertThat(response.getBody().dimensions()).containsEntry("freshness", 0.5);
            assertThat(response.getBody().dimensions()).containsEntry("ruleHit", 0.75);
            assertThat(response.getBody().evaluatedAt()).isNotNull();
        }
    }
}
