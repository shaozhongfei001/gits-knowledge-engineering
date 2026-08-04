package com.gien.gits.action.port;

import com.gien.gits.engagement.CrmWritebackCommand;

/**
 * CRM回写通道端口 — 将回写命令发送到外部CRM系统。
 * 实现可以是HTTP REST调用或仅日志记录（开发/测试用）。
 */
public interface CrmWritebackChannel {

    WritebackResult send(CrmWritebackCommand command);

    /**
     * 回写结果 — 记录是否成功、消息ID和详情
     */
    record WritebackResult(boolean success, String messageId, String detail) {
        public static WritebackResult success(String messageId) {
            return new WritebackResult(true, messageId, "Accepted");
        }
        public static WritebackResult failed(String reason) {
            return new WritebackResult(false, null, reason);
        }
    }
}
