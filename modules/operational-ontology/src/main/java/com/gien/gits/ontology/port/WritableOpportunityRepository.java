package com.gien.gits.ontology.port;

import com.gien.gits.ontology.domain.Opportunity;

/**
 * 商机可写仓储端口
 */
public interface WritableOpportunityRepository extends OpportunityRepository {
    void save(Opportunity opportunity);
    void updateStatus(String opportunityId, String status);
}
