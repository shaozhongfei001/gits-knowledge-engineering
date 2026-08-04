package com.gien.gits.ontology.port;

import com.gien.gits.ontology.PolicyRule;

import java.util.List;
import java.util.Optional;

/**
 * 策略规则仓储端口 — 只读操作。
 *
 * <p>定义对 {@link PolicyRule} 聚合的查询契约，由适配器层实现。
 * 写操作见 {@link WritablePolicyRuleRepository}。</p>
 */
public interface PolicyRuleRepository {

    /**
     * 根据规则ID查找策略规则。
     *
     * @param ruleId 规则唯一标识
     * @return 找到的策略规则，若不存在则返回空
     */
    Optional<PolicyRule> findByRuleId(String ruleId);

    /**
     * 根据严重程度查找策略规则（枚举版）。
     *
     * @param severity 严重程度
     * @return 符合条件的策略规则列表
     */
    List<PolicyRule> findBySeverity(PolicyRule.Severity severity);

    /**
     * 根据严重程度查找策略规则（字符串版）。
     *
     * @param severity 严重程度字符串
     * @return 符合条件的策略规则列表
     */
    List<PolicyRule> findBySeverity(String severity);

    /**
     * 查找所有策略规则。
     *
     * @return 所有策略规则列表
     */
    List<PolicyRule> findAll();
}
