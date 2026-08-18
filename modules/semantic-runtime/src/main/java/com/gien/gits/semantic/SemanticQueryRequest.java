package com.gien.gits.semantic;

import java.util.Map;

/**
 * 语义查询请求。仅携带已注册的语义查询 ID；若调用方尝试传入原始 SPARQL，
 * 需使用 {@link #rawQuery()} 显式标注，守卫层将对其 fail-closed 拒绝。
 *
 * @param queryId   已注册语义查询 ID（例如 SQ-CUSTOMER-RELATIONSHIP）
 * @param parameters绑定参数（如 customerId）
 * @param rawQuery  若调用方要求执行原始 SPARQL 则传入；非空即拒绝
 */
public record SemanticQueryRequest(
        SemanticQueryId queryId,
        Map<String, String> parameters,
        String rawQuery) {

    public SemanticQueryRequest {
        parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
    }

    public static SemanticQueryRequest of(SemanticQueryId queryId, Map<String, String> parameters) {
        return new SemanticQueryRequest(queryId, parameters, null);
    }
}
