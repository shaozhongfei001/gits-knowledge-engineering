package com.gien.gits.adapter.persistence.foundation.engagement.service;

import com.gien.gits.adapter.persistence.foundation.engagement.mapper.MeetingScriptMapper;
import com.gien.gits.engagement.MeetingScript;
import com.gien.gits.engagement.port.WritableMeetingScriptRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * MyBatis 会面脚本仓储实现 — foundation/engagement 层
 */
public class MyBatisMeetingScriptService implements WritableMeetingScriptRepository {

    private final MeetingScriptMapper mapper;

    public MyBatisMeetingScriptService(MeetingScriptMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(MeetingScript script) {
        mapper.insert(UUID.randomUUID().toString(), script);
    }

    @Override
    public Optional<MeetingScript> findByScriptId(String scriptId) {
        return mapper.findByScriptId(scriptId);
    }

    @Override
    public List<MeetingScript> findByCustomerId(String customerId) {
        return mapper.findByCustomerId(customerId);
    }

    @Override
    public List<MeetingScript> findByOperatingCaseId(String operatingCaseId) {
        return mapper.findByOperatingCaseId(operatingCaseId);
    }

    @Override
    public List<MeetingScript> findByJourneyId(String journeyId) {
        return mapper.findByJourneyId(journeyId);
    }
}
