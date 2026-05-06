package com.aidotnet.erp.dashboard.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.dashboard.application.WorkspaceService;
import com.aidotnet.erp.dashboard.application.WorkspaceService.CreateCalendarEventCommand;
import com.aidotnet.erp.dashboard.application.WorkspaceService.UpdateCalendarEventCommand;
import com.aidotnet.erp.dashboard.domain.CalendarEvent;
import com.aidotnet.erp.dashboard.domain.CalendarEventType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 日历事件入站控制器，提供日历事件的RESTful API。
 * <p>
 * 描述: 管理工作台日历事件，支持创建、更新、删除和查询日历事件。
 *       日历事件可关联业务对象(如订单交付、采购到货等)，实现业务与日历的联动。
 * </p>
 * <p>
 * 接口规范:
 *   - 路径: /dashboard/api/in/v1/calendar
 *   - 认证: 需登录用户，通过Token获取租户ID
 *   - 返回: 统一Result包装，包含code/msg/data
 * </p>
 * <p>
 * 事件类型:
 *   - OPERATION: 运营事件，如促销活动、库存盘点
 *   - TASK: 任务截止，如审批截止、报表提交
 *   - PROMOTION: 促销活动，如限时折扣、秒杀
 *   - DEADLINE: 截止日期，如付款截止、发货截止
 *   - REMINDER: 自定义提醒
 * </p>
 *
 * @author ERP系统
 */
@RestController
@RequestMapping("/dashboard/api/in/v1/calendar")
public class CalendarController {

    private final WorkspaceService workspaceService;

    /**
     * 构造函数 - 依赖注入工作台服务
     *
     * @param workspaceService 工作台应用服务
     */
    public CalendarController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    /**
     * 创建日历事件。
     *
     * @param request 创建日历事件请求体
     * @return 新创建的日历事件
     */
    @PostMapping("/events")
    public Result<CalendarEvent> createEvent(@Valid @RequestBody CreateCalendarEventRequest request) {
        return Result.ok(workspaceService.createCalendarEvent(currentTenant(), new CreateCalendarEventCommand(
                request.userId(), request.title(), request.description(), request.eventType(),
                request.startTime(), request.endTime(), request.businessType(), request.businessId(),
                request.color(), request.allDay(), request.reminder())));
    }

    /**
     * 更新日历事件。
     *
     * @param eventId 事件ID
     * @param request 更新日历事件请求体
     * @return 更新后的日历事件
     */
    @PutMapping("/events/{eventId}")
    public Result<CalendarEvent> updateEvent(@PathVariable String eventId, @Valid @RequestBody UpdateCalendarEventRequest request) {
        return Result.ok(workspaceService.updateCalendarEvent(currentTenant(), eventId, new UpdateCalendarEventCommand(
                request.title(), request.description(), request.startTime(), request.endTime(), request.color())));
    }

    /**
     * 删除日历事件。
     *
     * @param eventId 事件ID
     * @return 操作结果
     */
    @DeleteMapping("/events/{eventId}")
    public Result<Void> deleteEvent(@PathVariable String eventId) {
        workspaceService.deleteCalendarEvent(currentTenant(), eventId);
        return Result.ok(null);
    }

    /**
     * 查询日历事件列表。
     * <p>
     * 支持按时间范围筛选，不传时间范围则返回用户所有事件。
     * </p>
     *
     * @param userId    用户ID
     * @param startTime 开始时间(可选)
     * @param endTime   结束时间(可选)
     * @return 日历事件列表
     */
    @GetMapping("/events")
    public Result<List<CalendarEvent>> listEvents(@RequestParam String userId,
                                                   @RequestParam(required = false) Instant startTime,
                                                   @RequestParam(required = false) Instant endTime) {
        return Result.ok(workspaceService.listCalendarEvents(currentTenant(), userId, startTime, endTime));
    }

    /**
     * 查询即将到来的日历事件。
     * <p>
     * 返回当前时间之后的未来事件，按开始时间升序排列。
     * </p>
     *
     * @param userId 用户ID
     * @return 即将到来的日历事件列表
     */
    @GetMapping("/events/upcoming")
    public Result<List<CalendarEvent>> listUpcomingEvents(@RequestParam String userId) {
        return Result.ok(workspaceService.listUpcomingCalendarEvents(currentTenant(), userId));
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
     * 创建日历事件请求体
     *
     * @param userId       用户ID，必填
     * @param title        事件标题，必填
     * @param description  事件描述
     * @param eventType    事件类型: OPERATION/TASK/PROMOTION/DEADLINE/REMINDER
     * @param startTime    开始时间
     * @param endTime      结束时间
     * @param businessType 关联业务类型，如 ORDER/PURCHASE/SHIPMENT
     * @param businessId   关联业务ID
     * @param color        显示颜色，如 #FF5733
     * @param allDay       是否全天事件
     * @param reminder     提醒设置，如 5m/1h/1d
     */
    public record CreateCalendarEventRequest(
            @NotBlank String userId, @NotBlank String title, String description,
            CalendarEventType eventType, Instant startTime, Instant endTime,
            String businessType, String businessId, String color,
            boolean allDay, String reminder) {}

    /**
     * 更新日历事件请求体
     *
     * @param title       事件标题
     * @param description 事件描述
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @param color       显示颜色
     */
    public record UpdateCalendarEventRequest(
            String title, String description, Instant startTime, Instant endTime, String color) {}
}
