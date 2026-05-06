package com.aidotnet.erp.dashboard.infrastructure;

import com.aidotnet.erp.dashboard.domain.AIInsightCard;
import com.aidotnet.erp.dashboard.domain.Announcement;
import com.aidotnet.erp.dashboard.domain.AnnouncementStatus;
import com.aidotnet.erp.dashboard.domain.AnnouncementType;
import com.aidotnet.erp.dashboard.domain.CalendarEvent;
import com.aidotnet.erp.dashboard.domain.DashboardWidget;
import com.aidotnet.erp.dashboard.domain.HelpArticle;
import com.aidotnet.erp.dashboard.domain.HelpArticleStatus;
import com.aidotnet.erp.dashboard.domain.QuickEntry;
import com.aidotnet.erp.dashboard.domain.TodoItem;
import com.aidotnet.erp.dashboard.domain.TodoStatus;
import com.aidotnet.erp.dashboard.domain.UserDashboard;
import com.aidotnet.erp.dashboard.domain.WidgetStatus;
import com.aidotnet.erp.dashboard.infrastructure.data.AIInsightCardDO;
import com.aidotnet.erp.dashboard.infrastructure.data.AnnouncementDO;
import com.aidotnet.erp.dashboard.infrastructure.data.CalendarEventDO;
import com.aidotnet.erp.dashboard.infrastructure.data.DashboardWidgetDO;
import com.aidotnet.erp.dashboard.infrastructure.data.HelpArticleDO;
import com.aidotnet.erp.dashboard.infrastructure.data.QuickEntryDO;
import com.aidotnet.erp.dashboard.infrastructure.data.TodoItemDO;
import com.aidotnet.erp.dashboard.infrastructure.data.UserDashboardDO;
import com.aidotnet.erp.dashboard.infrastructure.mapper.DashboardMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

/**
 * 仪表盘数据仓储实现
 * <p>
 * 描述: 负责仪表盘域所有实体(除DashboardMetric外)的CRUD操作，包括用户仪表盘配置、
 *       公告、帮助文章、待办事项、仪表盘组件、日历事件、AI洞察卡片和快捷入口等。
 *       DashboardMetric由DashboardStore单独管理。
 * </p>
 * <p>
 * 核心职责:
 *   1. 用户仪表盘 - 创建/更新/查询用户自定义仪表盘布局，支持默认仪表盘管理
 *   2. 公告管理 - 创建/删除/查询系统公告，支持按发布状态筛选
 *   3. 帮助文章 - 创建/更新/删除/查询帮助文档，支持按分类和状态筛选
 *   4. 待办事项 - 创建/更新/删除/查询待办事项，支持按状态筛选
 *   5. 仪表盘组件 - 创建/更新/删除/查询组件配置，支持按分类和状态筛选
 *   6. 日历事件 - 创建/更新/删除/查询日历事件，支持时间范围和即将到来筛选
 *   7. AI洞察卡片 - 创建/删除/查询/标记已读/忽略AI洞察，支持按类型和未读状态筛选
 *   8. 快捷入口 - 创建/更新/删除/查询快捷入口，支持按用户筛选
 * </p>
 * <p>
 * 设计说明:
 *   1. 采用DO(数据对象)与领域对象分离模式，通过转换方法实现层间数据映射
 *   2. JSONB字段(如config/data)通过Jackson序列化/反序列化处理
 *   3. 所有操作均带租户隔离(tenantId)，确保多租户数据安全
 *   4. save方法采用upsert模式(存在则更新，不存在则插入)
 * </p>
 *
 * @author ERP系统
 * @see DashboardMapper
 */
@Repository
public class DashboardRepository {

    private final DashboardMapper mapper;
    private final ObjectMapper objectMapper;

    /**
     * 构造函数 - 依赖注入映射器和JSON序列化器
     *
     * @param mapper        MyBatis仪表盘映射接口
     * @param objectMapper  Jackson JSON序列化/反序列化器
     */
    public DashboardRepository(DashboardMapper mapper, ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    /* ================================ 用户仪表盘 ================================ */

    /**
     * 保存用户仪表盘(存在则更新，不存在则插入)
     *
     * @param dashboard 用户仪表盘领域对象
     * @return 保存后的用户仪表盘
     */
    public UserDashboard saveUserDashboard(UserDashboard dashboard) {
        UserDashboardDO existing = mapper.selectUserDashboard(dashboard.tenantId(), dashboard.dashboardId());
        UserDashboardDO data = toUserDashboardData(dashboard);
        if (existing == null) {
            mapper.insertUserDashboard(data);
        } else {
            mapper.updateUserDashboard(data);
        }
        return dashboard;
    }

    /**
     * 按ID查询用户仪表盘
     *
     * @param tenantId    租户ID
     * @param dashboardId 仪表盘ID
     * @return 用户仪表盘(可能为空)
     */
    public Optional<UserDashboard> findUserDashboard(String tenantId, String dashboardId) {
        return Optional.ofNullable(mapper.selectUserDashboard(tenantId, dashboardId)).map(this::toUserDashboardDomain);
    }

    /**
     * 查询用户默认仪表盘
     *
     * @param tenantId 租户ID
     * @param userId   用户ID
     * @return 默认仪表盘(可能为空)
     */
    public Optional<UserDashboard> findDefaultUserDashboard(String tenantId, String userId) {
        return Optional.ofNullable(mapper.selectDefaultUserDashboard(tenantId, userId)).map(this::toUserDashboardDomain);
    }

    /**
     * 查询用户所有仪表盘
     *
     * @param tenantId 租户ID
     * @param userId   用户ID
     * @return 仪表盘列表
     */
    public List<UserDashboard> listUserDashboards(String tenantId, String userId) {
        return mapper.selectUserDashboards(tenantId, userId).stream().map(this::toUserDashboardDomain).collect(Collectors.toList());
    }

    /* ================================ 公告 ================================ */

    /**
     * 保存公告
     *
     * @param announcement 公告领域对象
     * @return 保存后的公告
     */
    public Announcement saveAnnouncement(Announcement announcement) {
        mapper.insertAnnouncement(toAnnouncementData(announcement));
        return announcement;
    }

    /**
     * 删除公告
     *
     * @param tenantId   租户ID
     * @param announceId 公告ID
     */
    public void deleteAnnouncement(String tenantId, String announceId) {
        mapper.deleteAnnouncement(tenantId, announceId);
    }

    /**
     * 按ID查询公告
     *
     * @param tenantId   租户ID
     * @param announceId 公告ID
     * @return 公告(可能为空)
     */
    public Optional<Announcement> findAnnouncement(String tenantId, String announceId) {
        return Optional.ofNullable(mapper.selectAnnouncement(tenantId, announceId)).map(this::toAnnouncementDomain);
    }

    /**
     * 查询租户下所有公告
     *
     * @param tenantId 租户ID
     * @return 公告列表
     */
    public List<Announcement> listAnnouncements(String tenantId) {
        return mapper.selectAnnouncements(tenantId).stream().map(this::toAnnouncementDomain).collect(Collectors.toList());
    }

    /**
     * 查询已发布公告
     *
     * @param tenantId 租户ID
     * @return 已发布公告列表
     */
    public List<Announcement> listPublishedAnnouncements(String tenantId) {
        return mapper.selectPublishedAnnouncements(tenantId).stream().map(this::toAnnouncementDomain).collect(Collectors.toList());
    }

    /* ================================ 帮助文章 ================================ */

    /**
     * 保存帮助文章(存在则更新，不存在则插入)
     *
     * @param article 帮助文章领域对象
     * @return 保存后的帮助文章
     */
    public HelpArticle saveHelpArticle(HelpArticle article) {
        HelpArticleDO existing = mapper.selectHelpArticle(article.tenantId(), article.articleId());
        HelpArticleDO data = toHelpArticleData(article);
        if (existing == null) {
            mapper.insertHelpArticle(data);
        } else {
            mapper.updateHelpArticle(data);
        }
        return article;
    }

    /**
     * 删除帮助文章
     *
     * @param tenantId  租户ID
     * @param articleId 文章ID
     */
    public void deleteHelpArticle(String tenantId, String articleId) {
        mapper.deleteHelpArticle(tenantId, articleId);
    }

    /**
     * 按ID查询帮助文章
     *
     * @param tenantId  租户ID
     * @param articleId 文章ID
     * @return 帮助文章(可能为空)
     */
    public Optional<HelpArticle> findHelpArticle(String tenantId, String articleId) {
        return Optional.ofNullable(mapper.selectHelpArticle(tenantId, articleId)).map(this::toHelpArticleDomain);
    }

    /**
     * 查询租户下所有帮助文章
     *
     * @param tenantId 租户ID
     * @return 帮助文章列表
     */
    public List<HelpArticle> listHelpArticles(String tenantId) {
        return mapper.selectHelpArticles(tenantId).stream().map(this::toHelpArticleDomain).collect(Collectors.toList());
    }

    /**
     * 查询已发布帮助文章
     *
     * @param tenantId 租户ID
     * @return 已发布帮助文章列表
     */
    public List<HelpArticle> listPublishedHelpArticles(String tenantId) {
        return mapper.selectPublishedHelpArticles(tenantId).stream().map(this::toHelpArticleDomain).collect(Collectors.toList());
    }

    /**
     * 按分类查询已发布帮助文章
     *
     * @param tenantId 租户ID
     * @param category 文章分类
     * @return 帮助文章列表
     */
    public List<HelpArticle> listHelpArticlesByCategory(String tenantId, String category) {
        return mapper.selectHelpArticlesByCategory(tenantId, category).stream().map(this::toHelpArticleDomain).collect(Collectors.toList());
    }

    /* ================================ 待办事项 ================================ */

    /**
     * 保存待办事项(存在则更新，不存在则插入)
     *
     * @param todo 待办事项领域对象
     * @return 保存后的待办事项
     */
    public TodoItem saveTodoItem(TodoItem todo) {
        TodoItemDO existing = mapper.selectTodoItem(todo.tenantId(), todo.todoId());
        TodoItemDO data = toTodoItemData(todo);
        if (existing == null) {
            mapper.insertTodoItem(data);
        } else {
            mapper.updateTodoItem(data);
        }
        return todo;
    }

    /**
     * 删除待办事项
     *
     * @param tenantId 租户ID
     * @param todoId   待办ID
     */
    public void deleteTodoItem(String tenantId, String todoId) {
        mapper.deleteTodoItem(tenantId, todoId);
    }

    /**
     * 按ID查询待办事项
     *
     * @param tenantId 租户ID
     * @param todoId   待办ID
     * @return 待办事项(可能为空)
     */
    public Optional<TodoItem> findTodoItem(String tenantId, String todoId) {
        return Optional.ofNullable(mapper.selectTodoItem(tenantId, todoId)).map(this::toTodoItemDomain);
    }

    /**
     * 查询用户所有待办事项
     *
     * @param tenantId 租户ID
     * @param userId   用户ID
     * @return 待办事项列表
     */
    public List<TodoItem> listTodoItems(String tenantId, String userId) {
        return mapper.selectTodoItems(tenantId, userId).stream().map(this::toTodoItemDomain).collect(Collectors.toList());
    }

    /**
     * 查询用户待处理待办事项
     *
     * @param tenantId 租户ID
     * @param userId   用户ID
     * @return 待处理待办事项列表
     */
    public List<TodoItem> listPendingTodoItems(String tenantId, String userId) {
        return mapper.selectPendingTodoItems(tenantId, userId).stream().map(this::toTodoItemDomain).collect(Collectors.toList());
    }

    /* ================================ 仪表盘组件 ================================ */

    /**
     * 保存仪表盘组件(存在则更新，不存在则插入)
     *
     * @param widget 组件领域对象
     * @return 保存后的组件
     */
    public DashboardWidget saveDashboardWidget(DashboardWidget widget) {
        DashboardWidgetDO existing = mapper.selectDashboardWidget(widget.tenantId(), widget.widgetId());
        DashboardWidgetDO data = toDashboardWidgetData(widget);
        if (existing == null) {
            mapper.insertDashboardWidget(data);
        } else {
            mapper.updateDashboardWidget(data);
        }
        return widget;
    }

    /**
     * 删除仪表盘组件
     *
     * @param tenantId 租户ID
     * @param widgetId 组件ID
     */
    public void deleteDashboardWidget(String tenantId, String widgetId) {
        mapper.deleteDashboardWidget(tenantId, widgetId);
    }

    /**
     * 按ID查询仪表盘组件
     *
     * @param tenantId 租户ID
     * @param widgetId 组件ID
     * @return 组件(可能为空)
     */
    public Optional<DashboardWidget> findDashboardWidget(String tenantId, String widgetId) {
        return Optional.ofNullable(mapper.selectDashboardWidget(tenantId, widgetId)).map(this::toDashboardWidgetDomain);
    }

    /**
     * 按分类查询仪表盘组件
     *
     * @param tenantId 租户ID
     * @param category 组件分类
     * @return 组件列表
     */
    public List<DashboardWidget> listDashboardWidgets(String tenantId, String category) {
        return mapper.selectDashboardWidgets(tenantId, category).stream().map(this::toDashboardWidgetDomain).collect(Collectors.toList());
    }

    /**
     * 查询活跃仪表盘组件
     *
     * @param tenantId 租户ID
     * @return 活跃组件列表
     */
    public List<DashboardWidget> listActiveDashboardWidgets(String tenantId) {
        return mapper.selectActiveDashboardWidgets(tenantId).stream().map(this::toDashboardWidgetDomain).collect(Collectors.toList());
    }

    /* ================================ 日历事件 ================================ */

    /**
     * 保存日历事件(存在则更新，不存在则插入)
     *
     * @param event 日历事件领域对象
     * @return 保存后的日历事件
     */
    public CalendarEvent saveCalendarEvent(CalendarEvent event) {
        CalendarEventDO existing = mapper.selectCalendarEvent(event.tenantId(), event.eventId());
        CalendarEventDO data = toCalendarEventData(event);
        if (existing == null) {
            mapper.insertCalendarEvent(data);
        } else {
            mapper.updateCalendarEvent(data);
        }
        return event;
    }

    /**
     * 删除日历事件
     *
     * @param tenantId 租户ID
     * @param eventId  事件ID
     */
    public void deleteCalendarEvent(String tenantId, String eventId) {
        mapper.deleteCalendarEvent(tenantId, eventId);
    }

    /**
     * 按ID查询日历事件
     *
     * @param tenantId 租户ID
     * @param eventId  事件ID
     * @return 日历事件(可能为空)
     */
    public Optional<CalendarEvent> findCalendarEvent(String tenantId, String eventId) {
        return Optional.ofNullable(mapper.selectCalendarEvent(tenantId, eventId)).map(this::toCalendarEventDomain);
    }

    /**
     * 按时间范围查询日历事件
     *
     * @param tenantId  租户ID
     * @param userId    用户ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 日历事件列表
     */
    public List<CalendarEvent> listCalendarEvents(String tenantId, String userId, Instant startTime, Instant endTime) {
        return mapper.selectCalendarEvents(tenantId, userId, startTime, endTime).stream().map(this::toCalendarEventDomain).collect(Collectors.toList());
    }

    /**
     * 查询即将到来的日历事件
     *
     * @param tenantId 租户ID
     * @param userId   用户ID
     * @param fromTime 起始时间
     * @return 即将到来的日历事件列表
     */
    public List<CalendarEvent> listUpcomingCalendarEvents(String tenantId, String userId, Instant fromTime) {
        return mapper.selectUpcomingCalendarEvents(tenantId, userId, fromTime).stream().map(this::toCalendarEventDomain).collect(Collectors.toList());
    }

    /* ================================ AI洞察卡片 ================================ */

    /**
     * 保存AI洞察卡片
     *
     * @param card AI洞察卡片领域对象
     * @return 保存后的AI洞察卡片
     */
    public AIInsightCard saveAIInsightCard(AIInsightCard card) {
        mapper.insertAIInsightCard(toAIInsightCardData(card));
        return card;
    }

    /**
     * 删除AI洞察卡片
     *
     * @param tenantId 租户ID
     * @param cardId   卡片ID
     */
    public void deleteAIInsightCard(String tenantId, String cardId) {
        mapper.deleteAIInsightCard(tenantId, cardId);
    }

    /**
     * 按ID查询AI洞察卡片
     *
     * @param tenantId 租户ID
     * @param cardId   卡片ID
     * @return AI洞察卡片(可能为空)
     */
    public Optional<AIInsightCard> findAIInsightCard(String tenantId, String cardId) {
        return Optional.ofNullable(mapper.selectAIInsightCard(tenantId, cardId)).map(this::toAIInsightCardDomain);
    }

    /**
     * 按类型查询AI洞察卡片
     *
     * @param tenantId    租户ID
     * @param userId      用户ID
     * @param insightType 洞察类型
     * @return AI洞察卡片列表
     */
    public List<AIInsightCard> listAIInsightCards(String tenantId, String userId, String insightType) {
        return mapper.selectAIInsightCards(tenantId, userId, insightType).stream().map(this::toAIInsightCardDomain).collect(Collectors.toList());
    }

    /**
     * 查询未读AI洞察卡片
     *
     * @param tenantId 租户ID
     * @param userId   用户ID
     * @return 未读AI洞察卡片列表
     */
    public List<AIInsightCard> listUnreadAIInsightCards(String tenantId, String userId) {
        return mapper.selectUnreadAIInsightCards(tenantId, userId).stream().map(this::toAIInsightCardDomain).collect(Collectors.toList());
    }

    /**
     * 更新AI洞察卡片已读状态
     *
     * @param tenantId 租户ID
     * @param cardId   卡片ID
     * @param isRead   是否已读
     */
    public void markAIInsightCardRead(String tenantId, String cardId, boolean isRead) {
        mapper.updateAIInsightCardReadStatus(tenantId, cardId, isRead);
    }

    /**
     * 更新AI洞察卡片忽略状态
     *
     * @param tenantId    租户ID
     * @param cardId      卡片ID
     * @param isDismissed 是否已忽略
     */
    public void dismissAIInsightCard(String tenantId, String cardId, boolean isDismissed) {
        mapper.updateAIInsightCardDismissed(tenantId, cardId, isDismissed);
    }

    /* ================================ 快捷入口 ================================ */

    /**
     * 保存快捷入口(存在则更新，不存在则插入)
     *
     * @param entry 快捷入口领域对象
     * @return 保存后的快捷入口
     */
    public QuickEntry saveQuickEntry(QuickEntry entry) {
        QuickEntryDO existing = mapper.selectQuickEntries(entry.tenantId(), entry.userId()).stream()
                .filter(e -> e.getEntryId().equals(entry.entryId())).findFirst().orElse(null);
        QuickEntryDO data = toQuickEntryData(entry);
        if (existing == null) {
            mapper.insertQuickEntry(data);
        } else {
            mapper.updateQuickEntry(data);
        }
        return entry;
    }

    /**
     * 删除快捷入口
     *
     * @param tenantId 租户ID
     * @param entryId  入口ID
     */
    public void deleteQuickEntry(String tenantId, String entryId) {
        mapper.deleteQuickEntry(tenantId, entryId);
    }

    /**
     * 查询用户快捷入口
     *
     * @param tenantId 租户ID
     * @param userId   用户ID
     * @return 快捷入口列表
     */
    public List<QuickEntry> listQuickEntries(String tenantId, String userId) {
        return mapper.selectQuickEntries(tenantId, userId).stream().map(this::toQuickEntryDomain).collect(Collectors.toList());
    }

    /* ================================ DO与领域对象转换 ================================ */

    private UserDashboardDO toUserDashboardData(UserDashboard d) {
        UserDashboardDO data = new UserDashboardDO();
        data.setDashboardId(d.dashboardId());
        data.setTenantId(d.tenantId());
        data.setUserId(d.userId());
        data.setLayoutConfig(d.layoutConfig());
        data.setWidgets(d.widgets());
        data.setIsDefault(d.isDefault());
        data.setCreatedAt(d.createdAt() != null ? d.createdAt() : Instant.now());
        data.setUpdatedAt(d.updatedAt() != null ? d.updatedAt() : Instant.now());
        return data;
    }

    private UserDashboard toUserDashboardDomain(UserDashboardDO d) {
        return new UserDashboard(d.getDashboardId(), d.getTenantId(), d.getUserId(), d.getLayoutConfig(),
                d.getWidgets(), d.isIsDefault(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private AnnouncementDO toAnnouncementData(Announcement a) {
        AnnouncementDO data = new AnnouncementDO();
        data.setAnnounceId(a.announceId());
        data.setTenantId(a.tenantId());
        data.setTitle(a.title());
        data.setContent(a.content());
        data.setType(a.type().name());
        data.setPriority(a.priority());
        data.setPublishTime(a.publishTime());
        data.setExpireTime(a.expireTime());
        data.setStatus(a.status().name());
        data.setCreatedAt(a.createdAt() != null ? a.createdAt() : Instant.now());
        return data;
    }

    private Announcement toAnnouncementDomain(AnnouncementDO d) {
        return new Announcement(d.getAnnounceId(), d.getTenantId(), d.getTitle(), d.getContent(),
                AnnouncementType.valueOf(d.getType()), d.getPriority(), d.getPublishTime(), d.getExpireTime(),
                AnnouncementStatus.valueOf(d.getStatus()), d.getCreatedAt());
    }

    private HelpArticleDO toHelpArticleData(HelpArticle a) {
        HelpArticleDO data = new HelpArticleDO();
        data.setArticleId(a.articleId());
        data.setTenantId(a.tenantId());
        data.setTitle(a.title());
        data.setContent(a.content());
        data.setCategory(a.category());
        data.setTags(a.tags());
        data.setViewCount(a.viewCount());
        data.setStatus(a.status().name());
        data.setCreatedAt(a.createdAt() != null ? a.createdAt() : Instant.now());
        data.setUpdatedAt(a.updatedAt() != null ? a.updatedAt() : Instant.now());
        return data;
    }

    private HelpArticle toHelpArticleDomain(HelpArticleDO d) {
        return new HelpArticle(d.getArticleId(), d.getTenantId(), d.getTitle(), d.getContent(), d.getCategory(),
                d.getTags(), d.getViewCount(), HelpArticleStatus.valueOf(d.getStatus()), d.getCreatedAt(), d.getUpdatedAt());
    }

    private TodoItemDO toTodoItemData(TodoItem t) {
        TodoItemDO data = new TodoItemDO();
        data.setTodoId(t.todoId());
        data.setTenantId(t.tenantId());
        data.setUserId(t.userId());
        data.setType(t.type());
        data.setBusinessType(t.businessType());
        data.setBusinessId(t.businessId());
        data.setTitle(t.title());
        data.setStatus(t.status().name());
        data.setDueTime(t.dueTime());
        data.setCreatedAt(t.createdAt() != null ? t.createdAt() : Instant.now());
        data.setUpdatedAt(t.updatedAt() != null ? t.updatedAt() : Instant.now());
        return data;
    }

    private TodoItem toTodoItemDomain(TodoItemDO d) {
        return new TodoItem(d.getTodoId(), d.getTenantId(), d.getUserId(), d.getType(), d.getBusinessType(),
                d.getBusinessId(), d.getTitle(), TodoStatus.valueOf(d.getStatus()), d.getDueTime(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private DashboardWidgetDO toDashboardWidgetData(DashboardWidget w) {
        DashboardWidgetDO data = new DashboardWidgetDO();
        data.setWidgetId(w.widgetId());
        data.setTenantId(w.tenantId());
        data.setType(w.type());
        data.setName(w.name());
        data.setDataSource(w.dataSource());
        data.setRefreshRateSeconds(w.refreshRateSeconds());
        data.setConfig(toJson(w.config()));
        data.setStatus(w.status().name());
        data.setCategory(w.category());
        data.setDescription(w.description());
        data.setCreatedAt(w.createdAt() != null ? w.createdAt() : Instant.now());
        data.setUpdatedAt(w.updatedAt() != null ? w.updatedAt() : Instant.now());
        return data;
    }

    private DashboardWidget toDashboardWidgetDomain(DashboardWidgetDO d) {
        return new DashboardWidget(d.getWidgetId(), d.getTenantId(), d.getType(), d.getName(), d.getDataSource(),
                d.getRefreshRateSeconds(), fromJson(d.getConfig(), new TypeReference<Map<String, Object>>() {}),
                WidgetStatus.valueOf(d.getStatus()), d.getCategory(), d.getDescription(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private CalendarEventDO toCalendarEventData(CalendarEvent e) {
        CalendarEventDO data = new CalendarEventDO();
        data.setEventId(e.eventId());
        data.setTenantId(e.tenantId());
        data.setUserId(e.userId());
        data.setTitle(e.title());
        data.setDescription(e.description());
        data.setEventType(e.eventType().name());
        data.setStartTime(e.startTime());
        data.setEndTime(e.endTime());
        data.setBusinessType(e.businessType());
        data.setBusinessId(e.businessId());
        data.setColor(e.color());
        data.setAllDay(e.allDay());
        data.setReminder(e.reminder());
        data.setCreatedAt(e.createdAt() != null ? e.createdAt() : Instant.now());
        data.setUpdatedAt(e.updatedAt() != null ? e.updatedAt() : Instant.now());
        return data;
    }

    private CalendarEvent toCalendarEventDomain(CalendarEventDO d) {
        return new CalendarEvent(d.getEventId(), d.getTenantId(), d.getUserId(), d.getTitle(), d.getDescription(),
                com.aidotnet.erp.dashboard.domain.CalendarEventType.valueOf(d.getEventType()),
                d.getStartTime(), d.getEndTime(), d.getBusinessType(), d.getBusinessId(), d.getColor(),
                d.isAllDay(), d.getReminder(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private AIInsightCardDO toAIInsightCardData(AIInsightCard c) {
        AIInsightCardDO data = new AIInsightCardDO();
        data.setCardId(c.cardId());
        data.setTenantId(c.tenantId());
        data.setUserId(c.userId());
        data.setTitle(c.title());
        data.setSummary(c.summary());
        data.setInsightType(c.insightType());
        data.setSeverity(c.severity());
        data.setData(toJson(c.data()));
        data.setSourceDomain(c.sourceDomain());
        data.setSuggestion(c.suggestion());
        data.setActionUrl(c.actionUrl());
        data.setRead(c.isRead());
        data.setDismissed(c.isDismissed());
        data.setValidUntil(c.validUntil());
        data.setCreatedAt(c.createdAt() != null ? c.createdAt() : Instant.now());
        data.setUpdatedAt(c.updatedAt() != null ? c.updatedAt() : Instant.now());
        return data;
    }

    private AIInsightCard toAIInsightCardDomain(AIInsightCardDO d) {
        return new AIInsightCard(d.getCardId(), d.getTenantId(), d.getUserId(), d.getTitle(), d.getSummary(),
                d.getInsightType(), d.getSeverity(), fromJson(d.getData(), new TypeReference<Map<String, Object>>() {}),
                d.getSourceDomain(), d.getSuggestion(), d.getActionUrl(), d.isRead(), d.isDismissed(),
                d.getValidUntil(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private QuickEntryDO toQuickEntryData(QuickEntry e) {
        QuickEntryDO data = new QuickEntryDO();
        data.setEntryId(e.entryId());
        data.setTenantId(e.tenantId());
        data.setUserId(e.userId());
        data.setEntryCode(e.entryCode());
        data.setEntryName(e.entryName());
        data.setIcon(e.icon());
        data.setUrl(e.url());
        data.setCategory(e.category());
        data.setSortOrder(e.sortOrder());
        data.setCreatedAt(e.createdAt() != null ? e.createdAt() : Instant.now());
        data.setUpdatedAt(e.updatedAt() != null ? e.updatedAt() : Instant.now());
        return data;
    }

    private QuickEntry toQuickEntryDomain(QuickEntryDO d) {
        return new QuickEntry(d.getEntryId(), d.getTenantId(), d.getUserId(), d.getEntryCode(), d.getEntryName(),
                d.getIcon(), d.getUrl(), d.getCategory(), d.getSortOrder(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private String toJson(Object value) {
        if (value == null) return null;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T fromJson(String json, TypeReference<T> typeRef) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
