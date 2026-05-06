# PDM 产品开发流程补强设计

## 背景

本轮继续沿 `PDM -> SOM -> OMS` 主线推进，聚焦 `P2-010 产品开发流程` 的可验收能力补强。当前 `PDM` 已有：

- 选品提报创建、提交、审核骨架
- 产品开发流程创建、单条团队分配、阶段更新

但仍存在直接影响验收的缺口：

- 开发流程允许基于未审核提报创建
- 阶段更新没有顺序约束，也不会自动进入完成态
- 只有单条团队分配，缺少批量能力
- 没有阶段统计与进度汇总输出

## 四色建模

### 粉色原型

- 提报创建时间
- 开发流程创建时间
- 阶段更新时间

### 黄色原型

- 审核人
- 开发人员
- 编辑人员
- 设计人员

### 蓝色原型

- 选品提报
- 产品开发流程
- 阶段统计结果
- 批量分配命令

### 绿色原型

- 类目
- SPU
- 团队成员

## 范围

### 本轮纳入

- 只有 `APPROVED` 状态提报可以创建开发流程
- 开发阶段按既定顺序流转，不允许跳级和逆行
- `LISTED/ARCHIVED` 阶段自动将开发流程状态置为 `completed`
- 提供批量团队分配能力
- 提供开发流程统计能力，输出阶段数量、负责人负载、平均进度

### 本轮暂不纳入

- 采购角色字段扩展
- 独立流程历史表
- 可配置阶段模板
- 提报审核即自动生成 SKU

## 设计决策

### 1. 不新增表，统计基于现有开发流程记录实时计算

`pdm_product_development` 已包含 `stage/developer/editor/designer/priority/status`，本轮不引入 `history` 或 `stats` 新表，避免把范围扩展到持久化模型重构。统计结果由应用服务在读取列表后聚合。

### 2. 阶段顺序固定为枚举顺序

当前 `DevStage` 已定义：

`RESEARCH -> SAMPLING -> TESTING -> MASS_PRODUCTION -> LISTED -> ARCHIVED`

本轮直接使用该顺序作为流转铁律：

- 允许同阶段更新备注
- 允许前进一个阶段
- 允许 `LISTED -> ARCHIVED`
- 禁止跳级和回退

### 3. 进度按阶段映射为固定百分比

不单独存储进度字段，按阶段实时换算：

- `RESEARCH` = 0
- `SAMPLING` = 20
- `TESTING` = 40
- `MASS_PRODUCTION` = 70
- `LISTED` = 90
- `ARCHIVED` = 100

统计接口返回平均进度，用于满足“进度统计正确”的验收口径。

### 4. 团队分配扩展为批量分配而非新增流程节点

保留现有单条分配接口，新增批量分配接口，对多个 `devId` 套用同一组 `developer/editor/designer/priority`。这样能补齐需求规格中“批量分配开发、编辑、美工人员”的能力，而不改变领域模型。

## API 设计

保留：

- `PUT /pdm/api/in/v1/product-developments/{devId}/stage`
- `PUT /pdm/api/in/v1/product-developments/{devId}/team`

新增：

- `PUT /pdm/api/in/v1/product-developments/team/batch`
- `GET /pdm/api/in/v1/product-developments/stats`

## 错误处理

- 提报未审核通过：`PROPOSAL_STATUS_INVALID`
- 开发阶段跳级/逆行：`DEV_STAGE_INVALID`
- 已完成或已取消流程再推进：`DEV_STATUS_INVALID`
- 开发流程不存在：`DEV_NOT_FOUND`

## 测试策略

- 单元测试覆盖：
  - 未审批提报不可创建开发流程
  - 阶段不可跳级、完成态不可再推进
  - 批量分配会更新全部目标流程
  - 统计结果的阶段数量、负责人负载、平均进度正确

## 约束

- 不新增运行时依赖，仅补测试依赖
- API 路径遵循 `/pdm/api/in/v1/...`
- 所有路径锁定 D 盘，C 盘零写入，Maven 依赖新增需审批
