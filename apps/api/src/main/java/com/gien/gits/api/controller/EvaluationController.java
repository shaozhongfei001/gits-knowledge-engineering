package com.gien.gits.api.controller;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gien.gits.evaluation.EvaluationContext;
import com.gien.gits.evaluation.EvaluationPort;
import com.gien.gits.evaluation.EvaluationResult;
import com.gien.gits.ontology.Claim;
import com.gien.gits.ontology.ClaimStatus;
import com.gien.gits.ontology.Interaction;
import com.gien.gits.ontology.OperatingCase;
import com.gien.gits.ontology.PolicyRule;
import com.gien.gits.ontology.port.ClaimRepository;
import com.gien.gits.ontology.port.InteractionRepository;
import com.gien.gits.ontology.port.OperatingCaseRepository;
import com.gien.gits.ontology.port.PolicyRuleRepository;

/**
 * Evaluation REST controller — exposes rule-based scoring for operating cases.
 */
@RestController
@RequestMapping("/api/evaluation")
public class EvaluationController {

    private final EvaluationPort evaluator;
    private final OperatingCaseRepository caseRepo;
    private final ClaimRepository claimRepo;
    private final InteractionRepository interactionRepo;
    private final PolicyRuleRepository ruleRepo;

    public EvaluationController(EvaluationPort evaluator,
                                OperatingCaseRepository caseRepo,
                                ClaimRepository claimRepo,
                                InteractionRepository interactionRepo,
                                PolicyRuleRepository ruleRepo) {
        this.evaluator = evaluator;
        this.caseRepo = caseRepo;
        this.claimRepo = claimRepo;
        this.interactionRepo = interactionRepo;
        this.ruleRepo = ruleRepo;
    }

    /** Evaluate an operating case and return its composite score with dimension breakdown. */
    @GetMapping("/{caseId}")
    public ResponseEntity<EvaluationResponse> evaluate(@PathVariable UUID caseId) {
        var operatingCase = caseRepo.findById(caseId);
        if (operatingCase.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        EvaluationContext context = buildContext(operatingCase.get());
        EvaluationResult result = evaluator.score(context);

        return ResponseEntity.ok(new EvaluationResponse(
                caseId,
                result.composite(),
                result.dimensions(),
                new ContextSummary(
                        context.evidenceCount(),
                        context.evidenceCompleteCount(),
                        context.lastDataUpdateAt(),
                        context.ruleHitCount(),
                        context.totalRuleCount()),
                Instant.now()));
    }

    private EvaluationContext buildContext(OperatingCase oc) {
        List<Claim> claims = claimRepo.findByCaseId(oc.caseId());
        List<Interaction> interactions = interactionRepo.findByCaseId(oc.caseId());
        List<PolicyRule> allRules = ruleRepo.findAll();

        int evidenceCount = claims.size();
        int evidenceCompleteCount = (int) claims.stream()
                .filter(c -> c.status() == ClaimStatus.VERIFIED_FACT)
                .count();

        Instant lastUpdate = interactions.stream()
                .map(Interaction::occurredAt)
                .max(Instant::compareTo)
                .orElse(oc.recordedAt());

        // Rule hit count: count rules whose severity is CRITICAL or HIGH as "hit"
        // This is a simplified heuristic; a real implementation would check actual rule evaluation results
        int ruleHitCount = (int) allRules.stream()
                .filter(r -> r.severity() == PolicyRule.Severity.CRITICAL
                        || r.severity() == PolicyRule.Severity.HIGH)
                .count();

        return new EvaluationContext(
                evidenceCount,
                evidenceCompleteCount,
                lastUpdate,
                ruleHitCount,
                allRules.size());
    }

    // ── Response DTOs ────────────────────────────────────────────────

    public record EvaluationResponse(
            UUID caseId,
            double compositeScore,
            java.util.Map<String, Double> dimensions,
            ContextSummary contextSummary,
            Instant evaluatedAt) {}

    public record ContextSummary(
            int evidenceCount,
            int evidenceCompleteCount,
            Instant lastDataUpdateAt,
            int ruleHitCount,
            int totalRuleCount) {}
}
