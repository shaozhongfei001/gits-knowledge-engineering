package com.gien.gits.context;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public interface ContextAssemblyPort {
    EvidenceBundle assemble(Request request);

    record Request(UUID caseId, String purpose, String identityTokenRef, Map<String, Object> permissionContext, Instant asOf) {}
}
