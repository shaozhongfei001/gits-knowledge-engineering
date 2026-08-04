package com.gien.gits.ontology.port;

import com.gien.gits.ontology.KycGapProfile;

import java.util.Optional;

/**
 * KYC差距档案仓储端口 — 只读操作。
 *
 * <p>定义对 {@link KycGapProfile} 聚合的查询契约，由适配器层实现。
 * 写操作见 {@link WritableKycGapProfileRepository}。</p>
 */
public interface KycGapProfileRepository {

    /**
     * 根据档案ID查找KYC差距档案。
     *
     * @param profileId 档案唯一标识
     * @return 找到的KYC差距档案，若不存在则返回空
     */
    Optional<KycGapProfile> findByProfileId(String profileId);

    /**
     * 根据客户ID查找最新的KYC差距档案。
     *
     * @param customerId 客户唯一标识
     * @return 最新的KYC差距档案，若不存在则返回空
     */
    Optional<KycGapProfile> findLatestByCustomerId(String customerId);
}
