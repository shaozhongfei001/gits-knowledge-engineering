package com.gien.gits.adapter.persistence.foundation.ontology.mapper;

import com.gien.gits.ontology.PolicyRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 政策规则 Mapper — foundation/ontology 层
 */
@Mapper
public interface PolicyRuleMapper {

    void insert(PolicyRule policyRule);

    Optional<PolicyRule> findByRuleId(@Param("ruleId") String ruleId);

    List<PolicyRule> findBySeverity(@Param("severity") String severity);

    List<PolicyRule> findAll();
}
