package com.gien.gits.adapter.llm;

import com.gien.gits.api.metrics.BusinessMetrics;
import com.gien.gits.engagement.port.LlmClient;
import com.gien.gits.engagement.port.LlmClientException;

import java.util.UUID;

/**
 * Mock LLM客户端 — 根据systemPrompt关键词返回预定义的结构化JSON。
 * 用于开发和测试环境，模拟50-200ms延迟。
 * P11 G5/G6: 增加外联脚本、会面脚本、报告生成的场景识别
 */
public class MockLlmClient implements LlmClient {

    private final BusinessMetrics businessMetrics;

    public MockLlmClient(BusinessMetrics businessMetrics) {
        this.businessMetrics = businessMetrics;
    }

    @Override
    public String complete(String systemPrompt, String userPrompt) {
        // 模拟网络延迟 50-200ms
        try {
            Thread.sleep(50 + (long) (Math.random() * 150));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LlmClientException("Mock LLM interrupted", e);
        }

        if (systemPrompt == null || userPrompt == null) {
            throw new LlmClientException("systemPrompt and userPrompt must not be null");
        }

        String promptLower = systemPrompt.toLowerCase();
        String response;

        // 语义提取场景: 返回包含patterns数组的JSON
        if (promptLower.contains("语义") || promptLower.contains("pattern")
                || promptLower.contains("提取") || promptLower.contains("extract")) {
            response = buildSemanticExtractionResponse(userPrompt);
        }
        // 外联脚本场景: 返回外联脚本结构化JSON
        else if (promptLower.contains("外联") || promptLower.contains("outreach")) {
            response = buildOutreachScriptResponse();
        }
        // 会面脚本场景: 返回会面脚本结构化JSON
        else if (promptLower.contains("会面") || promptLower.contains("meeting")) {
            response = buildMeetingScriptResponse();
        }
        // 报告生成场景: 返回报告结构化JSON
        else if (promptLower.contains("报告") || promptLower.contains("report")
                || promptLower.contains("分析师")) {
            response = buildReportResponse();
        }
        // 脚本生成场景(通用): 返回包含content字段的JSON
        else if (promptLower.contains("脚本") || promptLower.contains("script")
                || promptLower.contains("生成") || promptLower.contains("generate")) {
            response = buildScriptGenerationResponse(userPrompt);
        }
        // 通用场景: 返回简单确认
        else {
            response = buildGenericResponse(userPrompt);
        }

        businessMetrics.recordLlmCall("mock", "success");
        return response;
    }

    private String buildSemanticExtractionResponse(String userPrompt) {
        String id1 = "EXT-" + UUID.randomUUID().toString().substring(0, 8);
        String id2 = "EXT-" + UUID.randomUUID().toString().substring(0, 8);
        return """
                {
                  "patterns": [
                    {
                      "objectId": "%s",
                      "type": "OPPORTUNITY_SIGNAL",
                      "claimType": "FINANCING_NEED",
                      "content": "客户提及融资需求",
                      "speaker": "客户方",
                      "evidenceRef": "TR-RAW",
                      "status": "DETECTED",
                      "confidence": 0.75,
                      "notFact": true,
                      "requiresReconciliation": true,
                      "conflictWith": null,
                      "nextQuestion": "确认融资需求的具体金额和用途"
                    },
                    {
                      "objectId": "%s",
                      "type": "CUSTOMER_COMMITMENT",
                      "claimType": "MATERIAL_PROVIDE",
                      "content": "客户表示将提供相关材料",
                      "speaker": "客户方",
                      "evidenceRef": "TR-RAW",
                      "status": "DETECTED",
                      "confidence": 0.80,
                      "notFact": false,
                      "requiresReconciliation": false,
                      "conflictWith": null,
                      "nextQuestion": "确认材料清单和提交时间"
                    }
                  ]
                }""".formatted(id1, id2);
    }

    private String buildOutreachScriptResponse() {
        return """
                {
                  "greeting": "尊敬的客户您好，感谢您对我们银行的长期支持。",
                  "purposeStatement": "今天致电是想和您沟通一下近期的合作进展和新方案。",
                  "keyTopics": ["融资需求确认", "KYC信息更新", "新产品方案介绍"],
                  "proposedActions": ["安排面谈确认融资细节", "收集最新经营数据", "发送产品方案资料"],
                  "closingStatement": "感谢您的时间，我们会尽快跟进相关事项，后续保持联系。",
                  "toneGuidance": "专业、真诚、以客户需求为导向"
                }""";
    }

    private String buildMeetingScriptResponse() {
        return """
                {
                  "opening": "感谢您百忙之中抽出时间，今天主要就近期合作方向做个沟通。",
                  "agendaItems": ["经营状况了解", "KYC信息补全", "业务机会讨论", "产品方案介绍"],
                  "deepDiveTopics": ["融资需求具体金额和用途", "近期经营变化和规划"],
                  "commitmentRequests": ["提供最新财务报表", "确认融资需求时间表"],
                  "nextSteps": ["整理会面纪要", "准备融资方案", "安排下次跟进"],
                  "closingSummary": "本次会面确认了客户融资需求和KYC信息补全计划，将在一周内提供方案。"
                }""";
    }

    private String buildReportResponse() {
        return """
                {
                  "summary": "客户关系整体稳定，存在融资需求和KYC信息缺口，建议加强跟进。",
                  "keyFindings": ["客户提及融资需求", "KYC信息存在2项缺口", "风险等级无变化"],
                  "riskIndicators": ["中风险客户，需关注经营数据变化"],
                  "opportunityIndicators": ["融资需求信号置信度较高", "产品交叉销售机会"],
                  "recommendations": ["尽快补全KYC信息", "推进融资方案", "安排下次拜访"],
                  "actionItems": ["更新KYC档案", "准备融资方案", "录入承诺事项"]
                }""";
    }

    private String buildScriptGenerationResponse(String userPrompt) {
        return """
                {
                  "content": "尊敬的客户您好，感谢您对我们银行的支持。根据您的业务发展需求，我们为您准备了以下方案建议...",
                  "metadata": {
                    "model": "mock-llm",
                    "generatedAt": "%s"
                  }
                }""".formatted(java.time.Instant.now().toString());
    }

    private String buildGenericResponse(String userPrompt) {
        return """
                {
                  "response": "Mock LLM response for the given prompt.",
                  "model": "mock-llm"
                }""";
    }
}
