# SCM 供应商管理设计

## 背景

SCM 已有供应商、采购单、补货建议的基础骨架，但 `P2-026` 仍有三个明显缺口：

- 供应商主数据只有基础字段，联系人、资质、评分汇总没有落库和统一查询视图
- `/scm/api/in/v1/suppliers` 虽然存在，但 `get/update` 仍通过 `list` 过滤，缺少完整档案返回
- 采购建单只校验供应商存在，不拦截停用或黑名单供应商

本次实现聚焦验收要求“供应商、资质、联系人、评分可维护；停用供应商不可新建采购；API 路径 `/scm/api/in/v1/suppliers`”，不扩展到采购计划、审批、跟单。

## 四色建模

- 粉：供应商建档时间、联系人更新时间、资质有效期、评分时间、采购单创建时间
- 黄：采购专员、供应链经理、供应商联系人、系统评分服务
- 蓝：建档、更新供应商、维护联系人、维护资质、记录评分、查询供应商档案、创建采购单校验
- 绿：供应商、供应商联系人、供应商资质、供应商评分汇总、供应商评估记录、采购单

## 设计决策

### 1. 供应商聚合视图统一为 `SupplierProfile`

保留现有 `Supplier` 作为主数据实体，新增 `SupplierProfile` 作为接口出参：

- `supplier`：基础主数据
- `contacts`：联系人列表
- `qualifications`：资质列表
- `score`：当前评分汇总

这样 `/scm/api/in/v1/suppliers` 的创建、查询、更新都围绕一个聚合视图展开，避免前端拼装多次请求。

### 2. 联系人和资质采用“全量替换”维护

联系人和资质使用独立表存储，但在接口层采用全量提交、全量替换：

- `PUT /scm/api/in/v1/suppliers/{supplierId}` 传入新列表时，先删除旧数据再插入新数据
- 未传列表则保持现状不变

这能在当前单体阶段用最少复杂度满足“可维护”，同时避免局部 patch 带来的排序、删除语义歧义。

### 3. 评分分为“评估历史 + 当前汇总”

已有 `SupplierEvaluation` 继续保存每次评估明细；新增 `SupplierScore` 的持久化与汇总逻辑：

- 手工维护评分时直接更新 `SupplierScore`
- 通过评估接口新增评估记录时，同步回写最新 `SupplierScore`

这样既保留评分历史，又能在供应商档案页直接读取当前分数。

### 4. 采购建单前显式校验供应商状态

`PurchaseService.createPurchaseOrder` 在供应商存在校验后追加状态校验：

- `ACTIVE` 可建单
- `INACTIVE`、`BLACKLISTED` 不可建单
- `PENDING_REVIEW` 也不可建单

错误统一抛出业务异常，避免停用供应商绕过主数据管控继续生成采购草稿。

### 5. 数据层采用增量兼容

不回改历史 Flyway 版本，新增迁移补齐：

- `scm_supplier` 缺失字段
- `scm_purchase_order`、`scm_purchase_order_line` 与当前 Mapper 不一致的字段
- 新增 `scm_supplier_contact`、`scm_supplier_qualification`
- `scm_supplier_score` 调整为当前聚合评分结构

同时更新 `db/schema.sql`，保证 H2 零依赖启动结构与代码一致。

## API 方案

- `POST /scm/api/in/v1/suppliers`：创建供应商档案，可带联系人、资质、评分
- `GET /scm/api/in/v1/suppliers`：查询供应商档案列表
- `GET /scm/api/in/v1/suppliers/{supplierId}`：查询单个供应商档案
- `PUT /scm/api/in/v1/suppliers/{supplierId}`：更新供应商基础信息，并按需替换联系人、资质、评分
- `POST /scm/api/in/v1/suppliers/{supplierId}/evaluations`：新增供应商评估并刷新评分汇总
- `GET /scm/api/in/v1/suppliers/{supplierId}/evaluations`：查询评估历史

## 测试策略

- 创建供应商档案时应返回联系人、资质、评分
- 更新供应商档案时应保留未修改字段并替换传入的联系人/资质/评分
- 新增供应商评估后应写入评估记录并刷新评分汇总
- 停用供应商创建采购单应抛出业务异常
- 活跃供应商创建采购单仍应成功，避免误伤既有采购流程
