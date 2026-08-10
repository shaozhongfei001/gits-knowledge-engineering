package com.gien.gits.worker.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.gien.gits.ontology.Claim;
import com.gien.gits.ontology.ClaimStatus;
import com.gien.gits.ontology.ClaimType;
import com.gien.gits.ontology.event.CloudEvent;
import com.gien.gits.ontology.event.DomainEventType;
import com.gien.gits.ontology.port.ClaimReconciliationPort;
import com.gien.gits.ontology.port.ClaimReconciliationPort.ReconciliationResult;
import com.gien.gits.ontology.port.ClaimReconciliationPort.ReconciliationStatus;
import com.gien.gits.ontology.port.DomainEventPublisher;
import com.gien.gits.ontology.port.WritableClaimRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

/**
 * Tests for ClaimCandidateRecordedHandler business logic.
 *
 * <p>Verifies:
 * <ul>
 *   <li>Event data extraction → Claim construction and persistence</li>
 *   <li>ClaimReconciliationPort.reconcile() called with correct parameters</li>
 *   <li>VERIFIED_FACT result → claim status updated to VERIFIED_FACT</li>
 *   <li>CONFLICT result → claim status updated to CONFLICT</li>
 *   <li>CANDIDATE_CLAIM result → claim status stays CANDIDATE</li>
 *   <li>Domain event published after reconciliation</li>
 *   <li>Empty event data → handler skips gracefully</li>
 * </ul>
 *
 * <p>Rule #6: Claim≠Fact — a claim only becomes a fact after reconciliation confirms it.
 */
@ExtendWith(MockitoExtension.class)
class ClaimCandidateRecordedHandlerTest {

    @Mock
    private WritableClaimRepository claimRepository;

    @Mock
    private ClaimReconciliationPort claimReconciliationPort;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @Captor
    private ArgumentCaptor<Claim> claimCaptor;

    @Captor
    private ArgumentCaptor<CloudEvent> eventCaptor;

    private ClaimCandidateRecordedHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ClaimCandidateRecordedHandler(
                claimRepository, claimReconciliationPort, domainEventPublisher);
    }

    private CloudEvent sampleClaimEvent(Map<String, Object> data) {
        return CloudEvent.builder()
                .id("evt-claim-001")
                .source("/gits/kno/test")
                .type(DomainEventType.CLAIM_CANDIDATE_RECORDED)
                .time(Instant.now().toString())
                .subject("case:" + UUID.randomUUID())
                .data(data)
                .build();
    }

    @Test
    void persistsClaim_fromEventData() {
        UUID claimId = UUID.randomUUID();
        UUID caseId = UUID.randomUUID();

        Map<String, Object> data = new HashMap<>();
        data.put("claimId", claimId.toString());
        data.put("caseId", caseId.toString());
        data.put("claimType", "CUSTOMER_STATEMENT");
        data.put("statement", "客户表示年营收约3000万元");

        when(claimReconciliationPort.reconcile(false, false, false))
                .thenReturn(new ReconciliationResult(ReconciliationStatus.CANDIDATE_CLAIM, "Rule-3"));

        handler.handle(sampleClaimEvent(data));

        verify(claimRepository).save(claimCaptor.capture());
        Claim saved = claimCaptor.getValue();
        assertThat(saved.claimId()).isEqualTo(claimId);
        assertThat(saved.caseId()).isEqualTo(caseId);
        assertThat(saved.claimType()).isEqualTo(ClaimType.CUSTOMER_STATEMENT);
        assertThat(saved.statement()).isEqualTo("客户表示年营收约3000万元");
        assertThat(saved.status()).isEqualTo(ClaimStatus.CANDIDATE);
    }

    @Test
    void updatesToVerifiedFact_whenReconciliationConfirms() {
        when(claimReconciliationPort.reconcile(false, true, true))
                .thenReturn(new ReconciliationResult(ReconciliationStatus.VERIFIED_FACT, "Rule-2"));

        Map<String, Object> data = new HashMap<>();
        data.put("claimId", UUID.randomUUID().toString());
        data.put("caseId", UUID.randomUUID().toString());
        data.put("claimType", "SYSTEM_FACT");
        data.put("statement", "工商登记信息确认");
        data.put("conflictDetected", false);
        data.put("authoritativeMatch", true);
        data.put("evidenceComplete", true);

        handler.handle(sampleClaimEvent(data));

        ArgumentCaptor<ClaimStatus> statusCaptor = ArgumentCaptor.forClass(ClaimStatus.class);
        verify(claimRepository).updateStatus(any(UUID.class), statusCaptor.capture());
        assertThat(statusCaptor.getValue()).isEqualTo(ClaimStatus.VERIFIED_FACT);
    }

    @Test
    void updatesToConflict_whenConflictDetected() {
        when(claimReconciliationPort.reconcile(true, false, false))
                .thenReturn(new ReconciliationResult(
                        ReconciliationStatus.CONFLICT_REQUIRES_HUMAN_REVIEW, "Rule-1"));

        Map<String, Object> data = new HashMap<>();
        data.put("claimId", UUID.randomUUID().toString());
        data.put("caseId", UUID.randomUUID().toString());
        data.put("claimType", "CUSTOMER_STATEMENT");
        data.put("statement", "矛盾陈述");
        data.put("conflictDetected", true);
        data.put("authoritativeMatch", false);
        data.put("evidenceComplete", false);

        handler.handle(sampleClaimEvent(data));

        ArgumentCaptor<ClaimStatus> statusCaptor = ArgumentCaptor.forClass(ClaimStatus.class);
        verify(claimRepository).updateStatus(any(UUID.class), statusCaptor.capture());
        assertThat(statusCaptor.getValue()).isEqualTo(ClaimStatus.CONFLICT);
    }

    @Test
    void staysCandidate_whenReconciliationReturnsCandidate() {
        when(claimReconciliationPort.reconcile(false, false, false))
                .thenReturn(new ReconciliationResult(ReconciliationStatus.CANDIDATE_CLAIM, "Rule-3"));

        Map<String, Object> data = new HashMap<>();
        data.put("claimId", UUID.randomUUID().toString());
        data.put("caseId", UUID.randomUUID().toString());
        data.put("claimType", "OPPORTUNITY");
        data.put("statement", "潜在融资需求");
        data.put("conflictDetected", false);
        data.put("authoritativeMatch", false);
        data.put("evidenceComplete", false);

        handler.handle(sampleClaimEvent(data));

        verify(claimRepository, never()).updateStatus(any(), any());
    }

    @Test
    void publishesControlledActionRequest_whenConflictDetected() {
        when(claimReconciliationPort.reconcile(true, false, false))
                .thenReturn(new ReconciliationResult(
                        ReconciliationStatus.CONFLICT_REQUIRES_HUMAN_REVIEW, "Rule-1"));

        Map<String, Object> data = new HashMap<>();
        data.put("claimId", UUID.randomUUID().toString());
        data.put("caseId", UUID.randomUUID().toString());
        data.put("claimType", "CUSTOMER_STATEMENT");
        data.put("statement", "矛盾陈述");
        data.put("conflictDetected", true);
        data.put("authoritativeMatch", false);
        data.put("evidenceComplete", false);

        handler.handle(sampleClaimEvent(data));

        verify(domainEventPublisher).publish(eventCaptor.capture());
        CloudEvent published = eventCaptor.getValue();
        assertThat(published.type()).isEqualTo(DomainEventType.CONTROLLED_ACTION_REQUESTED);
        assertThat(published.data()).containsEntry("targetObjectId", data.get("claimId"));
    }

    @Test
    void noEventPublished_whenReconciliationVerified() {
        when(claimReconciliationPort.reconcile(false, true, true))
                .thenReturn(new ReconciliationResult(ReconciliationStatus.VERIFIED_FACT, "Rule-2"));

        Map<String, Object> data = new HashMap<>();
        data.put("claimId", UUID.randomUUID().toString());
        data.put("caseId", UUID.randomUUID().toString());
        data.put("claimType", "SYSTEM_FACT");
        data.put("statement", "已验证事实");
        data.put("conflictDetected", false);
        data.put("authoritativeMatch", true);
        data.put("evidenceComplete", true);

        handler.handle(sampleClaimEvent(data));

        verify(domainEventPublisher, never()).publish(any());
    }

    @Test
    void noEventPublished_whenReconciliationCandidate() {
        when(claimReconciliationPort.reconcile(false, false, false))
                .thenReturn(new ReconciliationResult(ReconciliationStatus.CANDIDATE_CLAIM, "Rule-3"));

        Map<String, Object> data = new HashMap<>();
        data.put("claimId", UUID.randomUUID().toString());
        data.put("caseId", UUID.randomUUID().toString());
        data.put("claimType", "OPPORTUNITY");
        data.put("statement", "潜在需求");
        data.put("conflictDetected", false);
        data.put("authoritativeMatch", false);
        data.put("evidenceComplete", false);

        handler.handle(sampleClaimEvent(data));

        verify(domainEventPublisher, never()).publish(any());
    }

    @Test
    void skipsGracefully_onEmptyEventData() {
        CloudEvent emptyDataEvent = CloudEvent.builder()
                .id("evt-empty")
                .source("/gits/kno/test")
                .type(DomainEventType.CLAIM_CANDIDATE_RECORDED)
                .time(Instant.now().toString())
                .subject("test")
                .data(Map.of())
                .build();

        assertThatNoException().isThrownBy(() -> handler.handle(emptyDataEvent));
        verify(claimRepository, never()).save(any());
        verify(claimReconciliationPort, never()).reconcile(any(Boolean.class), any(Boolean.class), any(Boolean.class));
    }

    @Test
    void defaultsToCustomerStatement_forUnknownClaimType() {
        when(claimReconciliationPort.reconcile(false, false, false))
                .thenReturn(new ReconciliationResult(ReconciliationStatus.CANDIDATE_CLAIM, "Rule-3"));

        Map<String, Object> data = new HashMap<>();
        data.put("claimId", UUID.randomUUID().toString());
        data.put("caseId", UUID.randomUUID().toString());
        data.put("claimType", "NONEXISTENT_TYPE");
        data.put("statement", "test");

        handler.handle(sampleClaimEvent(data));

        verify(claimRepository).save(claimCaptor.capture());
        assertThat(claimCaptor.getValue().claimType()).isEqualTo(ClaimType.CUSTOMER_STATEMENT);
    }
}
