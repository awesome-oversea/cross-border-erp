package com.aidotnet.erp.bi.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 排名数据对象
 * <p>
 * 描述: 对应bi_ranking_data表，用于存储各维度的排名数据。
 *       排名数据支持按不同维度(如平台、品类、仓库等)进行排序，
 *       以JSONB格式存储排名项(items)，支持灵活的排名展示。
 * </p>
 * <p>
 * 业务规则:
 *   1. 同一排名类型+维度可存在多条记录(按生成时间区分)
 *   2. items以JSON数组格式存储，每个元素包含rankKey/label/value/rank
 *   3. 排名数据由系统定时任务或手动触发生成
 * </p>
 *
 * @author ERP系统
 * @see com.aidotnet.erp.bi.domain.RankingData
 */
@TableName("bi_ranking_data")
public class RankingDataDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String rankingId;

    private String tenantId;

    private String rankingType;

    private String dimension;

    private String items;

    private Instant generatedAt;

    public RankingDataDO() {}

    public String getRankingId() { return rankingId; }
    public void setRankingId(String rankingId) { this.rankingId = rankingId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getRankingType() { return rankingType; }
    public void setRankingType(String rankingType) { this.rankingType = rankingType; }
    public String getDimension() { return dimension; }
    public void setDimension(String dimension) { this.dimension = dimension; }
    public String getItems() { return items; }
    public void setItems(String items) { this.items = items; }
    public Instant getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(Instant generatedAt) { this.generatedAt = generatedAt; }
}
