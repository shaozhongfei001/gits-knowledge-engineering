package com.gien.gits.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gien.gits.action.port.CrmWritebackChannel;
import org.junit.jupiter.api.Test;

class CrmWritebackTest {

    @Test
    void writebackResultSuccess() {
        CrmWritebackChannel.WritebackResult result =
                CrmWritebackChannel.WritebackResult.success("MSG-001");

        assertTrue(result.success());
        assertEquals("MSG-001", result.messageId());
        assertEquals("Accepted", result.detail());
    }

    @Test
    void writebackResultFailed() {
        CrmWritebackChannel.WritebackResult result =
                CrmWritebackChannel.WritebackResult.failed("Connection refused");

        assertFalse(result.success());
        assertEquals(null, result.messageId());
        assertEquals("Connection refused", result.detail());
    }

    @Test
    void crmWritebackChannelInterface() {
        assertNotNull(CrmWritebackChannel.class);
        try {
            CrmWritebackChannel.class.getMethod("send", com.gien.gits.engagement.CrmWritebackCommand.class);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("CrmWritebackChannel should have send method", e);
        }
    }
}
