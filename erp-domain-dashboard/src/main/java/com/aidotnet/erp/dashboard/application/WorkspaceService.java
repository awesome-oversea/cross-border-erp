package com.aidotnet.erp.dashboard.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.dashboard.domain.AIInsightCard;
import com.aidotnet.erp.dashboard.domain.Announcement;
import com.aidotnet.erp.dashboard.domain.AnnouncementStatus;
import com.aidotnet.erp.dashboard.domain.AnnouncementType;
import com.aidotnet.erp.dashboard.domain.CalendarEvent;
import com.aidotnet.erp.dashboard.domain.CalendarEventType;
import com.aidotnet.erp.dashboard.domain.DashboardWidget;
import com.aidotnet.erp.dashboard.domain.HelpArticle;
import com.aidotnet.erp.dashboard.domain.HelpArticleStatus;
import com.aidotnet.erp.dashboard.domain.QuickEntry;
import com.aidotnet.erp.dashboard.domain.TodoItem;
import com.aidotnet.erp.dashboard.domain.TodoStatus;
import com.aidotnet.erp.dashboard.domain.UserDashboard;
import com.aidotnet.erp.dashboard.domain.WidgetStatus;
import com.aidotnet.erp.dashboard.infrastructure.DashboardRepository;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 工作台应用服务
 * <p>
 * 描述: 工作台域核心服务，负责仪表盘布局、公告、帮助文章、待办事项、
 *       日历事件、AI洞察卡片、快捷入口等全生命周期管理。
 *       是用户日常工作的统一入口和操作中心。
 * </p>
 * <p>
 * 核心能力:
 *   1. 仪表盘管理 - 创建/更新/删除用户自定义仪表盘布局
 *   2. 公告管理 - 创建/发布/撤回系统公告
 *   3. 帮助文章 - 创建/发布/归档帮助文档
 *   4. 待办事项 - 创建/开始/完成/取消待办
 *   5. 日历事件 - 创建/更新/删除日历事件
 *   6. AI洞察卡片 - 生成/标记已读AI分析洞察
 *   7. 快捷入口 - 自定义常用功能快速访问
 * </p>
 * <p>
 * 业务规则:
 *   1. 每个用户只能有一个默认仪表盘
 *   2. 公告/文章需发布后才对用户可见
 *   3. 待办状态流转: PENDING → IN_PROGRESS → COMPLETED/CANCELLED
 *   4. AI洞察卡片按严重等级排序展示
 * </p>
 *
 * @author ERP系统
 */
@Service
public class WorkspaceService {

    private final DashboardRepository repository;

    /**
     * 构造函数 - 依赖注入仪表盘仓储
     *
     * @param repository 仪表盘数据仓储
     */
    public WorkspaceService(DashboardRepository repository) {
        this.repository = repository;
    }

    /**
     * 创建用户仪表盘
     * <p>
     * 若设置为默认仪表盘，自动取消原默认仪表盘。
     * </p>
     *
     * @param tenantId 租户ID
     * @param command  创建仪表盘命令
     * @return 新创建的仪表盘
     */
    public UserDashboard createDashboard(String tenantId, CreateDashboardCommand command) {
        Instant now = Instant.now();
        UserDashboard dashboard = new UserDashboard(UUID.randomUUID().toString(), tenantId, command.userId(),
                command.layoutConfig(), command.widgets(), command.isDefault(), now, now);
        if (command.isDefault()) {
            clearExistingDefault(tenantId, command.userId());
        }
        return repository.saveUserDashboard(dashboard);
    }

    public UserDashboard updateDashboard(String tenantId, String dashboardId, UpdateDashboardCommand command) {
        UserDashboard dashboard = getDashboard(tenantId, dashboardId);
        Instant now = Instant.now();
        return repository.saveUserDashboard(new UserDashboard(dashboard.dashboardId(), dashboard.tenantId(),
                dashboard.userId(),
                valueOrDefault(command.layoutConfig(), dashboard.layoutConfig()),
                valueOrDefault(command.widgets(), dashboard.widgets()),
                command.isDefault() != null ? command.isDefault() : dashboard.isDefault(),
                dashboard.createdAt(), now));
    }

    public List<UserDashboard> listDashboards(String tenantId, String userId) {
        return repository.listUserDashboards(tenantId, userId);
    }

    public UserDashboard getDefaultDashboard(String tenantId, String userId) {
        return repository.findDefaultUserDashboard(tenantId, userId)
                .orElseGet(() -> {
                    UserDashboard defaultDb = new UserDashboard(UUID.randomUUID().toString(), tenantId, userId,
                            "{}", "[]", true, Instant.now(), Instant.now());
                    return repository.saveUserDashboard(defaultDb);
                });
    }

    public Announcement createAnnouncement(String tenantId, CreateAnnouncementCommand command) {
        Instant now = Instant.now();
        return repository.saveAnnouncement(new Announcement(UUID.randomUUID().toString(), tenantId,
                command.title(), command.content(), command.type(), command.priority(),
                command.publishTime(), command.expireTime(), AnnouncementStatus.DRAFT, now));
    }

    public Announcement publishAnnouncement(String tenantId, String announceId) {
        Announcement announcement = getAnnouncement(tenantId, announceId);
        if (announcement.status() == AnnouncementStatus.PUBLISHED) {
            throw new BizException("ANNOUNCEMENT_ALREADY_PUBLISHED", "公告已发布");
        }
        return repository.saveAnnouncement(new Announcement(announcement.announceId(), announcement.tenantId(),
                announcement.title(), announcement.content(), announcement.type(), announcement.priority(),
                announcement.publishTime(), announcement.expireTime(), AnnouncementStatus.PUBLISHED, announcement.createdAt()));
    }

    public List<Announcement> listAnnouncements(String tenantId) {
        return repository.listAnnouncements(tenantId);
    }

    public List<Announcement> listPublishedAnnouncements(String tenantId) {
        return repository.listPublishedAnnouncements(tenantId);
    }

    public HelpArticle createHelpArticle(String tenantId, CreateHelpArticleCommand command) {
        Instant now = Instant.now();
        return repository.saveHelpArticle(new HelpArticle(UUID.randomUUID().toString(), tenantId,
                command.title(), command.content(), command.category(), command.tags(),
                0, HelpArticleStatus.DRAFT, now, now));
    }

    public HelpArticle publishHelpArticle(String tenantId, String articleId) {
        HelpArticle article = getHelpArticle(tenantId, articleId);
        if (article.status() == HelpArticleStatus.PUBLISHED) {
            throw new BizException("HELP_ARTICLE_ALREADY_PUBLISHED", "帮助文章已发布");
        }
        return repository.saveHelpArticle(new HelpArticle(article.articleId(), article.tenantId(),
                article.title(), article.content(), article.category(), article.tags(),
                article.viewCount(), HelpArticleStatus.PUBLISHED, article.createdAt(), Instant.now()));
    }

    public HelpArticle viewHelpArticle(String tenantId, String articleId) {
        HelpArticle article = getHelpArticle(tenantId, articleId);
        return repository.saveHelpArticle(new HelpArticle(article.articleId(), article.tenantId(),
                article.title(), article.content(), article.category(), article.tags(),
                article.viewCount() + 1, article.status(), article.createdAt(), Instant.now()));
    }

    public List<HelpArticle> listHelpArticles(String tenantId) {
        return repository.listHelpArticles(tenantId);
    }

    public List<HelpArticle> listPublishedHelpArticles(String tenantId) {
        return repository.listPublishedHelpArticles(tenantId);
    }

    public List<HelpArticle> listHelpArticlesByCategory(String tenantId, String category) {
        return repository.listHelpArticlesByCategory(tenantId, category);
    }

    public TodoItem createTodoItem(String tenantId, CreateTodoCommand command) {
        Instant now = Instant.now();
        return repository.saveTodoItem(new TodoItem(UUID.randomUUID().toString(), tenantId, command.userId(),
                command.type(), command.businessType(), command.businessId(), command.title(),
                TodoStatus.PENDING, command.dueTime(), now, now));
    }

    public TodoItem completeTodoItem(String tenantId, String todoId) {
        TodoItem todo = getTodoItem(tenantId, todoId);
        if (todo.status() == TodoStatus.COMPLETED || todo.status() == TodoStatus.CANCELLED) {
            throw new BizException("TODO_STATUS_INVALID", "待办事项已完成或已取消");
        }
        return repository.saveTodoItem(new TodoItem(todo.todoId(), todo.tenantId(), todo.userId(),
                todo.type(), todo.businessType(), todo.businessId(), todo.title(),
                TodoStatus.COMPLETED, todo.dueTime(), todo.createdAt(), Instant.now()));
    }

    public TodoItem startTodoItem(String tenantId, String todoId) {
        TodoItem todo = getTodoItem(tenantId, todoId);
        if (todo.status() != TodoStatus.PENDING) {
            throw new BizException("TODO_STATUS_INVALID", "只有待处理的待办事项可以开始");
        }
        return repository.saveTodoItem(new TodoItem(todo.todoId(), todo.tenantId(), todo.userId(),
                todo.type(), todo.businessType(), todo.businessId(), todo.title(),
                TodoStatus.IN_PROGRESS, todo.dueTime(), todo.createdAt(), Instant.now()));
    }

    public List<TodoItem> listTodoItems(String tenantId, String userId) {
        return repository.listTodoItems(tenantId, userId);
    }

    public List<TodoItem> listPendingTodoItems(String tenantId, String userId) {
        return repository.listPendingTodoItems(tenantId, userId);
    }

    private UserDashboard getDashboard(String tenantId, String dashboardId) {
        return repository.findUserDashboard(tenantId, dashboardId)
                .orElseThrow(() -> new BizException("DASHBOARD_NOT_FOUND", "仪表盘不存在"));
    }

    private void clearExistingDefault(String tenantId, String userId) {
        repository.findDefaultUserDashboard(tenantId, userId).ifPresent(existing -> {
            repository.saveUserDashboard(new UserDashboard(existing.dashboardId(), existing.tenantId(),
                    existing.userId(), existing.layoutConfig(), existing.widgets(), false, existing.createdAt(), Instant.now()));
        });
    }

    private Announcement getAnnouncement(String tenantId, String announceId) {
        return repository.findAnnouncement(tenantId, announceId)
                .orElseThrow(() -> new BizException("ANNOUNCEMENT_NOT_FOUND", "公告不存在"));
    }

    private HelpArticle getHelpArticle(String tenantId, String articleId) {
        return repository.findHelpArticle(tenantId, articleId)
                .orElseThrow(() -> new BizException("HELP_ARTICLE_NOT_FOUND", "帮助文章不存在"));
    }

    private TodoItem getTodoItem(String tenantId, String todoId) {
        return repository.findTodoItem(tenantId, todoId)
                .orElseThrow(() -> new BizException("TODO_ITEM_NOT_FOUND", "待办事项不存在"));
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value != null ? value : defaultValue;
    }

    public record CreateDashboardCommand(@NotBlank String userId, String layoutConfig, String widgets, boolean isDefault) {}

    public record UpdateDashboardCommand(String layoutConfig, String widgets, Boolean isDefault) {}

    public record CreateAnnouncementCommand(@NotBlank String title, @NotBlank String content,
                                            AnnouncementType type, int priority,
                                            Instant publishTime, Instant expireTime) {}

    public record CreateHelpArticleCommand(@NotBlank String title, @NotBlank String content,
                                           @NotBlank String category, String tags) {}

    public record CreateTodoCommand(@NotBlank String userId, String type, String businessType,
                                    String businessId, @NotBlank String title, Instant dueTime) {}

    @Transactional
    public DashboardWidget createDashboardWidget(String tenantId, CreateWidgetCommand command) {
        Instant now = Instant.now();
        DashboardWidget widget = new DashboardWidget(UUID.randomUUID().toString(), tenantId, command.type(),
                command.name(), command.dataSource(), command.refreshRateSeconds(), command.config(),
                WidgetStatus.ACTIVE, command.category(), command.description(), now, now);
        return repository.saveDashboardWidget(widget);
    }

    @Transactional
    public DashboardWidget updateDashboardWidget(String tenantId, String widgetId, UpdateWidgetCommand command) {
        DashboardWidget existing = getDashboardWidget(tenantId, widgetId);
        return repository.saveDashboardWidget(new DashboardWidget(existing.widgetId(), existing.tenantId(),
                existing.type(),
                command.name() != null ? command.name() : existing.name(),
                command.dataSource() != null ? command.dataSource() : existing.dataSource(),
                command.refreshRateSeconds() > 0 ? command.refreshRateSeconds() : existing.refreshRateSeconds(),
                command.config() != null ? command.config() : existing.config(),
                existing.status(), existing.category(),
                command.description() != null ? command.description() : existing.description(),
                existing.createdAt(), Instant.now()));
    }

    public List<DashboardWidget> listDashboardWidgets(String tenantId, String category) {
        return repository.listDashboardWidgets(tenantId, category);
    }

    public List<DashboardWidget> listActiveDashboardWidgets(String tenantId) {
        return repository.listActiveDashboardWidgets(tenantId);
    }

    private DashboardWidget getDashboardWidget(String tenantId, String widgetId) {
        return repository.findDashboardWidget(tenantId, widgetId)
                .orElseThrow(() -> new BizException("WIDGET_NOT_FOUND", "组件不存在"));
    }

    @Transactional
    public CalendarEvent createCalendarEvent(String tenantId, CreateCalendarEventCommand command) {
        Instant now = Instant.now();
        CalendarEvent event = new CalendarEvent(UUID.randomUUID().toString(), tenantId, command.userId(),
                command.title(), command.description(), command.eventType(), command.startTime(),
                command.endTime(), command.businessType(), command.businessId(), command.color(),
                command.allDay(), command.reminder(), now, now);
        return repository.saveCalendarEvent(event);
    }

    @Transactional
    public CalendarEvent updateCalendarEvent(String tenantId, String eventId, UpdateCalendarEventCommand command) {
        CalendarEvent existing = getCalendarEvent(tenantId, eventId);
        return repository.saveCalendarEvent(new CalendarEvent(existing.eventId(), existing.tenantId(),
                existing.userId(),
                command.title() != null ? command.title() : existing.title(),
                command.description() != null ? command.description() : existing.description(),
                existing.eventType(),
                command.startTime() != null ? command.startTime() : existing.startTime(),
                command.endTime() != null ? command.endTime() : existing.endTime(),
                existing.businessType(), existing.businessId(),
                command.color() != null ? command.color() : existing.color(),
                existing.allDay(), existing.reminder(), existing.createdAt(), Instant.now()));
    }

    @Transactional
    public void deleteCalendarEvent(String tenantId, String eventId) {
        repository.deleteCalendarEvent(tenantId, eventId);
    }

    public List<CalendarEvent> listCalendarEvents(String tenantId, String userId, Instant startTime, Instant endTime) {
        return repository.listCalendarEvents(tenantId, userId, startTime, endTime);
    }

    public List<CalendarEvent> listUpcomingCalendarEvents(String tenantId, String userId) {
        return repository.listUpcomingCalendarEvents(tenantId, userId, Instant.now());
    }

    private CalendarEvent getCalendarEvent(String tenantId, String eventId) {
        return repository.findCalendarEvent(tenantId, eventId)
                .orElseThrow(() -> new BizException("CALENDAR_EVENT_NOT_FOUND", "日历事件不存在"));
    }

    @Transactional
    public AIInsightCard createAIInsightCard(String tenantId, CreateAIInsightCardCommand command) {
        Instant now = Instant.now();
        AIInsightCard card = new AIInsightCard(UUID.randomUUID().toString(), tenantId, command.userId(),
                command.title(), command.summary(), command.insightType(), command.severity(),
                command.data(), command.sourceDomain(), command.suggestion(), command.actionUrl(),
                false, false, command.validUntil(), now, now);
        return repository.saveAIInsightCard(card);
    }

    @Transactional
    public void markAIInsightCardRead(String tenantId, String cardId) {
        repository.findAIInsightCard(tenantId, cardId)
                .orElseThrow(() -> new BizException("INSIGHT_CARD_NOT_FOUND", "洞察卡片不存在"));
        repository.markAIInsightCardRead(tenantId, cardId, true);
    }

    @Transactional
    public void dismissAIInsightCard(String tenantId, String cardId) {
        repository.findAIInsightCard(tenantId, cardId)
                .orElseThrow(() -> new BizException("INSIGHT_CARD_NOT_FOUND", "洞察卡片不存在"));
        repository.dismissAIInsightCard(tenantId, cardId, true);
    }

    public List<AIInsightCard> listAIInsightCards(String tenantId, String userId, String insightType) {
        return repository.listAIInsightCards(tenantId, userId, insightType);
    }

    public List<AIInsightCard> listUnreadAIInsightCards(String tenantId, String userId) {
        return repository.listUnreadAIInsightCards(tenantId, userId);
    }

    @Transactional
    public QuickEntry createQuickEntry(String tenantId, CreateQuickEntryCommand command) {
        Instant now = Instant.now();
        QuickEntry entry = new QuickEntry(UUID.randomUUID().toString(), tenantId, command.userId(),
                command.entryCode(), command.entryName(), command.icon(), command.url(),
                command.category(), command.sortOrder(), now, now);
        return repository.saveQuickEntry(entry);
    }

    @Transactional
    public QuickEntry updateQuickEntry(String tenantId, String entryId, UpdateQuickEntryCommand command) {
        List<QuickEntry> entries = repository.listQuickEntries(tenantId, null);
        QuickEntry existing = entries.stream()
                .filter(e -> e.entryId().equals(entryId))
                .findFirst()
                .orElseThrow(() -> new BizException("QUICK_ENTRY_NOT_FOUND", "快捷入口不存在"));
        return repository.saveQuickEntry(new QuickEntry(existing.entryId(), existing.tenantId(), existing.userId(),
                existing.entryCode(),
                command.entryName() != null ? command.entryName() : existing.entryName(),
                command.icon() != null ? command.icon() : existing.icon(),
                command.url() != null ? command.url() : existing.url(),
                command.category() != null ? command.category() : existing.category(),
                command.sortOrder() >= 0 ? command.sortOrder() : existing.sortOrder(),
                existing.createdAt(), Instant.now()));
    }

    @Transactional
    public void deleteQuickEntry(String tenantId, String entryId) {
        repository.deleteQuickEntry(tenantId, entryId);
    }

    public List<QuickEntry> listQuickEntries(String tenantId, String userId) {
        return repository.listQuickEntries(tenantId, userId);
    }

    public record CreateWidgetCommand(@NotBlank String type, @NotBlank String name, String dataSource,
                                       int refreshRateSeconds, Map<String, Object> config,
                                       String category, String description) {}
    public record UpdateWidgetCommand(String name, String dataSource, int refreshRateSeconds,
                                       Map<String, Object> config, String description) {}
    public record CreateCalendarEventCommand(@NotBlank String userId, @NotBlank String title, String description,
                                              CalendarEventType eventType, Instant startTime, Instant endTime,
                                              String businessType, String businessId, String color,
                                              boolean allDay, String reminder) {}
    public record UpdateCalendarEventCommand(String title, String description, Instant startTime,
                                              Instant endTime, String color) {}
    public record CreateAIInsightCardCommand(@NotBlank String userId, @NotBlank String title, String summary,
                                              String insightType, String severity, Map<String, Object> data,
                                              String sourceDomain, String suggestion, String actionUrl,
                                              Instant validUntil) {}
    public record CreateQuickEntryCommand(@NotBlank String userId, @NotBlank String entryCode,
                                           @NotBlank String entryName, String icon, String url,
                                           String category, int sortOrder) {}
    public record UpdateQuickEntryCommand(String entryName, String icon, String url,
                                           String category, int sortOrder) {}
}
