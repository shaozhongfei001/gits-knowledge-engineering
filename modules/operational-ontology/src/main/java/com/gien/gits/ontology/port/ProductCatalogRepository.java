package com.gien.gits.ontology.port;

import com.gien.gits.ontology.ProductKnowledgeCard;

import java.util.List;
import java.util.Optional;

/**
 * 产品目录仓储端口 — 只读操作。
 *
 * <p>定义对 {@link ProductKnowledgeCard} 聚合的查询契约，由适配器层实现。
 * 写操作见 {@link WritableProductCatalogRepository}。</p>
 */
public interface ProductCatalogRepository {

    /**
     * 根据产品ID查找产品知识卡。
     *
     * @param productId 产品唯一标识
     * @return 找到的产品知识卡，若不存在则返回空
     */
    Optional<ProductKnowledgeCard> findByProductId(String productId);

    /**
     * 查找所有产品知识卡。
     *
     * @return 所有产品知识卡列表
     */
    List<ProductKnowledgeCard> findAll();
}
