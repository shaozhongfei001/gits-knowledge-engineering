package com.gien.gits.ontology.port;

import com.gien.gits.ontology.GateDecision;
import com.gien.gits.ontology.GateType;
import com.gien.gits.ontology.HumanGate;
import com.gien.gits.ontology.HumanGateStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 人工门禁 Port 接口
 */
public interface HumanGateRepository {
    HumanGate save(HumanGate gate);
    Optional<HumanGate> findById(String gateId);
    List<HumanGate> findByStatus(HumanGateStatus status);
    List<HumanGate> findByGateType(GateType gateType);
    List<HumanGate> findByJourneyId(String journeyId);
    List<HumanGate> findByCustomerId(String customerId);
    List<HumanGate> findAll();
    HumanGate decide(String gateId, GateDecision decision, Map<String, Object> modification,
                     String reason, String actorId);
}
