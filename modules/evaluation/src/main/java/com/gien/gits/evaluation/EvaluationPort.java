package com.gien.gits.evaluation;

import java.util.Map;

public interface EvaluationPort {
    Result evaluate(RunManifest manifest, String caseSetVersion);

    record Result(String gateState, Map<String, Number> metrics) {}
}
