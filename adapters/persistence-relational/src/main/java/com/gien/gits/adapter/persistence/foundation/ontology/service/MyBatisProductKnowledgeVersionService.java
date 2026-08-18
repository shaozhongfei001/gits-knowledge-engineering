package com.gien.gits.adapter.persistence.foundation.ontology.service;

import com.gien.gits.adapter.persistence.foundation.ontology.mapper.ProductKnowledgeVersionMapper;
import com.gien.gits.ontology.domain.ProductKnowledgeVersion;
import com.gien.gits.ontology.port.WritableProductKnowledgeVersionRepository;

import java.util.List;
import java.util.Optional;

/**
 * MyBatis 产品知识版本仓储实现 — foundation/ontology 层
 */
public class MyBatisProductKnowledgeVersionService implements WritableProductKnowledgeVersionRepository {

    private final ProductKnowledgeVersionMapper mapper;

    public MyBatisProductKnowledgeVersionService(ProductKnowledgeVersionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(ProductKnowledgeVersion version) {
        mapper.insert(version);
    }

    @Override
    public Optional<ProductKnowledgeVersion> findByVersionId(String versionId) {
        return mapper.findByVersionId(versionId);
    }

    @Override
    public List<ProductKnowledgeVersion> findByProductId(String productId) {
        return mapper.findByProductId(productId);
    }

    @Override
    public Optional<ProductKnowledgeVersion> findLatestByProductId(String productId) {
        return mapper.findLatestByProductId(productId);
    }

    @Override
    public List<ProductKnowledgeVersion> findByCategory(String category) {
        return mapper.findByCategory(category);
    }

    @Override
    public List<ProductKnowledgeVersion> findRecentVersions(int limit) {
        return mapper.findRecentVersions(limit);
    }
}
