package com.gien.gits.ontology;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OperationalOntologyTest {

    @Test
    void candidateClaimIsNotAuthoritative() {
        Claim claim = new Claim(UUID.randomUUID(), UUID.randomUUID(), ClaimType.CUSTOMER_STATEMENT, ClaimStatus.CANDIDATE,
                "客户计划扩大结算合作", null, null, Instant.now(), null);
        assertFalse(claim.isAuthoritative());
    }

    @Test
    void verifiedFactIsExplicit() {
        Claim claim = new Claim(UUID.randomUUID(), UUID.randomUUID(), ClaimType.SYSTEM_FACT, ClaimStatus.VERIFIED_FACT,
                "权威系统事实", Instant.now(), null, Instant.now(), null);
        assertTrue(claim.isAuthoritative());
    }

    @Test
    void rejectedHumanDecisionCannotAuthorizeAction() {
        HumanConfirmation rejected = new HumanConfirmation(UUID.randomUUID(), UUID.randomUUID(),
                HumanConfirmation.Decision.REJECTED, "reviewer-01", Instant.now());
        var target = new ControlledAction.Target("CRM", "TASK", "T-1", "v7",
                ControlledAction.Target.Operation.CREATE_TASK, Map.of("title", "follow-up"));
        assertThrows(IllegalArgumentException.class, () -> new ControlledAction(
                UUID.randomUUID(), UUID.randomUUID(), rejected, target, "1234567890abcdef", Instant.now(), ControlledAction.Status.REQUESTED));
    }
}
