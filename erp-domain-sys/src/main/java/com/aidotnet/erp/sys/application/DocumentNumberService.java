package com.aidotnet.erp.sys.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.sys.domain.DocumentNumberRule;
import com.aidotnet.erp.sys.domain.DocumentNumberSegment;
import com.aidotnet.erp.sys.infrastructure.SysExtStore;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 单据编号中心应用服务
 * <p>
 * 描述: 系统设置域单据编号生成服务，负责各业务域单据编号的统一生成和管理。
 *       支持按租户+编号规则+日期维度生成连续编号，确保编号唯一性和可追溯性。
 * </p>
 * <p>
 * 核心能力:
 *   1. 编号规则管理 - 创建/更新编号规则，定义前缀/日期格式/序号长度
 *   2. 编号生成 - 按规则生成唯一编号，格式: 前缀+日期+序号(如PO20260101001)
 *   3. 序号重置 - 每日自动重置序号，确保编号紧凑
 * </p>
 *
 * @author ERP系统
 * @see DocumentNumberRule
 */
@Service
public class DocumentNumberService {
    private final SysExtStore extStore;

    public DocumentNumberService(SysExtStore extStore) {
        this.extStore = extStore;
    }

    @Transactional
    public DocumentNumberRule createRule(String tenantId, CreateNumberRuleCommand command) {
        Instant now = Instant.now();
        DocumentNumberRule rule = new DocumentNumberRule(
                UUID.randomUUID().toString(), tenantId, command.ruleName(), command.documentType(),
                command.prefix(), command.dateFormat(), command.sequenceLength(), 0,
                command.step(), command.resetDaily(), command.resetMonthly(), command.resetYearly(),
                now, now, now);
        return extStore.saveDocumentNumberRule(rule);
    }

    @Transactional
    public DocumentNumberRule updateRule(String tenantId, String ruleId, CreateNumberRuleCommand command) {
        DocumentNumberRule existing = getRule(tenantId, ruleId);
        Instant now = Instant.now();
        DocumentNumberRule updated = new DocumentNumberRule(
                existing.ruleId(), existing.tenantId(), command.ruleName(), command.documentType(),
                command.prefix(), command.dateFormat(), command.sequenceLength(), existing.currentSequence(),
                command.step(), command.resetDaily(), command.resetMonthly(), command.resetYearly(),
                existing.lastResetAt(), existing.createdAt(), now);
        return extStore.saveDocumentNumberRule(updated);
    }

    public DocumentNumberRule getRule(String tenantId, String ruleId) {
        return extStore.findDocumentNumberRule(tenantId, ruleId)
                .orElseThrow(() -> new BizException("RULE_NOT_FOUND", "编号规则不存在"));
    }

    public DocumentNumberRule getRuleByDocumentType(String tenantId, String documentType) {
        return extStore.findDocumentNumberRuleByType(tenantId, documentType)
                .orElseThrow(() -> new BizException("RULE_NOT_FOUND", "编号规则不存在: " + documentType));
    }

    public List<DocumentNumberRule> listRules(String tenantId) {
        return extStore.listDocumentNumberRules(tenantId);
    }

    @Transactional
    public synchronized String generateNumber(String tenantId, String documentType) {
        DocumentNumberRule rule = getRuleByDocumentType(tenantId, documentType);
        rule = checkAndReset(rule);
        long nextSeq = rule.currentSequence() + rule.step();
        String datePart = "";
        if (rule.dateFormat() != null && !rule.dateFormat().isEmpty()) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(rule.dateFormat());
                datePart = LocalDate.now().format(formatter);
            } catch (Exception e) {
                datePart = "";
            }
        }
        String seqPart = String.format("%0" + rule.sequenceLength() + "d", nextSeq);
        String fullNumber = (rule.prefix() != null ? rule.prefix() : "") + datePart + seqPart;
        Instant now = Instant.now();
        DocumentNumberRule updated = new DocumentNumberRule(
                rule.ruleId(), rule.tenantId(), rule.ruleName(), rule.documentType(),
                rule.prefix(), rule.dateFormat(), rule.sequenceLength(), nextSeq,
                rule.step(), rule.resetDaily(), rule.resetMonthly(), rule.resetYearly(),
                rule.lastResetAt(), rule.createdAt(), now);
        extStore.saveDocumentNumberRule(updated);
        DocumentNumberSegment segment = new DocumentNumberSegment(
                UUID.randomUUID().toString(), tenantId, documentType, datePart, nextSeq, fullNumber, now);
        extStore.saveDocumentNumberSegment(segment);
        return fullNumber;
    }

    @Transactional
    public List<String> generateBatch(String tenantId, String documentType, int count) {
        java.util.ArrayList<String> numbers = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            numbers.add(generateNumber(tenantId, documentType));
        }
        return numbers;
    }

    private DocumentNumberRule checkAndReset(DocumentNumberRule rule) {
        Instant now = Instant.now();
        LocalDate today = LocalDate.now();
        LocalDate lastResetDate = rule.lastResetAt().atZone(ZoneId.systemDefault()).toLocalDate();
        boolean needsReset = false;
        if (rule.resetDaily() && !today.equals(lastResetDate)) {
            needsReset = true;
        } else if (rule.resetMonthly() && (today.getYear() != lastResetDate.getYear() || today.getMonthValue() != lastResetDate.getMonthValue())) {
            needsReset = true;
        } else if (rule.resetYearly() && today.getYear() != lastResetDate.getYear()) {
            needsReset = true;
        }
        if (needsReset) {
            return new DocumentNumberRule(
                    rule.ruleId(), rule.tenantId(), rule.ruleName(), rule.documentType(),
                    rule.prefix(), rule.dateFormat(), rule.sequenceLength(), 0,
                    rule.step(), rule.resetDaily(), rule.resetMonthly(), rule.resetYearly(),
                    now, rule.createdAt(), now);
        }
        return rule;
    }

    public record CreateNumberRuleCommand(
            String ruleName, String documentType, String prefix, String dateFormat,
            int sequenceLength, long step, boolean resetDaily, boolean resetMonthly, boolean resetYearly) {}
}
