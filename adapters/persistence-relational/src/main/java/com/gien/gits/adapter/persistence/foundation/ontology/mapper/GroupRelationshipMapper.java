package com.gien.gits.adapter.persistence.foundation.ontology.mapper;

import com.gien.gits.adapter.persistence.foundation.ontology.dto.GroupRelationshipRow;
import com.gien.gits.ontology.GroupRelationship;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 集团关系 Mapper — foundation/ontology 层
 */
@Mapper
public interface GroupRelationshipMapper {

    void insert(GroupRelationship groupRelationship);

    List<GroupRelationshipRow> findRowsByGroupId(@Param("groupId") String groupId);
}
