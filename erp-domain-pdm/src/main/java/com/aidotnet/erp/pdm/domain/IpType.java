package com.aidotnet.erp.pdm.domain;

/**
 * 知识产权类型枚举
 * <p>
 * 描述: 定义知识产权的分类类型，用于产品IP合规管理
 * </p>
 */
public enum IpType {
    /** 专利 - 发明专利、实用新型、外观设计专利 */
    PATENT,
    /** 商标 - 注册商标、服务标志 */
    TRADEMARK,
    /** 版权 - 著作权、软件著作权 */
    COPYRIGHT,
    /** 外观设计 - 工业产品外观设计保护 */
    DESIGN
}
