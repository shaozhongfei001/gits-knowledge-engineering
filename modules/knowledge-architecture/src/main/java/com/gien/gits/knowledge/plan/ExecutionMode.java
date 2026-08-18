package com.gien.gits.knowledge.plan;

/**
 * 执行模式。P20 仅允许 SHADOW；任何 PRODUCTION 模式请求必须 fail-closed 拒绝。
 */
public enum ExecutionMode {
    SHADOW,
    PRODUCTION
}
