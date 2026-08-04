package com.gien.gits.ontology.port;

import com.gien.gits.ontology.KycGapProfile;

/**
 * 可写KYC差距档案仓储端口 — 在 {@link KycGapProfileRepository} 只读契约基础上增加写操作。
 *
 * <p>适配器层的 JDBC 实现类应实现此接口，从而同时满足读/写契约。</p>
 */
public interface WritableKycGapProfileRepository extends KycGapProfileRepository {

    /**
     * 保存KYC差距档案聚合。
     *
     * @param kycGapProfile 待保存的KYC差距档案
     */
    void save(KycGapProfile kycGapProfile);
}
