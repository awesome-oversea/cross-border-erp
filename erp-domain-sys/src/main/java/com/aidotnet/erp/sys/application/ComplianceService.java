package com.aidotnet.erp.sys.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.sys.domain.ComplianceAlert;
import com.aidotnet.erp.sys.domain.ComplianceRule;
import com.aidotnet.erp.sys.domain.PlatformPolicyChange;
import com.aidotnet.erp.sys.infrastructure.SysExtStore;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 合规管理应用服务
 * <p>
 * 描述: 系统设置域合规服务，负责贸易合规检查、平台合规检查、
 *       合规规则管理等业务逻辑。确保跨境贸易符合各国法规要求。
 * </p>
 *
 * @author ERP系统
 */
@Service
public class ComplianceService {
    private static final Logger log = LoggerFactory.getLogger(ComplianceService.class);
    private final SysExtStore extStore;

    public ComplianceService(SysExtStore extStore) {
        this.extStore = extStore;
    }

    @Transactional
    public ComplianceRule createComplianceRule(String tenantId, CreateComplianceRuleCommand cmd) {
        String ruleId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        ComplianceRule rule = new ComplianceRule(ruleId, tenantId, cmd.platform(), cmd.ruleType(),
                cmd.ruleName(), cmd.description(), cmd.severity(), true, now, now);
        extStore.saveComplianceRule(rule);
        log.info("Created compliance rule: id={} tenant={} platform={} type={}", ruleId, tenantId, cmd.platform(), cmd.ruleType());
        return rule;
    }

    @Transactional
    public ComplianceRule updateComplianceRule(String tenantId, String ruleId, UpdateComplianceRuleCommand cmd) {
        ComplianceRule existing = extStore.findComplianceRule(tenantId, ruleId)
                .orElseThrow(() -> new BizException("RULE_NOT_FOUND", "合规规则不存在: " + ruleId));
        ComplianceRule updated = new ComplianceRule(existing.ruleId(), existing.tenantId(), existing.platform(),
                existing.ruleType(), cmd.ruleName() != null ? cmd.ruleName() : existing.ruleName(),
                cmd.description() != null ? cmd.description() : existing.description(),
                cmd.severity() != null ? cmd.severity() : existing.severity(),
                cmd.enabled() != null ? cmd.enabled() : existing.enabled(),
                existing.createdAt(), Instant.now());
        extStore.saveComplianceRule(updated);
        return updated;
    }

    public List<ComplianceRule> listComplianceRules(String tenantId, String platform, String ruleType) {
        return extStore.findComplianceRules(tenantId, platform, ruleType);
    }

    @Transactional
    public ComplianceAlert createAlert(String tenantId, CreateAlertCommand cmd) {
        String alertId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        ComplianceAlert alert = new ComplianceAlert(alertId, tenantId, cmd.platform(), cmd.ruleId(),
                cmd.alertType(), cmd.title(), cmd.description(), cmd.severity(),
                ComplianceAlert.AlertStatus.OPEN.name(), cmd.referenceType(), cmd.referenceId(),
                now, null);
        extStore.saveComplianceAlert(alert);
        log.warn("Compliance alert created: id={} tenant={} severity={} title={}", alertId, tenantId, cmd.severity(), cmd.title());
        return alert;
    }

    @Transactional
    public ComplianceAlert resolveAlert(String tenantId, String alertId, String resolution) {
        ComplianceAlert existing = extStore.findComplianceAlert(tenantId, alertId)
                .orElseThrow(() -> new BizException("ALERT_NOT_FOUND", "合规预警不存在: " + alertId));
        ComplianceAlert resolved = new ComplianceAlert(existing.alertId(), existing.tenantId(), existing.platform(),
                existing.ruleId(), existing.alertType(), existing.title(), existing.description(),
                existing.severity(), ComplianceAlert.AlertStatus.RESOLVED.name(),
                existing.referenceType(), existing.referenceId(), existing.detectedAt(), Instant.now());
        extStore.saveComplianceAlert(resolved);
        return resolved;
    }

    public List<ComplianceAlert> listAlerts(String tenantId, String platform, String status) {
        return extStore.findComplianceAlerts(tenantId, platform, status);
    }

    @Transactional
    public PlatformPolicyChange recordPolicyChange(RecordPolicyChangeCommand cmd) {
        String changeId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        PlatformPolicyChange change = new PlatformPolicyChange(changeId, cmd.platform(), cmd.policyArea(),
                cmd.changeTitle(), cmd.changeSummary(), cmd.impactLevel(), cmd.sourceUrl(),
                cmd.effectiveDate(), now);
        extStore.savePlatformPolicyChange(change);
        log.info("Platform policy change recorded: id={} platform={} area={}", changeId, cmd.platform(), cmd.policyArea());
        return change;
    }

    public List<PlatformPolicyChange> listPolicyChanges(String platform, String policyArea) {
        return extStore.findPlatformPolicyChanges(platform, policyArea);
    }

    @Transactional
    public void scanCompliance(String tenantId, String platform) {
        log.info("Starting compliance scan for tenant={} platform={}", tenantId, platform);
        List<ComplianceRule> rules = extStore.findComplianceRules(tenantId, platform, null);
        for (ComplianceRule rule : rules) {
            if (!rule.enabled()) continue;
            try {
                scanRule(tenantId, rule);
            } catch (Exception e) {
                log.error("Error scanning rule {}: {}", rule.ruleId(), e.getMessage());
            }
        }
        log.info("Compliance scan completed for tenant={} platform={} rules={}", tenantId, platform, rules.size());
    }

    private void scanRule(String tenantId, ComplianceRule rule) {
        log.debug("Scanning compliance rule: id={} type={}", rule.ruleId(), rule.ruleType());
    }

    public record CreateComplianceRuleCommand(String platform, String ruleType, String ruleName,
                                              String description, String severity) {}
    public record UpdateComplianceRuleCommand(String ruleName, String description, String severity, Boolean enabled) {}
    public record CreateAlertCommand(String platform, String ruleId, String alertType, String title,
                                     String description, String severity, String referenceType, String referenceId) {}
    public record RecordPolicyChangeCommand(String platform, String policyArea, String changeTitle,
                                            String changeSummary, String impactLevel, String sourceUrl,
                                            Instant effectiveDate) {}
}
