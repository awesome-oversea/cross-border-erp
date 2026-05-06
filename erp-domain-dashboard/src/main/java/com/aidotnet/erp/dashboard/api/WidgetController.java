package com.aidotnet.erp.dashboard.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.dashboard.application.WorkspaceService;
import com.aidotnet.erp.dashboard.application.WorkspaceService.CreateWidgetCommand;
import com.aidotnet.erp.dashboard.application.WorkspaceService.UpdateWidgetCommand;
import com.aidotnet.erp.dashboard.domain.DashboardWidget;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
 * 仪表盘组件入站控制器，提供组件配置的RESTful API。
 * <p>
 * 描述: 管理工作台可配置的展示组件，包括图表、表格、指标卡等。
 *       组件是仪表盘的基本展示单元，支持多种数据源和刷新频率配置。
 * </p>
 * <p>
 * 接口规范:
 *   - 路径: /dashboard/api/in/v1/widgets
 *   - 认证: 需登录用户，通过Token获取租户ID
 *   - 返回: 统一Result包装，包含code/msg/data
 * </p>
 * <p>
 * 组件类型:
 *   - LINE_CHART: 折线图，适合趋势展示
 *   - BAR_CHART: 柱状图，适合对比展示
 *   - PIE_CHART: 饼图，适合占比展示
 *   - METRIC_CARD: 指标卡，适合关键指标展示
 *   - TABLE: 数据表格，适合明细展示
 * </p>
 *
 * @author ERP系统
 */
@RestController
@RequestMapping("/dashboard/api/in/v1/widgets")
public class WidgetController {

    private final WorkspaceService workspaceService;

    /**
     * 构造函数 - 依赖注入工作台服务
     *
     * @param workspaceService 工作台应用服务
     */
    public WidgetController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    /**
     * 创建仪表盘组件。
     *
     * @param request 创建组件请求体
     * @return 新创建的组件(ACTIVE状态)
     */
    @PostMapping
    public Result<DashboardWidget> create(@Valid @RequestBody CreateWidgetRequest request) {
        return Result.ok(workspaceService.createDashboardWidget(currentTenant(), new CreateWidgetCommand(
                request.type(), request.name(), request.dataSource(), request.refreshRateSeconds(),
                request.config(), request.category(), request.description())));
    }

    /**
     * 更新仪表盘组件。
     *
     * @param widgetId 组件ID
     * @param request  更新组件请求体
     * @return 更新后的组件
     */
    @PutMapping("/{widgetId}")
    public Result<DashboardWidget> update(@PathVariable String widgetId, @Valid @RequestBody UpdateWidgetRequest request) {
        return Result.ok(workspaceService.updateDashboardWidget(currentTenant(), widgetId, new UpdateWidgetCommand(
                request.name(), request.dataSource(), request.refreshRateSeconds(), request.config(), request.description())));
    }

    /**
     * 查询组件列表。
     * <p>
     * 支持按分类筛选，不传category则返回所有组件。
     * </p>
     *
     * @param category 组件分类(可选)，如 SALES/INVENTORY/FINANCE
     * @return 组件列表
     */
    @GetMapping
    public Result<List<DashboardWidget>> list(@RequestParam(required = false) String category) {
        return Result.ok(workspaceService.listDashboardWidgets(currentTenant(), category));
    }

    /**
     * 查询活跃组件列表。
     * <p>
     * 仅返回ACTIVE状态的组件，用于前端渲染仪表盘。
     * </p>
     *
     * @return 活跃组件列表
     */
    @GetMapping("/active")
    public Result<List<DashboardWidget>> listActive() {
        return Result.ok(workspaceService.listActiveDashboardWidgets(currentTenant()));
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
     * 创建组件请求体
     *
     * @param type               组件类型，必填，如 LINE_CHART/METRIC_CARD/TABLE
     * @param name               组件名称，必填
     * @param dataSource         数据源标识，关联BI域指标定义
     * @param refreshRateSeconds 自动刷新间隔(秒)，0表示不自动刷新
     * @param config             组件配置，JSON格式，包含样式、过滤条件等
     * @param category           组件分类，如 SALES/INVENTORY/FINANCE
     * @param description        组件描述
     */
    public record CreateWidgetRequest(
            @NotBlank String type, @NotBlank String name, String dataSource,
            int refreshRateSeconds, Map<String, Object> config,
            String category, String description) {}

    /**
     * 更新组件请求体
     *
     * @param name               组件名称
     * @param dataSource         数据源标识
     * @param refreshRateSeconds 自动刷新间隔(秒)
     * @param config             组件配置，JSON格式
     * @param description        组件描述
     */
    public record UpdateWidgetRequest(
            String name, String dataSource, int refreshRateSeconds,
            Map<String, Object> config, String description) {}
}
