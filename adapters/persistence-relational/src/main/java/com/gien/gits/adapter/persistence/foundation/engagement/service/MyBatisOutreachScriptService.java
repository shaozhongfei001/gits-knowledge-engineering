package com.gien.gits.adapter.persistence.foundation.engagement.service;

import com.gien.gits.adapter.persistence.foundation.engagement.mapper.OutreachScriptMapper;
import com.gien.gits.engagement.OutreachScript;
import com.gien.gits.engagement.port.WritableOutreachScriptRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * MyBatis 外拓脚本仓储实现 — foundation/engagement 层
 */
public class MyBatisOutreachScriptService implements WritableOutreachScriptRepository {

    private final OutreachScriptMapper mapper;

    public MyBatisOutreachScriptService(OutreachScriptMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(OutreachScript script) {
        mapper.insert(UUID.randomUUID().toString(), script);
    }

    @Override
    public Optional<OutreachScript> findByScriptId(String scriptId) {
        return mapper.findByScriptId(scriptId);
    }

    @Override
    public List<OutreachScript> findByCustomerId(String customerId) {
        return mapper.findByCustomerId(customerId);
    }

    @Override
    public List<OutreachScript> findByOperatingCaseId(String operatingCaseId) {
        return mapper.findByOperatingCaseId(operatingCaseId);
    }

    @Override
    public List<OutreachScript> findByJourneyId(String journeyId) {
        return mapper.findByJourneyId(journeyId);
    }
}
