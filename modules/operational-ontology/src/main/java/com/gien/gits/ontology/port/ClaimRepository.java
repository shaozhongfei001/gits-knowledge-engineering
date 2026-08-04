package com.gien.gits.ontology.port;

import com.gien.gits.ontology.Claim;
import com.gien.gits.ontology.ClaimStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 声明仓储端口 — 只读操作。
 *
 * <p>定义对 {@link Claim} 聚合的查询契约，由适配器层实现。
 * 写操作见 {@link WritableClaimRepository}。</p>
 */
public interface ClaimRepository {

    /**
     * 根据声明ID查找声明。
     *
     * @param claimId 声明唯一标识
     * @return 找到的声明，若不存在则返回空
     */
    Optional<Claim> findById(UUID claimId);

    /**
     * 根据运营案例ID查找其关联的所有声明。
     *
     * @param caseId 运营案例唯一标识
     * @return 关联的声明列表
     */
    List<Claim> findByCaseId(UUID caseId);
}
