package com.aidotnet.erp.dashboard.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.dashboard.application.WorkspaceService;
import com.aidotnet.erp.dashboard.application.WorkspaceService.CreateAIInsightCardCommand;
import com.aidotnet.erp.dashboard.domain.AIInsightCard;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
 * AI洞察卡片入站控制器，提供AI洞察的RESTful API。
 * <p>
 * 描述: 管理AI生成的业务洞察卡片，包括异常预警、趋势分析、
 *       机会发现和风险提醒等。卡片由各业务域事件触发AI分析生成，
 *       用户可标记已读或忽略。
 * </p>
 * <p>
 * 接口规范:
 *   - 路径: /dashboard/api/in/v1/ai-insights
 *   - 认证: 需登录用户，通过Token获取租户ID
 *   - 返回: 统一Result包装，包含code/msg/data
 * </p>
 * <p>
 * 洞察类型:
 *   - anomaly: 异常检测，如库存异常、物流延迟
 *   - trend: 趋势分析，如销售趋势、流量变化
 *   - opportunity: 机会发现，如爆款潜力、补货时机
 *   - risk: 风险预警，如库存预警、财务风险
 * </p>
 *
 * @author ERP系统
 */
@RestController
@RequestMapping("/dashboard/api/in/v1/ai-insights")
public class AIInsightController {

    private final WorkspaceService workspaceService;

    /**
     * 构造函数 - 依赖注入工作台服务
     *
     * @param workspaceService 工作台应用服务
     */
    public AIInsightController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    /**
     * 创建AI洞察卡片。
     * <p>
     * 通常由事件处理器自动调用，也可由管理员手动创建。
     * </p>
     *
     * @param request 创建洞察卡片请求体
     * @return 新创建的AI洞察卡片
     */
    @PostMapping("/cards")
    public Result<AIInsightCard> createCard(@Valid @RequestBody CreateAIInsightCardRequest request) {
        return Result.ok(workspaceService.createAIInsightCard(currentTenant(), new CreateAIInsightCardCommand(
                request.userId(), request.title(), request.summary(), request.insightType(),
                request.severity(), request.data(), request.sourceDomain(), request.suggestion(),
                request.actionUrl(), request.validUntil())));
    }

    /**
     * 标记AI洞察卡片为已读。
     *
     * @param cardId 卡片ID
     * @return 操作结果
     */
    @PatchMapping("/cards/{cardId}/read")
    public Result<Void> markRead(@PathVariable String cardId) {
        workspaceService.markAIInsightCardRead(currentTenant(), cardId);
        return Result.ok(null);
    }

    /**
     * 忽略AI洞察卡片。
     * <p>
     * 忽略后卡片不再展示在洞察列表中，但数据保留。
     * </p>
     *
     * @param cardId 卡片ID
     * @return 操作结果
     */
    @PatchMapping("/cards/{cardId}/dismiss")
    public Result<Void> dismiss(@PathVariable String cardId) {
        workspaceService.dismissAIInsightCard(currentTenant(), cardId);
        return Result.ok(null);
    }

    /**
     * 查询AI洞察卡片列表。
     * <p>
     * 支持按洞察类型筛选，不传insightType则返回所有类型。
     * </p>
     *
     * @param userId      用户ID
     * @param insightType 洞察类型(可选): anomaly/trend/opportunity/risk
     * @return AI洞察卡片列表
     */
    @GetMapping("/cards")
    public Result<List<AIInsightCard>> listCards(@RequestParam String userId,
                                                  @RequestParam(required = false) String insightType) {
        return Result.ok(workspaceService.listAIInsightCards(currentTenant(), userId, insightType));
    }

    /**
     * 查询未读AI洞察卡片。
     * <p>
     * 仅返回未读且未忽略的卡片，按严重等级降序排列。
     * </p>
     *
     * @param userId 用户ID
     * @return 未读AI洞察卡片列表
     */
    @GetMapping("/cards/unread")
    public Result<List<AIInsightCard>> listUnreadCards(@RequestParam String userId) {
        return Result.ok(workspaceService.listUnreadAIInsightCards(currentTenant(), userId));
    }

    /**
     * 获取当前租户ID
     *
     * @return 租户ID
     * @throws BizException TENANT_REQUIRED - 租户不能为空
     */
    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    /**
     * 创建AI洞察卡片请求体
     *
     * @param userId       目标用户ID，必填
     * @param title        洞察标题，必填
     * @param summary      洞察摘要
     * @param insightType  洞察类型: anomaly/trend/opportunity/risk
     * @param severity     严重等级: critical/high/medium/low
     * @param data         洞察数据，JSON格式
     * @param sourceDomain 来源业务域: OMS/WMS/ADS/FMS等
     * @param suggestion   AI建议
     * @param actionUrl    操作跳转链接
     * @param validUntil   过期时间
     */
    public record CreateAIInsightCardRequest(
            @NotBlank String userId, @NotBlank String title, String summary,
            String insightType, String severity, Map<String, Object> data,
            String sourceDomain, String suggestion, String actionUrl,
            Instant validUntil) {}
}
