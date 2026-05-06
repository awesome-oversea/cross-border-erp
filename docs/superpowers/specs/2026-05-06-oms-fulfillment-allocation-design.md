# OMS 履约计划分仓与物流优选设计

## 背景

`P2-021` 已经补齐了 `OMS` 履约计划层的手工拆包/合包能力，但拆包后的包裹默认处于 `WAITING_INVENTORY`，还缺少 `P2-022` 要求的分仓与物流优选闭环。

任务清单与验收要求：

- `P2-022`：分仓与物流优选
- 验收：按库存、国家、属性、运费、时效匹配；无可用方案进入异常

## 四色建模

- 粉：履约计划更新时间、分仓执行时刻、物流推荐时刻
- 黄：订单运营人员、OMS 履约服务、WMS 库存服务、TMS 物流推荐服务
- 蓝：包裹待分配、仓库匹配、库存预占、物流优选、异常标记
- 绿：销售订单、履约计划、履约包裹、包裹行、仓库、库存余额、物流推荐结果

## 设计决策

### 1. 操作对象仍是当前履约计划

本轮不新建履约单模型，也不改 `SalesOrder` 结构。分仓与物流优选直接作用在 `OrderFulfillmentPlan.packages` 上。

### 2. 只处理待分配包裹，保留已就绪包裹

为了避免对已 `READY` 包裹重复预占库存，本轮只重算以下包裹：

- `WAITING_INVENTORY`
- `WAITING_CARRIER`
- 缺少仓库或承运商信息的包裹

已 `READY` 包裹直接保留。

### 3. 分仓规则

每个包裹必须由单一仓库完整覆盖，不再二次拆包。仓库选择顺序：

1. 库存满足包裹全部 SKU 数量
2. 目的国优先匹配仓库国家
3. 可用库存更充足的仓库优先
4. 仓库编码稳定排序

若没有任何仓库能完整满足包裹，则包裹进入 `WAITING_INVENTORY`。

### 4. 物流优选规则

仓库选定后，调用 `TmsClient.recommendCarriers` 获取推荐结果，按以下规则选最佳方案：

1. `recommendationScore` 高优先
2. `estimatedCost` 低优先
3. `estimatedDeliveryDays` 低优先

若无推荐结果，则释放库存预占，包裹进入 `WAITING_CARRIER`。

### 5. 计划状态

- 全部包裹成功分配并拿到物流方案：`PLANNED`
- 部分包裹成功，部分因库存不足待处理：`PARTIALLY_ALLOCATED`
- 没有可用分仓方案，或无物流方案：`EXCEPTION`

## API

- `POST /oms/api/in/v1/orders/{orderId}/allocate`

返回 `Result<OrderFulfillmentPlan>`。

## 测试策略

- 包裹可按国家优先选仓并成功拿到物流方案
- 物流推荐为空时进入异常
- 与既有 `OrderServiceTest`、`OrderImportAuditServiceTest` 组合回归
