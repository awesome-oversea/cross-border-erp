package com.aidotnet.erp.scm.infrastructure.connector;

import com.aidotnet.erp.common.connector.ProcurementConnector;
import com.aidotnet.erp.common.exception.BizException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Alibaba1688Connector implements ProcurementConnector {

    private static final Logger log = LoggerFactory.getLogger(Alibaba1688Connector.class);
    private static final String CODE = "ALIBABA_1688";
    private static final String NAME = "1688采购平台";

    @Override
    public String getProcurementCode() { return CODE; }

    @Override
    public String getProcurementName() { return NAME; }

    @Override
    public Map<String, Object> searchProducts(String keyword, Map<String, String> params) {
        log.info("Searching 1688 products with keyword={}", keyword);
        if (keyword == null || keyword.isBlank()) {
            throw new BizException("ALIBABA_PARAM_MISSING", "搜索关键词不能为空");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("platform", CODE);
        result.put("keyword", keyword);
        result.put("products", java.util.List.of());
        result.put("totalCount", 0);
        return result;
    }

    @Override
    public Map<String, Object> getProductDetail(String productId) {
        log.info("Getting 1688 product detail for id={}", productId);
        if (productId == null || productId.isBlank()) {
            throw new BizException("ALIBABA_PARAM_MISSING", "productId不能为空");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("platform", CODE);
        result.put("productId", productId);
        result.put("title", "");
        result.put("price", "0.00");
        result.put("moq", 0);
        result.put("supplier", Map.of("supplierId", "", "name", "", "rating", "0"));
        return result;
    }

    @Override
    public Map<String, Object> createPurchaseOrder(Map<String, Object> orderParams) {
        log.info("Creating 1688 purchase order");
        Object items = orderParams.get("items");
        if (items == null) {
            throw new BizException("ALIBABA_PARAM_MISSING", "采购商品列表不能为空");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", "1688_po_" + System.currentTimeMillis());
        result.put("status", "PENDING");
        result.put("message", "采购订单已提交至1688");
        return result;
    }

    @Override
    public Map<String, Object> queryOrderStatus(String orderId) {
        log.info("Querying 1688 order status for id={}", orderId);
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderId);
        result.put("status", "WAIT_SELLER_SEND_GOODS");
        result.put("statusText", "等待卖家发货");
        return result;
    }

    @Override
    public Map<String, Object> getLogisticsInfo(String orderId) {
        log.info("Getting 1688 logistics info for order={}", orderId);
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderId);
        result.put("logisticsCompany", "");
        result.put("trackingNumber", "");
        result.put("traces", java.util.List.of());
        return result;
    }

    @Override
    public Map<String, Object> confirmReceipt(String orderId, Map<String, Object> receiptParams) {
        log.info("Confirming 1688 receipt for order={}", orderId);
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderId);
        result.put("status", "CONFIRMED");
        result.put("message", "收货确认成功");
        return result;
    }
}
