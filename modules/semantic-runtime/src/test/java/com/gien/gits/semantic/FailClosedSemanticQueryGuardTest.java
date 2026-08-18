package com.gien.gits.semantic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.junit.jupiter.api.Test;

class FailClosedSemanticQueryGuardTest {

    private final FailClosedSemanticQueryGuard guard = new FailClosedSemanticQueryGuard(new RegisteredSemanticQueryCatalog());

    @Test
    void registeredQueryIsAllowed() {
        SemanticQueryResult result = guard.execute(SemanticQueryRequest.of(
                new SemanticQueryId("SQ-CUSTOMER-RELATIONSHIP"),
                Map.of("customerId", "CUST-001")));

        assertTrue(result.isAllowed());
        SemanticQueryResult.QueryResult queryResult = assertInstanceOf(SemanticQueryResult.QueryResult.class, result);
        assertEquals("SQ-CUSTOMER-RELATIONSHIP", queryResult.queryId().value());
    }

    @Test
    void allRegisteredContractQueriesAreAllowed() {
        RegisteredSemanticQueryCatalog catalog = new RegisteredSemanticQueryCatalog();
        for (String id : catalog.registeredIds()) {
            SemanticQueryResult result = guard.execute(
                    SemanticQueryRequest.of(new SemanticQueryId(id), Map.of()));
            assertTrue(result.isAllowed(), "expected registered query to pass: " + id);
        }
    }

    @Test
    void unregisteredQueryIsDeniedFailClosed() {
        SemanticQueryResult result = guard.execute(SemanticQueryRequest.of(
                new SemanticQueryId("SQ-NOT-REGISTERED"),
                Map.of()));

        assertFalse(result.isAllowed());
        assertEquals("DENY_ONLY_REGISTERED_QUERY_ID", ((SemanticQueryResult.Denied) result).decisionCode());
    }

    @Test
    void arbitrarySparqlIsDeniedFailClosed() {
        // 调用方尝试传原始 SPARQL：rawQuery 非空 → 拒绝
        SemanticQueryRequest request = new SemanticQueryRequest(
                new SemanticQueryId("SQ-CUSTOMER-RELATIONSHIP"), Map.of(),
                "SELECT ?s ?p ?o WHERE { ?s ?p ?o }");

        SemanticQueryResult result = guard.execute(request);
        assertFalse(result.isAllowed());
        assertEquals("DENY_ONLY_REGISTERED_QUERY_ID", ((SemanticQueryResult.Denied) result).decisionCode());
    }

    @Test
    void nullQueryIsDeniedFailClosed() {
        SemanticQueryResult result = guard.execute(new SemanticQueryRequest(null, Map.of(), null));
        assertFalse(result.isAllowed());
        assertEquals("DENY_ONLY_REGISTERED_QUERY_ID", ((SemanticQueryResult.Denied) result).decisionCode());
    }

    @Test
    void nullRequestIsDeniedFailClosed() {
        SemanticQueryResult result = guard.execute(null);
        assertFalse(result.isAllowed());
        assertEquals("DENY_ONLY_REGISTERED_QUERY_ID", ((SemanticQueryResult.Denied) result).decisionCode());
    }

    @Test
    void invalidQueryIdNamingIsRejectedAtConstruction() {
        // 非法命名（不是注册 ID 形态，会被当作原始查询）在构造即拒绝
        assertThrows(IllegalArgumentException.class, () -> new SemanticQueryId("SELECT ?s WHERE { }"));
        assertThrows(IllegalArgumentException.class, () -> new SemanticQueryId(""));
        assertThrows(IllegalArgumentException.class, () -> new SemanticQueryId(null));
    }
}
