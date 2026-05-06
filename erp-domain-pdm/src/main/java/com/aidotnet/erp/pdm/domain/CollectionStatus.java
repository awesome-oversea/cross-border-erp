package com.aidotnet.erp.pdm.domain;

/**
 * 产品采集状态枚举
 * <p>
 * 描述: 定义竞品采集的生命周期状态
 * </p>
 * <p>
 * 状态流转: COLLECTED → ANALYZING → ANALYZED → CONVERTED → ARCHIVED
 * </p>
 */
public enum CollectionStatus {
    /** 已采集 - 产品信息已从外部平台采集 */
    COLLECTED,
    /** 分析中 - AI正在分析采集数据 */
    ANALYZING,
    /** 已分析 - 分析完成，可转化为选品建议 */
    ANALYZED,
    /** 已转化 - 已转化为选品建议或产品开发 */
    CONVERTED,
    /** 已归档 - 采集记录已归档 */
    ARCHIVED
}
