package com.gien.gits.engagement.port;

import com.gien.gits.engagement.MeetingScript;

/**
 * 可写会面脚本仓储端口 — 在 {@link MeetingScriptRepository} 只读契约基础上增加写操作。
 *
 * <p>适配器层的 JDBC 实现类应实现此接口，从而同时满足读/写契约。</p>
 */
public interface WritableMeetingScriptRepository extends MeetingScriptRepository {

    /**
     * 保存会面脚本。
     *
     * @param script 待保存的会面脚本
     */
    void save(MeetingScript script);
}
