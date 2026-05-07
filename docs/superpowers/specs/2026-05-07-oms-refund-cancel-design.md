# OMS 退款与取消流程设计
## 背景

OMS 履约主线已经具备订单导入、履约计划、发货与平台回传能力。`P2-024` 需要补齐退款与取消订单的业务闭环，使 OMS 在订单取消和退款审批完成后，能够按规则向 WMS/FMS 回写库存与财务成本事件，并保留可追踪的领域事件。

## 四色建模

- 粉：取消申请时刻、退款申请时刻、退款审批时刻、退款完成时刻
- 黄：订单运营人员、财务审核人员、OMS 订单服务、WMS、FMS
- 蓝：取消订单、申请退款、审核退款、驳回退款、完成退款、释放预占库存、记录退款损耗
- 绿：销售订单、退款单、履约计划、履约包裹、订单行、库存预占、成本事件

## 设计决策

### 1. 取消与退款的审批边界
`P2-024` 不新增独立审批实体，复用现有退款状态流转：

- `REQUESTED -> APPROVED -> COMPLETED`
- `REQUESTED -> REJECTED`

取消订单保持现有直接执行入口，但补齐库存释放、财务回写和领域事件，满足“退款/取消流程可审批；库存和财务成本事件按规则回写”的验收要求。

### 2. 库存回写规则

- 取消订单：仅对未发货订单执行库存释放
- 释放对象：当前履约计划中 `READY` 且存在 `warehouseId` 的包裹行
- 已发货订单不走取消分支，因此不做库存回滚
- 已发货/已签收退款不在 OMS 直接回库；退货回库归 `P3-015` 售后流程处理

### 3. 财务回写规则

- 已支付订单取消：按订单行金额占比写入 `RETURN_COST` 成本事件
- 退款完成：按退款金额在订单行之间按金额占比分摊，写入 `RETURN_COST` 成本事件
- 成本事件来源：
  - 取消：`sourceType=OMS_ORDER`，`sourceId=orderId`
  - 退款：`sourceType=REFUND`，`sourceId=refundId`

这样既能满足利润核算口径，也能保持 SKU 维度的成本归集能力。

### 4. 状态恢复规则

退款驳回时不能一律回退到 `PAID`，按当前订单上下文恢复：

- `RETURN_REQUESTED -> DELIVERED`
- `REFUND_REQUESTED` 且 `fulfillmentStatus` 为 `SHIPPED` / `PARTIALLY_SHIPPED` -> `SHIPPED`
- 其他 `REFUND_REQUESTED` -> `PAID`

### 5. 领域事件

补齐以下事件，便于后续风控、BI、CRM 订阅：

- `erp.oms.order.cancelled.v1`
- `erp.oms.refund.requested.v1`
- `erp.oms.refund.approved.v1`
- `erp.oms.refund.rejected.v1`
- `erp.oms.order.refunded.v1`

## 测试策略

- 取消已支付未发货订单时释放当前履约计划中的预占库存
- 退款驳回时按订单履约状态恢复为 `PAID` / `SHIPPED` / `DELIVERED`
- 退款完成时向 FMS 写入按 SKU 分摊的 `RETURN_COST` 成本事件
- 取消与退款关键节点发布领域事件
