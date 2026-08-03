package com.gien.gits.customerjourney;

import com.gien.gits.ontology.CaseStatus;
import com.gien.gits.ontology.OperatingCase;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class OperatingCaseStateMachine {

    private static final Map<CaseStatus, Set<CaseStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(CaseStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(CaseStatus.OPEN, EnumSet.of(CaseStatus.IN_PROGRESS, CaseStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(CaseStatus.IN_PROGRESS, EnumSet.of(CaseStatus.IN_PROGRESS, CaseStatus.WAITING_FOR_HUMAN, CaseStatus.CLOSED, CaseStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(CaseStatus.WAITING_FOR_HUMAN, EnumSet.of(CaseStatus.IN_PROGRESS, CaseStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(CaseStatus.CLOSED, EnumSet.noneOf(CaseStatus.class));
        ALLOWED_TRANSITIONS.put(CaseStatus.CANCELLED, EnumSet.noneOf(CaseStatus.class));
    }

    private OperatingCaseStateMachine() {
    }

    public static OperatingCase transition(OperatingCase current, CaseStatus target) {
        if (!validateTransition(current.status(), target)) {
            throw new IllegalStateException(
                    "Invalid transition from " + current.status() + " to " + target);
        }
        return new OperatingCase(
                current.caseId(),
                current.caseType(),
                target,
                current.purpose(),
                current.validFrom(),
                current.validTo(),
                current.recordedAt(),
                current.createdBy());
    }

    public static boolean validateTransition(CaseStatus from, CaseStatus to) {
        Set<CaseStatus> allowed = ALLOWED_TRANSITIONS.get(from);
        return allowed != null && allowed.contains(to);
    }
}