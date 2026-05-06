package com.aidotnet.erp.sys.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.sys.domain.BusinessAlert;
import com.aidotnet.erp.sys.infrastructure.SysExtStore;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 业务告警管理应用服务
 * <p>
 * 系统设置域业务告警服务，负责业务告警的创建/确认/解决/忽略/查询等业务逻辑。
 * 支持多级别(INFO/WARNING/CRITICAL)告警和多渠道通知。
 * </p>
 *
 * <pre>
 * 告警状态流转: OPEN -> ACKNOWLEDGED -> RESOLVED/DISMISSED
 * </pre>
 *
 * @author ERP系统
 */
@Service
public class BusinessAlertService {

    private static final Logger log = LoggerFactory.getLogger(BusinessAlertService.class);

    private final SysExtStore extStore;

    public BusinessAlertService(SysExtStore extStore) {
        this.extStore = extStore;
    }

    /**
     * 创建业务告警
     *
     * @param tenantId 租户ID
     * @param command  告警创建命令
     * @return 创建的告警对象
     */
    @Transactional
    public BusinessAlert createAlert(String tenantId, CreateAlertCommand command) {
        Instant now = Instant.now();
        BusinessAlert alert = new BusinessAlert(
                UUID.randomUUID().toString(), tenantId, command.alertType(), command.alertCode(),
                command.severity(), command.domain(), command.title(), command.description(),
                command.sourceType(), command.sourceId(), "OPEN", null, null,
                now, null, now, now);
        BusinessAlert saved = extStore.saveBusinessAlert(alert);
        log.info("Business alert created: type={}, severity={}, title={}", command.alertType(), command.severity(), command.title());
        return saved;
    }

    /**
     * 确认告警(分配处理人)
     *
     * @param tenantId    租户ID
     * @param alertId     告警ID
     * @param assignedTo  分配给的处理人
     * @return 更新后的告警对象
     */
    @Transactional
    public BusinessAlert acknowledgeAlert(String tenantId, String alertId, String assignedTo) {
        BusinessAlert existing = getAlert(tenantId, alertId);
        if (!"OPEN".equals(existing.status())) {
            throw new BizException("ALERT_NOT_OPEN", "只有OPEN状态的预警可以确认");
        }
        Instant now = Instant.now();
        BusinessAlert updated = new BusinessAlert(
                existing.alertId(), existing.tenantId(), existing.alertType(), existing.alertCode(),
                existing.severity(), existing.domain(), existing.title(), existing.description(),
                existing.sourceType(), existing.sourceId(), "ACKNOWLEDGED", assignedTo, null,
                existing.occurredAt(), existing.resolvedAt(), existing.createdAt(), now);
        return extStore.saveBusinessAlert(updated);
    }

    /**
     * 解决告警
     *
     * @param tenantId   租户ID
     * @param alertId    告警ID
     * @param resolution 解决说明
     * @return 更新后的告警对象
     */
    @Transactional
    public BusinessAlert resolveAlert(String tenantId, String alertId, String resolution) {
        BusinessAlert existing = getAlert(tenantId, alertId);
        if ("RESOLVED".equals(existing.status()) || "DISMISSED".equals(existing.status())) {
            throw new BizException("ALERT_ALREADY_RESOLVED", "预警已处理");
        }
        Instant now = Instant.now();
        BusinessAlert updated = new BusinessAlert(
                existing.alertId(), existing.tenantId(), existing.alertType(), existing.alertCode(),
                existing.severity(), existing.domain(), existing.title(), existing.description(),
                existing.sourceType(), existing.sourceId(), "RESOLVED", existing.assignedTo(), resolution,
                existing.occurredAt(), now, existing.createdAt(), now);
        return extStore.saveBusinessAlert(updated);
    }

    /**
     * 忽略告警
     *
     * @param tenantId 租户ID
     * @param alertId  告警ID
     * @return 更新后的告警对象
     */
    @Transactional
    public BusinessAlert dismissAlert(String tenantId, String alertId) {
        BusinessAlert existing = getAlert(tenantId, alertId);
        Instant now = Instant.now();
        BusinessAlert updated = new BusinessAlert(
                existing.alertId(), existing.tenantId(), existing.alertType(), existing.alertCode(),
                existing.severity(), existing.domain(), existing.title(), existing.description(),
                existing.sourceType(), existing.sourceId(), "DISMISSED", existing.assignedTo(), "已忽略",
                existing.occurredAt(), now, existing.createdAt(), now);
        return extStore.saveBusinessAlert(updated);
    }

    /**
     * 查询单个告警
     *
     * @param tenantId 租户ID
     * @param alertId  告警ID
     * @return 告警对象
     */
    public BusinessAlert getAlert(String tenantId, String alertId) {
        return extStore.findBusinessAlert(tenantId, alertId)
                .orElseThrow(() -> new BizException("ALERT_NOT_FOUND", "预警不存在"));
    }

    /**
     * 查询告警列表
     *
     * @param tenantId  租户ID
     * @param alertType 告警类型(可选)
     * @param status    告警状态(可选)
     * @return 告警列表
     */
    public List<BusinessAlert> listAlerts(String tenantId, String alertType, String status) {
        return extStore.listBusinessAlerts(tenantId, alertType, status);
    }

    /**
     * 查询未处理告警列表
     *
     * @param tenantId 租户ID
     * @return 未处理告警列表
     */
    public List<BusinessAlert> listOpenAlerts(String tenantId) {
        return extStore.listBusinessAlerts(tenantId, null, "OPEN");
    }

    /**
     * 统计未处理告警数量
     *
     * @param tenantId 租户ID
     * @return 未处理告警数量
     */
    public long countOpenAlerts(String tenantId) {
        return extStore.listBusinessAlerts(tenantId, null, "OPEN").size();
    }

    /**
     * 创建告警命令
     *
     * @param alertType   告警类型
     * @param alertCode   告警编码
     * @param severity    严重程度(INFO/WARNING/CRITICAL)
     * @param domain      所属业务域
     * @param title       告警标题
     * @param description 告警描述
     * @param sourceType  来源类型
     * @param sourceId    来源ID
     */
    public record CreateAlertCommand(String alertType, String alertCode, String severity,
                                     String domain, String title, String description,
                                     String sourceType, String sourceId) {}
}
