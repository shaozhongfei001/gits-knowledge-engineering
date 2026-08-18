package com.gien.gits.adapter.persistence.foundation.ontology.mapper;

import com.gien.gits.ontology.KycGapProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * KYC缺口画像 Mapper — foundation/ontology 层
 */
@Mapper
public interface KycGapProfileMapper {

    void insert(KycGapProfile profile);

    Optional<KycGapProfile> findByProfileId(@Param("profileId") String profileId);

    Optional<KycGapProfile> findByCustomerId(@Param("customerId") String customerId);

    Optional<KycGapProfile> findLatestByCustomerId(@Param("customerId") String customerId);

    List<KycGapProfile> findByEntity(@Param("entity") String entity);
}
