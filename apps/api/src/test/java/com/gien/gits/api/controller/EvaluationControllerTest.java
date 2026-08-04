package com.gien.gits.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.gien.gits.evaluation.EvaluationContext;
import com.gien.gits.evaluation.EvaluationPort;
import com.gien.gits.evaluation.EvaluationResult;
import com.gien.gits.ontology.CaseStatus;
import com.gien.gits.ontology.CaseType;
import com.gien.gits.ontology.OperatingCase;
import com.gien.gits.ontology.port.ClaimRepository;
import com.gien.gits.ontology.port.InteractionRepository;
import com.gien.gits.ontology.port.OperatingCaseRepository;
import com.gien.gits.ontology.port.PolicyRuleRepository;

/**
 * Unit tests for EvaluationController using direct invocation (no Spring context).
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

    @Test
    void evaluateReturnsScoreForExistingCase() {
        UUID caseId = UUID.randomUUID();
        var operatingCase = new OperatingCase(caseId, CaseType.CONTINUOUS_ENGAGEMENT, CaseStatus.OPEN, "test-purpose", Instant.now(), null, Instant.now(), "test-user");
        when(caseRepo.findById(caseId)).thenReturn(Optional.of(operatingCase));
        when(claimRepo.findByCaseId(caseId)).thenReturn(List.of());
        when(interactionRepo.findByCaseId(caseId)).thenReturn(List.of());
        when(ruleRepo.findAll()).thenReturn(List.of());

        var result = new EvaluationResult(0.65, Map.of("evidence", 0.7, "freshness", 0.5, "ruleHit", 0.75));
        when(evaluator.score(any(EvaluationContext.class))).thenReturn(result);

        ResponseEntity<EvaluationController.EvaluationResponse> response = controller.evaluate(caseId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        var body = response.getBody();
        assertNotNull(body);
        assertEquals(caseId, body.caseId());
        assertEquals(0.65, body.compositeScore(), 0.001);
        assertEquals(0.7, body.dimensions().get("evidence"), 0.001);
        assertEquals(0.5, body.dimensions().get("freshness"), 0.001);
        assertEquals(0.75, body.dimensions().get("ruleHit"), 0.001);
        assertNotNull(body.contextSummary());
        assertNotNull(body.evaluatedAt());
    }

    @Test
    void evaluateReturns404ForNonexistentCase() {
        UUID caseId = UUID.randomUUID();
        when(caseRepo.findById(caseId)).thenReturn(Optional.empty());

        ResponseEntity<EvaluationController.EvaluationResponse> response = controller.evaluate(caseId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void evaluateBuildsContextFromRepositories() {
        UUID caseId = UUID.randomUUID();
        var operatingCase = new OperatingCase(caseId, CaseType.CONTINUOUS_ENGAGEMENT, CaseStatus.OPEN, "test-purpose", Instant.now(), null, Instant.now(), "test-user");
        when(caseRepo.findById(caseId)).thenReturn(Optional.of(operatingCase));
        when(claimRepo.findByCaseId(caseId)).thenReturn(List.of());
        when(interactionRepo.findByCaseId(caseId)).thenReturn(List.of());
        when(ruleRepo.findAll()).thenReturn(List.of());

        var result = new EvaluationResult(0.5, Map.of("evidence", 0.5, "freshness", 0.5, "ruleHit", 0.5));
        when(evaluator.score(any(EvaluationContext.class))).thenReturn(result);

        controller.evaluate(caseId);

        // Verify context was built correctly with empty data
        Mockito.verify(evaluator).score(any(EvaluationContext.class));
        Mockito.verify(claimRepo).findByCaseId(caseId);
        Mockito.verify(interactionRepo).findByCaseId(caseId);
        Mockito.verify(ruleRepo).findAll();
    }
}
