# 跨境电商ERP

**Cloud-Native · DDD · 14-Domain · Multi-Tenant SaaS**

[English](readme-en.md) | 中文

[![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Next.js](https://img.shields.io/badge/Next.js-14-000000?logo=nextdotjs&logoColor=white)](https://nextjs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.4-3178C6?logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

---

## 📖 个人能力展示

> 这是一个**个人架构能力展示项目**，不是商业产品。
>
> 项目完整覆盖**跨境电商全链路 14 个业务域**，从选品开发到财务核算，体现对跨境电商业务的深入理解；
> 采用**DDD 模块化单体架构**，18 个 Maven 模块、严格四层分层、事件驱动跨域协作，展示工程架构能力。
>
> - **架构设计文档**：[docs/architecture-overview.md](docs/architecture-overview.md)
> - **需求规格说明书**：[docs/requirements-specification.md](docs/requirements-specification.md)
> - **架构决策记录 ADR**：[docs/adr/](docs/adr/)

---

## 项目定位

面向跨境电商卖家的**全链路 SaaS ERP 平台**，覆盖从选品开发、店铺运营、广告投放、订单履约、供应链采购、仓储物流、财务核算到商业智能的**完整业务闭环**。

本项目旨在展示：

- **业务架构能力**：14个业务域的领域建模、跨域流程编排、业务中台抽象
- **技术架构能力**：模块化单体→微服务演进路径、事件驱动架构、双层网关、多租户隔离
- **数据架构能力**：Schema隔离多租户、Outbox事件溯源、Canal+Flink实时数据管道
- **安全架构能力**：JWT+RBAC+10维数据权限、API网关安全层、数据脱敏
- **AI架构能力**：AI选品/定价/补货/风控增强标签、PMS集成、智能推荐工作流
- **工程架构能力**：DDD分层规范、CI/CD流水线、Docker/K8s云原生部署

---

## 系统架构全景

```
                              ┌─────────────────────────────────────────────────┐
                              │                   客户端层                       │
                              │   Next.js 14 (SSR/SSG) + React 18 + Ant Design │
                              └──────────────────────┬──────────────────────────┘
                                                     │
                              ┌──────────────────────▼──────────────────────────┐
                              │              双层 API 网关                       │
                              │  Kong 3.6 (SSL/域名/全局限流)                    │
                              │     → Spring Cloud Gateway (认证/租户/路由)      │
                              └──────────────────────┬──────────────────────────┘
                                                     │
          ┌────────────┬────────────┬────────────────┼────────────────┬────────────┬────────────┐
          │            │            │                │                │            │            │
    ┌─────▼─────┐┌─────▼─────┐┌────▼────┐  ┌───────▼───────┐┌──────▼──────┐┌─────▼─────┐┌─────▼─────┐
    │   IAM     ││   PDM     ││  SOM    │  │     OMS       ││    SCM      ││   WMS     ││   FBA     │
    │ 身份权限  ││ 产品开发  ││ 销售运营│  │   订单管理    ││   供应链    ││  仓储管理 ││ 亚马逊物流│
    └─────┬─────┘└─────┬─────┘└────┬────┘  └───────┬───────┘└──────┬──────┘└─────┬─────┘└─────┬─────┘
          │            │            │                │                │            │            │
    ┌─────▼─────┐┌─────▼─────┐┌────▼────┐  ┌───────▼───────┐┌──────▼──────┐┌─────▼─────┐┌─────▼─────┐
    │   ADS     ││   TMS     ││  CRM    │  │     FMS       ││     BI      ││    SYS    ││ Dashboard │
    │ 广告投放  ││ 物流追踪  ││客服售后 │  │   财务管理    ││  商业智能   ││  系统设置 ││   工作台  │
    └───────────┘└───────────┘└─────────┘  └───────────────┘└─────────────┘└───────────┘└───────────┘
          │            │            │                │                │            │            │
          └────────────┴────────────┴────────────────┼────────────────┴────────────┴────────────┘
                                                     │
                              ┌──────────────────────▼──────────────────────────┐
                              │              技术中台 & 基础设施                  │
                              │  事件驱动(Kafka/RocketMQ) · 缓存(Redis)          │
                              │  搜索(ES) · 存储(MinIO) · 通知(多渠道)           │
                              │  审计 · 工作流 · 调度 · 数据脱敏                  │
                              └──────────────────────┬──────────────────────────┘
                                                     │
                              ┌──────────────────────▼──────────────────────────┐
                              │              数据层                              │
                              │  PostgreSQL 16 (Schema多租户) · ClickHouse(OLAP)│
                              │  Canal(binlog) → Kafka → Flink(流计算)          │
                              └─────────────────────────────────────────────────┘
```

---

## DDD领域驱动设计

### 14个限界上下文

| # | 领域 | 限界上下文 | 核心聚合 | AI增强 |
|---|------|----------|---------|--------|
| 1 | Dashboard | 工作台域 | 经营指标、待办事项 | ★AI看板 |
| 2 | IAM | 组织权限域 | 用户、角色、权限、租户 | — |
| 3 | PDM | 产品开发域 | SPU、SKU、分类、品牌 | ★AI选品 |
| 4 | SOM | 销售运营域 | 店铺、Listing、价格规则 | ★AI定价 |
| 5 | ADS | 广告管理域 | 广告活动、广告组、关键词 | ★AI优化 |
| 6 | OMS | 订单域 | 销售订单、订单行、退款 | ★AI风控 |
| 7 | SCM | 供应链域 | 供应商、采购单、采购计划 | ★AI补货 |
| 8 | WMS | 仓储域 | 仓库、库存、出入库单 | ★AI预测 |
| 9 | FBA | 亚马逊物流域 | 入库计划、FBA货件、补货建议 | ★AI FBA |
| 10 | TMS | 物流域 | 物流商、物流单、轨迹 | ★AI物流 |
| 11 | CRM | 客服售后域 | 客户、工单、邮件规则 | ★AI情感 |
| 12 | FMS | 财务域 | 成本事件、应收款、对账 | ★AI归集 |
| 13 | BI | 商业智能域 | 指标、KPI、报表、驾驶舱 | ★KPI |
| 14 | SYS | 系统设置域 | 配置、Webhook、业务规则 | — |

### DDD分层架构（每个领域模块内部）

```
erp-domain-xxx/
└── src/main/java/com/aidotnet/erp/xxx/
    ├── interfaces/          ← 接口层 (REST Controller, DTO, OpenAPI)
    ├── application/         ← 应用层 (用例协调、事务编排)
    ├── domain/              ← 领域核心 (纯Java, 零框架依赖)
    │   ├── model/           ← 聚合根、实体、值对象
    │   ├── service/         ← 领域服务
    │   ├── event/           ← 领域事件
    │   └── repository/      ← 仓储接口
    └── infrastructure/      ← 基础设施 (MyBatis Mapper、消息、外部客户端)
```

**核心约束**：领域层（domain）不导入任何Spring/MyBatis类，仅依赖 `erp-common` 中的领域抽象。

---

## 跨域业务闭环

本系统最核心的架构能力体现在**跨域流程编排**：

```
┌──────────────────────────────────────────────────────────────────────────┐
│                        销售履约闭环                                      │
│  OMS(订单导入→付款) → WMS(库存预留→拣货→发货) → TMS(物流追踪) → FMS(核算) │
└──────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────┐
│                        采购入库闭环                                      │
│  SCM(供应商→采购单→审批→收货) → WMS(入库→质检) → FMS(成本事件自动生成)    │
└──────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────┐
│                        采购计划闭环                                      │
│  OMS(订单需求) → SCM(补货建议→采购计划→审批→转采购单) → WMS(入库)        │
└──────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────┐
│                        入库质检闭环                                      │
│  WMS(收货→冻结→质检→良品放行/次品冻结) → 库存可用                        │
└──────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────┐
│                        出库履约闭环                                      │
│  WMS(预留→拣货→打包→称重→发货) → 库存扣减 → TMS(物流单)                  │
└──────────────────────────────────────────────────────────────────────────┘
```

---

## 工程结构

```
erp/
├── pom.xml                        # 父POM (依赖管理)
├── erp-common/                    # 公共模块 (安全/事件/缓存/搜索/存储/通知/审计/工作流)
├── erp-gateway/                   # Spring Cloud Gateway (内部网关)
├── erp-domain-dashboard/          # 工作台域
├── erp-domain-iam/                # 组织权限域 (认证/RBAC/10维数据权限)
├── erp-domain-pdm/                # 产品开发域
├── erp-domain-som/                # 销售运营域
├── erp-domain-ads/                # 广告管理域
├── erp-domain-oms/                # 订单域
├── erp-domain-scm/                # 供应链域
├── erp-domain-wms/                # 仓储域
├── erp-domain-fba/                # FBA/海外仓域
├── erp-domain-tms/                # 物流域
├── erp-domain-crm/                # 客服售后域
├── erp-domain-fms/                # 财务域
├── erp-domain-bi/                 # 商业智能域
├── erp-domain-sys/                # 系统设置域
├── erp-app/                       # 启动模块 (单进程聚合)
├── erp-web/                       # 前端 (Next.js 14 + React 18 + Ant Design 5)
├── docker-compose.yml             # 本地开发环境
├── Dockerfile                     # 容器镜像构建
├── docs/adr/                      # 架构决策记录 (ADR)
└── .github/workflows/ci.yml      # CI/CD 流水线
```

---

## 技术栈

### 后端 — Spring Cloud Java 云原生

| 分类 | 技术选型 | 版本 |
|------|---------|------|
| 基础框架 | Spring Boot + Spring Cloud + Spring Cloud Alibaba | 3.2.x / 2023.x |
| JDK | Eclipse Temurin | 17 |
| 服务治理 | Nacos (注册/配置中心) | 2.3.x |
| 熔断限流 | Sentinel | 1.8.x |
| 远程调用 | OpenFeign + LoadBalancer | — |
| 双层网关 | Kong (外部) → Spring Cloud Gateway (内部) | 3.6 |
| 异步消息 | Spring Cloud Stream + Kafka + RocketMQ | 3.6 / 5.x |
| 分布式事务 | Seata (AT模式，预留) | 1.8.x |
| 安全 | Spring Security 6 + JWT + BCrypt | — |
| ORM | MyBatis-Plus (多租户插件/逻辑删除/分页) | 3.5.x |
| 数据库 | PostgreSQL | 16 |
| 缓存 | Redis | 7.x |
| 搜索 | Elasticsearch | 8.x |
| 对象存储 | MinIO (S3兼容) | — |
| 数据库迁移 | Flyway | — |
| API文档 | SpringDoc OpenAPI (14域分组) | — |
| 代码规范 | EditorConfig + Spotless | — |

### 数据架构

| 组件 | 职责 |
|------|------|
| Canal | binlog实时采集 → Kafka |
| Flink | 流计算、实时聚合 → ES/Redis/PG |
| ClickHouse | OLAP分析引擎 |
| EFK | Elasticsearch + Fluentd + Kibana 日志体系 |

### 前端 — SSR/SSG

| 分类 | 技术选型 | 版本 |
|------|---------|------|
| 框架 | Next.js (App Router) | 14.2 |
| UI库 | React + Ant Design | 18 / 5.x |
| 语言 | TypeScript | 5.4 |
| 状态管理 | Zustand | 4.5 |
| 数据获取 | SWR | 2.2 |
| 图表 | ECharts | 5.5 |
| HTTP | Axios | 1.7 |

---

## 安全架构

### 多租户隔离

- **Schema级隔离**：每个租户独立PostgreSQL Schema，数据物理隔离
- **请求级隔离**：`X-Tenant-Id` Header贯穿全链路，MyBatis-Plus自动注入租户条件
- **索引隔离**：Elasticsearch索引按租户前缀隔离

### 10维数据权限模型

| 维度 | 说明 |
|------|------|
| tenant | 租户隔离 |
| org | 公司/组织范围 |
| department | 部门范围 |
| store | 店铺范围 |
| marketplace | 市场范围 (Amazon US/JP等) |
| channel | 渠道范围 (Amazon/TikTok/Walmart等) |
| warehouse | 仓库范围 (本地仓/FBA仓/海外仓) |
| supplier | 供应商范围 |
| category | 类目范围 |
| data_level | 明细/汇总/脱敏级别 |

### 认证授权

```
客户端 → Kong(SSL终结) → SCG(JWT验证/租户Header注入) → 业务服务(@RequirePermission)
                                                              ↓
                                                    PermissionAspect(AOP)
                                                    → 10维DataScope过滤
```

---

## 事件驱动架构

```
领域事件 → Outbox表 → 定时轮询 → Kafka/RocketMQ → 消费者 → 跨域同步
                                    ↓
                              Topic: erp.{domain}.{aggregate}.{event}.v1
```

**Outbox模式**确保事件不丢失，实现最终一致性。强一致场景使用RocketMQ事务消息（半消息+回查）。

---

## 中台架构

### 14个业务中台

| # | 中台 | 职责 |
|---|------|------|
| 1 | 内容审核中心 | 商品/Listing内容合规审核 |
| 2 | 货币汇率中心 | 实时汇率获取与缓存 |
| 3 | 支付聚合中心 | Stripe/支付宝/Ping++ 多渠道支付 |
| 4 | 订单策略中心 | 拆单/合单/有货先发策略引擎 |
| 5 | 物流策略中心 | 物流优选规则、运费规则引擎 |
| 6 | 计费策略中心 | 平台费用计算、佣金规则引擎 |
| 7 | 客户数据平台(CDP) | 客户画像、标签体系、行为分析 |
| 8 | 发票税务中台 | 发票模板、VAT计算 |
| 9 | 合规风控中台 | 平台合规检测、贸易合规规则 |
| 10 | 选品分析中台 | 市场趋势分析、竞品数据聚合 |
| 11 | 广告优化中台 | 广告策略模板、出价算法接口 |
| 12 | 成本归集引擎 | 多维度成本自动归集规则 |
| 13 | 利润核算引擎 | 利润计算、汇率换算、分摊规则 |
| 14 | 进销存凭证引擎 | 凭证自动生成、会计科目映射 |

### 11个技术中台

| # | 中台 | 实现 |
|---|------|------|
| 1 | 消息通知中心 | 短信/飞书/钉钉/企微/邮件统一接口 |
| 2 | 文件处理中心 | 图片压缩/格式转换/文档生成 → MinIO |
| 3 | 工作流引擎 | `ApprovalStateMachine` 多级审批/会签/加签 |
| 4 | 任务调度中心 | `@ScheduledTask` + Quartz/XXL-JOB |
| 5 | 权限管理中心 | OAuth2 + RBAC + 10维数据权限 |
| 6 | 日志审计中心 | `@Audited` AOP + Fluentd采集 |
| 7 | API网关 | Kong + Spring Cloud Gateway 双层 |
| 8 | 多语言翻译中心 | 云翻译API + i18n资源管理 |
| 9 | 数据脱敏中心 | Jackson序列化拦截 + `data_level` 维度 |
| 10 | API管理平台 | SpringDoc OpenAPI 14域分组 |
| 11 | 连接器管理平台 | 插件化架构，统一对接外部API |

---

## 快速开始

### 环境要求

- JDK 17+ (Eclipse Temurin)
- Maven 3.9+
- Node.js 18+ / npm
- Docker & Docker Compose

### 1. 启动基础设施

```bash
docker compose up -d postgres redis kafka kafka-init minio elasticsearch
```

### 2. 启动后端

```bash
cd erp-app
mvn spring-boot:run -Dspring-boot.run.profiles=local
# 后端启动于 http://localhost:8080
```

### 3. 启动前端

```bash
cd erp-web
npm install
npm run dev
# 前端启动于 http://localhost:3000
```

### 4. 访问系统

- 前端地址：http://localhost:3000
- 默认租户：`tenant-demo`
- 管理员账号：`admin` / `admin123`
- API文档：http://localhost:8080/swagger-ui.html

---

## 前端业务能力展示

> 以下页面严格对齐《跨境电商ERP需求规格说明书V4》14域业务能力，覆盖从选品开发→刊登运营→订单处理→多仓备货→仓储执行→物流发货→客服售后→财务核算→经营分析全链路。

### Dashboard — 工作台域 ★AI看板

| 页面 | 路由 | 业务能力 |
|------|------|---------|
| 经营驾驶舱 | `/dashboard` | 核心指标看板(今日订单/销售额/库存预警/待处理工单)、销售趋势图、平台分布饼图、最近订单列表、工作流通知聚合 |

### IAM — 组织权限域

| 页面 | 路由 | 业务能力 |
|------|------|---------|
| 用户管理 | `/iam/users` | 用户CRUD、角色分配、租户隔离、10维数据权限 |
| 角色管理 | `/iam/roles` | 角色定义、菜单权限+数据权限配置、对象级权限 |
| 岗位管理 | `/iam/positions` | 岗位定义、岗位层级、岗位与部门关联 |
| 对象权限 | `/iam/permissions` | 产品/Listing/广告等业务对象精细权限控制 |

### PDM — 产品开发域 ★AI选品

| 页面 | 路由 | 业务能力 |
|------|------|---------|
| 产品管理 | `/pdm/products` | SPU/SKU管理、分类/品牌、变体(母体/子体)、产品生命周期(DRAFT→ACTIVE)、开发审核、渠道SKU映射、组合产品、包材管理、产品权限控制、产品问题记录 |
| Listing管理 | `/pdm/listings` | 渠道SKU映射、上架前配置(限价/标题库/图片库)、UPC管理、敏感词库、知识产权管理 |

### SOM — 销售运营域 ★AI Listing优化

| 页面 | 路由 | 业务能力 |
|------|------|---------|
| 店铺管理 | `/som/stores` | 多平台店铺授权(Amazon/Shopify/TikTok等)、连接状态监控、店铺代码唯一性校验 |
| Listing管理 | `/som/listings` | 多平台刊登、批量调价/促销/修改可售数、一键翻译多语种、平衡库存策略、滞销品清库、Reviews同步 |
| 价格规则 | `/som/price-rules` | Listing价格计算器、Buybox采集调价、平台限价防内部价格战、自动调价规则 |
| Listing监控 | `/som/monitors` | 销量/价格/Buybox/在售状态/FBA库龄/星级评论异常监控、短信/邮件/钉钉告警、运营日历 |

### ADS — 广告管理域 ★AI优化

| 页面 | 路由 | 业务能力 |
|------|------|---------|
| 广告活动 | `/ads/campaigns` | SP/SB/SD/SBV四种广告类型、批量管理多店铺广告、ACoS/曝光/点击/转化/花费、批量更改状态/预算/竞价、PMS广告优化集成 |
| 广告策略 | `/ads/strategies` | 搜索词提炼、否定关键词管理、自动调价(基于曝光/点击条件)、广告仪表盘、分组分析视图 |

### OMS — 订单域 ★AI风控

| 页面 | 路由 | 业务能力 |
|------|------|---------|
| 订单列表 | `/oms/orders` | 订单三层分离(渠道订单/ERP销售单/履约单/包裹单)、平台订单同步、批量导入、拆单/合单/有货先发、物流申报规则、平台标记发货、风险订单检测、插头国标规则、退款/退换货 |
| 订单审核 | `/oms/audit` | 订单审核策略、风控校验、利润率检测、异常订单拦截、黑名单管理 |

### SCM — 供应链域 ★AI补货

| 页面 | 路由 | 业务能力 |
|------|------|---------|
| 供应商 | `/scm/suppliers` | 供应商管理(联系人/资质/评分)、供应商平台(线上接单)、禁用供应商校验、1688采购对接 |
| 采购单 | `/scm/purchase-orders` | 5种采购模式(市场/工厂/天猫淘宝/1688/加工)、采购审核流程(多级/自动)、采购合同、跟单管理、销单入库核对、三仓备货分析、PMS AI补货建议 |

### WMS — 仓储域 ★AI预测

| 页面 | 路由 | 业务能力 |
|------|------|---------|
| 仓库管理 | `/wms/warehouses` | 仓库基础信息、区域/库位管理、库位分组统计、自定义标签模板 |
| 库存台账 | `/wms/inventory` | 库存事务账五类状态(在手/预占/可售/在途/不良)、收货→质检→入库全流程、不良品退货/返修/回购换货、出货质检、调拨/盘点、Removal订单、手工出入库、跨域导航(→FBA库存/采购单) |

### FBA — 亚马逊物流域

| 页面 | 路由 | 业务能力 |
|------|------|---------|
| FBA库存 | `/fba/inventory` | EFN/NARF库存查看、库龄分析、库存预警(可售天数<14红色标记) |
| 补货建议 | `/fba/restock` | FBA补货建议(自定义日均/备货时效/去噪规则)、备货购物车、一键生成货件计划 |
| 备货计划 | `/fba/shipments` | 三种创建模式(同步Amazon/加载ShipmentID/先创建无ID)、发货打包SOP、称重发货、FBA头程五大异常处理 |

### TMS — 物流域

| 页面 | 路由 | 业务能力 |
|------|------|---------|
| 物流商 | `/tms/carriers` | 物流商API授权、运输方式管理、运费算法维护、运费试算 |
| 轨迹查询 | `/tms/tracking` | 2000+物流渠道轨迹追踪、上网时效分析、妥投统计、物流严选、绩效对比分析、发货后修改运费 |

### CRM — 客服售后域 ★AI情感

| 页面 | 路由 | 业务能力 |
|------|------|---------|
| 客户管理 | `/crm/customers` | 客户画像、标签体系、行为分析(CDP) |
| 工单管理 | `/crm/tickets` | 多平台邮件/消息管理、智能分配客服、假期自动回复、新人回复审核、工单状态流转(OPEN→ASSIGNED→RESOLVED→CLOSED) |
| 邮件规则 | `/crm/email-rules` | 邮件模板、快速回复、中英双语、中差评与纠纷处理 |
| 邮件营销 | `/crm/campaigns` | 自动推广、请求评论(排除退款/差评订单) |
| 质量问题 | `/crm/quality` | 质量问题记录与PDM/WMS联动、产品问题归类分析 |

### FMS — 财务域 ★AI成本归集

| 页面 | 路由 | 业务能力 |
|------|------|---------|
| 成本事件 | `/fms/cost-events` | 成本归集8类事件(采购/头程/仓储/佣金/广告/支付/尾程/其他)、成本事件自动生成(SCM收货→FMS联动)、异常成本检测 |
| 利润报表 | `/fms/profit` | 利润核算(FIFO)、多维度利润报表(平台/店铺/人员/产品/币种)、利润趋势图、对账管理(供应商/物流商)、应收款/付款确认、汇率快照化 |

### BI — 商业智能域 ★KPI

| 页面 | 路由 | 业务能力 |
|------|------|---------|
| 经营驾驶舱 | `/bi/cockpit` | 综合看板(各店铺销售/利润/广告/库存/账户状况)、运营监控(异常指标自动告警) |
| KPI指标 | `/bi/kpis` | KPI设定/跟踪/预警、开发人员提成报表、仓库人员KPI |
| 报表中心 | `/bi/reports` | 500+业务报表、订单利润报表、产品销量报告、断货分析、FBA货件分析 |
| 趋势分析 | `/bi/trends` | 即时销量多维分析、产品表现11项深度指标、自定义报告 |

### SYS — 系统设置域

| 页面 | 路由 | 业务能力 |
|------|------|---------|
| 系统参数 | `/sys/configs` | 系统参数配置(采购审核/利润检测等业务开关)、AI功能开关、启用/禁用控制 |
| Webhook | `/sys/webhooks` | 平台连接器管理、Webhook事件订阅、测试推送 |
| 业务规则 | `/sys/rules` | 物流申报规则、插头国标规则、审核流程配置、发票/合同模板、数据导入导出 |

### 页面统计

| 维度 | 数量 |
|------|------|
| 业务域 | 14 |
| 路由页面 | 45 |
| 业务能力点 | 200+ |
| AI增强功能 | 9域(选品/定价/优化/风控/补货/预测/情感/归集/KPI) |

**TypeScript类型检查全部通过**，0错误构建。

---

## 测试

### 后端集成测试

```bash
cd erp-app
mvn test -Dspring.profiles.active=test
```

覆盖14个业务域，关键测试场景：

| 测试类 | 验证内容 |
|--------|---------|
| `IamApiTests` | 登录/RBAC/租户禁用/审计日志 |
| `OmsApiTests` | 订单导入幂等/状态流转/履约闭环/库存联动 |
| `ScmApiTests` | 供应商/采购单审批/收货→WMS/FMS联动 |
| `WmsApiTests` | 库存CRUD/入库质检/出库履约全流程 |
| `FbaApiTests` | 入库计划/货件/库存流转 |
| `TmsApiTests` | 物流商/追踪/交付/租户隔离 |
| `FmsApiTests` | 应收款/付款确认/重复校验 |
| `CrmApiTests` | 客户/工单分配→解决→关闭流程 |
| `BiApiTests` | 指标/KPI/驾驶舱/租户隔离 |
| `DashboardApiTests` | 指标CRUD/租户隔离 |

### 前端构建验证

```bash
cd erp-web
npm run build
# ✓ 45个页面构建通过，0错误
```

---

## 架构决策记录 (ADR)

| 编号 | 标题 | 决策 |
|------|------|------|
| ADR-0001 | 模块化单体架构基线 | 首期模块化单体，按14领域模块组织，后续可拆分微服务 |
| ADR-001~003 | 技术选型 | Spring Boot 3 + MyBatis-Plus + PostgreSQL |
| ADR-004 | 消息中间件选型 | Kafka(高吞吐) + RocketMQ(事务消息) 双栈 |
| ADR-005 | 分布式事务策略 | 强一致→Seata/事务消息，最终一致→Saga+Outbox |
| ADR-006 | 数据库隔离策略 | Schema级多租户隔离 |
| ADR-007 | CDC数据同步架构 | Canal → Kafka → Flink 实时数据管道 |
| ADR-008 | 前端技术选型 | Next.js 14 (App Router) + Ant Design 5 |

---

## 部署架构

```
┌─────────────────────────────────────────────────────────────┐
│                     Kubernetes Cluster                       │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │  Ingress  │  │  Kong    │  │  SCG     │  │  ERP App │   │
│  │  (Nginx)  │→│  Gateway │→│  Gateway │→│  Pods     │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
│                                                              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │ PG RDS   │  │  Redis   │  │  Kafka   │  │  MinIO   │   │
│  │ Cluster  │  │ Cluster  │  │ Cluster  │  │ Cluster  │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
│                                                              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐                  │
│  │   ES     │  │ ClickHouse│  │  Nacos   │                  │
│  │ Cluster  │  │ Cluster  │  │ Cluster  │                  │
│  └──────────┘  └──────────┘  └──────────┘                  │
└─────────────────────────────────────────────────────────────┘
```

多环境支持：
- **本地开发**：H2 + Embedded Redis/Kafka，零容器启动
- **测试环境**：Docker Compose 一键编排
- **生产环境**：K8s + Helm，多云部署 (华为云/AWS/阿里云)

---

## 项目进展

| 阶段 | 状态 | 完成项 |
|------|------|--------|
| P0 工程基线 | ✅ | 18模块骨架、DDD分层、Docker Compose、Flyway、CI/CD |
| P1 技术中台 | ✅ | JWT认证、RBAC、10维权限、事件驱动、Outbox、缓存、搜索、审批 |
| P2 核心域业务 | ✅ | 14域业务逻辑 + 前端45页面 + 前后端联调 |
| P3 集成测试 | ✅ | 14域后端集成测试 + 跨域闭环验证 |
| P4 部署上线 | 🔄 | K8s Helm Charts、CI/CD完善、监控告警 |

---

## 贡献

欢迎提交 Issue 和 Pull Request。请确保：

1. 代码通过 `mvn spotless:check` 格式检查
2. 新增API遵循 `/{service-name}/api/{direction}/v1/{resource}` 路径规范
3. 领域层代码不依赖Spring框架
4. 所有业务数据保留 `tenantId` 和审计字段

---

## License

MIT License - 详见 [LICENSE](LICENSE) 文件
