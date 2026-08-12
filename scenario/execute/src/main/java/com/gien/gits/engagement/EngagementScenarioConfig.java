package com.gien.gits.engagement;

/** 持续经营场景配置 — 华东精工客户经营闭环 */
public record EngagementScenarioConfig(
        String scenarioId,
        String scenarioName,
        String customerId,
        String rmId,
        String rmName) {

    public static final String SCENARIO_ID = "RM-CONTINUOUS-ENGAGEMENT-HDEG-001";
    public static final String CUSTOMER_ID = "CUST-CORP-0001";
    public static final String RM_ID = "RM-ZW-001";
    public static final String RM_NAME = "张伟";

    public EngagementScenarioConfig {
        if (scenarioId == null || scenarioId.isBlank()) {
            throw new IllegalArgumentException("scenarioId is required");
        }
        if (scenarioName == null || scenarioName.isBlank()) {
            throw new IllegalArgumentException("scenarioName is required");
        }
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("customerId is required");
        }
    }
}
