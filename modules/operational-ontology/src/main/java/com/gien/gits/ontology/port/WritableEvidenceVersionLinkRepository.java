package com.gien.gits.ontology.port;

import com.gien.gits.ontology.domain.EvidenceVersionLink;

/**
 * 证据版本链接可写仓储端口
 */
public interface WritableEvidenceVersionLinkRepository extends EvidenceVersionLinkRepository {
    void save(EvidenceVersionLink link);
}
