package com.aidotnet.erp.pdm.domain;

/**
 * 合规违规项领域模型
 * <p>
 * 描述: 产品合规检查中发现的单个违规项，记录违规字段、违规词、
 *       违规类别和修改建议。用于指导运营人员修正产品信息。
 * </p>
 *
 * @param field        违规字段，如 "title"、"description"、"keywords"
 * @param violatedWord 触发违规的词语
 * @param category     违规类别: IP_RISK(知识产权风险)、TRADEMARK(商标)、
 *                     REGULATORY(法规)、PLATFORM_POLICY(平台政策)、CULTURAL(文化敏感)
 * @param suggestion   修改建议，如 "请移除或替换: xxx"
 * @author ERP系统
 */
public record ComplianceViolation(
        String field,
        String violatedWord,
        String category,
        String suggestion
) {}
