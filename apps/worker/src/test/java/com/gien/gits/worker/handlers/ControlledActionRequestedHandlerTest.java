package com.gien.gits.worker.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.gien.gits.action.ActionDispatchPort;
import com.gien.gits.action.port.CrmWritebackChannel;
import com.gien.gits.engagement.CrmWritebackCommand;
import com.gien.gits.ontology.ActionReceipt;
import com.gien.gits.ontology.ControlledAction;
import com.gien.gits.ontology.event.CloudEvent;
import com.gien.gits.ontology.event.DomainEventType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for ControlledActionRequestedHandler business logic.
 *
 * <p>Verifies:
 * <ul>
 *   <li>Event data extraction → ControlledAction construction</li>
 *   <li>ActionDispatchPort.dispatch() called with correct action</li>
 *   <li>Successful dispatch → CrmWritebackChannel.send() called with CRM-001 compliant command</li>
 *   <li>Failed dispatch → no CRM writeback, failure logged</li>
 *   <li>Empty event data → handler skips gracefully</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class ControlledActionRequestedHandlerTest {

    @Mock
    private ActionDispatchPort actionDispatchPort;

    @Mock
    private CrmWritebackChannel crmWritebackChannel;

    @Captor
    private ArgumentCaptor<ControlledAction> actionCaptor;

    @Captor
    private ArgumentCaptor<CrmWritebackCommand> commandCaptor;

    private ControlledActionRequestedHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ControlledActionRequestedHandler(actionDispatchPort, crmWritebackChannel);
    }

    private CloudEvent sampleActionEvent(Map<String, Object> data) {
        return CloudEvent.builder()
                .id("evt-action-001")
                .source("/gits/kno/test")
                .type(DomainEventType.CONTROLLED_ACTION_REQUESTED)
                .time(Instant.now().toString())
                .subject("proposal:" + UUID.randomUUID())
                .data(data)
                .build();
    }

    private Map<String, Object> fullActionData() {
        UUID proposalId = UUID.randomUUID();
        UUID confirmationId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();
        Map<String, Object> data = new HashMap<>();
        data.put("proposalId", proposalId.toString());
        data.put("confirmationId", confirmationId.toString());
        data.put("subjectId", subjectId.toString());
        data.put("decision", "APPROVED");
        data.put("actorId", "analyst-001");
        data.put("targetSystem", "CRM");
        data.put("targetObjectType", "INTERACTION");
        data.put("targetObjectId", "CRM-INT-12345");
        data.put("expectedVersion", "v42");
        data.put("operation", "CREATE_TASK");
        data.put("idempotencyKey", "IDEM-ACTION-001-2024");
        return data;
    }

    @Test
    void dispatchesControlledAction_fromEventData() {
        UUID proposalId = UUID.randomUUID();

        Map<String, Object> data = new HashMap<>();
        data.put("proposalId", proposalId.toString());
        data.put("confirmationId", UUID.randomUUID().toString());
        data.put("subjectId", UUID.randomUUID().toString());
        data.put("decision", "APPROVED");
        data.put("actorId", "analyst-001");
        data.put("targetSystem", "CRM");
        data.put("targetObjectId", "CRM-INT-12345");
        data.put("operation", "CREATE_TASK");
        data.put("idempotencyKey", "IDEM-ACTION-001-2024");

        ActionReceipt successReceipt = new ActionReceipt(
                UUID.randomUUID(), UUID.randomUUID(), ActionReceipt.Status.SUCCEEDED,
                "v43", null, Instant.now());
        when(actionDispatchPort.dispatch(any())).thenReturn(successReceipt);
        when(crmWritebackChannel.send(any())).thenReturn(
                CrmWritebackChannel.WritebackResult.success("MSG-001"));

        handler.handle(sampleActionEvent(data));

        verify(actionDispatchPort).dispatch(actionCaptor.capture());
        ControlledAction action = actionCaptor.getValue();
        assertThat(action.proposalId()).isEqualTo(proposalId);
        assertThat(action.confirmation().decision().name()).isEqualTo("APPROVED");
        assertThat(action.confirmation().actorId()).isEqualTo("analyst-001");
        assertThat(action.target().system()).isEqualTo("CRM");
        assertThat(action.target().objectId()).isEqualTo("CRM-INT-12345");
        assertThat(action.target().operation()).isEqualTo(ControlledAction.Target.Operation.CREATE_TASK);
        assertThat(action.idempotencyKey()).isEqualTo("IDEM-ACTION-001-2024");
    }

    @Test
    void sendsCrmWriteback_onSuccessfulDispatch() {
        ActionReceipt successReceipt = new ActionReceipt(
                UUID.randomUUID(), UUID.randomUUID(), ActionReceipt.Status.SUCCEEDED,
                "v43", null, Instant.now());
        when(actionDispatchPort.dispatch(any())).thenReturn(successReceipt);
        when(crmWritebackChannel.send(any())).thenReturn(
                CrmWritebackChannel.WritebackResult.success("MSG-001"));

        Map<String, Object> data = new HashMap<>();
        data.put("proposalId", UUID.randomUUID().toString());
        data.put("targetSystem", "CRM");
        data.put("operation", "CREATE_TASK");

        handler.handle(sampleActionEvent(data));

        verify(crmWritebackChannel).send(commandCaptor.capture());
        CrmWritebackCommand cmd = commandCaptor.getValue();
        assertThat(cmd.requiresHumanConfirm()).isTrue();
        assertThat(cmd.objectType()).isEqualTo(CrmWritebackCommand.ObjectType.INTERACTION);
    }

    @Test
    void noCrmWriteback_onFailedDispatch() {
        ActionReceipt failedReceipt = new ActionReceipt(
                UUID.randomUUID(), UUID.randomUUID(), ActionReceipt.Status.FAILED,
                null, "CRM_CONNECTION_TIMEOUT", Instant.now());
        when(actionDispatchPort.dispatch(any())).thenReturn(failedReceipt);

        Map<String, Object> data = new HashMap<>();
        data.put("proposalId", UUID.randomUUID().toString());
        data.put("targetSystem", "CRM");

        handler.handle(sampleActionEvent(data));

        verify(crmWritebackChannel, never()).send(any());
    }

    @Test
    void skipsGracefully_onEmptyEventData() {
        CloudEvent emptyDataEvent = CloudEvent.builder()
                .id("evt-empty")
                .source("/gits/kno/test")
                .type(DomainEventType.CONTROLLED_ACTION_REQUESTED)
                .time(Instant.now().toString())
                .subject("test")
                .data(Map.of())
                .build();

        assertThatNoException().isThrownBy(() -> handler.handle(emptyDataEvent));
        verify(actionDispatchPort, never()).dispatch(any());
    }

    @Test
    void skipsGracefully_onNullEventData() {
        CloudEvent nullDataEvent = CloudEvent.builder()
                .id("evt-null")
                .source("/gits/kno/test")
                .type(DomainEventType.CONTROLLED_ACTION_REQUESTED)
                .time(Instant.now().toString())
                .subject("test")
                .data(null)
                .build();

        assertThatNoException().isThrownBy(() -> handler.handle(nullDataEvent));
        verify(actionDispatchPort, never()).dispatch(any());
    }

    @Test
    void crmWritebackAlwaysRequiresHumanConfirm_ruleCrm001() {
        ActionReceipt successReceipt = new ActionReceipt(
                UUID.randomUUID(), UUID.randomUUID(), ActionReceipt.Status.SUCCEEDED,
                "v2", null, Instant.now());
        when(actionDispatchPort.dispatch(any())).thenReturn(successReceipt);
        when(crmWritebackChannel.send(any())).thenReturn(
                CrmWritebackChannel.WritebackResult.success("MSG-002"));

        Map<String, Object> data = new HashMap<>();
        data.put("proposalId", UUID.randomUUID().toString());
        data.put("operation", "UPDATE_WHITELISTED_FIELDS");

        handler.handle(sampleActionEvent(data));

        verify(crmWritebackChannel).send(commandCaptor.capture());
        assertThat(commandCaptor.getValue().requiresHumanConfirm()).isTrue();
    }

    @Test
    void mapsUpdateOperation_correctly() {
        ActionReceipt successReceipt = new ActionReceipt(
                UUID.randomUUID(), UUID.randomUUID(), ActionReceipt.Status.SUCCEEDED,
                "v2", null, Instant.now());
        when(actionDispatchPort.dispatch(any())).thenReturn(successReceipt);
        when(crmWritebackChannel.send(any())).thenReturn(
                CrmWritebackChannel.WritebackResult.success("MSG-003"));

        Map<String, Object> data = new HashMap<>();
        data.put("proposalId", UUID.randomUUID().toString());
        data.put("operation", "UPDATE_WHITELISTED_FIELDS");

        handler.handle(sampleActionEvent(data));

        verify(crmWritebackChannel).send(commandCaptor.capture());
        assertThat(commandCaptor.getValue().operation()).isEqualTo(CrmWritebackCommand.Operation.UPDATE);
    }
}
