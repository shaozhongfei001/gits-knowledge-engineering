package com.gien.gits.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.gien.gits.ontology.ActionReceipt;
import com.gien.gits.ontology.ControlledAction;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ActionReceiptTest {

    @Test
    void succeededReceiptConstruction() {
        UUID receiptId = UUID.randomUUID();
        UUID actionId = UUID.randomUUID();
        Instant receivedAt = Instant.now();

        ActionReceipt receipt = new ActionReceipt(receiptId, actionId, ActionReceipt.Status.SUCCEEDED,
                "v7", null, receivedAt);

        assertEquals(receiptId, receipt.receiptId());
        assertEquals(actionId, receipt.actionId());
        assertEquals(ActionReceipt.Status.SUCCEEDED, receipt.status());
        assertEquals("v7", receipt.targetVersionAfter());
        assertNotNull(receipt.receivedAt());
    }

    @Test
    void failedReceiptConstruction() {
        UUID receiptId = UUID.randomUUID();
        UUID actionId = UUID.randomUUID();
        Instant receivedAt = Instant.now();

        ActionReceipt receipt = new ActionReceipt(receiptId, actionId, ActionReceipt.Status.FAILED,
                null, "CONNECTION_REFUSED", receivedAt);

        assertEquals(ActionReceipt.Status.FAILED, receipt.status());
        assertEquals("CONNECTION_REFUSED", receipt.failureCode());
    }

    @Test
    void receiptStatusValues() {
        assertEquals(3, ActionReceipt.Status.values().length);
        assertEquals(ActionReceipt.Status.SUCCEEDED, ActionReceipt.Status.valueOf("SUCCEEDED"));
        assertEquals(ActionReceipt.Status.FAILED, ActionReceipt.Status.valueOf("FAILED"));
        assertEquals(ActionReceipt.Status.COMPENSATED, ActionReceipt.Status.valueOf("COMPENSATED"));
    }

    @Test
    void succeededReceiptRequiresTargetVersionAfter() {
        assertThrows(IllegalArgumentException.class, () ->
                new ActionReceipt(UUID.randomUUID(), UUID.randomUUID(), ActionReceipt.Status.SUCCEEDED,
                        null, null, Instant.now()));
    }

    @Test
    void failedReceiptRequiresFailureCode() {
        assertThrows(IllegalArgumentException.class, () ->
                new ActionReceipt(UUID.randomUUID(), UUID.randomUUID(), ActionReceipt.Status.FAILED,
                        null, null, Instant.now()));
    }

    @Test
    void controlledActionTargetConstruction() {
        ControlledAction.Target target = new ControlledAction.Target(
                "CRM", "TASK", "T-1", "v7",
                ControlledAction.Target.Operation.CREATE_TASK,
                Map.of("title", "follow-up"));

        assertEquals("CRM", target.system());
        assertEquals("TASK", target.objectType());
        assertEquals("T-1", target.objectId());
        assertEquals("v7", target.expectedVersion());
        assertEquals(ControlledAction.Target.Operation.CREATE_TASK, target.operation());
        assertEquals("follow-up", target.payload().get("title"));
    }

    @Test
    void controlledActionTargetOperationValues() {
        assertEquals(2, ControlledAction.Target.Operation.values().length);
    }
}
