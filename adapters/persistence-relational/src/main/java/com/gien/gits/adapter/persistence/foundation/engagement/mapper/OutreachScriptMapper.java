package com.gien.gits.adapter.persistence.foundation.engagement.mapper;

import com.gien.gits.engagement.OutreachScript;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 外拓脚本 Mapper — foundation/engagement 层
 */
@Mapper
public interface OutreachScriptMapper {

    void insert(@Param("id") String id, @Param("script") OutreachScript script);

    Optional<OutreachScript> findByScriptId(@Param("scriptId") String scriptId);

    List<OutreachScript> findByCustomerId(@Param("customerId") String customerId);

    List<OutreachScript> findByOperatingCaseId(@Param("operatingCaseId") String operatingCaseId);

    List<OutreachScript> findByJourneyId(@Param("journeyId") String journeyId);
}
