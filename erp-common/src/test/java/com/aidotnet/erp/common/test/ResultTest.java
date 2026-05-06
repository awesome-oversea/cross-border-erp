package com.aidotnet.erp.common.test;

import com.aidotnet.erp.common.api.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Result统一响应测试")
class ResultTest {

    @Test
    @DisplayName("ok带数据应返回成功Result")
    void okWithData() {
        Result<String> result = Result.ok("hello");
        assertTrue(result.success());
        assertEquals("SUCCESS", result.code());
        assertEquals("hello", result.data());
        assertNotNull(result.timestamp());
    }

    @Test
    @DisplayName("ok无数据应返回成功Result")
    void okWithoutData() {
        Result<Void> result = Result.ok();
        assertTrue(result.success());
        assertNull(result.data());
    }

    @Test
    @DisplayName("fail应返回失败Result")
    void failResult() {
        Result<Void> result = Result.fail("ERROR", "something wrong");
        assertFalse(result.success());
        assertEquals("ERROR", result.code());
        assertEquals("something wrong", result.message());
        assertNull(result.data());
    }
}
