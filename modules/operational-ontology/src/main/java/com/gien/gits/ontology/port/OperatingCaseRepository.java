package com.gien.gits.ontology.port;

import com.gien.gits.ontology.CaseStatus;
import com.gien.gits.ontology.OperatingCase;

import java.util.Optional;
import java.util.UUID;

/**
 * 运营案例仓储端口 — 只读操作。
 *
 * <p>定义对 {@link OperatingCase} 聚合的查询契约，由适配器层实现。
 * 写操作见 {@link WritableOperatingCaseRepository}。</p>
 */
public interface OperatingCaseRepository {

    /**
     * 根据案例ID查找运营案例。
     *
     * @param caseId 案例唯一标识
     * @return 找到的运营案例，若不存在则返回空
     */
    Optional<OperatingCase> findById(UUID caseId);
}
