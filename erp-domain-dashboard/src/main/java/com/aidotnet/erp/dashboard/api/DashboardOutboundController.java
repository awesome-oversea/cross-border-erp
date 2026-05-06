package com.aidotnet.erp.dashboard.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.dashboard.application.DashboardService;
import com.aidotnet.erp.dashboard.application.WorkspaceService;
import com.aidotnet.erp.dashboard.domain.Announcement;
import com.aidotnet.erp.dashboard.domain.CalendarEvent;
import com.aidotnet.erp.dashboard.domain.DashboardMetric;
import com.aidotnet.erp.dashboard.domain.DashboardWidget;
import com.aidotnet.erp.dashboard.domain.TodoItem;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 仪表盘出站控制器，提供跨域数据导出接口。
 * <p>
 * 描述: 为其他子系统提供仪表盘数据的只读查询接口，
 *       支持其他域获取工作台的组件配置、指标数据、待办事项等。
 *       所有接口通过X-Tenant-Id请求头实现多租户隔离。
 * </p>
 * <p>
 * 接口规范:
 *   - 路径: /dashboard/api/out/v1
 *   - 认证: 服务间调用，通过X-Tenant-Id传递租户上下文
 *   - 返回: 统一Result包装，包含code/msg/data
 * </p>
 * <p>
 * 跨域关联:
 *   - OMS → DASHBOARD: 查询订单相关待办事项
 *   - WMS → DASHBOARD: 查询库存预警指标
 *   - BI → DASHBOARD: 查询仪表盘组件配置
 *   - IAM → DASHBOARD: 查询用户工作台摘要
 * </p>
 *
 * @author ERP系统
 */
@RestController
@RequestMapping("/dashboard/api/out/v1")
public class DashboardOutboundController {

    private final DashboardService dashboardService;
    private final WorkspaceService workspaceService;

    /**
     * 构造函数 - 依赖注入仪表盘服务和工作台服务
     *
     * @param dashboardService 仪表盘指标服务
     * @param workspaceService 工作台服务
     */
    public DashboardOutboundController(DashboardService dashboardService, WorkspaceService workspaceService) {
        this.dashboardService = dashboardService;
        this.workspaceService = workspaceService;
    }

    /**
     * 导出仪表盘组件配置。
     * <p>
     * 供其他子系统查询当前租户的活跃组件配置，
     * 用于BI域报表嵌入、OMS域订单面板等场景。
     * </p>
     *
     * @param tenantId 租户ID
     * @return 组件配置列表
     */
    @GetMapping("/widgets")
    public Result<List<Map<String, Object>>> exportWidgets(@RequestHeader("X-Tenant-Id") String tenantId) {
        List<DashboardWidget> widgets = workspaceService.listActiveDashboardWidgets(tenantId);
        List<Map<String, Object>> result = widgets.stream()
                .map(w -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("widgetId", w.widgetId());
                    map.put("type", w.type());
                    map.put("name", w.name());
                    map.put("dataSource", w.dataSource());
                    map.put("refreshRateSeconds", w.refreshRateSeconds());
                    map.put("config", w.config());
                    map.put("category", w.category());
                    map.put("status", w.status().name());
                    return map;
                })
                .collect(Collectors.toList());
        return Result.ok(result);
    }

    /**
     * 导出工作台摘要数据。
     * <p>
     * 供其他子系统查询当前租户的核心运营指标摘要，
     * 包含销售额、订单量、库存金额等关键指标。
     * </p>
     *
     * @param tenantId 租户ID
     * @return 工作台摘要(包含指标列表和导出时间)
     */
    @GetMapping("/summary")
    public Result<Map<String, Object>> exportSummary(@RequestHeader("X-Tenant-Id") String tenantId) {
        List<DashboardMetric> metrics = dashboardService.list(tenantId);
        Map<String, Object> metricMap = metrics.stream()
                .collect(Collectors.toMap(
                        DashboardMetric::metricCode,
                        m -> {
                            Map<String, Object> detail = new HashMap<>();
                            detail.put("metricName", m.metricName());
                            detail.put("metricValue", m.metricValue());
                            detail.put("unit", m.unit());
                            detail.put("updatedAt", m.updatedAt().toString());
                            return detail;
                        },
                        (existing, replacement) -> replacement
                ));
        Map<String, Object> summary = new HashMap<>();
        summary.put("metrics", metricMap);
        summary.put("metricCount", metrics.size());
        summary.put("exportedAt", Instant.now().toString());
        return Result.ok(summary);
    }

    /**
     * 导出待办事项。
     * <p>
     * 供其他子系统查询指定状态的待办事项，
     * 用于OMS域订单审批、SCM域采购审批等关联场景。
     * </p>
     *
     * @param tenantId 租户ID
     * @param userId   用户ID(可选，不传则查询租户下所有)
     * @param status   待办状态(可选，PENDING/IN_PROGRESS/COMPLETED/CANCELLED)
     * @return 待办事项列表
     */
    @GetMapping("/todos")
    public Result<List<Map<String, Object>>> exportTodos(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String status) {
        List<TodoItem> todos;
        if (userId != null && !userId.isBlank()) {
            if ("PENDING".equals(status)) {
                todos = workspaceService.listPendingTodoItems(tenantId, userId);
            } else {
                todos = workspaceService.listTodoItems(tenantId, userId);
            }
        } else {
            todos = List.of();
        }
        List<Map<String, Object>> result = todos.stream()
                .map(t -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("todoId", t.todoId());
                    map.put("userId", t.userId());
                    map.put("type", t.type());
                    map.put("businessType", t.businessType());
                    map.put("businessId", t.businessId());
                    map.put("title", t.title());
                    map.put("status", t.status().name());
                    map.put("dueTime", t.dueTime() != null ? t.dueTime().toString() : null);
                    return map;
                })
                .collect(Collectors.toList());
        return Result.ok(result);
    }

    /**
     * 导出公告列表。
     * <p>
     * 供其他子系统查询已发布的系统公告，
     * 用于OMS域订单页面、WMS域库存页面等嵌入公告展示。
     * </p>
     *
     * @param tenantId 租户ID
     * @return 已发布公告列表
     */
    @GetMapping("/announcements")
    public Result<List<Map<String, Object>>> exportAnnouncements(@RequestHeader("X-Tenant-Id") String tenantId) {
        List<Announcement> announcements = workspaceService.listPublishedAnnouncements(tenantId);
        List<Map<String, Object>> result = announcements.stream()
                .map(a -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("announceId", a.announceId());
                    map.put("title", a.title());
                    map.put("content", a.content());
                    map.put("type", a.type().name());
                    map.put("priority", a.priority());
                    map.put("publishTime", a.publishTime() != null ? a.publishTime().toString() : null);
                    map.put("expireTime", a.expireTime() != null ? a.expireTime().toString() : null);
                    return map;
                })
                .collect(Collectors.toList());
        return Result.ok(result);
    }

    /**
     * 导出日历事件。
     * <p>
     * 供其他子系统查询日历事件数据，
     * 用于TMS域物流日历、SCM域采购日历等关联场景。
     * </p>
     *
     * @param tenantId  租户ID
     * @param userId    用户ID
     * @param eventType 事件类型(可选，OPERATION/TASK/PROMOTION/DEADLINE/REMINDER)
     * @return 日历事件列表
     */
    @GetMapping("/calendar-events")
    public Result<List<Map<String, Object>>> exportCalendarEvents(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String eventType) {
        List<CalendarEvent> events;
        if (userId != null && !userId.isBlank()) {
            events = workspaceService.listUpcomingCalendarEvents(tenantId, userId);
        } else {
            events = List.of();
        }
        if (eventType != null && !eventType.isBlank()) {
            events = events.stream()
                    .filter(e -> e.eventType().name().equals(eventType))
                    .collect(Collectors.toList());
        }
        List<Map<String, Object>> result = events.stream()
                .map(e -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("eventId", e.eventId());
                    map.put("userId", e.userId());
                    map.put("title", e.title());
                    map.put("description", e.description());
                    map.put("eventType", e.eventType().name());
                    map.put("startTime", e.startTime().toString());
                    map.put("endTime", e.endTime().toString());
                    map.put("businessType", e.businessType());
                    map.put("businessId", e.businessId());
                    map.put("allDay", e.allDay());
                    return map;
                })
                .collect(Collectors.toList());
        return Result.ok(result);
    }

    /**
     * 按指标编码导出指标数据。
     * <p>
     * 供其他子系统查询特定指标的当前值，
     * 用于BI域报表数据源、ADS域ROAS指标同步等场景。
     * </p>
     *
     * @param tenantId   租户ID
     * @param metricCode 指标编码
     * @return 指标详情
     */
    @GetMapping("/metrics/{metricCode}")
    public Result<Map<String, Object>> exportMetric(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @PathVariable String metricCode) {
        DashboardMetric metric = dashboardService.getByCode(tenantId, metricCode);
        Map<String, Object> map = new HashMap<>();
        map.put("metricId", metric.metricId());
        map.put("metricCode", metric.metricCode());
        map.put("metricName", metric.metricName());
        map.put("metricValue", metric.metricValue());
        map.put("unit", metric.unit());
        map.put("updatedAt", metric.updatedAt().toString());
        return Result.ok(map);
    }
}
