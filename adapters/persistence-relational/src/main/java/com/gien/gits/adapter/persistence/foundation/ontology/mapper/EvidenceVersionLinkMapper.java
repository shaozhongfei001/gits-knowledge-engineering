package com.gien.gits.adapter.persistence.foundation.ontology.mapper;

import com.gien.gits.ontology.domain.EvidenceVersionLink;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 证据版本链接 Mapper — foundation/ontology 层
 */
@Mapper
public interface EvidenceVersionLinkMapper {

    void insert(EvidenceVersionLink link);

    Optional<EvidenceVersionLink> findByLinkId(@Param("linkId") String linkId);

    Optional<EvidenceVersionLink> findByEvidenceId(@Param("evidenceId") String evidenceId);

    List<EvidenceVersionLink> findVersionChain(@Param("evidenceId") String evidenceId);

    Optional<EvidenceVersionLink> findLatestVersion(@Param("evidenceId") String evidenceId);
}
