package com.gien.gits.ontology.port;

import com.gien.gits.ontology.PolicyRule;

/**
 * 可写策略规则仓储端口 — 在 {@link PolicyRuleRepository} 只读契约基础上增加写操作。
 *
 * <p>适配器层的 JDBC 实现类应实现此接口，从而同时满足读/写契约。</p>
 */
public interface WritablePolicyRuleRepository extends PolicyRuleRepository {

    /**
     * 保存策略规则聚合。
     *
     * @param policyRule 待保存的策略规则
     */
    void save(PolicyRule policyRule);
}
