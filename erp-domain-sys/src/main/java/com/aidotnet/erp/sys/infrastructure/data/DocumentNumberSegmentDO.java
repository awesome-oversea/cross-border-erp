package com.aidotnet.erp.sys.infrastructure.data;

import java.time.Instant;

/**
 * 单据编号段数据对象(DocumentNumberSegmentDO)
 * <p>
 * 描述: 单据编号段数据对象，对应sys_document_number_segment表。
 *       记录每次单据编号生成的详细分段信息，包括日期部分、序列部分和完整编号。
 *       用于编号生成审计和问题排查，确保编号唯一性和可追溯性。
 * </p>
 * <p>
 * 业务规则:
 *   1. fullNumber由prefix+datePart+sequencePart拼接而成
 *   2. 同一租户同一documentType下fullNumber必须唯一
 *   3. generatedAt记录编号生成的精确时间
 * </p>
 * <p>
 * 数据库映射:
 *   - 表名: sys_document_number_segment
 *   - 主键: segment_id
 *   - 唯一约束: (tenant_id, document_type, full_number)
 * </p>
 *
 * @author ERP系统
 * @see DocumentNumberRuleDO
 */
public class DocumentNumberSegmentDO {
    private String segmentId;
    private String tenantId;
    private String documentType;
    private String datePart;
    private long sequencePart;
    private String fullNumber;
    private Instant generatedAt;

    public String getSegmentId() { return segmentId; }
    public void setSegmentId(String segmentId) { this.segmentId = segmentId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }
    public String getDatePart() { return datePart; }
    public void setDatePart(String datePart) { this.datePart = datePart; }
    public long getSequencePart() { return sequencePart; }
    public void setSequencePart(long sequencePart) { this.sequencePart = sequencePart; }
    public String getFullNumber() { return fullNumber; }
    public void setFullNumber(String fullNumber) { this.fullNumber = fullNumber; }
    public Instant getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(Instant generatedAt) { this.generatedAt = generatedAt; }
}
