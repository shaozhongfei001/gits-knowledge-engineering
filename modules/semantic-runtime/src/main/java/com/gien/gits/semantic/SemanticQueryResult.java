package com.gien.gits.semantic;

/**
 * 语义查询结果（sealed：QueryResult 或 Denied）。
 *
 * <p>Denied 携带决策码与原因；fail-closed 下任意未注册/原始 SPARQL 一律返回
 * DENY_ONLY_REGISTERED_QUERY_ID，绝不返回部分数据。</p>
 */
public sealed interface SemanticQueryResult permits SemanticQueryResult.QueryResult, SemanticQueryResult.Denied {

    record QueryResult(SemanticQueryId queryId, long rowCount) implements SemanticQueryResult {}

    record Denied(String decisionCode, String reason) implements SemanticQueryResult {}

    default boolean isAllowed() {
        return this instanceof QueryResult;
    }
}
