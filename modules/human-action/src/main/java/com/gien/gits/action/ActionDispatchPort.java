package com.gien.gits.action;

import com.gien.gits.ontology.ActionReceipt;
import com.gien.gits.ontology.ControlledAction;

public interface ActionDispatchPort {
    ActionReceipt dispatch(ControlledAction action);
}
