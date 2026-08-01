package com.gien.gits.action;

import com.gien.gits.ontology.ActionReceipt;
import com.gien.gits.ontology.ControlledAction;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Default fail-closed {@link ActionDispatchPort} for the engineering candidate.
 *
 * <p>This dispatcher only <em>records</em> the controlled action; it performs no real
 * external side effect on any target system. Each call produces an {@link ActionReceipt}
 * with a freshly generated receipt id and a {@link ActionReceipt.Status#SUCCEEDED}
 * status, echoing the action's {@code expectedVersion} as the post-dispatch version
 * (no real mutation occurs).
 */
public final class RecordingActionDispatcher implements ActionDispatchPort {

    private final List<ControlledAction> dispatched = Collections.synchronizedList(new ArrayList<>());
    private final List<ActionReceipt> receipts = Collections.synchronizedList(new ArrayList<>());

    @Override
    public ActionReceipt dispatch(ControlledAction action) {
        Objects.requireNonNull(action, "action");
        ActionReceipt receipt = new ActionReceipt(
                UUID.randomUUID(),
                action.actionId(),
                ActionReceipt.Status.SUCCEEDED,
                action.target().expectedVersion(),
                null,
                Instant.now());
        dispatched.add(action);
        receipts.add(receipt);
        return receipt;
    }

    public List<ControlledAction> dispatchedActions() {
        return List.copyOf(dispatched);
    }

    public List<ActionReceipt> receipts() {
        return List.copyOf(receipts);
    }
}
