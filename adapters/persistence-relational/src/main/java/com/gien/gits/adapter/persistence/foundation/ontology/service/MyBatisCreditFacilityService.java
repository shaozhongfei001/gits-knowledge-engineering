package com.gien.gits.adapter.persistence.foundation.ontology.service;

import com.gien.gits.adapter.persistence.foundation.ontology.dto.CreditFacilityRow;
import com.gien.gits.adapter.persistence.foundation.ontology.mapper.CreditFacilityMapper;
import com.gien.gits.ontology.CreditFacility;
import com.gien.gits.ontology.port.WritableCreditFacilityRepository;

import java.util.List;
import java.util.Optional;

/**
 * MyBatis 授信额度仓储实现 — foundation/ontology 层
 */
public class MyBatisCreditFacilityService implements WritableCreditFacilityRepository {

    private final CreditFacilityMapper mapper;

    public MyBatisCreditFacilityService(CreditFacilityMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(CreditFacility creditFacility) {
        mapper.insert(creditFacility);
    }

    @Override
    public Optional<CreditFacility> findByFacilityId(String facilityId) {
        return mapper.findRowByFacilityId(facilityId)
                .map(CreditFacilityRow::toCreditFacility);
    }

    @Override
    public List<CreditFacility> findByCustomerId(String customerId) {
        return mapper.findRowsByCustomerId(customerId).stream()
                .map(CreditFacilityRow::toCreditFacility)
                .toList();
    }
}
