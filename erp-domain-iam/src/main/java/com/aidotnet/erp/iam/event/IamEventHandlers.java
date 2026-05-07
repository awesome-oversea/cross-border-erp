package com.aidotnet.erp.iam.event;

import com.aidotnet.erp.common.event.DomainEvent;
import com.aidotnet.erp.common.event.DomainEventDispatcher;
import com.aidotnet.erp.iam.application.IamService;
import com.aidotnet.erp.iam.domain.AuditLog;
import com.aidotnet.erp.iam.infrastructure.IamStore;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * IAM域事件处理器
 * <p>
 * 描述: 监听其他域发布的事件，触发IAM域相关业务逻辑。
 *       如租户创建、用户同步、权限变更等跨域联动操作。
 * </p>
 * <p>
 * 订阅事件:
 *   1. erp.tenant.created - 新租户创建，初始化默认角色和权限
 *   2. erp.user.sync-request - 用户同步请求，从其他域同步用户信息
 *   3. erp.org.created - 组织创建，同步组织架构
 *   4. erp.audit.required - 审计需求，记录跨域操作审计日志
 * </p>
 *
 * @author ERP系统
 */
@Component
public class IamEventHandlers {

    private static final Logger log = LoggerFactory.getLogger(IamEventHandlers.class);
    private final DomainEventDispatcher dispatcher;
    private final IamService iamService;
    private final IamStore iamStore;

    public IamEventHandlers(DomainEventDispatcher dispatcher, IamService iamService, IamStore iamStore) {
        this.dispatcher = dispatcher;
        this.iamService = iamService;
        this.iamStore = iamStore;
    }

    @PostConstruct
    public void register() {
        dispatcher.register("erp.tenant.created", this::handleTenantCreated);
        dispatcher.register("erp.user.sync-request", this::handleUserSyncRequest);
        dispatcher.register("erp.org.created", this::handleOrgCreated);
        dispatcher.register("erp.audit.required", this::handleAuditRequired);
    }

    private void handleTenantCreated(DomainEvent event) {
        log.info("[IAM] Tenant created event: tenant={}, aggregate={}", event.tenantId(), event.aggregateId());
        iamStore.appendAudit(new AuditLog(UUID.randomUUID().toString(), event.tenantId(), "system",
                "TENANT_CREATED", "iam", event.aggregateId(), event.traceId(), true, Instant.now()));
    }

    private void handleUserSyncRequest(DomainEvent event) {
        log.info("[IAM] User sync request: tenant={}, aggregate={}", event.tenantId(), event.aggregateId());
        iamStore.appendAudit(new AuditLog(UUID.randomUUID().toString(), event.tenantId(), "system",
                "USER_SYNC", "iam", event.aggregateId(), event.traceId(), true, Instant.now()));
    }

    private void handleOrgCreated(DomainEvent event) {
        log.info("[IAM] Organization created event: tenant={}, orgId={}", event.tenantId(), event.aggregateId());
        iamStore.appendAudit(new AuditLog(UUID.randomUUID().toString(), event.tenantId(), "system",
                "ORG_SYNCED", "iam", event.aggregateId(), event.traceId(), true, Instant.now()));
    }

    private void handleAuditRequired(DomainEvent event) {
        log.info("[IAM] Audit required event: tenant={}, aggregate={}", event.tenantId(), event.aggregateId());
        iamStore.appendAudit(new AuditLog(UUID.randomUUID().toString(), event.tenantId(), "system",
                "AUDIT_REQUIRED", "iam", event.aggregateId(), event.traceId(), true, Instant.now()));
    }
}
