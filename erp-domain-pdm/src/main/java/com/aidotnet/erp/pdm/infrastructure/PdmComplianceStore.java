package com.aidotnet.erp.pdm.infrastructure;

import com.aidotnet.erp.pdm.domain.SensitiveWord;
import com.aidotnet.erp.pdm.domain.UpcPool;
import com.aidotnet.erp.pdm.domain.UpcPool.UpcStatus;
import com.aidotnet.erp.pdm.infrastructure.data.SensitiveWordDO;
import com.aidotnet.erp.pdm.infrastructure.data.UpcPoolDO;
import com.aidotnet.erp.pdm.infrastructure.mapper.PdmComplianceMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

/**
 * PDM合规数据存储
 * <p>
 * 描述: PDM域合规管理数据存储层，负责敏感词和UPC码池的CRUD操作。
 *       基于MyBatis持久化存储。
 * </p>
 * <p>
 * 数据实体:
 *   - SensitiveWord: 敏感词，用于产品合规检查
 *   - UpcPool: UPC码池，用于产品条码分配
 * </p>
 *
 * @author ERP系统
 */
@Repository
public class PdmComplianceStore {

    /** 合规数据MyBatis映射器 */
    private final PdmComplianceMapper mapper;

    /**
     * 构造函数 - 依赖注入合规映射器
     *
     * @param mapper 合规数据MyBatis映射器
     */
    public PdmComplianceStore(PdmComplianceMapper mapper) {
        this.mapper = mapper;
    }

    /** 保存敏感词，存在则更新，不存在则新增 */
    public SensitiveWord saveSensitiveWord(SensitiveWord word) {
        SensitiveWordDO existing = mapper.selectSensitiveWord(word.tenantId(), word.wordId());
        SensitiveWordDO data = toSensitiveWordData(word);
        if (existing == null) {
            mapper.insertSensitiveWord(data);
        } else {
            mapper.updateSensitiveWord(data);
        }
        return word;
    }

    /** 按租户+ID查找敏感词 */
    public Optional<SensitiveWord> findSensitiveWord(String tenantId, String wordId) {
        return Optional.ofNullable(mapper.selectSensitiveWord(tenantId, wordId)).map(this::toSensitiveWordDomain);
    }

    /** 查询敏感词列表，支持按语言过滤 */
    public List<SensitiveWord> listSensitiveWords(String tenantId, String language) {
        return mapper.selectSensitiveWords(tenantId, language).stream().map(this::toSensitiveWordDomain).collect(Collectors.toList());
    }

    /** 查询所有已启用的敏感词，用于合规检查 */
    public List<SensitiveWord> listEnabledSensitiveWords(String tenantId) {
        return mapper.selectEnabledSensitiveWords(tenantId).stream().map(this::toSensitiveWordDomain).collect(Collectors.toList());
    }

    /** 保存UPC码池记录，存在则更新，不存在则新增 */
    public UpcPool saveUpcPool(UpcPool upc) {
        UpcPoolDO existing = mapper.selectUpcByPoolId(upc.poolId());
        UpcPoolDO data = toUpcPoolData(upc);
        if (existing == null) {
            mapper.insertUpcPool(data);
        } else {
            mapper.updateUpcPool(data);
        }
        return upc;
    }

    /** 按UPC编码查找 */
    public Optional<UpcPool> findUpcByCode(String tenantId, String upcCode) {
        return Optional.ofNullable(mapper.selectUpcByCode(tenantId, upcCode)).map(this::toUpcPoolDomain);
    }

    /** 查找一个可用的UPC码(AVAILABLE状态) */
    public Optional<UpcPool> findAvailableUpc(String tenantId) {
        return Optional.ofNullable(mapper.selectAvailableUpc(tenantId)).map(this::toUpcPoolDomain);
    }

    /** 查询UPC码池列表，支持按状态过滤 */
    public List<UpcPool> listUpcPool(String tenantId, UpcStatus status) {
        return mapper.selectUpcPool(tenantId, status.name()).stream().map(this::toUpcPoolDomain).collect(Collectors.toList());
    }

    private SensitiveWordDO toSensitiveWordData(SensitiveWord w) {
        SensitiveWordDO data = new SensitiveWordDO();
        data.setWordId(w.wordId());
        data.setTenantId(w.tenantId());
        data.setWord(w.word());
        data.setCategory(w.category());
        data.setLanguage(w.language());
        data.setEnabled(w.enabled());
        data.setCreatedAt(w.createdAt() != null ? w.createdAt() : Instant.now());
        return data;
    }

    private SensitiveWord toSensitiveWordDomain(SensitiveWordDO d) {
        return new SensitiveWord(d.getWordId(), d.getTenantId(), d.getWord(), d.getCategory(),
                d.getLanguage(), d.isEnabled(), d.getCreatedAt());
    }

    private UpcPoolDO toUpcPoolData(UpcPool u) {
        UpcPoolDO data = new UpcPoolDO();
        data.setPoolId(u.poolId());
        data.setTenantId(u.tenantId());
        data.setUpcCode(u.upcCode());
        data.setStatus(u.status().name());
        data.setAssignedSkuId(u.assignedSkuId());
        data.setAssignedAt(u.assignedAt());
        data.setCreatedAt(u.createdAt() != null ? u.createdAt() : Instant.now());
        return data;
    }

    private UpcPool toUpcPoolDomain(UpcPoolDO d) {
        return new UpcPool(d.getPoolId(), d.getTenantId(), d.getUpcCode(), UpcStatus.valueOf(d.getStatus()),
                d.getAssignedSkuId(), d.getAssignedAt(), d.getCreatedAt());
    }
}
