package com.gien.gits.api.service;

import com.gien.gits.engagement.port.LlmClient;
import com.gien.gits.engagement.port.LlmClientException;
import com.gien.gits.knowledge.port.KnowledgeWikiPort;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 知识地图读图服务（P22 G3：LLM 优先读图再执行）。
 *
 * <p>流程：执行任务前先通过 {@link KnowledgeWikiPort#renderMap} 渲染"LLM 可读受控知识地图"
 * （加载范围 scope 由规划器依据 {@code ActivationPlan.taskType} 决定），将其作为
 * {@link LlmClient#complete} 的 systemPrompt 注入，再执行任务（userPrompt）。
 * 符合方案 A：加载范围由规划器决定。</p>
 *
 * <p>failover：LLM 调用失败（{@link LlmClientException}）或地图渲染为空时，回退到模板逻辑
 * （返回结构化 fallback 文本），不抛异常、不改变现有业务行为（纯增量，不接线到既有 Service）。</p>
 */
public class KnowledgeWikiService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeWikiService.class);

    /** systemPrompt 头，定义 LLM 角色与读图指令。 */
    private static final String SYSTEM_PROMPT_HEADER =
            "你是一名银行客户经理知识助手。请先阅读以下【受控知识地图】（AUTHORITATIVE），"
                    + "再依据地图中的知识条目（KI）与知识要素（KE）执行任务。地图如下：\n\n";

    private final KnowledgeWikiPort knowledgeWikiPort;
    private final LlmClient llmClient;

    public KnowledgeWikiService(KnowledgeWikiPort knowledgeWikiPort, LlmClient llmClient) {
        this.knowledgeWikiPort = Objects.requireNonNull(knowledgeWikiPort, "knowledgeWikiPort");
        this.llmClient = Objects.requireNonNull(llmClient, "llmClient");
    }

    /**
     * 读图并执行（主流程）。
     *
     * <p>先渲染 scope 对应知识地图，注入 systemPrompt，再调用 {@link LlmClient#complete}。
     * 地图渲染为空（fail-closed）或 LLM 调用失败时回退到模板，不抛异常。</p>
     *
     * @param scope      读图范围（地图 ID；{@code null} 用根地图）
     * @param userPrompt 具体任务请求内容
     * @return LLM 生成的文本；fallback 时返回模板文本
     */
    public String executeWithKnowledgeMap(String scope, String userPrompt) {
        String mapText = knowledgeWikiPort.renderMap(scope);
        String systemPrompt = buildSystemPrompt(mapText);
        try {
            return llmClient.complete(systemPrompt, userPrompt);
        } catch (LlmClientException error) {
            log.warn("KnowledgeWiki LLM call failed, falling back to template: scope={} error={}",
                    scope, error.getMessage());
            return fallbackTemplate(userPrompt);
        }
    }

    /**
     * 构造注入知识地图的 systemPrompt。
     *
     * <p>地图渲染为空（fail-closed）时，systemPrompt 仅含角色说明并注明地图不可用。</p>
     */
    private String buildSystemPrompt(String mapText) {
        if (mapText == null || mapText.isBlank()) {
            return SYSTEM_PROMPT_HEADER
                    + "（知识地图当前不可用。请基于你的通用领域知识执行任务，并在结果中注明"
                    + "依据非受控知识源。）";
        }
        return SYSTEM_PROMPT_HEADER + mapText;
    }

    /** fallback 模板：LLM 不可用时返回结构化确定性文本，保证业务可预期。 */
    private String fallbackTemplate(String userPrompt) {
        return "{\"fallback\": true, \"knowledgeMapLoaded\": false, "
                + "\"task\": \"" + safeToken(userPrompt) + "\", "
                + "\"message\": \"LLM unavailable; knowledge map read skipped, "
                + "returning deterministic fallback.\"}";
    }

    private static String safeToken(String value) {
        if (value == null || value.isBlank()) {
            return "UNKNOWN";
        }
        String trimmed = value.trim();
        return trimmed.length() > 64 ? trimmed.substring(0, 64) : trimmed;
    }
}
