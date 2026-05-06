# 跨境电商 ERP 工程基线与领域最小闭环自验记录

## 范围

依据《跨境电商ERP系统——任务清单.md》和《跨境电商ERP系统——任务清单-验收标准.md》，本次继续处理当前仓库可落地的工程基线、技术中台能力，以及 14 个领域模块的最小可验收业务闭环。

## 工具链

- 已按要求统一放置到 `D:\erp`：
  - JDK 17：`D:\erp\jdk17\jdk-17.0.18+8`
  - Maven 3.9.9：`D:\erp\maven\apache-maven-3.9.9`
- 构建不依赖系统级 Java/Maven 配置。

## 已处理任务

| 领域 | 处理结果 | 验收证据 |
| :--- | :--- | :--- |
| P0 工程基线 | Maven 多模块、Spring Boot、Flyway、OpenAPI、日志、Dockerfile、Compose、CI、ADR 已可用 | 17 模块 `mvn test` 成功 |
| Common 技术中台 | 统一响应、异常、租户上下文、Trace 上下文、领域事件、错误码/i18n、幂等、防重复限流、审批流、定时任务、Mock 通知、Mock 翻译、敏感字段脱敏、连接器/API Key 注册 | `erp-common` 测试通过 |
| IAM | 登录、登出、Token 校验、RBAC、租户、用户、角色、审计 | `IamApiTests` |
| PDM | 类目、品牌、SPU、SKU、上架、重复 SKU 校验、租户隔离 | `PdmApiTests` |
| OMS | 平台订单导入、幂等校验、付款、发货、取消状态校验、租户隔离 | `OmsApiTests` |
| SCM | 供应商、采购单、提交、审批、收货、超收校验、租户隔离 | `ScmApiTests` |
| WMS | 仓库、入库、预占、释放、扣减、库存不足校验、租户隔离 | `WmsApiTests` |
| TMS | 承运商、运单、轨迹、妥投、取消状态校验、租户隔离 | `TmsApiTests` |
| FMS | 应收、确认、收款、超收校验、重复来源单校验、成本归集规则、订单维度成本分摊、利润结果持久化、偏差预警检测、租户隔离 | `FmsApiTests` |
| CRM | 客户、工单、指派、解决、关闭、状态校验、租户隔离 | `CrmApiTests` |
| SYS | 系统参数创建、更新、启停、按 key 查询、租户隔离 | `SysConfigApiTests` |
| Dashboard | 指标创建、更新、按编码查询、重复编码校验、租户隔离 | `DashboardApiTests` |
| SOM | 店铺创建、连接、停用、重复店铺校验、租户隔离 | `RemainingModulesApiTests` |
| ADS | 广告活动创建、启用、暂停、预算校验、租户隔离 | `RemainingModulesApiTests` |
| FBA | FBA 货件创建、提交、接收、重复货件校验、超收校验 | `RemainingModulesApiTests` |
| BI | 报表定义、重复报表编码校验、运行快照、租户隔离 | `RemainingModulesApiTests` |

## 数据库迁移

`erp-app/src/main/resources/db/migration/V1__baseline.sql` 已包含以下业务表基线：

- `sys_tenant`
- `iam_user_account`
- `iam_role`
- `iam_audit_log`
- `pdm_category`
- `pdm_brand`
- `pdm_spu`
- `pdm_sku`
- `oms_sales_order`
- `oms_order_line`
- `wms_warehouse`
- `wms_inventory_balance`
- `scm_supplier`
- `scm_purchase_order`
- `scm_purchase_order_line`
- `tms_carrier`
- `tms_shipment`
- `tms_tracking_event`
- `fms_receivable`
- `fms_payment_record`
- `crm_customer`
- `crm_service_ticket`
- `sys_config`
- `dashboard_metric`
- `som_sales_store`
- `ads_campaign`
- `fba_shipment`
- `bi_report_definition`

## 自验命令

```cmd
set JAVA_HOME=D:\erp\jdk17\jdk-17.0.18+8&& set PATH=D:\erp\jdk17\jdk-17.0.18+8\bin;D:\erp\maven\apache-maven-3.9.9\bin;%PATH%&& mvn test
```

## 自验结果

- Reactor 模块数：17
- 领域模块数：14
- 测试结果：`BUILD SUCCESS`
- 测试用例：20 个，全部通过
- Spring Boot 上下文：成功启动
- Flyway：成功执行 `V1__baseline.sql`
- `erp-common` 单元测试：领域事件、幂等、限流、审批、定时任务、通知、翻译、脱敏、连接器/API Key 均通过
- MockMvc 接口：IAM、PDM、OMS、SCM、WMS、TMS、FMS、CRM、SYS、Dashboard、SOM、ADS、FBA、BI 均通过最小闭环验收
- FMS 引擎闭环：`FmsApiTests#createAggregationRuleAggregateOrderCostAndPersistProfitResult` 已通过，覆盖成本归集规则创建、成本事件按订单维度归集、利润结果持久化、利润偏差预警检测与查询，对应验收项 `P2-040`、`P2-041`、`P4-013` 与“13.1 最小经营闭环”
- FMS 回归：`FmsApiTests` 全类 5 个集成测试通过，`BUILD SUCCESS`

## 说明

当前完成的是 14 个领域模块的最小可验收闭环，以及一批 P1 技术中台内存/Mock 可测实现。仓储采用内存实现，数据库 DDL 已建立基线。完整生产级实现仍需继续推进持久化 Mapper、复杂业务规则、真实 Kafka/Redis/MinIO/ES 集成、外部平台集成、异步消息可靠投递、权限细粒度覆盖和完整前端页面。

## OMS-WMS-SCM-TMS-FMS 最小闭环补齐

- 订单履约：履约计划、运单创建、平台发货回传日志已验证。
- 采购收货：收货后库存流水与产品成本事件已验证。
- 财务利润：成本事件已支持订单维度归集，利润不再按同 SKU 跨单串算。
- 引擎最小骨架：成本归集规则、分摊结果、利润结果已可持久化并通过接口验证。

## 结论

当前仓库已从后端骨架推进到可构建、可启动、可测试的模块化 Spring Boot ERP 基线，并具备 14 个领域模块的最小可验收业务闭环。动态自验已通过。
