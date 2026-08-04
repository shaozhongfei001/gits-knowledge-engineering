package com.gien.gits.ontology.port;

import com.gien.gits.ontology.ProductKnowledgeCard;

/**
 * 可写产品目录仓储端口 — 在 {@link ProductCatalogRepository} 只读契约基础上增加写操作。
 *
 * <p>适配器层的 JDBC 实现类应实现此接口，从而同时满足读/写契约。</p>
 */
public interface WritableProductCatalogRepository extends ProductCatalogRepository {

    /**
     * 保存产品知识卡聚合。
     *
     * @param productKnowledgeCard 待保存的产品知识卡
     */
    void save(ProductKnowledgeCard productKnowledgeCard);
}
