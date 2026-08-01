package com.gien.gits.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gien.gits.ontology.ActionReceipt;
import com.gien.gits.ontology.ControlledAction;
import com.gien.gits.ontology.HumanConfirmation;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ControlledActionServiceTest {

    private static ControlledAction authorizedAction() {
        HumanConfirmation confirmation = new HumanConfirmation(UUID.randomUUID(), UUID.randomUUID(),
                HumanConfirmation.Decision.APPROVED, "reviewer-01", Instant.now());
        ControlledAction.Target target = new ControlledAction.Target("CRM", "TASK", "T-1", "v7",
                ControlledAction.Target.Operation.CREATE_TASK, Map.of("title", "follow-up"));
        return new ControlledAction(UUID.randomUUID(), UUID.randomUUID(), confirmation, target,
                "1234567890abcdef", Instant.now(), ControlledAction.Status.REQUESTED);
    }

    @Test
    void dispatchAuthorizedActionReturnsReceipt() {
        RecordingActionDispatcher dispatcher = new RecordingActionDispatcher();
        ControlledActionService service = new ControlledActionService(dispatcher);
        ControlledAction action = authorizedAction();

        ActionReceipt receipt = service.dispatch(action);

        assertNotNull(receipt);
        assertNotNull(receipt.receiptId());
        assertEquals(action.actionId(), receipt.actionId());
        assertEquals(ActionReceipt.Status.SUCCEEDED, receipt.status());
        assertEquals("v7", receipt.targetVersionAfter());
        assertEquals(1, dispatcher.dispatchedActions().size());
        assertEquals(1, dispatcher.receipts().size());
    }

    @Test
    void dispatchUnauthorizedConfirmationIsRejected() {
        RecordingActionDispatcher dispatcher = new RecordingActionDispatcher();
        ControlledActionService service = new ControlledActionService(dispatcher);

        HumanConfirmation rejected = new HumanConfirmation(UUID.randomUUID(), UUID.randomUUID(),
                HumanConfirmation.Decision.REJECTED, "reviewer-01", Instant.now());
        ControlledAction.Target target = new ControlledAction.Target("CRM", "TASK", "T-1", "v7",
                ControlledAction.Target.Operation.CREATE_TASK, Map.of("title", "follow-up"));

        // ControlledAction is fail-closed: an unauthorized confirmation cannot even be
        // constructed, so the dispatch flow throws IllegalArgumentException before
        // reaching the port. The service additionally guards the gate defensively.
        assertThrows(IllegalArgumentException.class, () -> {
            ControlledAction action = new ControlledAction(UUID.randomUUID(), UUID.randomUUID(),
                    rejected, target, "1234567890abcdef", Instant.now(), ControlledAction.Status.REQUESTED);
            service.dispatch(action);
        });
        assertEquals(0, dispatcher.dispatchedActions().size());
    }

    @Test
    void recordingDispatcherRejectsNullAction() {
        RecordingActionDispatcher dispatcher = new RecordingActionDispatcher();
        assertThrows(NullPointerException.class, () -> dispatcher.dispatch(null));
    }

    @Test
    void receiptRecordsActionIdentityAndGeneratedId() {
        RecordingActionDispatcher dispatcher = new RecordingActionDispatcher();
        ControlledAction action = authorizedAction();

        ActionReceipt receipt = dispatcher.dispatch(action);

        assertNotNull(receipt.receiptId());
        assertTrue(receipt.receiptId() != action.actionId(),
                "receipt id should be freshly generated and distinct from action id");
        assertEquals(action.actionId(), receipt.actionId());
        assertNotNull(receipt.receivedAt());
    }
}
