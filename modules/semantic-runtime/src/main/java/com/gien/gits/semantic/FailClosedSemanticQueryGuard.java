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

    /**
     * 执行语义查询（严格 fail-closed：仅在请求携带已登记查询 ID 时才放行）。
     *
     * <p>校验顺序（任一不满足立即拒绝，绝不落到实际查询执行）：
     * <ol>
     *   <li>请求为 null → 拒绝；</li>
     *   <li>请求携带原始 SPARQL（rawQuery 非空）→ 拒绝（SEC-005：只允许注册 ID）；</li>
     *   <li>queryId 缺失 → 拒绝；</li>
     *   <li>queryId 未在登记目录中 → 拒绝；</li>
     *   <li>仅当 ID 已登记时才触达实际执行层（shadow 阶段返回确定性占位计数）。</li>
     * </ol>
     * 该守卫不连接任何生产/语义端点，不产生业务副作用。</p>
     */
    @Override
    public SemanticQueryResult execute(SemanticQueryRequest request) {
        // ── 1. 空请求拒绝 ─────────────────────────────────────────────────────
        if (request == null) {
            return denied("request is null (fail-closed)");
        }
        // ── 2. 原始 SPARQL 拒绝（SEC-005 核心护栏）────────────────────────────
        // 调用方若尝试传入任意 SPARQL（rawQuery 非空），一律拒绝，禁止执行任意查询。
        if (request.rawQuery() != null && !request.rawQuery().isBlank()) {
            return denied("raw SPARQL execution is not allowed; only registered semantic query IDs may execute");
        }
        // ── 3. 查询 ID 缺失拒绝 ───────────────────────────────────────────────
        if (request.queryId() == null) {
            return denied("semantic query id is missing (fail-closed)");
        }
        // ── 4. 未登记 ID 拒绝 ─────────────────────────────────────────────────
        // 仅允许在 RegisteredSemanticQueryCatalog 中登记的 ID 执行；未知 ID 一律拒绝。
        if (!catalog.isRegistered(request.queryId())) {
            return denied("semantic query id '" + request.queryId() + "' is not registered (fail-closed)");
        }
        // ── 5. 放行：已登记 ID 才触达实际执行层 ───────────────────────────────
        // shadow 阶段不连接真实端点，返回基于 ID 长度的确定性占位计数（可重放、无副作用）。
        long rowCount = estimateRowCount(request.queryId());
        return new SemanticQueryResult.QueryResult(request.queryId(), rowCount);
    }

    /** 构造统一的拒绝结果（统一决策码 DENY_ONLY_REGISTERED_QUERY_ID + 原因）。 */
    private SemanticQueryResult.Denied denied(String reason) {
        return new SemanticQueryResult.Denied(DENY_CODE, reason);
    }

    /**
     * shadow 阶段：对已注册查询返回确定性占位计数。
     *
     * <p>以查询 ID 长度作为占位行数，保证相同 ID 结果确定、可重放，且不连接真实端点。</p>
     */
    private long estimateRowCount(SemanticQueryId queryId) {
        return (long) queryId.value().length();
    }
}
