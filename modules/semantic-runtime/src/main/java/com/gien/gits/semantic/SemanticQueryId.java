package com.gien.gits.semantic;

/**
 * 已注册的语义查询 ID（例如 SQ-CUSTOMER-RELATIONSHIP）。
 *
 * <p>仅允许在 P20 合同集中登记的语义查询 ID 执行；任意 SPARQL 或未知 ID 一律拒绝（SEC-005）。
 * 该值类型在构造时校验命名规则，禁止出现可被当作原始查询的输入。</p>
 */
public record SemanticQueryId(String value) {

    public SemanticQueryId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("semantic query id is required");
        }
        if (!value.matches("SQ-[A-Z0-9-]+")) {
            throw new IllegalArgumentException("invalid semantic query id: " + value);
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
