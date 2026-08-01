package com.gien.gits.action;

import com.gien.gits.ontology.ActionReceipt;
import com.gien.gits.ontology.ControlledAction;
import java.util.Objects;

public final class ControlledActionService {
    private final ActionDispatchPort dispatchPort;

    public ControlledActionService(ActionDispatchPort dispatchPort) {
        this.dispatchPort = Objects.requireNonNull(dispatchPort, "dispatchPort");
    }

    public ActionReceipt dispatch(ControlledAction action) {
        Objects.requireNonNull(action, "action");
        if (!action.confirmation().authorizesAction()) {
            throw new IllegalArgumentException("action requires an approving human confirmation");
        }
        return dispatchPort.dispatch(action);
    }
}
