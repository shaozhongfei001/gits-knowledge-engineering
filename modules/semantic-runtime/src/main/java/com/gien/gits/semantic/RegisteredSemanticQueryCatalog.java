package com.gien.gits.semantic;

import java.util.Set;

/**
 * 已注册语义查询目录（P20 合同集登记）。
 *
 * <p>仅允许这些已登记的 ID 执行；未在集合中的任何查询 ID 一律 fail-closed 拒绝。
 * 注册集来自 P20 激活合同与技能声明的 semanticQueries/semanticDependencies 之并集。</p>
 */
public final class RegisteredSemanticQueryCatalog {

    /** P20 合同集登记的语义查询 ID（激活合同 semanticQueries ∪ 技能 semanticDependencies）。 */
    private static final Set<String> REGISTERED = Set.of(
            // ── 来自激活合同的 semanticQueries 字段声明的查询 ID ──
            "SQ-CUSTOMER-RELATIONSHIP",      // 客户关系网络查询
            "SQ-RELATED-LEGAL-ENTITIES",     // 关联法人查询
            "SQ-KYC-GAPS",                   // 客户 KYC 缺口查询
            "SQ-OPEN-COMMITMENTS",           // 未结承诺查询
            "SQ-ACTIVE-PRODUCT-VERSIONS",    // 活跃产品版本查询
            "SQ-CLAIM-SUBJECT-RELATIONS",    // 主张主体关系查询
            "SQ-CREDIT-AND-PROJECT-AMOUNTS", // 授信与项目金额查询
            "SQ-PROJECT-AND-BORROWER-ENTITY",// 项目与借款人实体查询
            // ── 来自技能 semanticDependencies 字段声明的查询 ID ──
            "SQ-CUSTOMER-NEED-AND-PROJECT"); // 客户需求与项目匹配查询

    private final Set<String> registered;

    public RegisteredSemanticQueryCatalog() {
        this.registered = REGISTERED;
    }

    /** 显式注入注册集（测试或扩展用）。 */
    public RegisteredSemanticQueryCatalog(Set<String> registered) {
        this.registered = Set.copyOf(registered);
    }

    public boolean isRegistered(SemanticQueryId queryId) {
        return queryId != null && registered.contains(queryId.value());
    }

    public Set<String> registeredIds() {
        return Set.copyOf(registered);
    }
}
