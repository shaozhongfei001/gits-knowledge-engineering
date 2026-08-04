package com.gien.gits.engagement;

public record CrmWritebackCommand(
        String commandId,
        ObjectType objectType,
        Operation operation,
        String beforeValue,
        String proposedValue,
        RiskLevel riskLevel,
        boolean requiresHumanConfirm,
        String rmAction,
        String auditRef,
        String idempotencyKey) {

    /**
     * CRM回写对象类型 — 与SQL CHECK约束一致
     */
    public enum ObjectType {
        INTERACTION, CUSTOMER, CREDIT_FACILITY, COMMITMENT
    }

    /**
     * CRM回写操作类型 — 与SQL CHECK约束一致
     */
    public enum Operation {
        CREATE, UPDATE, DELETE
    }

    /**
     * 风险等级 — 与SQL CHECK约束一致
     */
    public enum RiskLevel {
        HIGH, MEDIUM, LOW
    }

    public CrmWritebackCommand {
        if (commandId == null || commandId.isBlank()) {
            throw new IllegalArgumentException("commandId is required");
        }
        if (objectType == null) {
            throw new IllegalArgumentException("objectType is required");
        }
        if (operation == null) {
            throw new IllegalArgumentException("operation is required");
        }
        if (idempotencyKey == null || idempotencyKey.length() < 16) {
            throw new IllegalArgumentException("idempotencyKey must contain at least 16 characters");
        }
        if (!requiresHumanConfirm) {
            throw new IllegalArgumentException("requiresHumanConfirm must be true — 禁令#1: AI不可直接写入CRM");
        }
    }

    /**
     * 兼容旧String参数的构造函数
     */
    @Deprecated
    public CrmWritebackCommand(
            String commandId,
            String objectType,
            String operation,
            String beforeValue,
            String proposedValue,
            String riskLevel,
            boolean requiresHumanConfirm,
            String rmAction,
            String auditRef,
            String idempotencyKey) {
        this(commandId,
             ObjectType.valueOf(objectType),
             Operation.valueOf(operation),
             beforeValue,
             proposedValue,
             riskLevel != null ? RiskLevel.valueOf(riskLevel) : null,
             requiresHumanConfirm,
             rmAction,
             auditRef,
             idempotencyKey);
    }
}
