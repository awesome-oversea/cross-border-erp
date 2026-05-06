package com.aidotnet.erp.fms.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.fms.domain.CostBreakdown;
import com.aidotnet.erp.fms.domain.JournalEntry;
import com.aidotnet.erp.fms.domain.JournalEntryType;
import com.aidotnet.erp.fms.domain.TaxRule;
import com.aidotnet.erp.fms.infrastructure.FmsExtStore;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * FMS扩展业务服务
 * <p>
 * 描述: FMS域扩展功能服务，提供汇率查询、外汇交易、风险预警等能力。
 *       与FinanceService互为补充，支持更细粒度的财务扩展操作。
 * </p>
 *
 * @author ERP系统
 * @see FmsExtStore
 */
@Service
public class FmsExtService {

    private final FmsExtStore extStore;

    public FmsExtService(FmsExtStore extStore) {
        this.extStore = extStore;
    }

    @Transactional
    public CostBreakdown createCostBreakdown(String tenantId, CreateCostBreakdownCommand command) {
        BigDecimal amountInBase = command.amount();
        if (command.exchangeRate() != null && command.exchangeRate().compareTo(BigDecimal.ZERO) > 0) {
            amountInBase = command.amount().multiply(command.exchangeRate()).setScale(2, java.math.RoundingMode.HALF_UP);
        }
        CostBreakdown breakdown = new CostBreakdown(UUID.randomUUID().toString(), tenantId, command.costEventId(),
                command.costType(), command.costCategory(), command.amount(), command.currency(),
                command.exchangeRate() != null ? command.exchangeRate() : BigDecimal.ONE, amountInBase,
                command.remark(), Instant.now());
        return extStore.saveCostBreakdown(breakdown);
    }

    public List<CostBreakdown> listCostBreakdownsByEvent(String tenantId, String costEventId) {
        return extStore.listCostBreakdownsByEvent(tenantId, costEventId);
    }

    public List<CostBreakdown> listCostBreakdownsByType(String tenantId, String costType) {
        return extStore.listCostBreakdownsByType(tenantId, costType);
    }

    @Transactional
    public void createJournalEntries(String tenantId, CreateJournalEntriesCommand command) {
        BigDecimal totalDebit = command.entries().stream()
                .filter(e -> e.type() == JournalEntryType.DEBIT).map(JournalEntryCommand::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredit = command.entries().stream()
                .filter(e -> e.type() == JournalEntryType.CREDIT).map(JournalEntryCommand::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new BizException("JOURNAL_NOT_BALANCED", "借贷不平衡: 借方=" + totalDebit + " 贷方=" + totalCredit);
        }
        Instant now = Instant.now();
        for (JournalEntryCommand entryCmd : command.entries()) {
            JournalEntry entry = new JournalEntry(UUID.randomUUID().toString(), tenantId, entryCmd.accountCode(),
                    entryCmd.accountName(), entryCmd.type(), entryCmd.amount(), entryCmd.currency(),
                    command.referenceType(), command.referenceId(), entryCmd.remark(), command.entryDate(), now);
            extStore.saveJournalEntry(entry);
        }
    }

    public List<JournalEntry> listJournalEntriesByReference(String tenantId, String referenceType, String referenceId) {
        return extStore.listJournalEntriesByReference(tenantId, referenceType, referenceId);
    }

    public List<JournalEntry> listJournalEntriesByAccount(String tenantId, String accountCode) {
        return extStore.listJournalEntriesByAccount(tenantId, accountCode);
    }

    @Transactional
    public TaxRule createTaxRule(String tenantId, CreateTaxRuleCommand command) {
        Instant now = Instant.now();
        TaxRule rule = new TaxRule(UUID.randomUUID().toString(), tenantId, command.countryCode(), command.taxType(),
                command.taxRate(), command.taxCategory(), true, command.effectiveFrom(), command.effectiveTo(), now, now);
        return extStore.saveTaxRule(rule);
    }

    @Transactional
    public TaxRule updateTaxRule(String tenantId, String ruleId, UpdateTaxRuleCommand command) {
        TaxRule existing = getTaxRule(tenantId, ruleId);
        return extStore.saveTaxRule(new TaxRule(existing.ruleId(), existing.tenantId(), existing.countryCode(),
                existing.taxType(), command.taxRate() != null ? command.taxRate() : existing.taxRate(),
                command.taxCategory() != null ? command.taxCategory() : existing.taxCategory(),
                existing.enabled(), existing.effectiveFrom(),
                command.effectiveTo() != null ? command.effectiveTo() : existing.effectiveTo(),
                existing.createdAt(), Instant.now()));
    }

    @Transactional
    public TaxRule toggleTaxRule(String tenantId, String ruleId, boolean enabled) {
        TaxRule existing = getTaxRule(tenantId, ruleId);
        return extStore.saveTaxRule(new TaxRule(existing.ruleId(), existing.tenantId(), existing.countryCode(),
                existing.taxType(), existing.taxRate(), existing.taxCategory(), enabled,
                existing.effectiveFrom(), existing.effectiveTo(), existing.createdAt(), Instant.now()));
    }

    public BigDecimal calculateTax(String tenantId, String countryCode, String taxType, BigDecimal amount) {
        TaxRule rule = extStore.findActiveTaxRule(tenantId, countryCode, taxType)
                .orElseThrow(() -> new BizException("TAX_RULE_NOT_FOUND", "未找到有效税率规则"));
        return amount.multiply(rule.taxRate()).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
    }

    public List<TaxRule> listTaxRules(String tenantId, String countryCode) {
        return extStore.listTaxRules(tenantId, countryCode);
    }

    public TaxRule getTaxRule(String tenantId, String ruleId) {
        return extStore.findTaxRule(tenantId, ruleId)
                .orElseThrow(() -> new BizException("TAX_RULE_NOT_FOUND", "税率规则不存在"));
    }

    public record CreateCostBreakdownCommand(String costEventId, String costType, String costCategory,
                                              BigDecimal amount, String currency, BigDecimal exchangeRate, String remark) {}
    public record CreateJournalEntriesCommand(String referenceType, String referenceId, Instant entryDate,
                                               List<JournalEntryCommand> entries) {}
    public record JournalEntryCommand(String accountCode, String accountName, JournalEntryType type,
                                       BigDecimal amount, String currency, String remark) {}
    public record CreateTaxRuleCommand(String countryCode, String taxType, BigDecimal taxRate,
                                       String taxCategory, Instant effectiveFrom, Instant effectiveTo) {}
    public record UpdateTaxRuleCommand(BigDecimal taxRate, String taxCategory, Instant effectiveTo) {}
}
