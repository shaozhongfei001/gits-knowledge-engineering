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
            // activation contracts
            "SQ-CUSTOMER-RELATIONSHIP",
            "SQ-RELATED-LEGAL-ENTITIES",
            "SQ-KYC-GAPS",
            "SQ-OPEN-COMMITMENTS",
            "SQ-ACTIVE-PRODUCT-VERSIONS",
            "SQ-CLAIM-SUBJECT-RELATIONS",
            "SQ-CREDIT-AND-PROJECT-AMOUNTS",
            "SQ-PROJECT-AND-BORROWER-ENTITY",
            // skills
            "SQ-CUSTOMER-NEED-AND-PROJECT");

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
