package com.aidotnet.erp.common.compliance;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 内容审核中心
 * <p>
 * 描述: 业务中台(10.1) - 文案/图片/多语言内容审核服务。
 *     支持敏感词检测、违禁词过滤、商标侵权检查、文化禁忌检测。
 *     审核流程: 自动审核 + 人工复审。
 * </p>
 * <p>
 * 审核项按语言区域分组(如 zh-CN/en-US/de-DE)，
 * 不同市场的合规要求差异通过分组配置实现。
 * </p>
 *
 * @author ERP系统
 */
@Component
public class ContentReviewService {

    private static final Logger log = LoggerFactory.getLogger(ContentReviewService.class);
    private final Map<String, Set<String>> sensitiveWordsByLocale = new ConcurrentHashMap<>();
    private final Set<String> trademarkWords = ConcurrentHashMap.newKeySet();
    private final Set<String> prohibitedWords = ConcurrentHashMap.newKeySet();
    /** 文化禁忌词: 不同地区有特定文化禁忌 */
    private final Map<String, Set<String>> culturalTaboosByLocale = new ConcurrentHashMap<>();

    /**
     * 多语言文本审核
     * <p>
     * 按locale指定的语言区域执行敏感词/违禁词/商标/文化禁忌检测。
     * 所有命中的违规项以{类型}:{词}格式返回。
     * </p>
     */
    public ReviewResult reviewText(String text, String locale) {
        List<String> violations = new ArrayList<>();
        String normalized = text.toLowerCase();
        String lang = resolveLanguage(locale);

        // 全局敏感词检测
        for (String word : sensitiveWordsByLocale.getOrDefault("ALL", Set.of())) {
            if (normalized.contains(word.toLowerCase())) {
                violations.add("SENSITIVE:" + word);
            }
        }
        // 语言特定敏感词检测
        for (String word : sensitiveWordsByLocale.getOrDefault(lang, Set.of())) {
            if (normalized.contains(word.toLowerCase())) {
                violations.add("SENSITIVE:" + word);
            }
        }
        // 商标检测
        for (String word : trademarkWords) {
            if (normalized.contains(word.toLowerCase())) {
                violations.add("TRADEMARK:" + word);
            }
        }
        // 违禁词检测
        for (String word : prohibitedWords) {
            if (normalized.contains(word.toLowerCase())) {
                violations.add("PROHIBITED:" + word);
            }
        }
        // 文化禁忌检测
        for (String word : culturalTaboosByLocale.getOrDefault(lang, Set.of())) {
            if (normalized.contains(word.toLowerCase())) {
                violations.add("CULTURAL_TABOO:" + word);
            }
        }

        boolean passed = violations.isEmpty();
        if (!passed) {
            log.warn("Content review failed: locale={}, violations={}", locale, violations);
        }
        return new ReviewResult(passed, violations, passed ? "PASSED" : "REJECTED");
    }

    /** 从locale字符串解析语言代码(如 zh-CN → zh) */
    private String resolveLanguage(String locale) {
        if (locale == null || locale.isBlank()) return "ALL";
        try {
            return Locale.forLanguageTag(locale).getLanguage();
        } catch (Exception e) {
            return locale.split("-")[0];
        }
    }

    public void addSensitiveWord(String word) { sensitiveWordsByLocale.computeIfAbsent("ALL", k -> ConcurrentHashMap.newKeySet()).add(word); }
    public void addSensitiveWord(String word, String locale) { sensitiveWordsByLocale.computeIfAbsent(locale, k -> ConcurrentHashMap.newKeySet()).add(word); }
    public void addTrademarkWord(String word) { trademarkWords.add(word); }
    public void addProhibitedWord(String word) { prohibitedWords.add(word); }
    public void addCulturalTaboo(String word, String locale) { culturalTaboosByLocale.computeIfAbsent(locale, k -> ConcurrentHashMap.newKeySet()).add(word); }
    public void removeSensitiveWord(String word) { sensitiveWordsByLocale.values().forEach(s -> s.remove(word)); }

    public void batchAddSensitiveWords(List<String> words) {
        sensitiveWordsByLocale.computeIfAbsent("ALL", k -> ConcurrentHashMap.newKeySet()).addAll(words);
        log.info("Added {} sensitive words", words.size());
    }

    public int getSensitiveWordCount() {
        return sensitiveWordsByLocale.values().stream().mapToInt(Set::size).sum();
    }

    public record ReviewResult(boolean passed, List<String> violations, String status) {}
}
