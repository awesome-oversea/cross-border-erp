package com.aidotnet.erp.som.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

@TableName("som_channel_sku")
public class ChannelSkuDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String channelSkuId;
    private String tenantId;
    private String productSku;
    private String channel;
    private String channelSku;
    private String storeId;
    private String marketplaceId;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    public ChannelSkuDO() {}

    public String getChannelSkuId() { return channelSkuId; }
    public void setChannelSkuId(String channelSkuId) { this.channelSkuId = channelSkuId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getProductSku() { return productSku; }
    public void setProductSku(String productSku) { this.productSku = productSku; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getChannelSku() { return channelSku; }
    public void setChannelSku(String channelSku) { this.channelSku = channelSku; }
    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }
    public String getMarketplaceId() { return marketplaceId; }
    public void setMarketplaceId(String marketplaceId) { this.marketplaceId = marketplaceId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
