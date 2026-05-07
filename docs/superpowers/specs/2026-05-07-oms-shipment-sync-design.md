# OMS 发货与平台标记发货设计

## 背景

订单履约主线已完成：

- `P2-021` 手工拆包/合包
- `P2-022` 分仓与物流优选

下一步是 `P2-023`：发货确认、追踪号回填、平台状态回传，并支持失败重试。

## 四色建模

- 粉：发货确认时刻、平台同步时刻、重试时刻
- 黄：订单运营人员、OMS 发货服务、WMS、TMS、平台回传执行器
- 蓝：库存扣减、生成追踪号、包裹发货、平台标记发货、失败重试
- 绿：销售订单、履约计划、包裹、追踪号、平台同步日志

## 设计决策

### 1. 订单与包裹同时回写

发货后不仅更新包裹状态和追踪号，还需要回写订单履约状态。订单主状态仍为 `OrderStatus.SHIPPED`，订单 `fulfillmentStatus` 使用履约计划结果：

- 全部包裹发货：`SHIPPED`
- 部分包裹发货：`PARTIALLY_SHIPPED`

### 2. 追踪号以包裹为主，订单通过履约状态反映结果

当前 `SalesOrder` 没有单独的 tracking 字段，因此追踪号继续保存在 `OrderFulfillmentPackage`。订单侧回写 `fulfillmentStatus`，满足“订单/包裹均已回写”的验收要求。

### 3. 平台标记发货失败不阻断发货

平台同步失败不回滚库存扣减和包裹发货，失败结果写入：

- `OrderFulfillmentPackage.platformSyncStatus`
- `OrderFulfillmentPackage.platformSyncAttempts`
- `PlatformShipmentSyncLog`

### 4. 重试规则

仅重试 `platformSyncStatus != SUCCESS` 的已发货包裹。每次重试：

- 尝试次数 `+1`
- 追加一条平台同步日志
- 更新包裹同步状态与错误信息

## 测试策略

- 发货后订单履约状态回写为 `SHIPPED`
- 包裹生成追踪号并更新平台同步状态
- 平台标记发货失败后可重试，尝试次数递增，日志追加
