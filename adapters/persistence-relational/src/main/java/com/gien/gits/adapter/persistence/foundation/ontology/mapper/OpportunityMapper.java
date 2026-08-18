package com.gien.gits.adapter.persistence.foundation.ontology.mapper;

import com.gien.gits.ontology.domain.Opportunity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 商机 Mapper — foundation/ontology 层
 */
@Mapper
public interface OpportunityMapper {

    void insert(Opportunity opportunity);

    Optional<Opportunity> findByOpportunityId(@Param("opportunityId") String opportunityId);

    List<Opportunity> findByCustomerId(@Param("customerId") String customerId);

    List<Opportunity> findByStatus(@Param("status") String status);

    List<Opportunity> findActiveByCustomerId(@Param("customerId") String customerId);

    List<Opportunity> findByOpportunityType(@Param("opportunityType") String opportunityType);

    List<Opportunity> findByAssignedTo(@Param("assignedTo") String assignedTo);

    List<Opportunity> findAll();

    void updateStatus(@Param("opportunityId") String opportunityId,
                      @Param("status") String status);
}
