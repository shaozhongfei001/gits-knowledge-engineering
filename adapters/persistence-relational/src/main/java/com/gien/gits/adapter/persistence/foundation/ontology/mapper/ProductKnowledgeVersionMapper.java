package com.gien.gits.adapter.persistence.foundation.ontology.mapper;

import com.gien.gits.ontology.domain.ProductKnowledgeVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 产品知识版本 Mapper — foundation/ontology 层
 */
@Mapper
public interface ProductKnowledgeVersionMapper {

    void insert(ProductKnowledgeVersion version);

    Optional<ProductKnowledgeVersion> findByVersionId(@Param("versionId") String versionId);

    List<ProductKnowledgeVersion> findByProductId(@Param("productId") String productId);

    Optional<ProductKnowledgeVersion> findLatestByProductId(@Param("productId") String productId);

    List<ProductKnowledgeVersion> findByCategory(@Param("category") String category);

    List<ProductKnowledgeVersion> findRecentVersions(@Param("limit") int limit);
}
