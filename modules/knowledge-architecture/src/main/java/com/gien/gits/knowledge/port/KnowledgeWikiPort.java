package com.gien.gits.knowledge.port;

/**
 * 知识地图渲染 Port（P22 G3：LLM 优先读图）。
 *
 * <p>将受控知识地图（场景→知识域→KI→KE 分层导航）渲染为 LLM 可读的 Markdown 文本，
 * 供 {@code LlmClient} 作为 systemPrompt 注入，实现"大模型优先读图再执行任务"（方案 A：
 * 加载范围由规划器/调用方决定）。渲染内容须含权威标注（AUTHORITATIVE），
 * 仅承载已有合同（CTR-KMAP-001 / CTR-ASSET-001 / CTR-KELEM-001）定义的字段，不发明新字段。</p>
 *
 * <p>fail-closed：所有方法在未知 ID / 无匹配内容时返回空字符串（{@code ""}），绝不返回
 * {@code null}，也绝不抛出异常。</p>
 */
public interface KnowledgeWikiPort {

    /**
     * 渲染指定范围的知识地图/目录为 LLM 可读 Markdown。
     *
     * <p>scope 由调用方（规划器）依据 {@code ActivationPlan.taskType} 决定，可为地图 ID
     * （如 {@code KM-CORP-RM-PREVISIT}）或域 ID（如 {@code KD-CORP-RM}）；空/未知 scope
     * 回退到根知识地图。渲染包含场景入口（tasks/roles）、知识域、以及 KI→KE 导航。</p>
     *
     * @param scope 地图/域加载范围；{@code null} 或空白时渲染根地图
     * @return LLM 可读的 Markdown 地图文本；无匹配内容时返回空字符串（fail-closed）
     */
    String renderMap(String scope);

    /**
     * 渲染指定知识条目（KI）的要素清单为 LLM 可读 Markdown。
     *
     * @param kiId 知识条目 ID（如 {@code KI-009}）
     * @return 该 KI 下全部要素的 Markdown 清单；KI 不存在或为空时返回空字符串（fail-closed）
     */
    String renderKnowledgeItem(String kiId);

    /**
     * 渲染单个知识要素为 LLM 可读 Markdown。
     *
     * @param elementId 知识要素 ID（如 {@code KE-009-01}）
     * @return 该要素的 Markdown 详情；要素不存在时返回空字符串（fail-closed）
     */
    String renderElement(String elementId);
}
