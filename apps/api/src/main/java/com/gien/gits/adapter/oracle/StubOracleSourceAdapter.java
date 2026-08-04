package com.gien.gits.adapter.oracle;

import com.gien.gits.ontology.model.OracleClaim;
import com.gien.gits.ontology.port.OracleSourcePort;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Stub Oracle数据源适配器 — Oracle不可用时的默认实现。
 * 返回空列表，isAvailable()始终返回false。
 */
public class StubOracleSourceAdapter implements OracleSourcePort {

    @Override
    public List<OracleClaim> readClaims(LocalDateTime since) {
        return Collections.emptyList();
    }

    @Override
    public boolean isAvailable() {
        return false;
    }
}
