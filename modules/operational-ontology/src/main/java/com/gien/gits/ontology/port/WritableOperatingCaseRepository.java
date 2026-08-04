package com.gien.gits.ontology.port;

import com.gien.gits.ontology.CaseStatus;
import com.gien.gits.ontology.OperatingCase;

import java.util.UUID;

/**
 * 可写运营案例仓储端口 — 在 {@link OperatingCaseRepository} 只读契约基础上增加写操作。
 *
 * <p>适配器层的 JDBC 实现类应实现此接口，从而同时满足读/写契约。</p>
 */
public interface WritableOperatingCaseRepository extends OperatingCaseRepository {

    /**
     * 保存运营案例聚合。
     *
     * @param operatingCase 待保存的运营案例
     */
    void save(OperatingCase operatingCase);

    /**
     * 更新运营案例状态。
     *
     * @param caseId 案例唯一标识
     * @param status 目标状态
     */
    void updateStatus(UUID caseId, CaseStatus status);
}
