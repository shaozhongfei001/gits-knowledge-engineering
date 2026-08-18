package com.gien.gits.adapter.persistence.foundation.ontology.service;

import com.gien.gits.adapter.persistence.foundation.ontology.mapper.ProductKnowledgeCardMapper;
import com.gien.gits.ontology.ProductKnowledgeCard;
import com.gien.gits.ontology.port.WritableProductCatalogRepository;

import java.util.List;
import java.util.Optional;

/**
 * MyBatis 产品目录仓储实现 — foundation/ontology 层
 */
public class MyBatisProductCatalogService implements WritableProductCatalogRepository {

    private final ProductKnowledgeCardMapper mapper;

    public MyBatisProductCatalogService(ProductKnowledgeCardMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(ProductKnowledgeCard productKnowledgeCard) {
        mapper.insert(productKnowledgeCard);
    }

    @Override
    public Optional<ProductKnowledgeCard> findByProductId(String productId) {
        return mapper.findByProductId(productId);
    }

    @Override
    public List<ProductKnowledgeCard> findAll() {
        return mapper.findAll();
    }
}
