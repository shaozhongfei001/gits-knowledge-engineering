package com.gien.gits.api.service.prompt;

/**
 * LLM Prompt模板工具类 — 为脚本生成和报告生成构建system/user prompt
 */
public final class PromptTemplate {

    private PromptTemplate() {}

    // --- 脚本生成 System Prompt ---

    /**
     * 外联脚本生成的系统提示
     */
    public static String outreachScriptSystemPrompt() {
        return """
            你是一位资深银行客户经理助理，擅长撰写外联脚本。
            根据提供的客户上下文信息，生成专业的外联通话脚本。
            输出必须是合法JSON，格式如下：
            {
              "greeting": "问候语",
              "purposeStatement": "来电目的说明",
              "keyTopics": ["话题1", "话题2"],
              "proposedActions": ["行动1", "行动2"],
              "closingStatement": "结束语",
              "toneGuidance": "语气建议"
            }
            只输出JSON，不要附加任何解释。
            """;
    }

    /**
     * 会面脚本生成的系统提示
     */
    public static String meetingScriptSystemPrompt() {
        return """
            你是一位资深银行客户经理助理，擅长撰写会面脚本。
            根据提供的客户上下文信息，生成专业的会面交流脚本。
            输出必须是合法JSON，格式如下：
            {
              "opening": "开场白",
              "agendaItems": ["议题1", "议题2"],
              "deepDiveTopics": ["深入话题1", "深入话题2"],
              "commitmentRequests": ["承诺请求1", "承诺请求2"],
              "nextSteps": ["下一步1", "下一步2"],
              "closingSummary": "总结结束语"
            }
            只输出JSON，不要附加任何解释。
            """;
    }

    // --- 报告生成 System Prompt ---

    /**
     * 报告生成的系统提示
     * @param reportType 报告类型描述
     */
    public static String reportSystemPrompt(String reportType) {
        return String.format("""
            你是一位资深银行关系报告分析师，擅长撰写%s。
            根据提供的客户关系数据，生成专业的分析报告。
            输出必须是合法JSON，格式如下：
            {
              "summary": "报告摘要",
              "keyFindings": ["发现1", "发现2"],
              "riskIndicators": ["风险1", "风险2"],
              "opportunityIndicators": ["机会1", "机会2"],
              "recommendations": ["建议1", "建议2"],
              "actionItems": ["行动项1", "行动项2"]
            }
            只输出JSON，不要附加任何解释。
            """, reportType);
    }

    // --- User Prompt 构建 ---

    /**
     * 构建外联脚本的user prompt
     */
    public static String outreachScriptUserPrompt(String customerName, String customerContext,
                                                   String kycGaps, String journeyStage) {
        return String.format("""
            客户姓名: %s
            客户上下文: %s
            KYC缺口: %s
            旅程阶段: %s
            
            请根据以上信息生成外联脚本。
            """, customerName, customerContext, kycGaps, journeyStage);
    }

    /**
     * 构建会面脚本的user prompt
     */
    public static String meetingScriptUserPrompt(String customerName, String customerContext,
                                                  String kycGaps, String journeyStage) {
        return String.format("""
            客户姓名: %s
            客户上下文: %s
            KYC缺口: %s
            旅程阶段: %s
            
            请根据以上信息生成会面脚本。
            """, customerName, customerContext, kycGaps, journeyStage);
    }

    /**
     * 构建报告生成的user prompt
     */
    public static String reportUserPrompt(String reportType, String operatingCaseId,
                                           String customerId, String contextSummary) {
        return String.format("""
            报告类型: %s
            经营案例ID: %s
            客户ID: %s
            上下文摘要: %s
            
            请根据以上信息生成%s。
            """, reportType, operatingCaseId, customerId, contextSummary, reportType);
    }
}
