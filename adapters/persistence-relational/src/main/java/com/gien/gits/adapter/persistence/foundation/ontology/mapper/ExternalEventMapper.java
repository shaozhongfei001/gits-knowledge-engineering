package com.gien.gits.adapter.persistence.foundation.ontology.mapper;

import com.gien.gits.adapter.persistence.foundation.ontology.dto.ExternalEventRow;
import com.gien.gits.ontology.ExternalEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 外部事件 Mapper — foundation/ontology 层
 */
@Mapper
public interface ExternalEventMapper {

    void insert(ExternalEvent event);

    Optional<ExternalEventRow> findRowByEventId(@Param("eventId") String eventId);

    List<ExternalEventRow> findRowsByEntity(@Param("entity") String entity);

    List<ExternalEventRow> findRecentRows(@Param("limit") int limit);

    List<ExternalEventRow> findAllRows();
}
