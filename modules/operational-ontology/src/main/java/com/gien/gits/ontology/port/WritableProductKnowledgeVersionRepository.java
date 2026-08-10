package com.gien.gits.ontology.port;

import com.gien.gits.ontology.domain.ProductKnowledgeVersion;

/**
 * 产品知识版本可写仓储端口
 */
public interface WritableProductKnowledgeVersionRepository extends ProductKnowledgeVersionRepository {
    void save(ProductKnowledgeVersion version);
}
