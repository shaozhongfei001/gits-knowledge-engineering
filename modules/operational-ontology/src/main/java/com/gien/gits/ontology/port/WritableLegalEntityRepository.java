package com.gien.gits.ontology.port;

import com.gien.gits.ontology.LegalEntity;

/**
 * 可写法律实体仓储端口 — 在 {@link LegalEntityRepository} 只读契约基础上增加写操作。
 *
 * <p>适配器层的 JDBC 实现类应实现此接口，从而同时满足读/写契约。</p>
 */
public interface WritableLegalEntityRepository extends LegalEntityRepository {

    /**
     * 保存法律实体聚合。
     *
     * @param legalEntity 待保存的法律实体
     */
    void save(LegalEntity legalEntity);
}
