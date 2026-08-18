package com.gien.gits.adapter.persistence.foundation.ontology.service;

import com.gien.gits.adapter.persistence.foundation.ontology.mapper.PolicyRuleMapper;
import com.gien.gits.ontology.PolicyRule;
import com.gien.gits.ontology.port.WritablePolicyRuleRepository;

import java.util.List;
import java.util.Optional;

/**
 * MyBatis 政策规则仓储实现 — foundation/ontology 层
 */
public class MyBatisPolicyRuleService implements WritablePolicyRuleRepository {

    private final PolicyRuleMapper mapper;

    public MyBatisPolicyRuleService(PolicyRuleMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(PolicyRule policyRule) {
        mapper.insert(policyRule);
    }

    @Override
    public Optional<PolicyRule> findByRuleId(String ruleId) {
        return mapper.findByRuleId(ruleId);
    }

    @Override
    public List<PolicyRule> findBySeverity(PolicyRule.Severity severity) {
        return mapper.findBySeverity(severity.name());
    }

    @Override
    public List<PolicyRule> findBySeverity(String severity) {
        return mapper.findBySeverity(severity);
    }

    @Override
    public List<PolicyRule> findAll() {
        return mapper.findAll();
    }
}
