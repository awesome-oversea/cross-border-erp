package com.aidotnet.erp.scm.domain;

/**
 * 采购类型(5种采购模式)
 * <p>
 * 根据需求规格说明书和详细设计说明书V11定义，系统支持以下5种采购模式:
 * </p>
 * <p>
 * 1. MARKET - 市场采购(现货市场直接购买，适用于小批量、快速补货场景)
 * 2. FACTORY - 工厂采购(向生产厂家下单，适用于大批量、定制化场景)
 * 3. TAOBAO_TMALL - 淘宝天猫网络采购(通过淘宝/天猫平台下单)
 * 4. ALIBABA_1688 - 1688采购(对接1688平台，支持真实系统内下单付款)
 * 5. PROCESSING - 采购原料自主加工(原料采购→加工→成品入库流程)
 * </p>
 *
 * @author ERP系统
 */
public enum PurchaseType {

    /** 市场采购 - 现货市场直接购买 */
    MARKET,
    /** 工厂采购 - 向生产厂家下单 */
    FACTORY,
    /** 淘宝天猫网络采购 */
    TAOBAO_TMALL_1688,
    /** 1688采购 - 对接1688平台下单付款 */
    ALIBABA_1688,
    /** 采购原料自主加工 */
    PROCESSING;

    /**
     * 判断该采购模式是否需要审批流
     * <p>
     * 工厂采购和加工采购通常需要审批，市场采购和网络采购可简化。
     * </p>
     */
    public boolean requiresApprovalFlow() {
        return this == FACTORY || this == PROCESSING;
    }

    /**
     * 判断是否对接外部采购平台
     */
    public boolean isExternalPlatform() {
        return this == ALIBABA_1688 || this == TAOBAO_TMALL_1688;
    }
}
