package com.aidotnet.erp.sys.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.sys.application.SysExtService;
import com.aidotnet.erp.sys.application.SysExtService.RecordOperationLogCommand;
import com.aidotnet.erp.sys.domain.OperationLog;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sys/api/in/v1/audit")
public class AuditLogController {

    private final SysExtService sysExtService;

    public AuditLogController(SysExtService sysExtService) {
        this.sysExtService = sysExtService;
    }

    @PostMapping("/log")
    public Result<OperationLog> recordLog(@Valid @RequestBody RecordOperationLogRequest request) {
        return Result.ok(sysExtService.recordOperationLog(currentTenant(), new RecordOperationLogCommand(
                request.userId(), request.username(), request.module(), request.action(),
                request.targetObjectType(), request.targetObjectId(), request.detail(),
                request.ipAddress(), request.userAgent(), request.traceId())));
    }

    @GetMapping("/logs")
    public Result<List<OperationLog>> listLogs(@RequestParam(required = false) String module,
                                                @RequestParam(required = false) String userId) {
        return Result.ok(sysExtService.listOperationLogs(currentTenant(), module, userId));
    }

    @GetMapping("/logs/trace/{traceId}")
    public Result<List<OperationLog>> listLogsByTraceId(@PathVariable String traceId) {
        return Result.ok(sysExtService.listOperationLogsByTraceId(currentTenant(), traceId));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record RecordOperationLogRequest(
            @NotBlank String userId, @NotBlank String username, @NotBlank String module,
            @NotBlank String action, String targetObjectType, String targetObjectId,
            String detail, String ipAddress, String userAgent, String traceId) {}
}
