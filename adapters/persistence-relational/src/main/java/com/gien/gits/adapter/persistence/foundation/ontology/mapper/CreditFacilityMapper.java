package com.gien.gits.adapter.persistence.foundation.ontology.mapper;

import com.gien.gits.adapter.persistence.foundation.ontology.dto.CreditFacilityRow;
import com.gien.gits.ontology.CreditFacility;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 授信额度 Mapper — foundation/ontology 层
 */
@Mapper
public interface CreditFacilityMapper {

    void insert(CreditFacility creditFacility);

    Optional<CreditFacilityRow> findRowByFacilityId(@Param("facilityId") String facilityId);

    List<CreditFacilityRow> findRowsByCustomerId(@Param("customerId") String customerId);
}
