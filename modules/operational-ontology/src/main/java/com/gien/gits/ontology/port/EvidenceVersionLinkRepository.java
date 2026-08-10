package com.gien.gits.ontology.port;

import com.gien.gits.ontology.domain.EvidenceVersionLink;
import java.util.List;
import java.util.Optional;

/**
 * 证据版本链接仓储端口
 */
public interface EvidenceVersionLinkRepository {
    Optional<EvidenceVersionLink> findByLinkId(String linkId);
    Optional<EvidenceVersionLink> findByEvidenceId(String evidenceId);
    List<EvidenceVersionLink> findVersionChain(String evidenceId);
    Optional<EvidenceVersionLink> findLatestVersion(String evidenceId);
}
