package com.aidotnet.erp.pdm.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.pdm.domain.ComplianceCheckResult;
import com.aidotnet.erp.pdm.domain.ComplianceViolation;
import com.aidotnet.erp.pdm.domain.SensitiveWord;
import com.aidotnet.erp.pdm.domain.SensitiveWord.Category;
import com.aidotnet.erp.pdm.domain.UpcPool;
import com.aidotnet.erp.pdm.domain.UpcPool.UpcStatus;
import com.aidotnet.erp.pdm.infrastructure.PdmComplianceStore;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * PDM合规管理应用服务
 * <p>
 * 描述: 产品合规管理核心服务，负责敏感词管理、文本合规检查和UPC码池管理。
 *       确保产品上架前通过合规性校验，防止侵权和违规。
 * </p>
 * <p>
 * 核心能力:
 *   1. 敏感词管理 - 添加/删除/查询敏感词，支持多语言多类别
 *   2. 合规检查 - 对产品标题、描述、关键词进行敏感词扫描
 *   3. UPC码管理 - 批量导入、分配、释放UPC码
 * </p>
 *
 * @author ERP系统
 */
@Service
public class PdmComplianceService {

    private final PdmComplianceStore complianceStore;

    /**
     * 构造函数 - 依赖注入合规数据存储
     *
     * @param complianceStore 合规数据存储
     */
    public PdmComplianceService(PdmComplianceStore complianceStore) {
        this.complianceStore = complianceStore;
    }

    /**
     * 添加敏感词
     *
     * @param tenantId 租户ID
     * @param command  添加敏感词命令
     * @return 新创建的敏感词
     */
    @Transactional
    public SensitiveWord addSensitiveWord(String tenantId, AddSensitiveWordCommand command) {
        Instant now = Instant.now();
        SensitiveWord word = new SensitiveWord(UUID.randomUUID().toString(), tenantId, command.word(),
                command.category().name(), command.language(), true, now);
        return complianceStore.saveSensitiveWord(word);
    }

    /**
     * 删除敏感词(逻辑删除，设为禁用)
     *
     * @param tenantId 租户ID
     * @param wordId   敏感词ID
     */
    @Transactional
    public void removeSensitiveWord(String tenantId, String wordId) {
        SensitiveWord word = complianceStore.findSensitiveWord(tenantId, wordId)
                .orElseThrow(() -> new BizException("SENSITIVE_WORD_NOT_FOUND", "敏感词不存在"));
        complianceStore.saveSensitiveWord(new SensitiveWord(word.wordId(), word.tenantId(), word.word(),
                word.category(), word.language(), false, word.createdAt()));
    }

    /**
     * 查询敏感词列表
     *
     * @param tenantId 租户ID
     * @param language 语言过滤(可选)
     * @return 敏感词列表
     */
    public List<SensitiveWord> listSensitiveWords(String tenantId, String language) {
        return complianceStore.listSensitiveWords(tenantId, language);
    }

    /**
     * 产品合规检查
     * <p>
     * 对产品标题、描述、关键词进行敏感词扫描，返回检查结果和违规项。
     * </p>
     *
     * @param tenantId    租户ID
     * @param spuId       SPU ID
     * @param title       产品标题
     * @param description 产品描述
     * @param keywords    关键词列表
     * @return 合规检查结果
     */
    public ComplianceCheckResult checkCompliance(String tenantId, String spuId, String title, String description, List<String> keywords) {
        List<SensitiveWord> allWords = complianceStore.listEnabledSensitiveWords(tenantId);
        List<ComplianceViolation> violations = new ArrayList<>();
        String combinedText = (title + " " + description + " " + String.join(" ", keywords)).toLowerCase();
        for (SensitiveWord word : allWords) {
            if (combinedText.contains(word.word().toLowerCase())) {
                violations.add(new ComplianceViolation("text", word.word(), word.category(), "请移除或替换: " + word.word()));
            }
        }
        return new ComplianceCheckResult(spuId, violations.isEmpty(), violations);
    }

    /**
     * 批量导入UPC码
     * <p>
     * 跳过已存在的UPC码，仅导入新的UPC码。
     * </p>
     *
     * @param tenantId 租户ID
     * @param upcCodes UPC码列表
     * @return 最后导入的UPC码记录
     */
    @Transactional
    public UpcPool addUpcCodes(String tenantId, List<String> upcCodes) {
        Instant now = Instant.now();
        UpcPool last = null;
        for (String upcCode : upcCodes) {
            if (complianceStore.findUpcByCode(tenantId, upcCode).isPresent()) {
                continue;
            }
            last = complianceStore.saveUpcPool(new UpcPool(UUID.randomUUID().toString(), tenantId, upcCode,
                    UpcStatus.AVAILABLE, null, null, now));
        }
        return last;
    }

    /**
     * 分配UPC码给SKU
     * <p>
     * 从可用UPC码池中分配一个UPC码给指定SKU。
     * </p>
     *
     * @param tenantId 租户ID
     * @param skuId    目标SKU ID
     * @return 已分配的UPC码记录
     */
    @Transactional
    public UpcPool assignUpc(String tenantId, String skuId) {
        UpcPool available = complianceStore.findAvailableUpc(tenantId)
                .orElseThrow(() -> new BizException("NO_AVAILABLE_UPC", "没有可用的UPC码"));
        return complianceStore.saveUpcPool(new UpcPool(available.poolId(), available.tenantId(), available.upcCode(),
                UpcStatus.ASSIGNED, skuId, Instant.now(), available.createdAt()));
    }

    /**
     * 释放已分配的UPC码
     * <p>
     * 将UPC码状态从ASSIGNED改为AVAILABLE，解除与SKU的绑定。
     * </p>
     *
     * @param tenantId 租户ID
     * @param upcCode  UPC码
     * @return 释放后的UPC码记录
     */
    @Transactional
    public UpcPool releaseUpc(String tenantId, String upcCode) {
        UpcPool upc = complianceStore.findUpcByCode(tenantId, upcCode)
                .orElseThrow(() -> new BizException("UPC_NOT_FOUND", "UPC码不存在"));
        if (upc.status() != UpcStatus.ASSIGNED) {
            throw new BizException("UPC_STATUS_INVALID", "UPC码未被分配");
        }
        return complianceStore.saveUpcPool(new UpcPool(upc.poolId(), upc.tenantId(), upc.upcCode(),
                UpcStatus.AVAILABLE, null, null, upc.createdAt()));
    }

    /**
     * 查询UPC码池
     *
     * @param tenantId 租户ID
     * @param status   状态过滤(可选)
     * @return UPC码列表
     */
    public List<UpcPool> listUpcPool(String tenantId, UpcStatus status) {
        return complianceStore.listUpcPool(tenantId, status);
    }

    /** 添加敏感词命令 */
    public record AddSensitiveWordCommand(String word, Category category, String language) {}
}
