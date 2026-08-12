package com.gien.gits.adapter.persistence.foundation.ontology.mapper;

import com.gien.gits.ontology.Transaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 交易流水 Mapper — foundation/ontology 层
 */
@Mapper
public interface TransactionMapper {

    int insert(Transaction transaction);

    int insertBatch(@Param("list") List<Transaction> transactions);

    Optional<Transaction> findById(@Param("id") String id);

    List<Transaction> findByCustomerId(@Param("customerId") String customerId);

    List<Transaction> findByCustomerIdAndDateRange(@Param("customerId") String customerId,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

    List<Transaction> findRecentByCustomerId(@Param("customerId") String customerId,
                                              @Param("limit") int limit);
}
