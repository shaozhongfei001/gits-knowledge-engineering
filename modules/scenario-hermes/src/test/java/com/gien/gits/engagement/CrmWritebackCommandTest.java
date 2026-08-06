package com.gien.gits.engagement;

import com.gien.gits.engagement.CrmWritebackCommand.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CrmWritebackCommand 构造约束测试
 * 
 * 核心业务规则:
 *   禁令#1: AI不可直接写入CRM → requiresHumanConfirm必须为true
 *   幂等性: idempotencyKey至少16字符
 *   完整性: commandId/objectType/operation不可为空
 */
class CrmWritebackCommandTest {

    private CrmWritebackCommand validCommand() {
        return new CrmWritebackCommand(
            "CMD-001", ObjectType.CUSTOMER, Operation.UPDATE,
            "旧值", "新值", RiskLevel.HIGH,
            true, "人工审核通过", "AUDIT-20260806", "idempotency-key-12345678");
    }

    // ── 禁令#1: AI不可直接写入CRM ───────────────────────────────

    @Nested
    @DisplayName("禁令#1: requiresHumanConfirm必须为true")
    class HumanConfirmRuleTests {

        @Test
        @DisplayName("requiresHumanConfirm=false → 违反禁令#1，拒绝构造")
        void requiresHumanConfirmFalse_throwsException() {
            assertThatThrownBy(() -> new CrmWritebackCommand(
                "CMD-001", ObjectType.INTERACTION, Operation.CREATE,
                null, "value", RiskLevel.LOW,
                false,  // 违反禁令#1
                "review", "audit-001", "idempotency-key-12345678"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("requiresHumanConfirm must be true");
        }

        @Test
        @DisplayName("requiresHumanConfirm=true → 构造成功")
        void requiresHumanConfirmTrue_accepted() {
            CrmWritebackCommand cmd = validCommand();
            assertThat(cmd.requiresHumanConfirm()).isTrue();
        }
    }

    // ── 幂等性约束 ──────────────────────────────────────────────

    @Nested
    @DisplayName("幂等性: idempotencyKey至少16字符")
    class IdempotencyKeyTests {

        @Test
        @DisplayName("idempotencyKey少于16字符 → 拒绝")
        void shortKey_throwsException() {
            assertThatThrownBy(() -> new CrmWritebackCommand(
                "CMD-001", ObjectType.INTERACTION, Operation.CREATE,
                null, "value", RiskLevel.LOW,
                true, "review", "audit-001", "short"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("idempotencyKey must contain at least 16 characters");
        }

        @Test
        @DisplayName("idempotencyKey为null → 拒绝")
        void nullKey_throwsException() {
            assertThatThrownBy(() -> new CrmWritebackCommand(
                "CMD-001", ObjectType.INTERACTION, Operation.CREATE,
                null, "value", RiskLevel.LOW,
                true, "review", "audit-001", null))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("idempotencyKey恰好16字符 → 接受")
        void exactly16Chars_accepted() {
            CrmWritebackCommand cmd = new CrmWritebackCommand(
                "CMD-001", ObjectType.INTERACTION, Operation.CREATE,
                null, "value", RiskLevel.LOW,
                true, "review", "audit-001", "1234567890123456");
            assertThat(cmd.idempotencyKey()).isEqualTo("1234567890123456");
        }
    }

    // ── 完整性约束 ──────────────────────────────────────────────

    @Nested
    @DisplayName("完整性: 必填字段不可为空")
    class RequiredFieldsTests {

        @Test
        @DisplayName("commandId为空 → 拒绝")
        void blankCommandId_throwsException() {
            assertThatThrownBy(() -> new CrmWritebackCommand(
                "", ObjectType.INTERACTION, Operation.CREATE,
                null, "value", RiskLevel.LOW,
                true, "review", "audit-001", "idempotency-key-12345678"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("commandId is required");
        }

        @Test
        @DisplayName("commandId为null → 拒绝")
        void nullCommandId_throwsException() {
            assertThatThrownBy(() -> new CrmWritebackCommand(
                null, ObjectType.INTERACTION, Operation.CREATE,
                null, "value", RiskLevel.LOW,
                true, "review", "audit-001", "idempotency-key-12345678"))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("objectType为null → 拒绝")
        void nullObjectType_throwsException() {
            assertThatThrownBy(() -> new CrmWritebackCommand(
                "CMD-001", null, Operation.CREATE,
                null, "value", RiskLevel.LOW,
                true, "review", "audit-001", "idempotency-key-12345678"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("objectType is required");
        }

        @Test
        @DisplayName("operation为null → 拒绝")
        void nullOperation_throwsException() {
            assertThatThrownBy(() -> new CrmWritebackCommand(
                "CMD-001", ObjectType.INTERACTION, null,
                null, "value", RiskLevel.LOW,
                true, "review", "audit-001", "idempotency-key-12345678"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("operation is required");
        }
    }

    // ── 合法命令构造 ────────────────────────────────────────────

    @Test
    @DisplayName("合法命令 → 所有字段正确映射")
    void validCommand_allFieldsMapped() {
        CrmWritebackCommand cmd = validCommand();
        assertThat(cmd.commandId()).isEqualTo("CMD-001");
        assertThat(cmd.objectType()).isEqualTo(ObjectType.CUSTOMER);
        assertThat(cmd.operation()).isEqualTo(Operation.UPDATE);
        assertThat(cmd.beforeValue()).isEqualTo("旧值");
        assertThat(cmd.proposedValue()).isEqualTo("新值");
        assertThat(cmd.riskLevel()).isEqualTo(RiskLevel.HIGH);
        assertThat(cmd.requiresHumanConfirm()).isTrue();
        assertThat(cmd.rmAction()).isEqualTo("人工审核通过");
        assertThat(cmd.auditRef()).isEqualTo("AUDIT-20260806");
        assertThat(cmd.idempotencyKey()).isEqualTo("idempotency-key-12345678");
    }

}
