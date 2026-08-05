package com.gien.gits.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ContextAssemblyRequestTest {

    @Test
    void requestHoldsAllFields() {
        UUID caseId = UUID.randomUUID();
        Instant asOf = Instant.parse("2026-08-02T00:00:00Z");
        Map<String, Object> permissionContext = Map.of("decisionId", "DEC-123");

        ContextAssemblyPort.Request request = new ContextAssemblyPort.Request(
                caseId, "claim-reconciliation", "token-ref", permissionContext, asOf);

        assertEquals(caseId, request.caseId());
        assertEquals("claim-reconciliation", request.purpose());
        assertEquals("token-ref", request.identityTokenRef());
        assertEquals(permissionContext, request.permissionContext());
        assertEquals(asOf, request.asOf());
    }

    @Test
    void requestWithNullOptionals() {
        ContextAssemblyPort.Request request = new ContextAssemblyPort.Request(
                UUID.randomUUID(), "purpose", null, null, null);

        assertNotNull(request.caseId());
        assertEquals(null, request.identityTokenRef());
        assertEquals(null, request.permissionContext());
        assertEquals(null, request.asOf());
    }
}
