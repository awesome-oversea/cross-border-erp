package com.aidotnet.erp.common.translation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MockTranslationService {

    private final Map<String, String> glossary;
    private final List<TranslationRecord> records = new ArrayList<>();

    public MockTranslationService(Map<String, String> glossary) {
        this.glossary = glossary;
    }

    public TranslationRecord translate(String tenantId, String sourceLanguage, String targetLanguage, String text) {
        String translated = glossary.getOrDefault(text, "[" + targetLanguage + "] " + text);
        TranslationRecord record = new TranslationRecord(tenantId, sourceLanguage, targetLanguage, text, translated,
                glossary.containsKey(text), Instant.now());
        records.add(record);
        return record;
    }

    public List<TranslationRecord> records() {
        return List.copyOf(records);
    }
}
