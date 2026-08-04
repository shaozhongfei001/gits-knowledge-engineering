package com.gien.gits.ontology.port;

import com.gien.gits.ontology.model.OracleClaim;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Oracle只读管道端口 — 从Oracle系统读取索赔数据的接口。
 * 适配器实现通过JDBC连接Oracle数据库，默认使用StubOracleSourceAdapter（不可用）。
 */
public interface OracleSourcePort {
    /**
     * 读取指定时间之后更新的索赔记录。
     * @param since 上次同步时间
     * @return 索赔记录列表
     */
    List<OracleClaim> readClaims(LocalDateTime since);

    /**
     * 检查Oracle数据源是否可用。
     * @return true表示Oracle连接可用
     */
    boolean isAvailable();
}
