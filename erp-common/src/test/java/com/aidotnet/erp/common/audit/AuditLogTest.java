package com.aidotnet.erp.common.audit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("审计日志测试")
class AuditLogTest {

    @Test
    @DisplayName("创建审计日志应包含所有字段")
    void createAuditLog() {
        AuditLog log = AuditLog.of("T1", "U1", "admin", "CREATE", "iam:user", "创建用户", "trace-1");
        assertNotNull(log.logId());
        assertEquals("T1", log.tenantId());
        assertEquals("U1", log.userId());
        assertEquals("admin", log.username());
        assertEquals("CREATE", log.action());
        assertEquals("iam:user", log.resource());
        assertEquals("创建用户", log.description());
        assertEquals("trace-1", log.traceId());
        assertNotNull(log.occurredAt());
    }

    @Test
    @DisplayName("不同审计日志应有不同logId")
    void uniqueLogId() {
        AuditLog log1 = AuditLog.of("T1", "U1", "admin", "CREATE", "iam:user", "创建用户", "trace-1");
        AuditLog log2 = AuditLog.of("T1", "U1", "admin", "CREATE", "iam:user", "创建用户", "trace-1");
        assertNotEquals(log1.logId(), log2.logId());
    }
}
