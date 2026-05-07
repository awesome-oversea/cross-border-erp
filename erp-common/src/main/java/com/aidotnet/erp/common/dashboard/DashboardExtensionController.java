package com.aidotnet.erp.common.dashboard;

import com.aidotnet.erp.common.api.Result;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/platform/dashboard/api/v1")
public class DashboardExtensionController {

    private final DashboardExtensionService dashboardExtService;

    public DashboardExtensionController(DashboardExtensionService dashboardExtService) {
        this.dashboardExtService = dashboardExtService;
    }

    @PostMapping("/announcements")
    public Result<DashboardExtensionService.Announcement> publishAnnouncement(@RequestBody Map<String, Object> request) {
        return Result.ok(dashboardExtService.publishAnnouncement(
                (String) request.get("tenantId"),
                (String) request.get("title"),
                (String) request.get("content"),
                (String) request.getOrDefault("priority", "NORMAL"),
                (String) request.getOrDefault("category", "GENERAL"),
                (String) request.get("publisher")
        ));
    }

    @PatchMapping("/announcements/{annId}")
    public Result<DashboardExtensionService.Announcement> updateAnnouncement(
            @PathVariable String annId, @RequestBody Map<String, Object> request) {
        return Result.ok(dashboardExtService.updateAnnouncement(
                annId,
                (String) request.get("title"),
                (String) request.get("content"),
                (String) request.get("priority"),
                request.containsKey("active") && (Boolean) request.get("active")
        ));
    }

    @GetMapping("/announcements")
    public Result<List<DashboardExtensionService.Announcement>> listAnnouncements(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean active) {
        return Result.ok(dashboardExtService.listAnnouncements(tenantId, category, active));
    }

    @PostMapping("/help-articles")
    public Result<DashboardExtensionService.HelpArticle> createHelpArticle(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<String> tags = (List<String>) request.getOrDefault("tags", List.of());
        return Result.ok(dashboardExtService.createHelpArticle(
                (String) request.get("title"),
                (String) request.get("content"),
                (String) request.getOrDefault("category", "GENERAL"),
                tags,
                (String) request.get("author")
        ));
    }

    @PatchMapping("/help-articles/{articleId}")
    public Result<DashboardExtensionService.HelpArticle> updateHelpArticle(
            @PathVariable String articleId, @RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<String> tags = (List<String>) request.get("tags");
        return Result.ok(dashboardExtService.updateHelpArticle(
                articleId,
                (String) request.get("title"),
                (String) request.get("content"),
                (String) request.get("category"),
                tags,
                request.containsKey("active") && (Boolean) request.get("active")
        ));
    }

    @GetMapping("/help-articles")
    public Result<List<DashboardExtensionService.HelpArticle>> listHelpArticles(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean active) {
        return Result.ok(dashboardExtService.listHelpArticles(category, active));
    }

    @GetMapping("/help-articles/search")
    public Result<List<DashboardExtensionService.HelpArticle>> searchHelpArticles(
            @RequestParam String keyword) {
        return Result.ok(dashboardExtService.searchHelpArticles(keyword));
    }

    @PostMapping("/todo-items")
    public Result<DashboardExtensionService.TodoItem> createTodoItem(@RequestBody Map<String, Object> request) {
        Instant dueDate = null;
        if (request.containsKey("dueDate")) {
            dueDate = Instant.parse((String) request.get("dueDate"));
        }
        return Result.ok(dashboardExtService.createTodoItem(
                (String) request.get("userId"),
                (String) request.get("tenantId"),
                (String) request.get("title"),
                (String) request.getOrDefault("description", ""),
                (String) request.getOrDefault("todoType", "GENERAL"),
                (String) request.getOrDefault("businessId", ""),
                (String) request.getOrDefault("priority", "NORMAL"),
                dueDate
        ));
    }

    @PatchMapping("/todo-items/{todoId}/complete")
    public Result<DashboardExtensionService.TodoItem> completeTodoItem(
            @PathVariable String todoId, @RequestParam String userId) {
        return Result.ok(dashboardExtService.completeTodoItem(userId, todoId));
    }

    @PatchMapping("/todo-items/{todoId}/cancel")
    public Result<DashboardExtensionService.TodoItem> cancelTodoItem(
            @PathVariable String todoId, @RequestParam String userId) {
        return Result.ok(dashboardExtService.cancelTodoItem(userId, todoId));
    }

    @GetMapping("/todo-items")
    public Result<List<DashboardExtensionService.TodoItem>> listTodoItems(
            @RequestParam String userId,
            @RequestParam(required = false) String status) {
        return Result.ok(dashboardExtService.listTodoItems(userId, status));
    }

    @GetMapping("/todo-items/pending-count")
    public Result<Map<String, Integer>> getPendingTodoCount(@RequestParam String userId) {
        return Result.ok(Map.of("pendingCount", dashboardExtService.getPendingTodoCount(userId)));
    }
}
