package com.gien.gits.ontology.port;

import com.gien.gits.ontology.Interaction;

/**
 * 可写交互仓储端口 — 在 {@link InteractionRepository} 只读契约基础上增加写操作。
 *
 * <p>适配器层的 JDBC 实现类应实现此接口，从而同时满足读/写契约。</p>
 */
public interface WritableInteractionRepository extends InteractionRepository {

    /**
     * 保存交互聚合。
     *
     * @param interaction 待保存的交互
     */
    void save(Interaction interaction);
}
