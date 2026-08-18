package com.gien.gits.adapter.persistence.foundation.ontology.mapper;

import com.gien.gits.adapter.persistence.foundation.ontology.dto.TransactionRecordRow;
import com.gien.gits.ontology.TransactionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 交易记录 Mapper — foundation/ontology 层
 */
@Mapper
public interface TransactionRecordMapper {

    void insert(TransactionRecord record);

    List<TransactionRecordRow> findRowsByCustomerId(@Param("customerId") String customerId);
}
