package com.aidotnet.erp.fms.application;

import com.aidotnet.erp.fms.domain.FraudDetectionResult;
import com.aidotnet.erp.fms.domain.PlatformComplianceResult;
import com.aidotnet.erp.fms.domain.RiskAssessment;
import com.aidotnet.erp.fms.domain.RiskAssessment.RiskLevel;
import com.aidotnet.erp.fms.domain.RiskAssessment.TargetType;
import com.aidotnet.erp.fms.domain.TradeComplianceResult;
import com.aidotnet.erp.fms.domain.VatComplianceStatus;
import com.aidotnet.erp.fms.domain.VatComplianceStatus.FilingStatus;
import com.aidotnet.erp.fms.domain.VatComplianceStatus.RegistrationStatus;
import com.aidotnet.erp.fms.infrastructure.FmsExtStore;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 合规风控服务
 * <p>
 * 描述: FMS域业务中台(5.9)，提供贸易合规检测和VAT合规验证。
 *       支持平台合规检测(PlatformComplianceResult)、贸易合规(TradeComplianceResult)、
 *       VAT合规状态(VatComplianceStatus)和欺诈检测(FraudDetectionResult)。
 * </p>
 * <p>
 * 合规检测维度:
 *   1. 平台合规 - 商品/Listing内容合规审核
 *   2. 贸易合规 - 出口管制、制裁名单检测
 *   3. VAT合规 - 欧洲VAT注册/申报状态验证
 *   4. 欺诈检测 - 交易异常检测和风险评级
 * </p>
 *
 * @author ERP系统
 * @see PlatformComplianceResult
 * @see TradeComplianceResult
 * @see VatComplianceStatus
 * @see FraudDetectionResult
 */
@Service("fmsComplianceRiskService")
public class ComplianceRiskService {

    private static final BigDecimal HIGH_RISK_THRESHOLD = new BigDecimal("0.7");
    private static final BigDecimal MEDIUM_RISK_THRESHOLD = new BigDecimal("0.4");

    private final FmsExtStore extStore;

    public ComplianceRiskService(FmsExtStore extStore) {
        this.extStore = extStore;
    }

    @Transactional
    public RiskAssessment assessRisk(String tenantId, RiskAssessmentCommand command) {
        BigDecimal riskScore = BigDecimal.ZERO;
        Map<String, Object> riskFactors = new HashMap<>();
        riskScore = riskScore.add(assessOrderRisk(command, riskFactors));
        riskScore = riskScore.add(assessBuyerRisk(command, riskFactors));
        if (riskScore.compareTo(BigDecimal.ONE) > 0) riskScore = BigDecimal.ONE;
        String riskLevel = determineRiskLevel(riskScore);
        String recommendation = determineRecommendation(riskLevel);
        RiskAssessment assessment = new RiskAssessment(
                UUID.randomUUID().toString(), tenantId, command.targetType(), command.targetId(),
                riskScore, riskLevel, riskFactors, recommendation, Instant.now());
        return extStore.saveRiskAssessment(assessment);
    }

    @Transactional
    public List<FraudDetectionResult> detectFraud(String tenantId, FraudDetectionCommand command) {
        List<FraudDetectionResult> results = new ArrayList<>();
        results.addAll(checkStolenCard(tenantId, command));
        results.addAll(checkReturnAbuse(tenantId, command));
        results.addAll(checkSuspiciousIp(tenantId, command));
        results.addAll(checkVelocityAbuse(tenantId, command));
        for (FraudDetectionResult result : results) {
            extStore.saveFraudDetectionResult(result);
        }
        return results;
    }

    public List<RiskAssessment> listRiskAssessments(String tenantId, String targetType, String riskLevel) {
        return extStore.listRiskAssessments(tenantId, targetType, riskLevel);
    }

    public List<FraudDetectionResult> listFraudDetections(String tenantId, String status) {
        return extStore.listFraudDetectionResults(tenantId, status);
    }

    @Transactional
    public VatComplianceStatus saveVatStatus(String tenantId, SaveVatStatusCommand command) {
        VatComplianceStatus status = new VatComplianceStatus(
                UUID.randomUUID().toString(), tenantId, command.countryCode(), command.vatNumber(),
                command.registrationStatus(), command.filingStatus(), command.nextFilingDate(),
                command.registrationDate(), command.expiryDate(), command.details(), Instant.now());
        return extStore.saveVatComplianceStatus(status);
    }

    public List<VatComplianceStatus> listVatStatuses(String tenantId) {
        return extStore.listVatComplianceStatuses(tenantId);
    }

    public VatComplianceStatus getVatStatus(String tenantId, String countryCode) {
        return extStore.findVatComplianceStatus(tenantId, countryCode).orElse(null);
    }

    @Transactional
    public List<VatComplianceStatus> checkVatFilingAlerts(String tenantId) {
        List<VatComplianceStatus> allStatuses = extStore.listVatComplianceStatuses(tenantId);
        List<VatComplianceStatus> alerts = new ArrayList<>();
        Instant now = Instant.now();
        for (VatComplianceStatus status : allStatuses) {
            if (status.nextFilingDate() != null && status.nextFilingDate().isBefore(now.plus(java.time.Duration.ofDays(7))) &&
                    (status.filingStatus().equals(FilingStatus.DUE_SOON.name()) || status.filingStatus().equals(FilingStatus.UP_TO_DATE.name()))) {
                VatComplianceStatus updated = new VatComplianceStatus(
                        status.statusId(), status.tenantId(), status.countryCode(), status.vatNumber(),
                        status.registrationStatus(), FilingStatus.DUE_SOON.name(), status.nextFilingDate(),
                        status.registrationDate(), status.expiryDate(), status.details(), Instant.now());
                extStore.saveVatComplianceStatus(updated);
                alerts.add(updated);
            }
        }
        return alerts;
    }

    private BigDecimal assessOrderRisk(RiskAssessmentCommand command, Map<String, Object> factors) {
        BigDecimal score = BigDecimal.ZERO;
        if (command.orderAmount() != null && command.orderAmount().compareTo(new BigDecimal("5000")) > 0) {
            score = score.add(new BigDecimal("0.2"));
            factors.put("highOrderAmount", true);
        }
        if (command.isNewBuyer() != null && command.isNewBuyer()) {
            score = score.add(new BigDecimal("0.15"));
            factors.put("newBuyer", true);
        }
        if (command.isBlacklistedBuyer() != null && command.isBlacklistedBuyer()) {
            score = score.add(new BigDecimal("0.5"));
            factors.put("blacklistedBuyer", true);
        }
        return score;
    }

    private BigDecimal assessBuyerRisk(RiskAssessmentCommand command, Map<String, Object> factors) {
        BigDecimal score = BigDecimal.ZERO;
        if (command.returnRate() != null && command.returnRate().compareTo(new BigDecimal("0.3")) > 0) {
            score = score.add(new BigDecimal("0.2"));
            factors.put("highReturnRate", true);
        }
        if (command.orderCount() != null && command.orderCount() > 10) {
            score = score.add(new BigDecimal("0.1"));
            factors.put("highOrderFrequency", true);
        }
        return score;
    }

    private String determineRiskLevel(BigDecimal score) {
        if (score.compareTo(HIGH_RISK_THRESHOLD) >= 0) return RiskLevel.CRITICAL.name();
        if (score.compareTo(MEDIUM_RISK_THRESHOLD) >= 0) return RiskLevel.HIGH.name();
        if (score.compareTo(new BigDecimal("0.2")) >= 0) return RiskLevel.MEDIUM.name();
        return RiskLevel.LOW.name();
    }

    private String determineRecommendation(String riskLevel) {
        return switch (riskLevel) {
            case "CRITICAL" -> "BLOCK";
            case "HIGH" -> "MANUAL_REVIEW";
            case "MEDIUM" -> "ENHANCED_MONITORING";
            default -> "APPROVE";
        };
    }

    private List<FraudDetectionResult> checkStolenCard(String tenantId, FraudDetectionCommand command) {
        List<FraudDetectionResult> results = new ArrayList<>();
        if (command.isHighRiskIp() != null && command.isHighRiskIp()) {
            results.add(new FraudDetectionResult(UUID.randomUUID().toString(), tenantId, command.orderId(),
                    command.buyerId(), "STOLEN_CARD", "HIGH", "High-risk IP address detected",
                    Map.of("ipAddress", command.ipAddress() != null ? command.ipAddress() : "unknown"),
                    "OPEN", Instant.now()));
        }
        return results;
    }

    private List<FraudDetectionResult> checkReturnAbuse(String tenantId, FraudDetectionCommand command) {
        List<FraudDetectionResult> results = new ArrayList<>();
        if (command.returnCount() != null && command.returnCount() > 5) {
            results.add(new FraudDetectionResult(UUID.randomUUID().toString(), tenantId, command.orderId(),
                    command.buyerId(), "RETURN_ABUSE", "MEDIUM", "Excessive return count detected",
                    Map.of("returnCount", String.valueOf(command.returnCount())),
                    "OPEN", Instant.now()));
        }
        return results;
    }

    private List<FraudDetectionResult> checkSuspiciousIp(String tenantId, FraudDetectionCommand command) {
        return new ArrayList<>();
    }

    private List<FraudDetectionResult> checkVelocityAbuse(String tenantId, FraudDetectionCommand command) {
        List<FraudDetectionResult> results = new ArrayList<>();
        if (command.orderCountToday() != null && command.orderCountToday() > 5) {
            results.add(new FraudDetectionResult(UUID.randomUUID().toString(), tenantId, command.orderId(),
                    command.buyerId(), "VELOCITY_ABUSE", "HIGH", "Too many orders in short period",
                    Map.of("orderCountToday", String.valueOf(command.orderCountToday())),
                    "OPEN", Instant.now()));
        }
        return results;
    }

    public record RiskAssessmentCommand(
            String targetType, String targetId, BigDecimal orderAmount,
            Boolean isNewBuyer, Boolean isBlacklistedBuyer, BigDecimal returnRate,
            Integer orderCount) {}

    public record FraudDetectionCommand(
            String orderId, String buyerId, String ipAddress,
            Boolean isHighRiskIp, Integer returnCount, Integer orderCountToday) {}

    public record SaveVatStatusCommand(
            String countryCode, String vatNumber, String registrationStatus,
            String filingStatus, Instant nextFilingDate, Instant registrationDate,
            Instant expiryDate, Map<String, String> details) {}

    @Transactional
    public TradeComplianceResult checkTradeCompliance(String tenantId, TradeComplianceCommand command) {
        List<String> violations = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Map<String, Object> details = new HashMap<>();

        checkEmbargo(tenantId, command.destinationCountry(), violations, details);
        checkHsCodeCompliance(tenantId, command.hsCode(), command.destinationCountry(), violations, warnings, details);
        checkProductRestriction(tenantId, command.sellerSku(), command.destinationCountry(), violations, warnings, details);
        checkExportControl(tenantId, command.hsCode(), command.destinationCountry(), violations, warnings, details);

        String status = violations.isEmpty() ? (warnings.isEmpty() ? "PASSED" : "WARNING") : "VIOLATION";
        TradeComplianceResult result = new TradeComplianceResult(
                UUID.randomUUID().toString(), tenantId, command.orderId(), command.sellerSku(),
                command.hsCode(), command.originCountry(), command.destinationCountry(),
                status, violations, warnings, details, Instant.now());
        return extStore.saveTradeComplianceResult(result);
    }

    @Transactional
    public List<TradeComplianceResult> batchCheckTradeCompliance(String tenantId, List<TradeComplianceCommand> commands) {
        List<TradeComplianceResult> results = new ArrayList<>();
        for (TradeComplianceCommand command : commands) {
            results.add(checkTradeCompliance(tenantId, command));
        }
        return results;
    }

    public List<TradeComplianceResult> listTradeComplianceResults(String tenantId, String status) {
        return extStore.listTradeComplianceResults(tenantId, status);
    }

    @Transactional
    public PlatformComplianceResult checkPlatformCompliance(String tenantId, PlatformComplianceCommand command) {
        List<String> violations = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Map<String, Object> details = new HashMap<>();

        checkListingCompliance(tenantId, command, violations, warnings, details);
        checkPricingCompliance(tenantId, command, violations, warnings, details);
        checkRestrictedKeywords(tenantId, command, violations, warnings, details);

        String status = violations.isEmpty() ? (warnings.isEmpty() ? "PASSED" : "WARNING") : "VIOLATION";
        PlatformComplianceResult result = new PlatformComplianceResult(
                UUID.randomUUID().toString(), tenantId, command.platform(), command.listingId(),
                command.sellerSku(), status, violations, warnings, details, Instant.now());
        return extStore.savePlatformComplianceResult(result);
    }

    public List<PlatformComplianceResult> listPlatformComplianceResults(String tenantId, String platform, String status) {
        return extStore.listPlatformComplianceResults(tenantId, platform, status);
    }

    private void checkEmbargo(String tenantId, String destinationCountry, List<String> violations, Map<String, Object> details) {
        List<String> embargoedCountries = List.of("CU", "IR", "KP", "SY", "RU");
        if (destinationCountry != null && embargoedCountries.contains(destinationCountry.toUpperCase())) {
            violations.add("EMBARGO_COUNTRY:" + destinationCountry);
            details.put("embargo", "Destination country is under trade embargo: " + destinationCountry);
        }
    }

    private void checkHsCodeCompliance(String tenantId, String hsCode, String destinationCountry,
                                        List<String> violations, List<String> warnings, Map<String, Object> details) {
        if (hsCode == null || hsCode.isBlank()) {
            warnings.add("HS_CODE_MISSING");
            details.put("hsCodeWarning", "HS code is missing, may cause customs delays");
            return;
        }
        List<String> restrictedHsPrefixes = List.of("9301", "9302", "9303", "9304", "9305", "9306",
                "3602", "3603", "3604", "8428", "9013");
        for (String prefix : restrictedHsPrefixes) {
            if (hsCode.startsWith(prefix)) {
                violations.add("RESTRICTED_HS_CODE:" + hsCode);
                details.put("hsCodeViolation", "HS code " + hsCode + " falls under restricted category");
                break;
            }
        }
        if (hsCode.length() < 6) {
            warnings.add("HS_CODE_INCOMPLETE:" + hsCode);
            details.put("hsCodeIncomplete", "HS code should be at least 6 digits for international trade");
        }
    }

    private void checkProductRestriction(String tenantId, String sellerSku, String destinationCountry,
                                          List<String> violations, List<String> warnings, Map<String, Object> details) {
        List<String> restrictedProductTypes = List.of("BATTERY", "FLAMMABLE", "AEROSOL", "LITHIUM");
        if (sellerSku != null) {
            for (String type : restrictedProductTypes) {
                if (sellerSku.toUpperCase().contains(type)) {
                    warnings.add("RESTRICTED_PRODUCT_TYPE:" + type);
                    details.put("productRestriction", "Product may require special handling: " + type);
                }
            }
        }
    }

    private void checkExportControl(String tenantId, String hsCode, String destinationCountry,
                                     List<String> violations, List<String> warnings, Map<String, Object> details) {
        if ("US".equals(destinationCountry) && hsCode != null && hsCode.startsWith("8542")) {
            warnings.add("EAR_CONTROLLED:" + hsCode);
            details.put("exportControl", "Product may be subject to EAR export controls for US destination");
        }
    }

    private void checkListingCompliance(String tenantId, PlatformComplianceCommand command,
                                         List<String> violations, List<String> warnings, Map<String, Object> details) {
        if (command.title() != null && command.title().length() > 200) {
            warnings.add("TITLE_TOO_LONG");
            details.put("titleLength", "Title exceeds recommended length of 200 characters");
        }
    }

    private void checkPricingCompliance(String tenantId, PlatformComplianceCommand command,
                                         List<String> violations, List<String> warnings, Map<String, Object> details) {
        if (command.price() != null && command.price().compareTo(BigDecimal.ZERO) <= 0) {
            violations.add("INVALID_PRICE");
            details.put("pricingViolation", "Price must be greater than zero");
        }
    }

    private void checkRestrictedKeywords(String tenantId, PlatformComplianceCommand command,
                                          List<String> violations, List<String> warnings, Map<String, Object> details) {
        List<String> restrictedKeywords = List.of("FDA approved", "cure", "guaranteed", "100% natural");
        if (command.title() != null) {
            for (String keyword : restrictedKeywords) {
                if (command.title().toLowerCase().contains(keyword.toLowerCase())) {
                    violations.add("RESTRICTED_KEYWORD:" + keyword);
                    details.put("keywordViolation", "Restricted keyword found in listing: " + keyword);
                }
            }
        }
        if (command.description() != null) {
            for (String keyword : restrictedKeywords) {
                if (command.description().toLowerCase().contains(keyword.toLowerCase())) {
                    warnings.add("RESTRICTED_KEYWORD_IN_DESC:" + keyword);
                    details.put("keywordWarning", "Restricted keyword found in description: " + keyword);
                }
            }
        }
    }

    public record TradeComplianceCommand(
            String orderId, String sellerSku, String hsCode,
            String originCountry, String destinationCountry) {}

    public record PlatformComplianceCommand(
            String platform, String listingId, String sellerSku,
            String title, String description, BigDecimal price) {}
}
