package com.aidotnet.erp.common.translation;

import java.time.Instant;

public record TranslationRecord(String tenantId, String sourceLanguage, String targetLanguage, String sourceText,
                                String translatedText, boolean glossaryHit, Instant translatedAt) {
}
