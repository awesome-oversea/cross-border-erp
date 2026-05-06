package com.aidotnet.erp.pdm.domain;

import java.util.List;

/**
 * 合规检查结果领域模型
 * <p>
 * 描述: 产品合规检查的结果实体，包含检查是否通过及违规项列表。
 *       用于产品上架前的敏感词、知识产权等合规性校验。
 * </p>
 *
 * @param spuId      被检查的SPU ID
 * @param passed     是否通过合规检查
 * @param violations 违规项列表，为空则表示合规
 * @author ERP系统
 */
public record ComplianceCheckResult(
        String spuId,
        boolean passed,
        List<ComplianceViolation> violations
) {}
