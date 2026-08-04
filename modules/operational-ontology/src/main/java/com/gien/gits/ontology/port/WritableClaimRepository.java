package com.gien.gits.ontology.port;

import com.gien.gits.ontology.Claim;
import com.gien.gits.ontology.ClaimStatus;

import java.util.UUID;

/**
 * 可写声明仓储端口 — 在 {@link ClaimRepository} 只读契约基础上增加写操作。
 *
 * <p>适配器层的 JDBC 实现类应实现此接口，从而同时满足读/写契约。</p>
 */
public interface WritableClaimRepository extends ClaimRepository {

    /**
     * 保存声明聚合。
     *
     * @param claim 待保存的声明
     */
    void save(Claim claim);

    /**
     * 更新声明状态。
     *
     * @param claimId 声明唯一标识
     * @param status  目标状态
     */
    void updateStatus(UUID claimId, ClaimStatus status);
}
