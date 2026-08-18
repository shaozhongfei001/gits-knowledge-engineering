package com.gien.gits.knowledge.port;

import com.gien.gits.knowledge.KnowledgeMap;
import java.util.Optional;

/**
 * 知识地图读取 Port（CTR-KMAP-001 消费者：knowledge_map_registry）。
 *
 * <p>契约返回 {@link Optional}：未找到或内容不合法（fail-closed）时返回 {@link Optional#empty()}，
 * 不允许抛出解析异常，也不允许返回部分/非法对象。</p>
 */
public interface KnowledgeMapPort {

    Optional<KnowledgeMap> loadRoot();

    Optional<KnowledgeMap> load(String mapId);
}
