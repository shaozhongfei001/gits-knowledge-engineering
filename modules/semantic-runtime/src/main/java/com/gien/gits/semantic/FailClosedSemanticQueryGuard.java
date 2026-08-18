package com.gien.gits.semantic;

import java.util.Objects;

/**
 * Fail-closed 语义查询守卫（SEC-005）。
 *
 * <p>仅允许 {@link RegisteredSemanticQueryCatalog} 中登记的语义查询 ID 执行；对以下输入一律
 * 返回 DENY_ONLY_REGISTERED_QUERY_ID，绝不落到实际查询执行：
 * <ul>
 *   <li>请求携带原始 SPARQL（rawQuery 非空）；</li>
 *   <li>queryId 为 null 或未登记；</li>
 *   <li>queryId 构造失败（非法命名，被当作原始查询）。</li>
 * </ul>
 * 该守卫不连接任何生产/语义端点，不产生业务副作用。</p>
 */
public final class FailClosedSemanticQueryGuard implements SemanticQueryPort {

    public static final String DENY_CODE = "DENY_ONLY_REGISTERED_QUERY_ID";

    private final RegisteredSemanticQueryCatalog catalog;

    public FailClosedSemanticQueryGuard(RegisteredSemanticQueryCatalog catalog) {
        this.catalog = Objects.requireNonNull(catalog, "catalog");
    }

    @Override
    public SemanticQueryResult execute(SemanticQueryRequest request) {
        if (request == null) {
            return denied("request is null (fail-closed)");
        }
        if (request.rawQuery() != null && !request.rawQuery().isBlank()) {
            return denied("raw SPARQL execution is not allowed; only registered semantic query IDs may execute");
        }
        if (request.queryId() == null) {
            return denied("semantic query id is missing (fail-closed)");
        }
        if (!catalog.isRegistered(request.queryId())) {
            return denied("semantic query id '" + request.queryId() + "' is not registered (fail-closed)");
        }
        // 仅当为已注册 ID 时才触达实际执行层（此处 shadow 阶段返回确定性计数，不连接端点）。
        long rowCount = estimateRowCount(request.queryId());
        return new SemanticQueryResult.QueryResult(request.queryId(), rowCount);
    }

    private SemanticQueryResult.Denied denied(String reason) {
        return new SemanticQueryResult.Denied(DENY_CODE, reason);
    }

    /** shadow 阶段：对已注册查询返回确定性占位计数（不连接真实端点）。 */
    private long estimateRowCount(SemanticQueryId queryId) {
        return (long) queryId.value().length();
    }
}
