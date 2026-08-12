package com.gien.gits.engagement.port;

import com.gien.gits.engagement.OutreachScript;

/**
 * 可写外联脚本仓储端口 — 在 {@link OutreachScriptRepository} 只读契约基础上增加写操作。
 *
 * <p>适配器层的 JDBC 实现类应实现此接口，从而同时满足读/写契约。</p>
 */
public interface WritableOutreachScriptRepository extends OutreachScriptRepository {

    /**
     * 保存外联脚本。
     *
     * @param script 待保存的外联脚本
     */
    void save(OutreachScript script);
}
