package com.aidotnet.erp.common.compliance;

import com.aidotnet.erp.common.api.Result;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sys/api/v1/content-review")
public class ContentReviewController {

    private final ContentReviewService contentReviewService;

    public ContentReviewController(ContentReviewService contentReviewService) {
        this.contentReviewService = contentReviewService;
    }

    @PostMapping("/submit")
    public Result<ContentReviewService.ReviewResult> submitReview(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        String locale = request.getOrDefault("locale", "en");
        ContentReviewService.ReviewResult result = contentReviewService.reviewText(text, locale);
        return Result.ok(result);
    }

    @GetMapping("/{id}/result")
    public Result<ContentReviewService.ReviewResult> getResult(@PathVariable String id) {
        return Result.ok(null);
    }

    @GetMapping("/rules")
    public Result<Map<String, Object>> getRules() {
        return Result.ok(Map.of(
                "sensitiveWordCount", contentReviewService.getSensitiveWordCount()
        ));
    }

    @PostMapping("/sensitive-words/batch")
    public Result<Void> batchAddSensitiveWords(@RequestBody List<String> words) {
        contentReviewService.batchAddSensitiveWords(words);
        return Result.ok(null);
    }

    @PostMapping("/trademark-words/batch")
    public Result<Void> batchAddTrademarkWords(@RequestBody List<String> words) {
        contentReviewService.batchAddTrademarkWords(words);
        return Result.ok(null);
    }

    @PostMapping("/prohibited-words/batch")
    public Result<Void> batchAddProhibitedWords(@RequestBody List<String> words) {
        contentReviewService.batchAddProhibitedWords(words);
        return Result.ok(null);
    }
}
