package com.gien.gits.api.service;

import com.gien.gits.ontology.domain.ProductKnowledgeVersion;
import com.gien.gits.ontology.port.WritableProductKnowledgeVersionRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 产品知识版本服务 — 产品信息版本化管理
 */
public class ProductKnowledgeVersionService {

    private final WritableProductKnowledgeVersionRepository versionRepo;

    public ProductKnowledgeVersionService(WritableProductKnowledgeVersionRepository versionRepo) {
        this.versionRepo = Objects.requireNonNull(versionRepo);
    }

    public Optional<ProductKnowledgeVersion> findByVersionId(String versionId) {
        return versionRepo.findByVersionId(versionId);
    }

    public List<ProductKnowledgeVersion> findByProductId(String productId) {
        return versionRepo.findByProductId(productId);
    }

    public Optional<ProductKnowledgeVersion> findLatestByProductId(String productId) {
        return versionRepo.findLatestByProductId(productId);
    }

    public List<ProductKnowledgeVersion> findByCategory(String category) {
        return versionRepo.findByCategory(category);
    }

    public List<ProductKnowledgeVersion> findRecentVersions(int limit) {
        return versionRepo.findRecentVersions(limit);
    }

    public ProductKnowledgeVersion create(ProductKnowledgeVersion version) {
        versionRepo.save(version);
        return version;
    }
}
