package com.aidotnet.erp.dashboard.infrastructure.mapper;

import com.aidotnet.erp.dashboard.infrastructure.data.AIInsightCardDO;
import com.aidotnet.erp.dashboard.infrastructure.data.AnnouncementDO;
import com.aidotnet.erp.dashboard.infrastructure.data.CalendarEventDO;
import com.aidotnet.erp.dashboard.infrastructure.data.DashboardMetricDO;
import com.aidotnet.erp.dashboard.infrastructure.data.DashboardWidgetDO;
import com.aidotnet.erp.dashboard.infrastructure.data.HelpArticleDO;
import com.aidotnet.erp.dashboard.infrastructure.data.QuickEntryDO;
import com.aidotnet.erp.dashboard.infrastructure.data.TodoItemDO;
import com.aidotnet.erp.dashboard.infrastructure.data.UserDashboardDO;
import java.time.Instant;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 仪表盘数据映射接口
 * <p>
 * 描述: DASHBOARD域MyBatis映射接口，定义仪表盘所有实体的数据库操作方法。
 *       包括用户仪表盘配置、公告、帮助文章、待办事项、仪表盘组件、
 *       日历事件、AI洞察卡片、快捷入口和仪表盘指标等。
 * </p>
 * <p>
 * 设计说明:
 *   1. 所有查询方法必须带tenantId参数，确保多租户数据隔离
 *   2. insert/update操作使用DO对象作为参数，字段映射通过XML配置
 *   3. delete操作使用@Param注解标注参数，与XML中#{param}对应
 *   4. select操作返回DO对象，由Repository层转换为领域对象
 * </p>
 *
 * @author ERP系统
 */
@Mapper
public interface DashboardMapper {

    /* ================================ 仪表盘指标 ================================ */

    /**
     * 插入指标
     *
     * @param metric 指标数据对象
     */
    void insertDashboardMetric(DashboardMetricDO metric);

    /**
     * 更新指标
     *
     * @param metric 指标数据对象
     */
    void updateDashboardMetric(DashboardMetricDO metric);

    /**
     * 删除指标
     *
     * @param tenantId 租户ID
     * @param metricId 指标ID
     */
    void deleteDashboardMetric(@Param("tenantId") String tenantId, @Param("metricId") String metricId);

    /**
     * 按ID查询指标
     *
     * @param tenantId 租户ID
     * @param metricId 指标ID
     * @return 指标数据对象
     */
    DashboardMetricDO selectDashboardMetric(@Param("tenantId") String tenantId, @Param("metricId") String metricId);

    /**
     * 按编码查询指标
     *
     * @param tenantId   租户ID
     * @param metricCode 指标编码
     * @return 指标数据对象
     */
    DashboardMetricDO selectDashboardMetricByCode(@Param("tenantId") String tenantId, @Param("metricCode") String metricCode);

    /**
     * 查询租户下所有指标
     *
     * @param tenantId 租户ID
     * @return 指标列表
     */
    List<DashboardMetricDO> selectDashboardMetrics(@Param("tenantId") String tenantId);

    /* ================================ 用户仪表盘 ================================ */

    void insertUserDashboard(UserDashboardDO dashboard);
    void updateUserDashboard(UserDashboardDO dashboard);
    UserDashboardDO selectUserDashboard(@Param("tenantId") String tenantId, @Param("dashboardId") String dashboardId);
    UserDashboardDO selectDefaultUserDashboard(@Param("tenantId") String tenantId, @Param("userId") String userId);
    List<UserDashboardDO> selectUserDashboards(@Param("tenantId") String tenantId, @Param("userId") String userId);

    /* ================================ 公告 ================================ */

    void insertAnnouncement(AnnouncementDO announcement);
    void deleteAnnouncement(@Param("tenantId") String tenantId, @Param("announceId") String announceId);
    AnnouncementDO selectAnnouncement(@Param("tenantId") String tenantId, @Param("announceId") String announceId);
    List<AnnouncementDO> selectAnnouncements(@Param("tenantId") String tenantId);
    List<AnnouncementDO> selectPublishedAnnouncements(@Param("tenantId") String tenantId);

    /* ================================ 帮助文章 ================================ */

    void insertHelpArticle(HelpArticleDO article);
    void updateHelpArticle(HelpArticleDO article);
    void deleteHelpArticle(@Param("tenantId") String tenantId, @Param("articleId") String articleId);
    HelpArticleDO selectHelpArticle(@Param("tenantId") String tenantId, @Param("articleId") String articleId);
    List<HelpArticleDO> selectHelpArticles(@Param("tenantId") String tenantId);
    List<HelpArticleDO> selectPublishedHelpArticles(@Param("tenantId") String tenantId);
    List<HelpArticleDO> selectHelpArticlesByCategory(@Param("tenantId") String tenantId, @Param("category") String category);

    /* ================================ 待办事项 ================================ */

    void insertTodoItem(TodoItemDO todo);
    void updateTodoItem(TodoItemDO todo);
    void deleteTodoItem(@Param("tenantId") String tenantId, @Param("todoId") String todoId);
    TodoItemDO selectTodoItem(@Param("tenantId") String tenantId, @Param("todoId") String todoId);
    List<TodoItemDO> selectTodoItems(@Param("tenantId") String tenantId, @Param("userId") String userId);
    List<TodoItemDO> selectPendingTodoItems(@Param("tenantId") String tenantId, @Param("userId") String userId);

    /* ================================ 仪表盘组件 ================================ */

    void insertDashboardWidget(DashboardWidgetDO widget);
    void updateDashboardWidget(DashboardWidgetDO widget);
    void deleteDashboardWidget(@Param("tenantId") String tenantId, @Param("widgetId") String widgetId);
    DashboardWidgetDO selectDashboardWidget(@Param("tenantId") String tenantId, @Param("widgetId") String widgetId);
    List<DashboardWidgetDO> selectDashboardWidgets(@Param("tenantId") String tenantId, @Param("category") String category);
    List<DashboardWidgetDO> selectActiveDashboardWidgets(@Param("tenantId") String tenantId);

    /* ================================ 日历事件 ================================ */

    void insertCalendarEvent(CalendarEventDO event);
    void updateCalendarEvent(CalendarEventDO event);
    void deleteCalendarEvent(@Param("tenantId") String tenantId, @Param("eventId") String eventId);
    CalendarEventDO selectCalendarEvent(@Param("tenantId") String tenantId, @Param("eventId") String eventId);
    List<CalendarEventDO> selectCalendarEvents(@Param("tenantId") String tenantId, @Param("userId") String userId,
                                                @Param("startTime") Instant startTime, @Param("endTime") Instant endTime);
    List<CalendarEventDO> selectUpcomingCalendarEvents(@Param("tenantId") String tenantId, @Param("userId") String userId,
                                                        @Param("fromTime") Instant fromTime);

    /* ================================ AI洞察卡片 ================================ */

    void insertAIInsightCard(AIInsightCardDO card);
    void deleteAIInsightCard(@Param("tenantId") String tenantId, @Param("cardId") String cardId);
    AIInsightCardDO selectAIInsightCard(@Param("tenantId") String tenantId, @Param("cardId") String cardId);
    List<AIInsightCardDO> selectAIInsightCards(@Param("tenantId") String tenantId, @Param("userId") String userId,
                                                @Param("insightType") String insightType);
    List<AIInsightCardDO> selectUnreadAIInsightCards(@Param("tenantId") String tenantId, @Param("userId") String userId);
    void updateAIInsightCardReadStatus(@Param("tenantId") String tenantId, @Param("cardId") String cardId, @Param("isRead") boolean isRead);
    void updateAIInsightCardDismissed(@Param("tenantId") String tenantId, @Param("cardId") String cardId, @Param("isDismissed") boolean isDismissed);

    /* ================================ 快捷入口 ================================ */

    void insertQuickEntry(QuickEntryDO entry);
    void updateQuickEntry(QuickEntryDO entry);
    void deleteQuickEntry(@Param("tenantId") String tenantId, @Param("entryId") String entryId);
    List<QuickEntryDO> selectQuickEntries(@Param("tenantId") String tenantId, @Param("userId") String userId);
}
