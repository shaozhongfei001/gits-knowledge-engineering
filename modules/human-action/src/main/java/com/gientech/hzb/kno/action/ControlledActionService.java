package com.gientech.hzb.kno.action;

import com.gientech.hzb.kno.ontology.ActionReceipt;
import com.gientech.hzb.kno.ontology.ControlledAction;
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
