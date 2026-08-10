package com.gien.gits.ontology.port;

import com.gien.gits.ontology.domain.ProductKnowledgeVersion;
import java.util.List;
import java.util.Optional;

/**
 * 产品知识版本仓储端口
 */
public interface ProductKnowledgeVersionRepository {
    Optional<ProductKnowledgeVersion> findByVersionId(String versionId);
    List<ProductKnowledgeVersion> findByProductId(String productId);
    Optional<ProductKnowledgeVersion> findLatestByProductId(String productId);
    List<ProductKnowledgeVersion> findByCategory(String category);
    List<ProductKnowledgeVersion> findRecentVersions(int limit);
}
