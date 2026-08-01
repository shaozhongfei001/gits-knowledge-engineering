package com.gientech.hzb.kno.action;

import com.gientech.hzb.kno.ontology.ActionReceipt;
import com.gientech.hzb.kno.ontology.ControlledAction;

public interface ActionDispatchPort {
    ActionReceipt dispatch(ControlledAction action);
}
