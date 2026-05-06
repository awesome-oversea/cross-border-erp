package com.aidotnet.erp.sys.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.sys.domain.ContentAuditResult;
import com.aidotnet.erp.sys.domain.ContentAuditRule;
import com.aidotnet.erp.sys.domain.ContentAuditViolation;
import com.aidotnet.erp.sys.domain.TrademarkRecord;
import com.aidotnet.erp.sys.infrastructure.SysExtStore;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContentAuditService {

    private final SysExtStore extStore;

    public ContentAuditService(SysExtStore extStore) {
        this.extStore = extStore;
    }

    @Transactional
    public ContentAuditRule createRule(String tenantId, String ruleType, String category,
                                        String keyword, String keywordPattern, int severity,
                                        String action, String replacement, String description,
                                        boolean enabled, List<String> applicablePlatforms) {
        validateRuleParams(ruleType, severity, action);
        if (keywordPattern != null && !keywordPattern.isBlank()) {
            try {
                Pattern.compile(keywordPattern);
            } catch (Exception e) {
                throw new BizException("INVALID_PATTERN", "正则表达式不合法: " + e.getMessage());
            }
        }
        Instant now = Instant.now();
        ContentAuditRule rule = new ContentAuditRule(
                UUID.randomUUID().toString(), tenantId, ruleType, category,
                keyword, keywordPattern, severity, action, replacement,
                description, enabled, applicablePlatforms, now, now);
        extStore.saveContentAuditRule(rule);
        return rule;
    }

    @Transactional
    public ContentAuditRule updateRule(String tenantId, String ruleId, String keyword,
                                        String keywordPattern, int severity, String action,
                                        String replacement, String description, boolean enabled,
                                        List<String> applicablePlatforms) {
        ContentAuditRule existing = extStore.findContentAuditRule(tenantId, ruleId)
                .orElseThrow(() -> new BizException("RULE_NOT_FOUND", "审核规则不存在"));
        ContentAuditRule updated = new ContentAuditRule(
                existing.ruleId(), existing.tenantId(), existing.ruleType(), existing.category(),
                keyword != null ? keyword : existing.keyword(),
                keywordPattern != null ? keywordPattern : existing.keywordPattern(),
                severity >= 0 ? severity : existing.severity(),
                action != null ? action : existing.action(),
                replacement != null ? replacement : existing.replacement(),
                description != null ? description : existing.description(),
                enabled, applicablePlatforms != null ? applicablePlatforms : existing.applicablePlatforms(),
                existing.createdAt(), Instant.now());
        extStore.saveContentAuditRule(updated);
        return updated;
    }

    public ContentAuditResult auditText(String tenantId, String text, String sourceType,
                                         String sourceId, List<String> platforms) {
        List<ContentAuditRule> rules = extStore.listContentAuditRules(tenantId, "TEXT", true);
        if (platforms != null && !platforms.isEmpty()) {
            rules = rules.stream()
                    .filter(r -> r.applicablePlatforms() == null || r.applicablePlatforms().isEmpty()
                            || r.applicablePlatforms().stream().anyMatch(platforms::contains))
                    .collect(Collectors.toList());
        }
        List<ContentAuditViolation> violations = new ArrayList<>();
        String processedText = text;
        for (ContentAuditRule rule : rules) {
            List<ContentAuditViolation> found = matchRule(rule, processedText);
            if (!found.isEmpty()) {
                violations.addAll(found);
                if ("REPLACE".equals(rule.action()) && rule.replacement() != null) {
                    processedText = applyReplacement(processedText, rule, found);
                }
            }
        }
        boolean passed = violations.stream().noneMatch(v -> v.severity() >= 3);
        return new ContentAuditResult(
                UUID.randomUUID().toString(), tenantId, "TEXT", sourceType, sourceId,
                passed, violations, "SYSTEM", Instant.now());
    }

    public ContentAuditResult auditImage(String tenantId, String imageUrl, String sourceType,
                                          String sourceId) {
        List<ContentAuditRule> rules = extStore.listContentAuditRules(tenantId, "IMAGE", true);
        List<ContentAuditViolation> violations = new ArrayList<>();
        for (ContentAuditRule rule : rules) {
            if ("IMAGE_WATERMARK".equals(rule.category()) && imageUrl.contains("watermark")) {
                violations.add(new ContentAuditViolation(
                        rule.ruleId(), rule.ruleType(), rule.category(),
                        "watermark detected", rule.severity(), rule.action(), null, -1));
            }
        }
        boolean passed = violations.isEmpty();
        return new ContentAuditResult(
                UUID.randomUUID().toString(), tenantId, "IMAGE", sourceType, sourceId,
                passed, violations, "SYSTEM", Instant.now());
    }

    public ContentAuditResult auditTrademark(String tenantId, String text, String sourceType,
                                              String sourceId) {
        List<TrademarkRecord> trademarks = extStore.listTrademarkRecords(tenantId, "ACTIVE");
        List<ContentAuditViolation> violations = new ArrayList<>();
        for (TrademarkRecord tm : trademarks) {
            if (text.toLowerCase().contains(tm.trademarkName().toLowerCase())) {
                ContentAuditRule rule = findOrCreateTrademarkRule(tenantId, tm);
                violations.add(new ContentAuditViolation(
                        rule.ruleId(), "TRADEMARK", tm.niceClasses().toString(),
                        tm.trademarkName(), 3, "BLOCK", null,
                        text.toLowerCase().indexOf(tm.trademarkName().toLowerCase())));
            }
        }
        boolean passed = violations.isEmpty();
        return new ContentAuditResult(
                UUID.randomUUID().toString(), tenantId, "TRADEMARK", sourceType, sourceId,
                passed, violations, "SYSTEM", Instant.now());
    }

    @Transactional
    public TrademarkRecord registerTrademark(String tenantId, String trademarkName,
                                              String registrationNumber, String jurisdiction,
                                              List<String> niceClasses, String owner,
                                              Instant registeredAt, Instant expiresAt) {
        if (trademarkName == null || trademarkName.isBlank()) {
            throw new BizException("INVALID_TRADEMARK", "商标名称不能为空");
        }
        Instant now = Instant.now();
        TrademarkRecord record = new TrademarkRecord(
                UUID.randomUUID().toString(), tenantId, trademarkName, registrationNumber,
                jurisdiction, niceClasses, owner, "ACTIVE", registeredAt, expiresAt, now, now);
        extStore.saveTrademarkRecord(record);
        return record;
    }

    public List<ContentAuditRule> listRules(String tenantId, String ruleType) {
        return extStore.listContentAuditRules(tenantId, ruleType, null);
    }

    public List<ContentAuditResult> listAuditResults(String tenantId, String sourceType) {
        return extStore.listContentAuditResults(tenantId, sourceType);
    }

    private List<ContentAuditViolation> matchRule(ContentAuditRule rule, String text) {
        List<ContentAuditViolation> violations = new ArrayList<>();
        if (rule.keywordPattern() != null && !rule.keywordPattern().isBlank()) {
            Pattern pattern = Pattern.compile(rule.keywordPattern(), Pattern.CASE_INSENSITIVE);
            java.util.regex.Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                violations.add(new ContentAuditViolation(
                        rule.ruleId(), rule.ruleType(), rule.category(),
                        matcher.group(), rule.severity(), rule.action(),
                        rule.replacement(), matcher.start()));
            }
        } else if (rule.keyword() != null && !rule.keyword().isBlank()) {
            String lowerText = text.toLowerCase();
            String lowerKeyword = rule.keyword().toLowerCase();
            int index = lowerText.indexOf(lowerKeyword);
            while (index >= 0) {
                violations.add(new ContentAuditViolation(
                        rule.ruleId(), rule.ruleType(), rule.category(),
                        text.substring(index, index + rule.keyword().length()),
                        rule.severity(), rule.action(), rule.replacement(), index));
                index = lowerText.indexOf(lowerKeyword, index + 1);
            }
        }
        return violations;
    }

    private String applyReplacement(String text, ContentAuditRule rule, List<ContentAuditViolation> violations) {
        String result = text;
        for (ContentAuditViolation v : violations) {
            if (v.ruleId().equals(rule.ruleId())) {
                result = result.substring(0, v.position()) + rule.replacement()
                        + result.substring(v.position() + v.matchedContent().length());
            }
        }
        return result;
    }

    private ContentAuditRule findOrCreateTrademarkRule(String tenantId, TrademarkRecord tm) {
        return extStore.listContentAuditRules(tenantId, "TRADEMARK", true).stream()
                .filter(r -> tm.trademarkName().equalsIgnoreCase(r.keyword()))
                .findFirst()
                .orElseGet(() -> {
                    Instant now = Instant.now();
                    ContentAuditRule rule = new ContentAuditRule(
                            UUID.randomUUID().toString(), tenantId, "TRADEMARK", "TRADEMARK_INFRINGEMENT",
                            tm.trademarkName(), null, 3, "BLOCK", null,
                            "商标侵权: " + tm.trademarkName(), true, null, now, now);
                    extStore.saveContentAuditRule(rule);
                    return rule;
                });
    }

    private void validateRuleParams(String ruleType, int severity, String action) {
        if (ruleType == null || ruleType.isBlank()) {
            throw new BizException("INVALID_RULE_TYPE", "规则类型不能为空");
        }
        if (severity < 1 || severity > 5) {
            throw new BizException("INVALID_SEVERITY", "严重程度必须在1-5之间");
        }
        if (!List.of("BLOCK", "REPLACE", "FLAG", "WARN").contains(action)) {
            throw new BizException("INVALID_ACTION", "处理动作必须是BLOCK/REPLACE/FLAG/WARN之一");
        }
    }
}
