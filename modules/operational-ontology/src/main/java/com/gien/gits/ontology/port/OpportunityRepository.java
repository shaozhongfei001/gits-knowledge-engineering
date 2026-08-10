package com.gien.gits.ontology.port;

import com.gien.gits.ontology.domain.Opportunity;
import java.util.List;
import java.util.Optional;

/**
 * 商机仓储端口
 */
public interface OpportunityRepository {
    Optional<Opportunity> findByOpportunityId(String opportunityId);
    List<Opportunity> findByCustomerId(String customerId);
    List<Opportunity> findByStatus(String status);
    List<Opportunity> findByOpportunityType(String opportunityType);
    List<Opportunity> findByAssignedTo(String assignedTo);
    List<Opportunity> findActiveByCustomerId(String customerId);
    List<Opportunity> findAll();
}
