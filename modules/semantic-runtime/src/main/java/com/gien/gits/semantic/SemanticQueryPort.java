package com.gien.gits.semantic;

/**
 * 语义查询执行 Port（P20 不变量：只允许注册 Semantic Query ID）。
 *
 * <p>守卫层在真正执行查询前校验：仅已注册 ID 可执行；任意 SPARQL / 未注册 / 空白输入一律
 * fail-closed 返回 {@link SemanticQueryResult.Denied}（decisionCode=DENY_ONLY_REGISTERED_QUERY_ID）。</p>
 */
public interface SemanticQueryPort {

    SemanticQueryResult execute(SemanticQueryRequest request);
}
