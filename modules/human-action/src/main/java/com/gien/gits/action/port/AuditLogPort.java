package com.gien.gits.action.port;

import java.time.Instant;
import java.util.Map;

/**
 * 审计日志端口 — 记录敏感操作的审计轨迹。
 * 实现可以是日志记录、数据库持久化或外部审计系统推送。
 */
public interface AuditLogPort {

    /**
     * 记录审计事件
     *
     * @param action     操作类型 (LOGIN, DATA_ACCESS, CONFIG_CHANGE, etc.)
     * @param actor      操作者标识
     * @param resource   被操作的资源标识
     * @param outcome    操作结果 (SUCCESS, FAILURE)
     * @param details    附加详情
     * @param timestamp  事件时间戳
     */
    void log(String action, String actor, String resource, String outcome,
             Map<String, Object> details, Instant timestamp);

    /**
     * 记录审计事件（使用当前时间戳）
     */
    default void log(String action, String actor, String resource, String outcome,
                     Map<String, Object> details) {
        log(action, actor, resource, outcome, details, Instant.now());
    }
}
