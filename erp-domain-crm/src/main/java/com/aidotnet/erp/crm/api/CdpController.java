package com.aidotnet.erp.crm.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.crm.application.CustomerDataService;
import com.aidotnet.erp.crm.application.CustomerDataService.CreateProfileCommand;
import com.aidotnet.erp.crm.application.CustomerDataService.RecordBehaviorCommand;
import com.aidotnet.erp.crm.domain.CustomerBehavior;
import com.aidotnet.erp.crm.domain.CustomerProfile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 客户数据平台(CDP)控制器
 * <p>
 * 描述: 客户画像与行为数据管理接口，支持客户360度视图构建。
 *       提供客户画像创建/更新、风险等级管理、客户分群、行为记录等能力。
 *       CDP是CRM域的核心数据分析模块，为精准营销和客户运营提供数据基础。
 * </p>
 * <p>
 * 路径规范: /crm/api/in/v1 — 内部方向(in)，v1版本
 * </p>
 * <p>
 * 核心概念:
 *   1. 客户画像(CustomerProfile) - 客户的360度视图，包含消费统计、偏好、风险等级
 *   2. 客户行为(CustomerBehavior) - 客户的浏览、购买、退货等行为事件
 *   3. 客户分群(Segment) - 按消费能力、活跃度等维度划分客户群体
 *   4. 风险等级(RiskLevel) - LOW/MEDIUM/HIGH，用于退货风控
 * </p>
 *
 * @author ERP系统
 * @see CustomerDataService
 */
@RestController("crmCdpController")
@RequestMapping("/crm/api/in/v1")
public class CdpController {

    /** 客户数据服务 */
    private final CustomerDataService customerDataService;

    /** 构造函数注入CustomerDataService */
    public CdpController(CustomerDataService customerDataService) {
        this.customerDataService = customerDataService;
    }

    /** 创建或更新客户画像 */
    @PostMapping("/profiles")
    public Result<CustomerProfile> createOrUpdateProfile(@Valid @RequestBody CreateProfileRequest request) {
        return Result.ok(customerDataService.createOrUpdateProfile(currentTenant(), new CreateProfileCommand(
                request.customerId(), request.segment(), request.lifetimeValue(), request.avgOrderValue(),
                request.totalOrders(), request.totalReturns(), request.preferredChannel(),
                request.preferredLanguage(), request.attributes(), request.firstOrderAt(), request.lastOrderAt())));
    }

    /** 查询客户画像 */
    @GetMapping("/profiles/{customerId}")
    public Result<CustomerProfile> getProfile(@PathVariable String customerId) {
        return Result.ok(customerDataService.getProfile(currentTenant(), customerId));
    }

    /** 查询客户画像列表(可按分群过滤) */
    @GetMapping("/profiles")
    public Result<List<CustomerProfile>> listProfiles(@RequestParam(required = false) String segment) {
        return Result.ok(customerDataService.listProfiles(currentTenant(), segment));
    }

    /** 更新客户风险等级 */
    @PatchMapping("/profiles/{customerId}/risk-level")
    public Result<CustomerProfile> updateRiskLevel(@PathVariable String customerId,
                                                   @Valid @RequestBody UpdateRiskLevelRequest request) {
        return Result.ok(customerDataService.updateRiskLevel(currentTenant(), customerId, request.riskLevel()));
    }

    /** 更新客户分群 */
    @PatchMapping("/profiles/{customerId}/segment")
    public Result<CustomerProfile> updateSegment(@PathVariable String customerId,
                                                 @Valid @RequestBody UpdateSegmentRequest request) {
        return Result.ok(customerDataService.updateSegment(currentTenant(), customerId, request.segment()));
    }

    /** 记录客户行为事件 */
    @PostMapping("/behaviors")
    public Result<CustomerBehavior> recordBehavior(@Valid @RequestBody RecordBehaviorRequest request) {
        return Result.ok(customerDataService.recordBehavior(currentTenant(), new RecordBehaviorCommand(
                request.customerId(), request.behaviorType(), request.channel(),
                request.objectType(), request.objectId(), request.context(), request.occurredAt())));
    }

    /** 查询客户行为列表(可按行为类型过滤) */
    @GetMapping("/behaviors")
    public Result<List<CustomerBehavior>> listBehaviors(@RequestParam String customerId,
                                                        @RequestParam(required = false) String behaviorType) {
        return Result.ok(customerDataService.listBehaviors(currentTenant(), customerId, behaviorType));
    }

    /** 获取当前租户ID，为空则抛出业务异常 */
    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record CreateProfileRequest(@NotBlank String customerId, String segment, BigDecimal lifetimeValue,
                                       BigDecimal avgOrderValue, int totalOrders, int totalReturns,
                                       String preferredChannel, String preferredLanguage,
                                       Map<String, Object> attributes, Instant firstOrderAt,
                                       Instant lastOrderAt) {}
    public record UpdateRiskLevelRequest(@NotBlank String riskLevel) {}
    public record UpdateSegmentRequest(@NotBlank String segment) {}
    public record RecordBehaviorRequest(@NotBlank String customerId, @NotBlank String behaviorType,
                                        String channel, String objectType, String objectId,
                                        Map<String, Object> context, Instant occurredAt) {}
}
