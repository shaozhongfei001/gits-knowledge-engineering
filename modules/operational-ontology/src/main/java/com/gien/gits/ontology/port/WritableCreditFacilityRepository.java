package com.gien.gits.ontology.port;

import com.gien.gits.ontology.CreditFacility;

/**
 * 可写授信额度仓储端口 — 在 {@link CreditFacilityRepository} 只读契约基础上增加写操作。
 *
 * <p>适配器层的 JDBC 实现类应实现此接口，从而同时满足读/写契约。</p>
 */
public interface WritableCreditFacilityRepository extends CreditFacilityRepository {

    /**
     * 保存授信额度聚合。
     *
     * @param creditFacility 待保存的授信额度
     */
    void save(CreditFacility creditFacility);
}
