package com.aidotnet.erp.dashboard.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.dashboard.application.WorkspaceService;
import com.aidotnet.erp.dashboard.application.WorkspaceService.CreateAnnouncementCommand;
import com.aidotnet.erp.dashboard.application.WorkspaceService.CreateDashboardCommand;
import com.aidotnet.erp.dashboard.application.WorkspaceService.CreateHelpArticleCommand;
import com.aidotnet.erp.dashboard.application.WorkspaceService.CreateTodoCommand;
import com.aidotnet.erp.dashboard.application.WorkspaceService.UpdateDashboardCommand;
import com.aidotnet.erp.dashboard.domain.Announcement;
import com.aidotnet.erp.dashboard.domain.AnnouncementType;
import com.aidotnet.erp.dashboard.domain.HelpArticle;
import com.aidotnet.erp.dashboard.domain.TodoItem;
import com.aidotnet.erp.dashboard.domain.UserDashboard;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 工作台入站控制器，提供仪表盘布局、公告、帮助文章和待办事项的RESTful API。
 * <p>
 * 描述: 工作台域核心控制器，管理用户仪表盘配置、系统公告发布、
 *       帮助文档浏览和待办事项处理等日常办公功能。
 * </p>
 * <p>
 * 接口规范:
 *   - 路径: /dashboard/api/in/v1
 *   - 认证: 需登录用户，通过Token获取租户ID
 *   - 返回: 统一Result包装，包含code/msg/data
 * </p>
 * <p>
 * 功能模块:
 *   1. 仪表盘管理 - /dashboards: 创建/更新/查询用户自定义仪表盘
 *   2. 公告管理 - /announcements: 创建/发布/查询系统公告
 *   3. 帮助文章 - /help-articles: 创建/发布/浏览帮助文档
 *   4. 待办事项 - /todo-items: 创建/开始/完成待办
 * </p>
 *
 * @author ERP系统
 */
@RestController
@RequestMapping("/dashboard/api/in/v1")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    /**
     * 构造函数 - 依赖注入工作台服务
     *
     * @param workspaceService 工作台应用服务
     */
    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    /**
     * 创建用户仪表盘。
     * <p>
     * 若设置为默认仪表盘，自动取消原默认仪表盘。
     * </p>
     *
     * @param request 创建仪表盘请求体
     * @return 新创建的仪表盘
     */
    @PostMapping("/dashboards")
    public Result<UserDashboard> createDashboard(@Valid @RequestBody CreateDashboardRequest request) {
        return Result.ok(workspaceService.createDashboard(currentTenant(), new CreateDashboardCommand(
                request.userId(), request.layoutConfig(), request.widgets(), request.isDefault())));
    }

    /**
     * 更新用户仪表盘。
     *
     * @param dashboardId 仪表盘ID
     * @param request     更新仪表盘请求体
     * @return 更新后的仪表盘
     */
    @PutMapping("/dashboards/{dashboardId}")
    public Result<UserDashboard> updateDashboard(@PathVariable String dashboardId, @Valid @RequestBody UpdateDashboardRequest request) {
        return Result.ok(workspaceService.updateDashboard(currentTenant(), dashboardId, new UpdateDashboardCommand(
                request.layoutConfig(), request.widgets(), request.isDefault())));
    }

    /**
     * 查询用户仪表盘列表。
     *
     * @param userId 用户ID
     * @return 仪表盘列表
     */
    @GetMapping("/dashboards")
    public Result<List<UserDashboard>> listDashboards(String userId) {
        return Result.ok(workspaceService.listDashboards(currentTenant(), userId));
    }

    /**
     * 获取用户默认仪表盘。
     * <p>
     * 若用户无默认仪表盘，自动创建一个空默认仪表盘。
     * </p>
     *
     * @param userId 用户ID
     * @return 默认仪表盘
     */
    @GetMapping("/dashboards/default")
    public Result<UserDashboard> getDefaultDashboard(String userId) {
        return Result.ok(workspaceService.getDefaultDashboard(currentTenant(), userId));
    }

    /**
     * 创建系统公告。
     * <p>
     * 公告创建后为DRAFT状态，需调用发布接口后才对用户可见。
     * </p>
     *
     * @param request 创建公告请求体
     * @return 新创建的公告(DRAFT状态)
     */
    @PostMapping("/announcements")
    public Result<Announcement> createAnnouncement(@Valid @RequestBody CreateAnnouncementRequest request) {
        return Result.ok(workspaceService.createAnnouncement(currentTenant(), new CreateAnnouncementCommand(
                request.title(), request.content(), request.type(), request.priority(),
                request.publishTime(), request.expireTime())));
    }

    /**
     * 发布公告。
     * <p>
     * 将DRAFT状态公告发布为PUBLISHED状态，发布后对用户可见。
     * </p>
     *
     * @param announceId 公告ID
     * @return 发布后的公告
     */
    @PatchMapping("/announcements/{announceId}/publish")
    public Result<Announcement> publishAnnouncement(@PathVariable String announceId) {
        return Result.ok(workspaceService.publishAnnouncement(currentTenant(), announceId));
    }

    /**
     * 查询所有公告(包含草稿)。
     *
     * @return 公告列表
     */
    @GetMapping("/announcements")
    public Result<List<Announcement>> listAnnouncements() {
        return Result.ok(workspaceService.listAnnouncements(currentTenant()));
    }

    /**
     * 查询已发布公告(用户可见)。
     *
     * @return 已发布公告列表
     */
    @GetMapping("/announcements/published")
    public Result<List<Announcement>> listPublishedAnnouncements() {
        return Result.ok(workspaceService.listPublishedAnnouncements(currentTenant()));
    }

    /**
     * 创建帮助文章。
     * <p>
     * 文章创建后为DRAFT状态，需调用发布接口后才对用户可见。
     * </p>
     *
     * @param request 创建帮助文章请求体
     * @return 新创建的帮助文章(DRAFT状态)
     */
    @PostMapping("/help-articles")
    public Result<HelpArticle> createHelpArticle(@Valid @RequestBody CreateHelpArticleRequest request) {
        return Result.ok(workspaceService.createHelpArticle(currentTenant(), new CreateHelpArticleCommand(
                request.title(), request.content(), request.category(), request.tags())));
    }

    /**
     * 发布帮助文章。
     *
     * @param articleId 文章ID
     * @return 发布后的帮助文章
     */
    @PatchMapping("/help-articles/{articleId}/publish")
    public Result<HelpArticle> publishHelpArticle(@PathVariable String articleId) {
        return Result.ok(workspaceService.publishHelpArticle(currentTenant(), articleId));
    }

    /**
     * 浏览帮助文章(自动递增浏览量)。
     *
     * @param articleId 文章ID
     * @return 浏览后的帮助文章(浏览量+1)
     */
    @PatchMapping("/help-articles/{articleId}/view")
    public Result<HelpArticle> viewHelpArticle(@PathVariable String articleId) {
        return Result.ok(workspaceService.viewHelpArticle(currentTenant(), articleId));
    }

    /**
     * 查询所有帮助文章(包含草稿)。
     *
     * @return 帮助文章列表
     */
    @GetMapping("/help-articles")
    public Result<List<HelpArticle>> listHelpArticles() {
        return Result.ok(workspaceService.listHelpArticles(currentTenant()));
    }

    /**
     * 查询已发布帮助文章(用户可见)。
     *
     * @return 已发布帮助文章列表
     */
    @GetMapping("/help-articles/published")
    public Result<List<HelpArticle>> listPublishedHelpArticles() {
        return Result.ok(workspaceService.listPublishedHelpArticles(currentTenant()));
    }

    /**
     * 按分类查询已发布帮助文章。
     *
     * @param category 文章分类
     * @return 帮助文章列表
     */
    @GetMapping("/help-articles/category/{category}")
    public Result<List<HelpArticle>> listHelpArticlesByCategory(@PathVariable String category) {
        return Result.ok(workspaceService.listHelpArticlesByCategory(currentTenant(), category));
    }

    /**
     * 创建待办事项。
     * <p>
     * 待办事项可关联业务对象(如订单、发货单等)，创建后为PENDING状态。
     * </p>
     *
     * @param request 创建待办请求体
     * @return 新创建的待办事项(PENDING状态)
     */
    @PostMapping("/todo-items")
    public Result<TodoItem> createTodoItem(@Valid @RequestBody CreateTodoRequest request) {
        return Result.ok(workspaceService.createTodoItem(currentTenant(), new CreateTodoCommand(
                request.userId(), request.type(), request.businessType(), request.businessId(),
                request.title(), request.dueTime())));
    }

    /**
     * 完成待办事项。
     * <p>
     * 将待办事项状态从IN_PROGRESS变更为COMPLETED。
     * </p>
     *
     * @param todoId 待办ID
     * @return 完成后的待办事项
     */
    @PatchMapping("/todo-items/{todoId}/complete")
    public Result<TodoItem> completeTodoItem(@PathVariable String todoId) {
        return Result.ok(workspaceService.completeTodoItem(currentTenant(), todoId));
    }

    /**
     * 开始处理待办事项。
     * <p>
     * 将待办事项状态从PENDING变更为IN_PROGRESS。
     * </p>
     *
     * @param todoId 待办ID
     * @return 开始处理后的待办事项
     */
    @PatchMapping("/todo-items/{todoId}/start")
    public Result<TodoItem> startTodoItem(@PathVariable String todoId) {
        return Result.ok(workspaceService.startTodoItem(currentTenant(), todoId));
    }

    /**
     * 查询用户所有待办事项。
     *
     * @param userId 用户ID
     * @return 待办事项列表
     */
    @GetMapping("/todo-items")
    public Result<List<TodoItem>> listTodoItems(String userId) {
        return Result.ok(workspaceService.listTodoItems(currentTenant(), userId));
    }

    /**
     * 查询用户待处理待办事项。
     *
     * @param userId 用户ID
     * @return 待处理待办事项列表
     */
    @GetMapping("/todo-items/pending")
    public Result<List<TodoItem>> listPendingTodoItems(String userId) {
        return Result.ok(workspaceService.listPendingTodoItems(currentTenant(), userId));
    }

    /**
     * 获取当前租户ID。
     * <p>
     * 从TenantContext中获取当前登录用户的租户ID，
     * 若租户ID为空则抛出TENANT_REQUIRED异常。
     * </p>
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
     * 创建仪表盘请求体
     *
     * @param userId       用户ID，必填
     * @param layoutConfig 布局配置JSON
     * @param widgets      组件列表JSON
     * @param isDefault    是否为默认仪表盘
     */
    public record CreateDashboardRequest(@NotBlank String userId, String layoutConfig, String widgets,
                                         boolean isDefault) {}

    /**
     * 更新仪表盘请求体
     *
     * @param layoutConfig 布局配置JSON
     * @param widgets      组件列表JSON
     * @param isDefault    是否为默认仪表盘
     */
    public record UpdateDashboardRequest(String layoutConfig, String widgets, Boolean isDefault) {}

    /**
     * 创建公告请求体
     *
     * @param title       公告标题，必填
     * @param content     公告内容，必填
     * @param type        公告类型: SYSTEM/BUSINESS/URGENT
     * @param priority    优先级，数值越大越靠前
     * @param publishTime 计划发布时间
     * @param expireTime  过期时间
     */
    public record CreateAnnouncementRequest(@NotBlank String title, @NotBlank String content,
                                            AnnouncementType type, int priority,
                                            Instant publishTime, Instant expireTime) {}

    /**
     * 创建帮助文章请求体
     *
     * @param title    文章标题，必填
     * @param content  文章内容，必填
     * @param category 文章分类，必填
     * @param tags     文章标签，逗号分隔
     */
    public record CreateHelpArticleRequest(@NotBlank String title, @NotBlank String content,
                                           @NotBlank String category, String tags) {}

    /**
     * 创建待办请求体
     *
     * @param userId       负责人ID，必填
     * @param type         待办类型: APPROVAL/EXCEPTION/REVIEW
     * @param businessType 关联业务类型，如 ORDER/PURCHASE/SHIPMENT
     * @param businessId   关联业务单据ID
     * @param title        待办标题，必填
     * @param dueTime      截止时间
     */
    public record CreateTodoRequest(@NotBlank String userId, String type, String businessType,
                                    String businessId, @NotBlank String title, Instant dueTime) {}
}
