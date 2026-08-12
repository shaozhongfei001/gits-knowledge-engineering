package com.gien.gits.adapter.persistence.foundation.engagement.mapper;

import com.gien.gits.engagement.MeetingScript;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 会面脚本 Mapper — foundation/engagement 层
 */
@Mapper
public interface MeetingScriptMapper {

    void insert(@Param("id") String id, @Param("script") MeetingScript script);

    Optional<MeetingScript> findByScriptId(@Param("scriptId") String scriptId);

    List<MeetingScript> findByCustomerId(@Param("customerId") String customerId);

    List<MeetingScript> findByOperatingCaseId(@Param("operatingCaseId") String operatingCaseId);

    List<MeetingScript> findByJourneyId(@Param("journeyId") String journeyId);
}
