package com.gien.gits.api.service;

import com.gien.gits.knowledge.KnowledgeElement;
import com.gien.gits.knowledge.port.KnowledgeElementPort;
import com.gien.gits.knowledge.port.KnowledgeWikiPort;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 知识装配器（P23：知识地图驱动报告生成）。
 *
 * <p>按任务从知识地图组装 LLM 上下文：知识地图导航（锁定知识领域）+ 任务关联的知识条目
 * （KI）及其知识要素（KE）内容 + 业务数据。输出为 LLM 可读的 Markdown，供
 * {@code LlmClient} 作为 systemPrompt 注入，实现"大模型优先读知识地图再生成报告"。</p>
 *
 * <p>fail-closed：任何 KI/KE 拉取为空时跳过该条目，不抛异常；返回上下文可能为空（调用方决定）。</p>
 */
public final class KnowledgeAssembler {

    private final KnowledgeElementPort elementPort;
    private final KnowledgeWikiPort wikiPort;

    public KnowledgeAssembler(KnowledgeElementPort elementPort, KnowledgeWikiPort wikiPort) {
        this.elementPort = Objects.requireNonNull(elementPort);
        this.wikiPort = Objects.requireNonNull(wikiPort);
    }

    /**
     * 组装知识上下文。
     *
     * @param scope         知识地图/域范围（锁定知识领域）
     * @param knowledgeItemIds 任务关联的知识条目（KI）列表（来自激活合同 knowledgeItemIds）
     * @param businessContext  业务数据（客户/事件/KYC 等组装好的文本）
     * @return LLM 可读知识上下文 Markdown
     */
    public String assemble(String scope, List<String> knowledgeItemIds, String businessContext) {
        StringBuilder sb = new StringBuilder(512);
        sb.append("### 知识地图导航（权威受控）\n");
        String mapNav = wikiPort.renderMap(scope);
        if (!mapNav.isBlank()) {
            sb.append(mapNav).append('\n');
        }

        sb.append("\n### 任务关联知识条目（KI→KE）\n");
        if (knowledgeItemIds != null && !knowledgeItemIds.isEmpty()) {
            for (String kiId : knowledgeItemIds) {
                List<KnowledgeElement> elements = elementPort.listByKnowledgeItem(kiId);
                if (elements.isEmpty()) {
                    continue;
                }
                sb.append("#### ").append(kiId).append('\n');
                for (KnowledgeElement element : elements) {
                    sb.append("- [").append(element.kind()).append("] ")
                            .append(element.name()).append("：")
                            .append(element.content()).append('\n');
                }
            }
        }

        if (businessContext != null && !businessContext.isBlank()) {
            sb.append("\n### 业务数据\n").append(businessContext).append('\n');
        }
        return sb.toString();
    }
}
