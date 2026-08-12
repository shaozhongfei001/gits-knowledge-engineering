package com.gien.gits.adapter.persistence.foundation.ontology.service;

import com.gien.gits.adapter.persistence.foundation.ontology.mapper.EvidenceVersionLinkMapper;
import com.gien.gits.ontology.domain.EvidenceVersionLink;
import com.gien.gits.ontology.port.WritableEvidenceVersionLinkRepository;

import java.util.List;
import java.util.Optional;

/**
 * MyBatis 证据版本链接仓储实现 — foundation/ontology 层
 */
public class MyBatisEvidenceVersionLinkService implements WritableEvidenceVersionLinkRepository {

    private final EvidenceVersionLinkMapper mapper;

    public MyBatisEvidenceVersionLinkService(EvidenceVersionLinkMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(EvidenceVersionLink link) {
        mapper.insert(link);
    }

    @Override
    public Optional<EvidenceVersionLink> findByLinkId(String linkId) {
        return mapper.findByLinkId(linkId);
    }

    @Override
    public Optional<EvidenceVersionLink> findByEvidenceId(String evidenceId) {
        return mapper.findByEvidenceId(evidenceId);
    }

    @Override
    public List<EvidenceVersionLink> findVersionChain(String evidenceId) {
        return mapper.findVersionChain(evidenceId);
    }

    @Override
    public Optional<EvidenceVersionLink> findLatestVersion(String evidenceId) {
        return mapper.findLatestVersion(evidenceId);
    }
}
