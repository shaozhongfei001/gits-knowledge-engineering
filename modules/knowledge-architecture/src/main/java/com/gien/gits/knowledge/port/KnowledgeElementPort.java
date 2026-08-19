package com.gien.gits.knowledge.port;

import com.gien.gits.knowledge.KnowledgeElement;
import java.util.List;
import java.util.Optional;

/**
 * 知识要素读取 Port（CTR-KELEM-001 消费者：knowledge_map_registry、asset_registry）。
 *
 * <p>单个查找契约返回 {@link Optional}：未找到或内容不合法（fail-closed）时返回
 * {@link Optional#empty()}；按知识条目（KI）列出的接口返回空数组而非 {@code null}。</p>
 */
public interface KnowledgeElementPort {

    /**
     * 按要素 ID 查找单个知识要素。
     *
     * @param elementId 知识要素 ID（如 {@code KE-009-01}）
     * @return 要素存在且合法时返回非空；否则返回 {@link Optional#empty()}
     */
    Optional<KnowledgeElement> find(String elementId);

    /**
     * 列出指定知识条目（KI）下的全部知识要素。
     *
     * @param knowledgeItemId 知识条目 ID（如 {@code KI-009}）
     * @return 该 KI 下全部合法要素；为空时返回空数组，不返回 {@code null}
     */
    List<KnowledgeElement> listByKnowledgeItem(String knowledgeItemId);
}
