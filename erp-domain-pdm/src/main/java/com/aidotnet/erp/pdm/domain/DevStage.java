package com.aidotnet.erp.pdm.domain;

/**
 * 产品开发阶段枚举
 * <p>
 * 描述: 定义产品开发流程的阶段
 * </p>
 * <p>
 * 阶段流转: RESEARCH → SAMPLING → TESTING → MASS_PRODUCTION → LISTED → ARCHIVED
 * </p>
 */
public enum DevStage {
    /** 调研阶段 - 市场调研和产品可行性分析 */
    RESEARCH,
    /** 打样阶段 - 产品样品制作和测试 */
    SAMPLING,
    /** 测试阶段 - 产品质量测试和合规检测 */
    TESTING,
    /** 量产阶段 - 批量生产 */
    MASS_PRODUCTION,
    /** 已上架 - 产品已上架销售 */
    LISTED,
    /** 已归档 - 开发流程结束归档 */
    ARCHIVED
}
