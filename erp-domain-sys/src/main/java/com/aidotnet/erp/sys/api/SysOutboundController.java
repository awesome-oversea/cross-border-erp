package com.aidotnet.erp.sys.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.sys.application.BusinessAlertService;
import com.aidotnet.erp.sys.application.PmsIntegrationService;
import com.aidotnet.erp.sys.application.SysExtService;
import com.aidotnet.erp.sys.domain.AIFeatureToggle;
import com.aidotnet.erp.sys.domain.BusinessAlert;
import com.aidotnet.erp.sys.domain.DataDictionary;
import com.aidotnet.erp.sys.domain.LogisticsRule;
import com.aidotnet.erp.sys.domain.PmsDataTrustRule;
import com.aidotnet.erp.sys.domain.PmsFeedback;
import com.aidotnet.erp.sys.infrastructure.SysExtStore;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统设置域对外API控制器，提供其他子域查询SYS域数据的接口。
 * <p>
 * 描述: SYS域对外暴露的只读查询接口，供其他子域通过Feign调用获取
 *       系统配置、数据字典、物流规则、AI功能开关、PMS集成等数据。
 *       是跨域数据查询的统一入口。
 * </p>
 * <p>
 * 跨域关联:
 *   - OMS → SYS: 查询物流规则(计算运费)、查询数据字典(订单状态枚举)
 *   - WMS → SYS: 查询连接器配置(同步第三方仓库)
 *   - ADS → SYS: 查询AI功能开关(控制广告AI策略)
 *   - BI → SYS: 查询数据字典(报表维度枚举)、查询AI功能开关
 *   - DASHBOARD → SYS: 查询业务预警(展示预警卡片)
 *   - PMS → SYS: 查询数据信任规则(写入权限校验)
 * </p>
 *
 * @author ERP系统
 */
@RestController
@RequestMapping("/sys/api/out/v1")
public class SysOutboundController {

    private final SysExtStore extStore;
    private final SysExtService sysExtService;
    private final PmsIntegrationService pmsIntegrationService;
    private final BusinessAlertService alertService;

    public SysOutboundController(SysExtStore extStore,
                                 SysExtService sysExtService,
                                 PmsIntegrationService pmsIntegrationService,
                                 BusinessAlertService alertService) {
        this.extStore = extStore;
        this.sysExtService = sysExtService;
        this.pmsIntegrationService = pmsIntegrationService;
        this.alertService = alertService;
    }

    /**
     * 按类型查询数据字典。
     * <p>
     * 供各子域查询标准化的枚举值和选项集。
     * </p>
     *
     * @param tenantId 租户ID
     * @param dictType 字典类型(可选，为空则返回全部)
     * @return 数据字典列表
     */
    @GetMapping("/dictionaries")
    public Result<List<DataDictionary>> exportDictionaries(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestParam(required = false) String dictType) {
        return Result.ok(sysExtService.listDataDictionaries(tenantId, dictType));
    }

    /**
     * 查询物流规则。
     * <p>
     * 供OMS/TMS等域查询物流计费规则和时效预估。
     * </p>
     *
     * @param tenantId    租户ID
     * @param countryCode 国家编码(可选)
     * @return 物流规则列表
     */
    @GetMapping("/logistics-rules")
    public Result<List<LogisticsRule>> exportLogisticsRules(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestParam(required = false) String countryCode) {
        return Result.ok(sysExtService.listLogisticsRules(tenantId, countryCode));
    }

    /**
     * 计算物流费用。
     * <p>
     * 供OMS域计算订单运费。
     * </p>
     *
     * @param tenantId    租户ID
     * @param countryCode 国家编码
     * @param channel     物流渠道
     * @param weightKg    重量(kg)
     * @return 物流费用
     */
    @GetMapping("/logistics-rules/calculate")
    public Result<Map<String, Object>> calculateLogisticsCost(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestParam String countryCode,
            @RequestParam String channel,
            @RequestParam BigDecimal weightKg) {
        BigDecimal cost = sysExtService.calculateLogisticsCost(tenantId, countryCode, channel, weightKg);
        Map<String, Object> result = new HashMap<>();
        result.put("countryCode", countryCode);
        result.put("channel", channel);
        result.put("weightKg", weightKg);
        result.put("cost", cost);
        result.put("currency", "CNY");
        result.put("calculatedAt", Instant.now().toString());
        return Result.ok(result);
    }

    /**
     * 查询AI功能开关。
     * <p>
     * 供各子域查询AI功能启用状态，控制AI能力是否参与业务处理。
     * </p>
     *
     * @param tenantId 租户ID
     * @param domain   子域标识(可选，为空则返回全部)
     * @return AI功能开关列表
     */
    @GetMapping("/ai-toggles")
    public Result<List<AIFeatureToggle>> exportAIToggles(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestParam(required = false) String domain) {
        return Result.ok(sysExtService.listAIFeatureToggles(tenantId, domain));
    }

    /**
     * 检查AI功能是否启用。
     * <p>
     * 供各子域快速检查某个AI功能是否启用。
     * </p>
     *
     * @param tenantId    租户ID
     * @param featureCode 功能编码
     * @return 是否启用
     */
    @GetMapping("/ai-toggles/check")
    public Result<Map<String, Object>> checkAIFeatureEnabled(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestParam String featureCode) {
        boolean enabled = sysExtService.isAIFeatureEnabled(tenantId, featureCode);
        return Result.ok(Map.of("featureCode", featureCode, "enabled", enabled));
    }

    /**
     * 查询PMS数据信任规则。
     * <p>
     * 供PMS查询各域数据的信任级别和写入权限。
     * </p>
     *
     * @param tenantId 租户ID
     * @return 数据信任规则列表
     */
    @GetMapping("/pms/data-trust-rules")
    public Result<List<PmsDataTrustRule>> exportDataTrustRules(
            @RequestHeader("X-Tenant-Id") String tenantId) {
        return Result.ok(pmsIntegrationService.listDataTrustRules(tenantId));
    }

    /**
     * 查询待投递的PMS反馈。
     * <p>
     * 供PMS拉取ERP执行结果反馈。
     * </p>
     *
     * @param tenantId 租户ID
     * @return 待投递反馈列表
     */
    @GetMapping("/pms/feedbacks/pending")
    public Result<List<PmsFeedback>> exportPendingFeedbacks(
            @RequestHeader("X-Tenant-Id") String tenantId) {
        return Result.ok(pmsIntegrationService.listPendingFeedbacks(tenantId));
    }

    /**
     * 查询业务预警列表。
     * <p>
     * 供DASHBOARD等域查询系统级业务预警。
     * </p>
     *
     * @param tenantId 租户ID
     * @param alertType 预警类型(可选)
     * @param status   预警状态(可选: OPEN/ACKNOWLEDGED/RESOLVED)
     * @return 业务预警列表
     */
    @GetMapping("/alerts")
    public Result<List<BusinessAlert>> exportAlerts(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestParam(required = false) String alertType,
            @RequestParam(required = false) String status) {
        List<BusinessAlert> alerts = alertService.listAlerts(tenantId, alertType, status);
        return Result.ok(alerts);
    }

    /**
     * 查询系统配置摘要。
     * <p>
     * 供各子域查询系统级配置摘要，包括启用的AI功能数、
     * 活跃连接器数、数据字典类型数等。
     * </p>
     *
     * @param tenantId 租户ID
     * @return 系统配置摘要
     */
    @GetMapping("/config/summary")
    public Result<Map<String, Object>> exportConfigSummary(
            @RequestHeader("X-Tenant-Id") String tenantId) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("aiFeatureCount", sysExtService.listAIFeatureToggles(tenantId, null).size());
        summary.put("enabledAiFeatureCount", sysExtService.listAIFeatureToggles(tenantId, null).stream()
                .filter(AIFeatureToggle::enabled).count());
        summary.put("logisticsRuleCount", sysExtService.listLogisticsRules(tenantId, null).size());
        summary.put("dataTrustRuleCount", pmsIntegrationService.listDataTrustRules(tenantId).size());
        summary.put("pendingFeedbackCount", pmsIntegrationService.listPendingFeedbacks(tenantId).size());
        summary.put("generatedAt", Instant.now().toString());
        return Result.ok(summary);
    }
}
