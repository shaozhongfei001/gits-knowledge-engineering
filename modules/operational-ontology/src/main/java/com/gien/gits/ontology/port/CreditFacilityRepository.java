package com.gien.gits.ontology.port;

import com.gien.gits.ontology.CreditFacility;

import java.util.List;
import java.util.Optional;

/**
 * 授信额度仓储端口 — 只读操作。
 *
 * <p>定义对 {@link CreditFacility} 聚合的查询契约，由适配器层实现。
 * 写操作见 {@link WritableCreditFacilityRepository}。</p>
 */
public interface CreditFacilityRepository {

    /**
     * 根据额度ID查找授信额度。
     *
     * @param facilityId 授信额度唯一标识
     * @return 找到的授信额度，若不存在则返回空
     */
    Optional<CreditFacility> findByFacilityId(String facilityId);

    /**
     * 根据客户ID查找其名下所有授信额度。
     *
     * @param customerId 客户唯一标识
     * @return 该客户名下的授信额度列表
     */
    List<CreditFacility> findByCustomerId(String customerId);
}
