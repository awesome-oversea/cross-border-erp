package com.aidotnet.erp.fms.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 会计凭证领域模型
 * <p>
 * 描述: FMS域核心实体，记录会计凭证。遵循借贷记账法，
 *       totalDebit必须等于totalCredit(借贷必相等)。
 *       凭证行(VoucherLine)记录具体的借方/贷方分录。
 * </p>
 * <p>
 * 状态流转: DRAFT → POSTED → EXPORTED / VOIDED
 * 凭证类型: 收款凭证/付款凭证/转账凭证等
 * </p>
 *
 * @author ERP系统
 * @see VoucherLine
 * @see VoucherStatus
 */
public record Voucher(
        /** 凭证ID */
        String voucherId,
        /** 租户ID */
        String tenantId,
        /** 凭证号 */
        String voucherNumber,
        /** 凭证类型 */
        String voucherType,
        /** 关联业务类型 */
        String referenceType,
        /** 关联业务ID */
        String referenceId,
        /** 币种 */
        String currency,
        /** 借方合计 */
        BigDecimal totalDebit,
        /** 贷方合计 */
        BigDecimal totalCredit,
        /** 凭证状态 */
        VoucherStatus status,
        /** 凭证行列表 */
        List<VoucherLine> lines,
        /** 凭证日期 */
        Instant voucherDate,
        /** 过账人 */
        String postedBy,
        /** 过账时间 */
        Instant postedAt,
        /** 导出批次ID */
        String exportBatchId,
        /** 创建时间 */
        Instant createdAt,
        /** 更新时间 */
        Instant updatedAt
) {
    /** 凭证状态枚举 */
    public enum VoucherStatus {
        /** 草稿 */
        DRAFT,
        /** 已过账 */
        POSTED,
        /** 已导出 */
        EXPORTED,
        /** 已作废 */
        VOIDED
    }
}
