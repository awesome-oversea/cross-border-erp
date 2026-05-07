package com.aidotnet.erp.wms.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("wms_outbound_package_line")
public class OutboundPackageLineDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String packageLineId;
    private String packageId;
    private String orderLineId;
    private String sellerSku;
    private int quantity;
    private String batchNo;

    public String getPackageLineId() { return packageLineId; }
    public void setPackageLineId(String packageLineId) { this.packageLineId = packageLineId; }
    public String getPackageId() { return packageId; }
    public void setPackageId(String packageId) { this.packageId = packageId; }
    public String getOrderLineId() { return orderLineId; }
    public void setOrderLineId(String orderLineId) { this.orderLineId = orderLineId; }
    public String getSellerSku() { return sellerSku; }
    public void setSellerSku(String sellerSku) { this.sellerSku = sellerSku; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
}
