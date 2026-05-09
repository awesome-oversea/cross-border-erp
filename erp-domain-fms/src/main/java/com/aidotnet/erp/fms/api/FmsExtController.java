package com.aidotnet.erp.fms.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.fms.application.FmsExtService;
import com.aidotnet.erp.fms.application.FmsExtService.CreateCostBreakdownCommand;
import com.aidotnet.erp.fms.application.FmsExtService.CreateJournalEntriesCommand;
import com.aidotnet.erp.fms.application.FmsExtService.CreateTaxRuleCommand;
import com.aidotnet.erp.fms.application.FmsExtService.JournalEntryCommand;
import com.aidotnet.erp.fms.application.FmsExtService.UpdateTaxRuleCommand;
import com.aidotnet.erp.fms.application.InventoryVoucherEngine;
import com.aidotnet.erp.fms.application.InventoryVoucherEngine.AutoGenerateVoucherCommand;
import com.aidotnet.erp.fms.application.InventoryVoucherEngine.CreateVoucherTemplateCommand;
import com.aidotnet.erp.fms.application.InventoryVoucherEngine.InventoryVoucherCommand;
import com.aidotnet.erp.fms.application.ComplianceRiskService;
import com.aidotnet.erp.fms.application.ComplianceRiskService.TradeComplianceCommand;
import com.aidotnet.erp.fms.application.ComplianceRiskService.PlatformComplianceCommand;
import com.aidotnet.erp.fms.application.ComplianceRiskService.RiskAssessmentCommand;
import com.aidotnet.erp.fms.application.ComplianceRiskService.FraudDetectionCommand;
import com.aidotnet.erp.fms.application.ComplianceRiskService.SaveVatStatusCommand;
import com.aidotnet.erp.fms.domain.CostBreakdown;
import com.aidotnet.erp.fms.domain.JournalEntry;
import com.aidotnet.erp.fms.domain.JournalEntryType;
import com.aidotnet.erp.fms.domain.TaxCalculationResult;
import com.aidotnet.erp.fms.domain.TaxRule;
import com.aidotnet.erp.fms.domain.VatValidationResult;
import com.aidotnet.erp.fms.domain.Voucher;
import com.aidotnet.erp.fms.domain.VoucherTemplate;
import com.aidotnet.erp.fms.domain.FraudDetectionResult;
import com.aidotnet.erp.fms.domain.RiskAssessment;
import com.aidotnet.erp.fms.domain.TradeComplianceResult;
import com.aidotnet.erp.fms.domain.PlatformComplianceResult;
import com.aidotnet.erp.fms.domain.VatComplianceStatus;
import com.aidotnet.erp.fms.infrastructure.tax.TaxServiceFacade;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * FMS扩展业务控制器
 * <p>
 * 描述: 财务域扩展功能接口，提供汇率查询、外汇交易、风险预警等操作。
 * </p>
 * <p>
 * 路径规范: /fms/api/in/v1 与 /fms/api/v1
 * 描述: 保留原有 in 路径兼容，同时为 ERP 内部域间直连提供统一 v1 入口。
 * </p>
 *
 * @author ERP系统
 * @see FmsExtService
 * @see CurrencyExchangeService
 */
@RestController
@RequestMapping({"/fms/api/in/v1", "/fms/api/v1"})
public class FmsExtController {

    private final FmsExtService fmsExtService;
    private final TaxServiceFacade taxServiceFacade;
    private final InventoryVoucherEngine voucherEngine;
    private final ComplianceRiskService complianceRiskService;

    public FmsExtController(FmsExtService fmsExtService, TaxServiceFacade taxServiceFacade,
                            InventoryVoucherEngine voucherEngine, ComplianceRiskService complianceRiskService) {
        this.fmsExtService = fmsExtService;
        this.taxServiceFacade = taxServiceFacade;
        this.voucherEngine = voucherEngine;
        this.complianceRiskService = complianceRiskService;
    }

    @PostMapping("/cost-breakdowns")
    public Result<CostBreakdown> createCostBreakdown(@Valid @RequestBody CreateCostBreakdownRequest request) {
        return Result.ok(fmsExtService.createCostBreakdown(currentTenant(), new CreateCostBreakdownCommand(
                request.costEventId(), request.costType(), request.costCategory(), request.amount(),
                request.currency(), request.exchangeRate(), request.remark())));
    }

    @GetMapping("/cost-breakdowns")
    public Result<List<CostBreakdown>> listCostBreakdowns(@RequestParam(required = false) String costEventId,
                                                          @RequestParam(required = false) String costType) {
        if (costEventId != null) {
            return Result.ok(fmsExtService.listCostBreakdownsByEvent(currentTenant(), costEventId));
        }
        return Result.ok(fmsExtService.listCostBreakdownsByType(currentTenant(), costType));
    }

    @PostMapping("/journal-entries")
    public Result<Void> createJournalEntries(@Valid @RequestBody CreateJournalEntriesRequest request) {
        List<JournalEntryCommand> entries = request.entries().stream()
                .map(e -> new JournalEntryCommand(e.accountCode(), e.accountName(), e.type(), e.amount(), e.currency(), e.remark()))
                .toList();
        fmsExtService.createJournalEntries(currentTenant(), new CreateJournalEntriesCommand(
                request.referenceType(), request.referenceId(), request.entryDate(), entries));
        return Result.ok(null);
    }

    @GetMapping("/journal-entries")
    public Result<List<JournalEntry>> listJournalEntries(@RequestParam(required = false) String referenceType,
                                                         @RequestParam(required = false) String referenceId,
                                                         @RequestParam(required = false) String accountCode) {
        if (referenceType != null && referenceId != null) {
            return Result.ok(fmsExtService.listJournalEntriesByReference(currentTenant(), referenceType, referenceId));
        }
        return Result.ok(fmsExtService.listJournalEntriesByAccount(currentTenant(), accountCode));
    }

    @PostMapping("/tax-rules")
    public Result<TaxRule> createTaxRule(@Valid @RequestBody CreateTaxRuleRequest request) {
        return Result.ok(fmsExtService.createTaxRule(currentTenant(), new CreateTaxRuleCommand(
                request.countryCode(), request.taxType(), request.taxRate(), request.taxCategory(),
                request.effectiveFrom(), request.effectiveTo())));
    }

    @PutMapping("/tax-rules/{ruleId}")
    public Result<TaxRule> updateTaxRule(@PathVariable String ruleId, @Valid @RequestBody UpdateTaxRuleRequest request) {
        return Result.ok(fmsExtService.updateTaxRule(currentTenant(), ruleId,
                new UpdateTaxRuleCommand(request.taxRate(), request.taxCategory(), request.effectiveTo())));
    }

    @PostMapping("/tax-rules/{ruleId}/toggle")
    public Result<TaxRule> toggleTaxRule(@PathVariable String ruleId, @RequestParam boolean enabled) {
        return Result.ok(fmsExtService.toggleTaxRule(currentTenant(), ruleId, enabled));
    }

    @GetMapping("/tax-rules/calculate")
    public Result<BigDecimal> calculateTax(@RequestParam String countryCode, @RequestParam String taxType,
                                           @RequestParam BigDecimal amount) {
        return Result.ok(fmsExtService.calculateTax(currentTenant(), countryCode, taxType, amount));
    }

    @GetMapping("/tax-rules")
    public Result<List<TaxRule>> listTaxRules(@RequestParam String countryCode) {
        return Result.ok(fmsExtService.listTaxRules(currentTenant(), countryCode));
    }

    @GetMapping("/tax-rules/{ruleId}")
    public Result<TaxRule> getTaxRule(@PathVariable String ruleId) {
        return Result.ok(fmsExtService.getTaxRule(currentTenant(), ruleId));
    }

    @PostMapping("/voucher-templates")
    public Result<VoucherTemplate> createVoucherTemplate(@Valid @RequestBody CreateVoucherTemplateRequest request) {
        return Result.ok(voucherEngine.createTemplate(currentTenant(), new CreateVoucherTemplateCommand(
                request.templateName(), request.businessType(), request.debitAccount(),
                request.creditAccount(), request.description())));
    }

    @PutMapping("/voucher-templates/{templateId}")
    public Result<VoucherTemplate> updateVoucherTemplate(@PathVariable String templateId,
                                                          @Valid @RequestBody CreateVoucherTemplateRequest request) {
        return Result.ok(voucherEngine.updateTemplate(currentTenant(), templateId, new CreateVoucherTemplateCommand(
                request.templateName(), request.businessType(), request.debitAccount(),
                request.creditAccount(), request.description())));
    }

    @PostMapping("/voucher-templates/{templateId}/toggle")
    public Result<VoucherTemplate> toggleVoucherTemplate(@PathVariable String templateId, @RequestParam boolean enabled) {
        return Result.ok(voucherEngine.toggleTemplate(currentTenant(), templateId, enabled));
    }

    @GetMapping("/voucher-templates")
    public Result<List<VoucherTemplate>> listVoucherTemplates(@RequestParam(required = false) String businessType) {
        return Result.ok(voucherEngine.listTemplates(currentTenant(), businessType));
    }

    @PostMapping("/inventory-vouchers/auto-generate")
    public Result<Voucher> autoGenerateVoucher(@Valid @RequestBody AutoGenerateVoucherRequest request) {
        return Result.ok(voucherEngine.autoGenerate(currentTenant(), new AutoGenerateVoucherCommand(
                request.businessType(), request.voucherType(), request.sourceId(),
                request.amount(), request.currency(), request.sellerSku())));
    }

    @PostMapping("/inventory-vouchers/batch-auto-generate")
    public Result<List<Voucher>> batchAutoGenerateVoucher(@RequestBody List<AutoGenerateVoucherRequest> requests) {
        List<AutoGenerateVoucherCommand> commands = requests.stream()
                .map(r -> new AutoGenerateVoucherCommand(r.businessType(), r.voucherType(), r.sourceId(),
                        r.amount(), r.currency(), r.sellerSku()))
                .toList();
        return Result.ok(voucherEngine.batchAutoGenerate(currentTenant(), commands));
    }

    @PostMapping("/inventory-vouchers/purchase-inbound")
    public Result<Voucher> generatePurchaseInbound(@Valid @RequestBody InventoryVoucherRequest request) {
        return Result.ok(voucherEngine.generatePurchaseInbound(currentTenant(), new InventoryVoucherCommand(
                request.sourceId(), request.amount(), request.currency(), request.sellerSku())));
    }

    @PostMapping("/inventory-vouchers/sales-outbound")
    public Result<Voucher> generateSalesOutbound(@Valid @RequestBody InventoryVoucherRequest request) {
        return Result.ok(voucherEngine.generateSalesOutbound(currentTenant(), new InventoryVoucherCommand(
                request.sourceId(), request.amount(), request.currency(), request.sellerSku())));
    }

    @PostMapping("/inventory-vouchers/return-inbound")
    public Result<Voucher> generateReturnInbound(@Valid @RequestBody InventoryVoucherRequest request) {
        return Result.ok(voucherEngine.generateReturnInbound(currentTenant(), new InventoryVoucherCommand(
                request.sourceId(), request.amount(), request.currency(), request.sellerSku())));
    }

    @PostMapping("/inventory-vouchers/return-outbound")
    public Result<Voucher> generateReturnOutbound(@Valid @RequestBody InventoryVoucherRequest request) {
        return Result.ok(voucherEngine.generateReturnOutbound(currentTenant(), new InventoryVoucherCommand(
                request.sourceId(), request.amount(), request.currency(), request.sellerSku())));
    }

    @PostMapping("/inventory-vouchers/inventory-gain")
    public Result<Voucher> generateInventoryGain(@Valid @RequestBody InventoryVoucherRequest request) {
        return Result.ok(voucherEngine.generateInventoryGain(currentTenant(), new InventoryVoucherCommand(
                request.sourceId(), request.amount(), request.currency(), request.sellerSku())));
    }

    @PostMapping("/inventory-vouchers/inventory-loss")
    public Result<Voucher> generateInventoryLoss(@Valid @RequestBody InventoryVoucherRequest request) {
        return Result.ok(voucherEngine.generateInventoryLoss(currentTenant(), new InventoryVoucherCommand(
                request.sourceId(), request.amount(), request.currency(), request.sellerSku())));
    }

    @PostMapping("/inventory-vouchers/transfer-in")
    public Result<Voucher> generateTransferIn(@Valid @RequestBody InventoryVoucherRequest request) {
        return Result.ok(voucherEngine.generateTransferIn(currentTenant(), new InventoryVoucherCommand(
                request.sourceId(), request.amount(), request.currency(), request.sellerSku())));
    }

    @PostMapping("/inventory-vouchers/transfer-out")
    public Result<Voucher> generateTransferOut(@Valid @RequestBody InventoryVoucherRequest request) {
        return Result.ok(voucherEngine.generateTransferOut(currentTenant(), new InventoryVoucherCommand(
                request.sourceId(), request.amount(), request.currency(), request.sellerSku())));
    }

    @PostMapping("/inventory-vouchers/cost-settlement")
    public Result<Voucher> generateCostSettlement(@Valid @RequestBody InventoryVoucherRequest request) {
        return Result.ok(voucherEngine.generateCostSettlement(currentTenant(), new InventoryVoucherCommand(
                request.sourceId(), request.amount(), request.currency(), request.sellerSku())));
    }

    @PostMapping("/inventory-vouchers/adjustment")
    public Result<Voucher> generateAdjustment(@Valid @RequestBody InventoryVoucherRequest request) {
        return Result.ok(voucherEngine.generateAdjustment(currentTenant(), new InventoryVoucherCommand(
                request.sourceId(), request.amount(), request.currency(), request.sellerSku())));
    }

    @PostMapping("/vouchers/{voucherId}/approve")
    public Result<Voucher> approveVoucher(@PathVariable String voucherId, @RequestBody ApproveVoucherRequest request) {
        return Result.ok(voucherEngine.approveVoucher(currentTenant(), voucherId, request.approvedBy()));
    }

    @PostMapping("/vouchers/batch-approve")
    public Result<List<Voucher>> batchApproveVouchers(@RequestBody BatchApproveVoucherRequest request) {
        return Result.ok(voucherEngine.batchApproveVouchers(currentTenant(), request.voucherIds(), request.approvedBy()));
    }

    @PostMapping("/vouchers/{voucherId}/void")
    public Result<Voucher> voidVoucher(@PathVariable String voucherId) {
        return Result.ok(voucherEngine.voidVoucher(currentTenant(), voucherId));
    }

    @GetMapping("/inventory-vouchers")
    public Result<List<Voucher>> queryVouchers(@RequestParam(required = false) String voucherType,
                                               @RequestParam(required = false) String status) {
        return Result.ok(voucherEngine.queryVouchers(currentTenant(), voucherType, status));
    }


    @PostMapping("/compliance/risk-assessment")
    public Result<RiskAssessment> assessRisk(@Valid @RequestBody RiskAssessmentRequest request) {
        return Result.ok(complianceRiskService.assessRisk(currentTenant(), new RiskAssessmentCommand(
                request.targetType(), request.targetId(), request.orderAmount(),
                request.isNewBuyer(), request.isBlacklistedBuyer(), request.returnRate(),
                request.orderCount())));
    }

    @GetMapping("/compliance/risk-assessments")
    public Result<List<RiskAssessment>> listRiskAssessments(@RequestParam(required = false) String targetType,
                                                             @RequestParam(required = false) String riskLevel) {
        return Result.ok(complianceRiskService.listRiskAssessments(currentTenant(), targetType, riskLevel));
    }

    @PostMapping("/compliance/fraud-detection")
    public Result<List<FraudDetectionResult>> detectFraud(@Valid @RequestBody FraudDetectionRequest request) {
        return Result.ok(complianceRiskService.detectFraud(currentTenant(), new FraudDetectionCommand(
                request.orderId(), request.buyerId(), request.ipAddress(),
                request.isHighRiskIp(), request.returnCount(), request.orderCountToday())));
    }

    @GetMapping("/compliance/fraud-detections")
    public Result<List<FraudDetectionResult>> listFraudDetections(@RequestParam(required = false) String status) {
        return Result.ok(complianceRiskService.listFraudDetections(currentTenant(), status));
    }

    @PostMapping("/compliance/trade-check")
    public Result<TradeComplianceResult> checkTradeCompliance(@Valid @RequestBody TradeComplianceRequest request) {
        return Result.ok(complianceRiskService.checkTradeCompliance(currentTenant(), new TradeComplianceCommand(
                request.orderId(), request.sellerSku(), request.hsCode(),
                request.originCountry(), request.destinationCountry())));
    }

    @PostMapping("/compliance/trade-check/batch")
    public Result<List<TradeComplianceResult>> batchCheckTradeCompliance(@RequestBody List<TradeComplianceRequest> requests) {
        List<TradeComplianceCommand> commands = requests.stream()
                .map(r -> new TradeComplianceCommand(r.orderId(), r.sellerSku(), r.hsCode(),
                        r.originCountry(), r.destinationCountry()))
                .toList();
        return Result.ok(complianceRiskService.batchCheckTradeCompliance(currentTenant(), commands));
    }

    @GetMapping("/compliance/trade-results")
    public Result<List<TradeComplianceResult>> listTradeComplianceResults(@RequestParam(required = false) String status) {
        return Result.ok(complianceRiskService.listTradeComplianceResults(currentTenant(), status));
    }

    @PostMapping("/compliance/platform-check")
    public Result<PlatformComplianceResult> checkPlatformCompliance(@Valid @RequestBody PlatformComplianceRequest request) {
        return Result.ok(complianceRiskService.checkPlatformCompliance(currentTenant(), new PlatformComplianceCommand(
                request.platform(), request.listingId(), request.sellerSku(),
                request.title(), request.description(), request.price())));
    }

    @GetMapping("/compliance/platform-results")
    public Result<List<PlatformComplianceResult>> listPlatformComplianceResults(
            @RequestParam(required = false) String platform,
            @RequestParam(required = false) String status) {
        return Result.ok(complianceRiskService.listPlatformComplianceResults(currentTenant(), platform, status));
    }

    @PostMapping("/compliance/vat-status")
    public Result<VatComplianceStatus> saveVatStatus(@Valid @RequestBody SaveVatStatusRequest request) {
        return Result.ok(complianceRiskService.saveVatStatus(currentTenant(), new SaveVatStatusCommand(
                request.countryCode(), request.vatNumber(), request.registrationStatus(),
                request.filingStatus(), request.nextFilingDate(), request.registrationDate(),
                request.expiryDate(), request.details())));
    }

    @GetMapping("/compliance/vat-status")
    public Result<List<VatComplianceStatus>> listVatStatuses() {
        return Result.ok(complianceRiskService.listVatStatuses(currentTenant()));
    }

    @GetMapping("/compliance/vat-status/{countryCode}")
    public Result<VatComplianceStatus> getVatStatus(@PathVariable String countryCode) {
        return Result.ok(complianceRiskService.getVatStatus(currentTenant(), countryCode));
    }

    @PostMapping("/compliance/vat-filing-alerts")
    public Result<List<VatComplianceStatus>> checkVatFilingAlerts() {
        return Result.ok(complianceRiskService.checkVatFilingAlerts(currentTenant()));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record CreateCostBreakdownRequest(@NotBlank String costEventId, @NotBlank String costType,
                                             String costCategory, @Positive BigDecimal amount, String currency,
                                             BigDecimal exchangeRate, String remark) {}
    public record CreateJournalEntriesRequest(@NotBlank String referenceType, @NotBlank String referenceId,
                                              Instant entryDate, List<JournalEntryRequest> entries) {}
    public record JournalEntryRequest(@NotBlank String accountCode, String accountName, JournalEntryType type,
                                      @Positive BigDecimal amount, String currency, String remark) {}
    public record CreateTaxRuleRequest(@NotBlank String countryCode, @NotBlank String taxType,
                                       @Positive BigDecimal taxRate, String taxCategory,
                                       Instant effectiveFrom, Instant effectiveTo) {}
    public record UpdateTaxRuleRequest(BigDecimal taxRate, String taxCategory, Instant effectiveTo) {}

    @PostMapping("/tax/calculate")
    public Result<TaxCalculationResult> calculateTax(@Valid @RequestBody CalculateTaxRequest request) {
        return Result.ok(taxServiceFacade.calculateTax(currentTenant(), request.countryCode(), request.regionCode(),
                request.sellerSku(), request.orderId(), request.taxableAmount(), null));
    }

    @PostMapping("/tax/validate-vat")
    public Result<VatValidationResult> validateVat(@Valid @RequestBody ValidateVatRequest request) {
        return Result.ok(taxServiceFacade.validateVat(request.vatNumber(), request.countryCode()));
    }

    public record CalculateTaxRequest(@NotBlank String countryCode, String regionCode,
                                      String sellerSku, String orderId,
                                      @Positive BigDecimal taxableAmount) {}
    public record ValidateVatRequest(@NotBlank String vatNumber, @NotBlank String countryCode) {}

    public record CreateVoucherTemplateRequest(@NotBlank String templateName, @NotBlank String businessType,
                                               @NotBlank String debitAccount, @NotBlank String creditAccount,
                                               String description) {}
    public record AutoGenerateVoucherRequest(@NotBlank String businessType, @NotBlank String voucherType,
                                             @NotBlank String sourceId, @Positive BigDecimal amount,
                                             String currency, String sellerSku) {}
    public record InventoryVoucherRequest(@NotBlank String sourceId, @Positive BigDecimal amount,
                                          String currency, String sellerSku) {}
    public record ApproveVoucherRequest(@NotBlank String approvedBy) {}
    public record BatchApproveVoucherRequest(List<String> voucherIds, @NotBlank String approvedBy) {}

    public record RiskAssessmentRequest(String targetType, String targetId, BigDecimal orderAmount,
                                        Boolean isNewBuyer, Boolean isBlacklistedBuyer, BigDecimal returnRate,
                                        Integer orderCount) {}
    public record FraudDetectionRequest(@NotBlank String orderId, String buyerId, String ipAddress,
                                        Boolean isHighRiskIp, Integer returnCount, Integer orderCountToday) {}
    public record TradeComplianceRequest(String orderId, String sellerSku, String hsCode,
                                         String originCountry, String destinationCountry) {}
    public record PlatformComplianceRequest(@NotBlank String platform, String listingId, String sellerSku,
                                            String title, String description, BigDecimal price) {}
    public record SaveVatStatusRequest(@NotBlank String countryCode, String vatNumber, String registrationStatus,
                                       String filingStatus, Instant nextFilingDate, Instant registrationDate,
                                       Instant expiryDate, Map<String, String> details) {}
}
