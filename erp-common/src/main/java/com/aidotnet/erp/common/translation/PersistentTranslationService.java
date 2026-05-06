package com.aidotnet.erp.common.translation;

import com.aidotnet.erp.common.persistence.entity.TranslationRecordEntity;
import com.aidotnet.erp.common.persistence.mapper.TranslationRecordMapper;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PersistentTranslationService {

    private final TranslationRecordMapper mapper;
    private final Map<String, String> glossary;

    @Autowired
    public PersistentTranslationService(TranslationRecordMapper mapper) {
        this(mapper, Map.of());
    }

    PersistentTranslationService(TranslationRecordMapper mapper, Map<String, String> glossary) {
        this.mapper = mapper;
        this.glossary = glossary;
    }

    public TranslationRecord translate(String tenantId, String sourceLanguage, String targetLanguage,
                                       String sourceText) {
        boolean glossaryHit = glossary.containsKey(sourceText);
        String translatedText = glossaryHit ? glossary.get(sourceText) : "[" + targetLanguage + "] " + sourceText;
        LocalDateTime now = LocalDateTime.now();
        TranslationRecordEntity entity = new TranslationRecordEntity();
        entity.setTenantId(tenantId);
        entity.setSourceLanguage(sourceLanguage);
        entity.setTargetLanguage(targetLanguage);
        entity.setSourceText(sourceText);
        entity.setTranslatedText(translatedText);
        entity.setGlossaryHit(glossaryHit);
        entity.setTranslatedAt(now);
        mapper.insert(entity);
        return new TranslationRecord(tenantId, sourceLanguage, targetLanguage, sourceText, translatedText, glossaryHit,
                now.toInstant(ZoneOffset.UTC));
    }
}
