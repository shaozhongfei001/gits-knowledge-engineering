package com.gien.gits.adapter.persistence.foundation.ontology.mapper;

import com.gien.gits.ontology.ProductKnowledgeCard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 产品知识卡 Mapper — foundation/ontology 层
 */
@Mapper
public interface ProductKnowledgeCardMapper {

    void insert(ProductKnowledgeCard card);

    Optional<ProductKnowledgeCard> findByProductId(@Param("productId") String productId);

    List<ProductKnowledgeCard> findAll();
}
