package com.aidotnet.erp.common.dashboard;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DashboardExtensionService {

    private static final Logger log = LoggerFactory.getLogger(DashboardExtensionService.class);
    private final Map<String, Announcement> announcements = new ConcurrentHashMap<>();
    private final Map<String, HelpArticle> helpArticles = new ConcurrentHashMap<>();
    private final Map<String, List<TodoItem>> todoItemsByUser = new ConcurrentHashMap<>();

    public Announcement publishAnnouncement(String tenantId, String title, String content,
                                             String priority, String category, String publisher) {
        String annId = "ANN-" + System.currentTimeMillis();
        Announcement ann = new Announcement(annId, tenantId, title, content, priority,
                category, publisher, true, Instant.now(), Instant.now());
        announcements.put(annId, ann);
        log.info("Published announcement: id={}, title={}, priority={}", annId, title, priority);
        return ann;
    }

    public Announcement updateAnnouncement(String annId, String title, String content,
                                            String priority, boolean active) {
        Announcement ann = announcements.get(annId);
        if (ann == null) throw new IllegalArgumentException("Announcement not found: " + annId);
        Announcement updated = new Announcement(annId, ann.tenantId(),
                title != null ? title : ann.title(),
                content != null ? content : ann.content(),
                priority != null ? priority : ann.priority(),
                ann.category(), ann.publisher(), active, ann.publishedAt(), Instant.now());
        announcements.put(annId, updated);
        log.info("Updated announcement: id={}", annId);
        return updated;
    }

    public List<Announcement> listAnnouncements(String tenantId, String category, Boolean active) {
        return announcements.values().stream()
                .filter(a -> tenantId == null || tenantId.equals(a.tenantId()))
                .filter(a -> category == null || category.equals(a.category()))
                .filter(a -> active == null || active.equals(a.active()))
                .sorted((a, b) -> b.publishedAt().compareTo(a.publishedAt()))
                .toList();
    }

    public HelpArticle createHelpArticle(String title, String content, String category,
                                           List<String> tags, String author) {
        String articleId = "HELP-" + System.currentTimeMillis();
        HelpArticle article = new HelpArticle(articleId, title, content, category,
                tags, author, true, Instant.now(), Instant.now());
        helpArticles.put(articleId, article);
        log.info("Created help article: id={}, title={}, category={}", articleId, title, category);
        return article;
    }

    public HelpArticle updateHelpArticle(String articleId, String title, String content,
                                           String category, List<String> tags, boolean active) {
        HelpArticle article = helpArticles.get(articleId);
        if (article == null) throw new IllegalArgumentException("Help article not found: " + articleId);
        HelpArticle updated = new HelpArticle(articleId,
                title != null ? title : article.title(),
                content != null ? content : article.content(),
                category != null ? category : article.category(),
                tags != null ? tags : article.tags(),
                article.author(), active, article.createdAt(), Instant.now());
        helpArticles.put(articleId, updated);
        log.info("Updated help article: id={}", articleId);
        return updated;
    }

    public List<HelpArticle> listHelpArticles(String category, Boolean active) {
        return helpArticles.values().stream()
                .filter(a -> category == null || category.equals(a.category()))
                .filter(a -> active == null || active.equals(a.active()))
                .sorted((a, b) -> b.createdAt().compareTo(a.createdAt()))
                .toList();
    }

    public List<HelpArticle> searchHelpArticles(String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        return helpArticles.values().stream()
                .filter(a -> a.active())
                .filter(a -> a.title().toLowerCase().contains(lowerKeyword)
                        || a.content().toLowerCase().contains(lowerKeyword)
                        || a.tags().stream().anyMatch(t -> t.toLowerCase().contains(lowerKeyword)))
                .toList();
    }

    public TodoItem createTodoItem(String userId, String tenantId, String title, String description,
                                    String todoType, String businessId, String priority,
                                    Instant dueDate) {
        String todoId = "TODO-" + System.currentTimeMillis();
        TodoItem item = new TodoItem(todoId, userId, tenantId, title, description,
                todoType, businessId, priority, "PENDING", dueDate, null, Instant.now(), Instant.now());
        todoItemsByUser.computeIfAbsent(userId, k -> new ArrayList<>()).add(item);
        log.info("Created todo item: id={}, user={}, type={}", todoId, userId, todoType);
        return item;
    }

    public TodoItem completeTodoItem(String userId, String todoId) {
        List<TodoItem> items = todoItemsByUser.get(userId);
        if (items == null) throw new IllegalArgumentException("No todo items found for user: " + userId);
        TodoItem item = items.stream()
                .filter(i -> i.todoId().equals(todoId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Todo item not found: " + todoId));
        if (!"PENDING".equals(item.status())) {
            throw new IllegalStateException("Only PENDING items can be completed");
        }
        TodoItem completed = new TodoItem(item.todoId(), item.userId(), item.tenantId(),
                item.title(), item.description(), item.todoType(), item.businessId(),
                item.priority(), "COMPLETED", item.dueDate(), Instant.now(),
                item.createdAt(), Instant.now());
        items.removeIf(i -> i.todoId().equals(todoId));
        items.add(completed);
        log.info("Completed todo item: id={}, user={}", todoId, userId);
        return completed;
    }

    public TodoItem cancelTodoItem(String userId, String todoId) {
        List<TodoItem> items = todoItemsByUser.get(userId);
        if (items == null) throw new IllegalArgumentException("No todo items found for user: " + userId);
        TodoItem item = items.stream()
                .filter(i -> i.todoId().equals(todoId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Todo item not found: " + todoId));
        TodoItem cancelled = new TodoItem(item.todoId(), item.userId(), item.tenantId(),
                item.title(), item.description(), item.todoType(), item.businessId(),
                item.priority(), "CANCELLED", item.dueDate(), null,
                item.createdAt(), Instant.now());
        items.removeIf(i -> i.todoId().equals(todoId));
        items.add(cancelled);
        log.info("Cancelled todo item: id={}, user={}", todoId, userId);
        return cancelled;
    }

    public List<TodoItem> listTodoItems(String userId, String status) {
        List<TodoItem> items = todoItemsByUser.getOrDefault(userId, List.of());
        return items.stream()
                .filter(i -> status == null || status.equals(i.status()))
                .toList();
    }

    public int getPendingTodoCount(String userId) {
        return (int) todoItemsByUser.getOrDefault(userId, List.of()).stream()
                .filter(i -> "PENDING".equals(i.status()))
                .count();
    }

    public record Announcement(String annId, String tenantId, String title, String content,
                                String priority, String category, String publisher,
                                boolean active, Instant publishedAt, Instant updatedAt) {}
    public record HelpArticle(String articleId, String title, String content, String category,
                               List<String> tags, String author, boolean active,
                               Instant createdAt, Instant updatedAt) {}
    public record TodoItem(String todoId, String userId, String tenantId, String title,
                            String description, String todoType, String businessId,
                            String priority, String status, Instant dueDate,
                            Instant completedAt, Instant createdAt, Instant updatedAt) {}
}
