package com.aidotnet.erp.common.test;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TenantContext多租户上下文测试")
class TenantContextTest {

    @BeforeEach
    void setUp() {
        TenantContext.clear();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("设置和获取租户ID")
    void setAndGetTenantId() {
        TenantContext.setTenantId("tenant-001");
        assertEquals("tenant-001", TenantContext.getTenantId());
    }

    @Test
    @DisplayName("清除后租户ID应为null")
    void clearTenantId() {
        TenantContext.setTenantId("tenant-001");
        TenantContext.clear();
        assertNull(TenantContext.getTenantId());
    }

    @Test
    @DisplayName("BizException应携带错误码")
    void bizExceptionCode() {
        BizException ex = new BizException("TENANT_NOT_FOUND", "租户不存在");
        assertEquals("TENANT_NOT_FOUND", ex.getCode());
        assertEquals("租户不存在", ex.getMessage());
    }
}
