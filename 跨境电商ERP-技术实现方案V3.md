# 跨境电商ERP技术实现方案

文档版本：V4.0
编制日期：2026-05-01
文档状态：正式发布
编制依据：
- 跨境电商ERP系统——需求规格说明书V4
- 跨境电商ERP系统详细设计说明书V11
- AGENTS.md v5.0

---

## 文档修订历史

| 版本 | 日期 | 修订内容 |
|---|---|---|
| V1.0 | 2026-04-24 | 初始版本 |
| V2.0 | 2026-04-24 | 增加微服务架构设计 |
| V3.0 | 2026-04-24 | 完善技术栈选型、部署架构、监控运维方案 |
| V4.0 | 2026-05-01 | 对齐V11详细设计：14域+14业务中台+11技术中台+PMS集成+DDD分层+双层网关+事件驱动 |

---

## 1. 技术架构总览

### 1.1 架构设计原则

| 原则 | 说明 |
|---|---|
| 模块化单体优先 | 初期按14域划分模块，预留微服务拆分接口 |
| 事件驱动 | 核心业务流程采用Kafka+Outbox事件驱动架构 |
| 数据一致性优先 | 关键业务数据采用强一致性，非关键数据采用最终一致性 |
| DDD分层 | 每个域模块严格遵循interfaces/application/domain/infrastructure分层 |
| 可观测性内置 | 从架构层面内置日志、指标、链路追踪能力 |
| 安全左移 | 安全设计贯穿整个开发生命周期 |
| 审批主控 | PMS集成遵循审批主控原则，AI建议需人工审批 |

### 1.2 架构分层全景

```
┌─────────────────────────────────────────────────────────────────┐
│                     展示层 (Presentation)                        │
│  Next.js 14+ 管理后台 / PDA仓库作业 / 开放API / PMS集成         │
└──────────────────────────────┬──────────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────────┐
│                     网关层 (Gateway)                              │
│  Kong(外部: SSL/域名路由/WAF/全局限流) → SCG(内部: 认证/租户/熔断)│
└──────────────────────────────┬──────────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────────┐
│                 应用层 (Application Layer)                       │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ 14业务域: DASHBOARD/IAM/PDM/SOM/ADS/OMS/SCM/WMS/        │  │
│  │           FBA/TMS/CRM/FMS/BI/SYS                         │  │
│  └──────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ 14业务中台: 审核/汇率/支付/订单策略/物流策略/计费/CDP/    │  │
│  │            税务/合规/选品/广告/成本/利润/凭证              │  │
│  └──────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ 11技术中台: 通知/文件/工作流/调度/权限/审计/网关/         │  │
│  │            翻译/脱敏/API管理/连接器                        │  │
│  └──────────────────────────────────────────────────────────┘  │
└──────────────────────────────┬──────────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────────┐
│                 事件层 (Event Layer)                              │
│  Kafka/RocketMQ + Outbox Pattern + Spring Cloud Stream          │
└──────────────────────────────┬──────────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────────┐
│                 连接器层 (Connector Layer)                        │
│  Amazon SP-API / Shopify API / TikTok Shop / 1688 / 物流商     │
│  海外仓 / 支付 / PMS                                           │
└──────────────────────────────┬──────────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────────┐
│                 数据层 (Data Layer)                               │
│  PostgreSQL / Redis / Elasticsearch / MinIO / ClickHouse        │
│  Canal → Kafka → Flink → ES/PG/Redis/ClickHouse                │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. 技术栈选型

### 2.1 后端技术栈

| 层级 | 技术选型 | 版本 | 用途 |
|---|---|---|---|
| 基础框架 | Spring Boot | 3.2.x | 应用基线 |
| 微服务框架 | Spring Cloud | 2023.x | 服务治理 |
| 阿里扩展 | Spring Cloud Alibaba | 2023.x | Nacos/Sentinel/Seata |
| JDK | Eclipse Temurin | 17 | 运行时 |
| 服务注册 | Nacos | 2.3.x | 注册中心+配置中心 |
| 熔断限流 | Sentinel | 1.8.x | 限流/熔断/降级 |
| 远程调用 | OpenFeign + LoadBalancer | - | 域间调用 |
| 外部网关 | Kong | 3.x | SSL/域名路由/WAF/全局限流 |
| 内部网关 | Spring Cloud Gateway | - | 认证/租户Header/限流熔断 |
| 消息队列 | Kafka | 3.6+ | 高吞吐事件驱动 |
| 事务消息 | RocketMQ | 5.x | 分布式事务消息 |
| 分布式事务 | Seata | 1.8.x | AT模式（一期预留） |
| 长事务编排 | Temporal Java SDK | 1.x | 长事务编排、重试、补偿、回放 |
| 批处理 | Spring Batch | 5.x | 账单清洗、利润计算、批导入导出 |
| 安全 | Spring Security + JWT | 6.x / 0.12.5 | 认证鉴权 |
| ORM | MyBatis-Plus | 3.5.x | 多租户+逻辑删除+分页 |
| 数据库 | PostgreSQL | 15+ | 主库 |
| 本地开发库 | H2 | - | 单元测试 |
| 缓存 | Redis | 7.x | 缓存/分布式锁/会话 |
| 搜索 | Elasticsearch | 8.x | 全文搜索/日志 |
| 对象存储 | MinIO | - | S3兼容文件存储 |
| 数据库迁移 | Flyway | - | DDL版本管理 |
| API文档 | SpringDoc OpenAPI | - | 14域分组文档 |
| 代码规范 | EditorConfig + Spotless | - | 格式化强制 |
| 工作流 | 审批状态机（自研） | - | 轻量审批流 |

### 2.2 前端技术栈

| 层级 | 技术选型 | 版本 | 用途 |
|---|---|---|---|
| 框架 | Next.js | 14+ | App Router |
| UI库 | React | 18+ | 组件化 |
| 类型 | TypeScript | 5.x | 类型安全 |
| 组件库 | Ant Design | 5.x | 企业级UI |
| 状态管理 | Zustand | - | 轻量状态 |
| 请求 | Axios + SWR | - | 数据请求 |
| 图表 | ECharts | 5.x | 数据可视化 |

### 2.3 数据分析技术栈

| 层级 | 技术选型 | 用途 |
|---|---|---|
| CDC | Canal | PostgreSQL变更捕获 |
| 流处理 | Flink | 实时数据处理 |
| 分析库 | ClickHouse | OLAP分析 |
| 同步 | Canal→Kafka→Flink | 实时数据管道 |

### 2.4 运维技术栈

| 层级 | 技术选型 | 用途 |
|---|---|---|
| 容器 | Docker + K8s | 容器化部署 |
| 编排 | Helm | K8s包管理 |
| CI/CD | GitLab CI / GitHub Actions | 流水线 |
| 日志 | EFK (ES+Fluentd+Kibana) | 日志采集查询 |
| 指标 | Prometheus + Grafana | 监控告警 |
| 链路 | OpenTelemetry + Jaeger | 分布式追踪 |
| 告警 | AlertManager | 告警通知 |

---

## 3. 系统架构设计

### 3.1 DDD分层架构

每个域模块严格遵循DDD四层架构：

```
erp-domain-xxx/
└── src/main/java/com/aidotnet/erp/xxx/
    ├── interfaces/          # 接口层
    │   ├── rest/            # REST Controller
    │   ├── dto/             # 数据传输对象
    │   └── assembler/       # DTO↔领域对象转换
    ├── application/         # 应用层
    │   ├── service/         # 用例协调、事务编排
    │   └── command/         # 命令对象
    ├── domain/              # 领域核心（纯Java类，不依赖Spring）
    │   ├── model/           # 聚合根、实体、值对象
    │   │   ├── aggregate/   # 聚合根
    │   │   ├── entity/      # 实体
    │   │   └── vo/          # 值对象
    │   ├── service/         # 领域服务
    │   ├── event/           # 领域事件
    │   ├── repository/      # 仓储接口
    │   └── gateway/         # 外部网关接口
    └── infrastructure/      # 基础设施层
        ├── persistence/     # MyBatis Mapper实现
        ├── messaging/       # 消息发送实现
        ├── client/          # 外部API客户端
        └── config/          # Spring配置
```

### 3.2 模块依赖规则

- interfaces → application → domain ← infrastructure
- domain层不依赖任何Spring框架
- infrastructure实现domain层定义的接口
- 域间调用通过application层接口，禁止直接调用infrastructure

### 3.3 Maven多模块结构

```
erp-platform/
├── pom.xml                          # 父POM
├── erp-common/                      # 公共模块
│   ├── erp-common-core/             # 核心工具类
│   ├── erp-common-security/         # 安全工具类
│   ├── erp-common-mybatis/          # MyBatis配置
│   ├── erp-common-redis/            # Redis配置
│   ├── erp-common-kafka/            # Kafka配置
│   ├── erp-common-es/               # ES配置
│   ├── erp-common-minio/            # MinIO配置
│   └── erp-common-openapi/          # OpenAPI配置
├── erp-gateway/                     # Spring Cloud Gateway
├── erp-domain-dashboard/            # 工作台域
├── erp-domain-iam/                  # 组织权限域
├── erp-domain-pdm/                  # 产品开发域
├── erp-domain-som/                  # 销售运营域
├── erp-domain-ads/                  # 广告管理域
├── erp-domain-oms/                  # 订单域
├── erp-domain-scm/                  # 供应链域
├── erp-domain-wms/                  # 仓储域
├── erp-domain-fba/                  # FBA/海外仓域
├── erp-domain-tms/                  # 物流域
├── erp-domain-crm/                  # 客服售后域
├── erp-domain-fms/                  # 财务域
├── erp-domain-bi/                   # 商业智能域
├── erp-domain-sys/                  # 系统设置域
├── erp-connector/                   # 连接器模块
│   ├── erp-connector-amazon/        # Amazon SP-API
│   ├── erp-connector-shopify/       # Shopify API
│   ├── erp-connector-tiktok/        # TikTok Shop API
│   ├── erp-connector-1688/          # 1688 API
│   └── erp-connector-logistics/     # 物流商API
└── erp-starter/                     # 启动模块
```

---

## 4. 数据库设计

### 4.1 数据库策略

| 策略 | 说明 |
|---|---|
| 主库 | PostgreSQL 15+，单库多Schema |
| Schema划分 | 按域划分Schema（iam/pdm/som/oms/scm/wms/fba/tms/crm/fms/bi/sys） |
| 本地开发 | H2内存数据库 |
| 迁移工具 | Flyway，每个域独立迁移脚本 |
| 多租户 | 共享数据库+租户ID字段隔离 |
| 逻辑删除 | 统一deleted字段 |
| 审计字段 | 统一created_by/created_at/updated_by/updated_at |

### 4.2 核心数据模型

| 域 | 核心表 | 说明 |
|---|---|---|
| IAM | sys_tenant/sys_org/sys_user/sys_role/sys_permission | 组织权限 |
| PDM | pdm_product/pdm_sku/pdm_sku_mapping/pdm_category | 产品主数据 |
| SOM | som_shop/som_listing/som_price_log | 销售运营 |
| ADS | ads_campaign/ads_keyword/ads_report | 广告管理 |
| OMS | oms_channel_order/oms_sales_order/oms_fulfillment_order/oms_package | 订单三层 |
| SCM | scm_purchase_order/scm_supplier/scm_purchase_item | 供应链 |
| WMS | wms_inventory/wms_inventory_tx/wms_inbound/wms_outbound | 库存事务账 |
| FBA | fba_inventory/fba_replenish/fba_shipment | FBA管理 |
| TMS | tms_carrier/tms_shipment/tms_tracking | 物流管理 |
| CRM | crm_email/crm_refund/crm_return/crm_quality_issue | 客服售后 |
| FMS | fms_payment/fms_cost_event/fms_profit/fms_settlement | 财务 |
| BI | bi_report/bi_kpi/bi_dashboard | 商业智能 |
| SYS | sys_config/sys_connector/sys_dict/sys_pms_suggestion | 系统设置 |

### 4.3 库存事务账设计

```
wms_inventory_tx
├── id (PK)
├── tenant_id (租户ID)
├── warehouse_id (仓库ID)
├── sku_id (SKU ID)
├── tx_type (事务类型: IN/OUT/HOLD/RELEASE/ADJUST)
├── qty (数量, 正为入, 负为出)
├── before_qty (事务前数量)
├── after_qty (事务后数量)
├── status_type (状态: ON_HAND/PRE_OCCUPIED/AVAILABLE/IN_TRANSIT/DAMAGED)
├── source_type (来源类型: PURCHASE/ORDER/TRANSFER/ADJUSTMENT)
├── source_id (来源单据ID)
├── created_by / created_at
└── 备注: 不可修改, 只能新增冲正
```

### 4.4 成本事件设计

```
fms_cost_event
├── id (PK)
├── tenant_id (租户ID)
├── event_type (8类: PURCHASE/LOGISTICS/PLATFORM/ADS/WAREHOUSE/RETURN/TAX/OTHER)
├── amount (金额)
├── currency (币种)
├── exchange_rate (汇率快照)
├── settled_amount (结算金额, 本币)
├── sku_id / order_id / shop_id (关联维度)
├── source_type / source_id (来源)
├── event_date (事件日期)
├── created_by / created_at
└── 备注: 不可删除, 只能新增冲正
```

---

## 5. API网关设计

### 5.1 双层网关架构

```
外部请求 → Kong(外部网关) → Spring Cloud Gateway(内部网关) → 业务服务
```

| 网关 | 职责 | 技术选型 |
|---|---|---|
| Kong | SSL终止、域名路由、WAF防护、全局限流、IP白名单 | Kong 3.x |
| SCG | JWT认证、租户Header注入、API限流熔断、请求日志、灰度路由 | Spring Cloud Gateway |

### 5.2 API路径规范

```
/api/v1/{domain}/{resource}
```

| 域 | 路径前缀 | 示例 |
|---|---|---|
| IAM | /api/v1/iam | /api/v1/iam/users |
| PDM | /api/v1/pdm | /api/v1/pdm/products |
| SOM | /api/v1/som | /api/v1/som/shops |
| ADS | /api/v1/ads | /api/v1/ads/campaigns |
| OMS | /api/v1/oms | /api/v1/oms/orders |
| SCM | /api/v1/scm | /api/v1/scm/purchase-orders |
| WMS | /api/v1/wms | /api/v1/wms/inventory |
| FBA | /api/v1/fba | /api/v1/fba/replenish |
| TMS | /api/v1/tms | /api/v1/tms/shipments |
| CRM | /api/v1/crm | /api/v1/crm/emails |
| FMS | /api/v1/fms | /api/v1/fms/cost-events |
| BI | /api/v1/bi | /api/v1/bi/reports |
| SYS | /api/v1/sys | /api/v1/sys/config |
| PMS | /api/v1/pms | /api/v1/pms/suggestions |

---

## 6. 消息队列设计

### 6.1 Kafka主题规划

| 主题 | 用途 | 生产者 | 消费者 |
|---|---|---|---|
| order.created | 订单创建事件 | OMS | WMS/SCM/FMS |
| order.cancelled | 订单取消事件 | OMS | WMS/FMS |
| inventory.changed | 库存变更事件 | WMS | OMS/BI |
| purchase.received | 采购收货事件 | SCM | WMS/FMS |
| shipment.delivered | 发货完成事件 | TMS | OMS/CRM |
| cost.event.created | 成本事件创建 | FMS | BI |
| pms.suggestion.received | PMS建议接收 | SYS | 各域 |
| outbox.event | Outbox事件发布 | 各域 | 事件分发器 |

### 6.2 Outbox模式

```
业务操作 → 写业务表 + 写outbox表 → 事务提交 → CDC/定时轮询 → 发布到Kafka
```

- 保证业务操作和事件发布的原子性
- 避免分布式事务
- 支持事件重放

### 6.3 RocketMQ事务消息

用于需要跨域强一致的短链路场景（如订单→库存预占）：
- 发送半消息
- 执行本地事务
- 提交/回滚消息

### 6.4 Temporal长事务编排

用于跨域长链路业务场景（如采购→收货→质检→入库→结算、FBA发货→头程→入仓→异常处理）：

- **编排方式**：Temporal Workflow定义长事务流程，Activity封装各域操作
- **补偿回放**：每个Activity定义补偿逻辑，失败时自动回滚已执行步骤
- **重试策略**：可配置最大重试次数、退避策略、超时时间
- **一致性策略分层**：
  - 单服务内：本地事务
  - 跨服务短链路：Outbox + 幂等消费
  - 长事务：Temporal Saga / 补偿工作流
  - Seata：只作为局部场景可选方案（一期预留）

---

## 7. 缓存架构设计

### 7.1 缓存策略

| 数据类型 | 缓存策略 | TTL |
|---|---|---|
| 权限数据 | Cache-Aside | 30分钟 |
| 产品主数据 | Cache-Aside | 15分钟 |
| 库存可用量 | Write-Through | 5分钟 |
| 汇率数据 | Cache-Aside | 1小时 |
| 系统配置 | Cache-Aside | 30分钟 |
| 会话Token | TTL | 与JWT过期一致 |

### 7.2 缓存Key规范

```
erp:{domain}:{entity}:{id}
erp:{domain}:{entity}:list:{query_hash}
```

---

## 8. 安全架构设计

### 8.1 认证鉴权

| 层级 | 机制 | 说明 |
|---|---|---|
| 外部网关 | Kong JWT Plugin | Token验证 |
| 内部网关 | SCG GlobalFilter | 租户Header注入 |
| 应用层 | Spring Security | RBAC+10维权限 |
| 数据层 | MyBatis-Plus TenantLine | 租户数据隔离 |

### 8.2 10维数据权限

租户/组织/店铺/渠道/市场/品牌/仓库/供应商/品类/自定义

实现方式：
- SQL拦截器自动注入WHERE条件
- 基于角色的维度配置
- 支持对象级权限覆盖

### 8.3 数据脱敏

| 字段类型 | 脱敏规则 | 示例 |
|---|---|---|
| 手机号 | 中间4位* | 138****1234 |
| 邮箱 | @前保留首尾 | t***@example.com |
| 银行卡 | 保留后4位 | ************1234 |
| 身份证 | 保留首3尾4 | 110***********1234 |

---

## 9. 连接器架构设计

### 9.1 连接器接口

```java
public interface Connector<C extends ConnectorConfig, R extends ConnectorResult> {
    String getType();
    R execute(C config, ConnectorRequest request);
    R retry(C config, ConnectorRequest request);
    void compensate(C config, ConnectorRequest request);
}
```

### 9.2 连接器规范

- 必须支持幂等（基于requestId去重）
- 必须支持重试（指数退避）
- 必须支持补偿（正向操作失败后回滚）
- 必须支持回放（基于事件日志重放）
- 必须记录调用日志（请求/响应/耗时/状态）

### 9.3 一期连接器

| 连接器 | 平台 | 数据范围 |
|---|---|---|
| Amazon SP-API | Amazon | 订单/Listing/库存/广告/报告 |
| Shopify API | Shopify | 订单/产品/库存/客户 |
| TikTok Shop API | TikTok Shop | 订单/产品/库存/物流 |
| 1688 API | 1688 | 选品/下单付款 |
| 物流商API | 多家物流商 | 运费/面单/轨迹/索赔 |

---

## 10. PMS集成设计

### 10.1 集成架构

```
PMS → Kafka(pms.suggestion.received) → ERP建议池 → 人工审批 → 执行 → Kafka(pms.feedback) → PMS
```

### 10.2 审批状态机

```
建议接收 → 待审批 → 已审批 → 已执行 → 已反馈
                ↓
             已拒绝 → 已反馈
```

### 10.3 权限边界

- PMS只能写建议/草稿，不能直接修改ERP数据
- 所有AI建议必须经人工审批后执行
- 执行结果必须反馈给PMS用于模型优化

---

## 11. 性能优化方案

### 11.1 数据库优化

| 策略 | 说明 |
|---|---|
| 索引优化 | 基于查询模式创建复合索引 |
| 分区 | 订单/库存事务账按月分区 |
| 读写分离 | 一期预留，二期启用 |
| 连接池 | HikariCP，最大连接数20 |

### 11.2 缓存优化

| 策略 | 说明 |
|---|---|
| 多级缓存 | L1本地缓存(Caffeine) + L2分布式缓存(Redis) |
| 缓存预热 | 系统启动时预加载热点数据 |
| 缓存穿透 | 布隆过滤器+空值缓存 |
| 缓存击穿 | 互斥锁+热点数据永不过期 |

### 11.3 异步优化

| 策略 | 说明 |
|---|---|
| 事件驱动 | 核心链路异步解耦 |
| 批量操作 | 订单抓取/库存同步批量处理 |
| 并行处理 | CompletableFuture并行调用 |
| 延迟任务 | 调度框架处理定时任务 |

---

## 12. 部署架构设计

### 12.1 环境规划

| 环境 | 用途 | 配置 |
|---|---|---|
| local | 本地开发 | Docker Compose |
| test | 集成测试 | K8s单节点 |
| staging | 预发布 | K8s多节点 |
| production | 生产 | K8s多节点+HA |

### 12.2 Docker Compose（本地开发）

```yaml
services:
  postgres: PostgreSQL 15
  redis: Redis 7
  elasticsearch: ES 8
  minio: MinIO
  kafka: Kafka 3.6 + Zookeeper
  nacos: Nacos 2.3
  erp-app: ERP应用
```

### 12.3 K8s部署（生产）

| 组件 | 副本数 | 资源 |
|---|---|---|
| ERP应用 | 2+ | 2C4G |
| Kong网关 | 2 | 2C4G |
| PostgreSQL | 1主1从 | 4C16G |
| Redis | 1主1从 | 2C8G |
| Kafka | 3 Broker | 4C8G |
| ES | 3节点 | 4C16G |
| Nacos | 3节点 | 2C4G |

### 12.4 GitOps部署流程

```
代码提交 → CI流水线(构建+测试) → 镜像推送 → Helm Chart更新 → ArgoCD同步 → K8s滚动更新
```

---

## 13. 监控运维方案

### 13.1 监控体系

| 维度 | 工具 | 指标 |
|---|---|---|
| 基础设施 | Prometheus+NodeExporter | CPU/内存/磁盘/网络 |
| 应用性能 | Prometheus+Micrometer | QPS/延迟/错误率 |
| JVM | Prometheus+JMXExporter | GC/堆/线程 |
| 数据库 | Prometheus+PGExporter | 连接数/慢查询/锁等待 |
| 缓存 | Prometheus+RedisExporter | 命中率/内存/连接数 |
| 消息队列 | KafkaExporter | 消息积压/消费延迟 |
| 链路追踪 | OpenTelemetry+Jaeger | 请求链路/耗时分布 |

### 13.2 告警规则

| 告警 | 条件 | 级别 |
|---|---|---|
| 服务不可用 | 健康检查失败>3次 | P0 |
| 接口错误率 | >5%持续5分钟 | P1 |
| 接口延迟 | P99>5秒持续5分钟 | P1 |
| 消息积压 | Lag>10000 | P2 |
| 磁盘使用 | >85% | P2 |
| 内存使用 | >90% | P2 |

### 13.3 日志体系

```
应用日志 → Fluentd → Elasticsearch → Kibana
```

- 统一MDC：traceId/tenantId/userId/module
- 日志级别：ERROR/WARN/INFO/DEBUG
- 日志保留：热数据7天，温数据30天，冷数据90天

---

## 14. 技术债务管理

### 14.1 已知技术债务

| 债务 | 影响 | 清理计划 |
|---|---|---|
| 一期模块化单体 | 域间耦合 | 二期关键域服务化 |
| Seata预留未启用 | 跨域事务靠@Transactional | 二期按需启用 |
| 读写分离未启用 | 数据库单点 | 二期启用 |
| 多租户仅字段隔离 | 数据隔离级别低 | 三期Schema隔离 |
| 缓存一致性 | 延迟双删 | 二期Canal CDC同步 |

### 14.2 技术债务预防

- ADR决策记录：所有技术决策必须记录ADR
- 代码审查：架构变更需技术负责人审查
- 定期重构：每个Phase预留10%时间处理技术债务
- 依赖升级：每季度评估依赖版本升级

---

## 15. 结论

本方案V4.0在V3.0基础上全面对齐V11详细设计，核心变化：

- 架构从简单微服务升级为DDD四层+双层网关+事件驱动+Outbox
- 技术栈从混合选型统一为Spring Cloud Java 2023.x全家桶
- 新增14业务中台+11技术中台设计
- 新增PMS集成架构（建议池→审批→执行→反馈）
- 新增10维数据权限和对象级权限
- 新增库存事务账五类状态和成本事件化8类
- 新增Canal+Flink+ClickHouse数据分析管道
- 新增GitOps多环境部署策略

---

*本文档由跨境电商ERP全栈虚拟专家团队联合编制，V4.0对齐V4需求规格和V11详细设计。*
