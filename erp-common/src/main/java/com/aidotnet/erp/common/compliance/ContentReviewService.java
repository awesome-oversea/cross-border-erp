package com.aidotnet.erp.common.compliance;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ContentReviewService {

    private static final Logger log = LoggerFactory.getLogger(ContentReviewService.class);
    private final Set<String> sensitiveWords = ConcurrentHashMap.newKeySet();
    private final Set<String> trademarkWords = ConcurrentHashMap.newKeySet();
    private final Set<String> prohibitedWords = ConcurrentHashMap.newKeySet();

    public ReviewResult reviewText(String text, String locale) {
        List<String> violations = new ArrayList<>();

        for (String word : sensitiveWords) {
            if (text.toLowerCase().contains(word.toLowerCase())) {
                violations.add("SENSITIVE:" + word);
            }
        }

        for (String word : trademarkWords) {
            if (text.toLowerCase().contains(word.toLowerCase())) {
                violations.add("TRADEMARK:" + word);
            }
        }

        for (String word : prohibitedWords) {
            if (text.toLowerCase().contains(word.toLowerCase())) {
                violations.add("PROHIBITED:" + word);
            }
        }

        boolean passed = violations.isEmpty();
        if (!passed) {
            log.warn("Content review failed: violations={}", violations);
        }
        return new ReviewResult(passed, violations, passed ? "PASSED" : "REJECTED");
    }

    public void addSensitiveWord(String word) {
        sensitiveWords.add(word);
    }

    public void addTrademarkWord(String word) {
        trademarkWords.add(word);
    }

    public void addProhibitedWord(String word) {
        prohibitedWords.add(word);
    }

    public void removeSensitiveWord(String word) {
        sensitiveWords.remove(word);
    }

    public void batchAddSensitiveWords(List<String> words) {
        sensitiveWords.addAll(words);
        log.info("Added {} sensitive words", words.size());
    }

    public void batchAddTrademarkWords(List<String> words) {
        trademarkWords.addAll(words);
        log.info("Added {} trademark words", words.size());
    }

    public void batchAddProhibitedWords(List<String> words) {
        prohibitedWords.addAll(words);
        log.info("Added {} prohibited words", words.size());
    }

    public int getSensitiveWordCount() {
        return sensitiveWords.size();
    }

    public record ReviewResult(boolean passed, List<String> violations, String status) {}
}
