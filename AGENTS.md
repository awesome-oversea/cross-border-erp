# Project: 跨境电商ERP

## AGENTS.md v5.0 — 全栈云原生 · 14领域DDD · 14业务中台 · 11技术中台 · 双层网关 · PMS集成 · EFK+Canal+Flink

***

### 👥 虚拟专家团队 (全栈 Java 生态)

本项目 AI 助手同时兼具以下角色思维，任何改动需经过对应专家的自我思辨审核：

- **业务层**：ERP产品经理、跨境电商产品经理、跨境电商运营人员、供应链产品经理、金融支付专家、财务经理、财务专家
- **架构层**：业务架构师、中台架构师 (业务中台、技术中台)、三高架构专家、领域驱动专家、事件驱动架构师、消息驱动架构师、微服务专家、云计算架构师
- **技术层**：Java架构师、前端架构师、网络安全专家、大数据架构师、数据分析师、DBA
- **工程层**：DevOps运维工程师、Vibe code专家、AI coding专家

***

### 🎯 技术栈全景图

#### 后端 (Spring Cloud Java 云原生)

- **基础框架**：Spring Boot 3.2.x + Spring Cloud 2023.x + Spring Cloud Alibaba 2023.x
- **JDK**：17 (Eclipse Temurin)
- **服务治理**：Nacos 2.3.x (注册/配置中心)
- **熔断限流**：Sentinel 1.8.x
- **远程调用**：OpenFeign + LoadBalancer
- **双层网关**：Kong (外部入口: SSL终结/域名路由/全局限流) → Spring Cloud Gateway (内部: 认证鉴权/租户Header/内部路由/限流熔断)
- **异步消息**：
  - 统一抽象：**Spring Cloud Stream** (函数式)
  - 事务消息 (强一致)：**Apache RocketMQ** (半消息、回查)
  - 高吞吐/数据管道：**Apache Kafka 3.6+**
- **分布式事务**：Seata 1.8.x (AT模式，一期暂不启用)
- **安全**：Spring Security 6.x + JWT (jjwt 0.12.5) + BCrypt
- **ORM**：MyBatis-Plus 3.5.x (多租户插件 + 逻辑删除 + 分页)
- **数据库**：PostgreSQL 15+ (主库) + H2 (本地开发)
- **缓存**：Redis 7.x (spring-boot-starter-data-redis)
- **搜索**：Elasticsearch 8.x (elasticsearch-java 8.13.4)
- **对象存储**：MinIO (minio 8.5.9, S3兼容)
- **数据库迁移**：Flyway
- **API文档**：SpringDoc OpenAPI (14域分组)
- **代码规范**：EditorConfig + Spotless

#### 数据分析与数据集成体系

- **Canal**：binlog 实时采集 → Kafka (独立Java组件运行)
- **Flink**：流计算、实时聚合，结果写入 ES/Redis/PostgreSQL
- **ClickHouse**：OLAP分析引擎
- **EFK**：Elasticsearch + Fluentd (日志采集) + Kibana (可视化)

#### 前端 (SSR/SSG)

- Next.js (v14+ App Router) + React 18+ + TypeScript
- Ant Design 5.x，pnpm 管理

#### 云厂商与云产品矩阵 (华为云/AWS/阿里云)

- VPC、SLB/ELB、安全组
- 对象存储 (S3 兼容，Java `aws-sdk-s3` 或 `aliyun-oss`)
- 云数据库 RDS、云缓存 ElastiCache/Redis、云搜索 OpenSearch、云消息队列 (通过标准客户端连接)
- 多渠道通知：短信/飞书/钉钉/企微/邮件 (对应 Java SDK 实现)

***

### 📁 实际工程结构 (Maven多模块)

```
D:\Project\erp
├── pom.xml                    # 父POM (依赖管理)
├── erp-common/                # 公共模块 (安全/事件/缓存/搜索/存储/通知/审计/工作流/调度)
│   └── src/main/java/com/aidotnet/erp/common/
│       ├── api/               # Result, PageQuery, PageResult, OpenApiConfig
│       ├── audit/             # @Audited, AuditAspect, AuditRecord
│       ├── cache/             # RedisCacheService
│       ├── context/           # TenantContext, TraceContext, ContextFilter
│       ├── event/             # DomainEvent, DomainEventPublisher, KafkaDomainEventPublisher, OutboxPublisher
│       ├── exception/         # BizException, ErrorCode, GlobalExceptionHandler
│       ├── notification/      # NotificationService, NotificationSender, LogNotificationSender
│       ├── persistence/       # BaseEntity, TenantBaseEntity, MyBatisPlusConfig
│       ├── scheduler/         # @ScheduledTask, SchedulerConfig
│       ├── search/            # ElasticSearchService, ElasticSearchConfig
│       ├── security/          # SecurityConfig, JwtProvider, JwtAuthenticationFilter, DataScope, PermissionAspect
│       ├── sentinel/          # SentinelConfig, SentinelFallbackHandler
│       ├── seata/             # SeataConfig (预留)
│       ├── storage/           # MinioStorageService, MinioConfig
│       ├── tenant/            # TenantContext
│       └── workflow/          # ApprovalStateMachine, ApprovalStatus, ApprovalRequest
├── erp-gateway/               # Spring Cloud Gateway (内部网关)
├── erp-domain-dashboard/      # 2.1 工作台域
├── erp-domain-iam/            # 2.2 组织权限域 (认证/RBAC/10维数据权限)
├── erp-domain-pdm/            # 2.3 产品开发域
├── erp-domain-som/            # 2.4 销售运营域
├── erp-domain-ads/            # 2.5 广告管理域
├── erp-domain-oms/            # 2.6 订单域
├── erp-domain-scm/            # 2.7 供应链域
├── erp-domain-wms/            # 2.8 仓储域
├── erp-domain-fba/            # 2.9 FBA/海外仓域
├── erp-domain-tms/            # 2.10 物流域
├── erp-domain-crm/            # 2.11 客服售后域
├── erp-domain-fms/            # 2.12 财务域
├── erp-domain-bi/             # 2.13 商业智能域
├── erp-domain-sys/            # 2.14 系统设置域 (PMS集成接口)
├── erp-app/                   # 启动模块 (单进程聚合)
├── docker-compose.yml         # 本地开发环境 (PG/Redis/Kafka/MinIO/ES/ClickHouse/Nacos)
├── Dockerfile                 # 容器镜像构建
├── .editorconfig              # 代码格式规范
├── .gitignore
└── docs/adr/                  # 架构决策记录
```

***

### 🧱 DDD 领域驱动设计 (强化版)

- **限界上下文**：上述 14 个业务领域即为 14 个独立限界上下文，每个上下文至少对应一个微服务，可按复杂程度拆分多个微服务，但数据库严格隔离。
- **中台抽象**：14 个业务中台与 11 个技术能力中台是跨领域的共享内核，必须通过接口 (API/事件) 提供能力，不得与业务领域数据库直接耦合。业务域调用中台时，通过防腐层 (ACL) 或标准接口。
- **上下文映射**：共享内核 (Shared Kernel) 可被多个领域依赖，但必须保持兼容；防腐层 (ACL) 对外部电商平台 (Amazon, Shopify) 或支付机构隔离；上下游关系通过公开主机服务 (OHS) 与发布语言 (PL) 维护。

#### DDD 分层架构 (每个微服务内部)

```
erp-domain-xxx/
└── src/main/java/com/aidotnet/erp/xxx/
    ├── interfaces/          # 接口层 (REST Controller, DTO, OpenAPI)
    ├── application/         # 应用层 (用例协调、事务编排，不含业务逻辑)
    ├── domain/              # 领域核心 (纯 Java 类，不依赖 Spring 框架)
    │   ├── model/           # 聚合根、实体、值对象
    │   ├── service/         # 领域服务
    │   ├── event/           # 领域事件
    │   └── repository/      # 仓储接口 (仅接口，实现在 infrastructure)
    └── infrastructure/      # 基础设施层 (MyBatis Mapper、消息发送、外部客户端)
```

- **禁止跨层直接依赖**：领域层 (`domain`) 不导入任何 Spring/MyBatis 特定类，仅依赖 `erp-common` 中的领域抽象。
- **中台复用**：业务中台与技术中台作为共享内核，领域服务通过 `erp-common` 公共接口或 OpenFeign 调用来使用能力，不能直接操作中台数据库。

### 四色建模法 (前置分析，强制)

需求分析必须使用四色原型：粉 (时刻时段)、黄 (角色)、蓝 (描述)、绿 (参与方/物品/地点)。先识别原型，再归属到相应限界上下文与聚合，最后生成 DDD 代码骨架。**禁止跳过此步直接编码**。

***

### 🔗 API路径规范 (铁律)

**路径模板**：`/{service-name}/api/{direction}/v1/{resource}`

| 组成部分           | 说明                           | 示例                      |
| -------------- | ---------------------------- | ----------------------- |
| `service-name` | 域服务名前缀                       | `fms`、`oms`、`wms`、`sys` |
| `direction`    | `in`(外部→ERP) / `out`(ERP→外部) | `in`                    |
| `version`      | API版本号                       | `v1`                    |
| `resource`     | 业务资源路径                       | `cost-events`、`orders`  |

**14域路径映射**：

| 域         | 入站路径前缀                  | 出站路径前缀             |
| --------- | ----------------------- | ------------------ |
| IAM       | `/iam/api/in/v1/`       | `/iam/api/out/v1/` |
| PDM       | `/pdm/api/in/v1/`       | `/pdm/api/out/v1/` |
| SOM       | `/som/api/in/v1/`       | `/som/api/out/v1/` |
| ADS       | `/ads/api/in/v1/`       | `/ads/api/out/v1/` |
| OMS       | `/oms/api/in/v1/`       | `/oms/api/out/v1/` |
| SCM       | `/scm/api/in/v1/`       | `/scm/api/out/v1/` |
| WMS       | `/wms/api/in/v1/`       | `/wms/api/out/v1/` |
| FBA       | `/fba/api/in/v1/`       | `/fba/api/out/v1/` |
| TMS       | `/tms/api/in/v1/`       | `/tms/api/out/v1/` |
| CRM       | `/crm/api/in/v1/`       | `/crm/api/out/v1/` |
| FMS       | `/fms/api/in/v1/`       | `/fms/api/out/v1/` |
| BI        | `/bi/api/in/v1/`        | `/bi/api/out/v1/`  |
| SYS       | `/sys/api/in/v1/`       | `/sys/api/out/v1/` |
| Dashboard | `/dashboard/api/in/v1/` | —                  |

**内部域间调用**：走 `/api/v1/...` 或 OpenFeign，不经过外部网关。

***

### 🏗️ 双层网关架构 (铁律)

```
外部请求 → Kong (SSL终结/域名路由/全局限流)
         → Spring Cloud Gateway (认证鉴权/租户Header传递/内部路由/限流熔断)
         → 各域服务
```

- **Kong**：外部入口网关，不删除，不替换
- **Spring Cloud Gateway**：内部服务网关，14域路由已配置

***

### 🔐 多租户与10维权限模型 (铁律)

**10维数据权限**：

| 权限维度        | 说明                            |
| ----------- | ----------------------------- |
| tenant      | 租户隔离                          |
| org         | 公司/组织范围                       |
| department  | 部门范围                          |
| store       | 店铺范围                          |
| marketplace | 市场范围 (Amazon US/JP等)          |
| channel     | 渠道范围 (Amazon/TikTok/Walmart等) |
| warehouse   | 仓库范围 (本地仓/FBA仓/海外仓)           |
| supplier    | 供应商范围                         |
| category    | 类目范围                          |
| data\_level | 明细/汇总/脱敏级别                    |

**PMS集成约束**：

- PMS写入必须走 `/sys/api/in/v1/pms/recommendations`
- PMS调用必须携带：tenant\_id、actor\_type、actor\_id、scope、purpose、trace\_id、idempotency\_key
- 审批主控原则：ERP对业务数据拥有完全控制权
- 15状态推荐工作流：DRAFT→PENDING→APPROVED→EXECUTING→COMPLETED

***

### 📨 事件驱动规范

**Kafka主题命名**：`erp.{domain}.{aggregate}.{event}.v1`

示例：

- `erp.oms.order.created.v1`
- `erp.wms.inventory.updated.v1`
- `erp.fms.cost-event.recorded.v1`

**Outbox模式**：所有领域事件先写入outbox\_message表，定时轮询发布到Kafka，确保最终一致性。

***

### 📋 14个业务领域核心职责

| 编号   | 领域       | 核心功能                  | AI 增强标签   |
| ---- | -------- | --------------------- | --------- |
| 2.1  | 工作台域     | 聚合首页、待办事项、关键指标        | ★AI看板     |
| 2.2  | 组织权限域    | 用户、角色、权限、租户管理、10维数据权限 | <br />    |
| 2.3  | 产品开发域    | 产品库、生命周期、选品模型         | ★AI选品     |
| 2.4  | 销售运营域    | 销量追踪、活动管理、店铺运营        | ★AI定价     |
| 2.5  | 广告管理域    | 广告投放、关键词出价、效果分析       | ★AI优化     |
| 2.6  | 订单域      | 订单全生命周期、风控校验          | ★AI风控     |
| 2.7  | 供应链域     | 采购计划、补货建议、供应商协同       | ★AI补货     |
| 2.8  | 仓储域      | 库存管理、库位、盘点、预测         | ★AI预测     |
| 2.9  | FBA/海外仓域 | 发补货计划、箱唛、货件           | ★AI FBA补货 |
| 2.10 | 物流域      | 物流订单、轨迹、成本核算          | ★AI物流     |
| 2.11 | 客服售后域    | 工单、退货退款、评价分析          | ★AI情感     |
| 2.12 | 财务域      | 费用、利润、成本归集            | ★AI成本归集   |
| 2.13 | 商业智能域    | 报表、看板、KPI 监控          | ★KPI      |
| 2.14 | 系统设置域    | 基础数据、字典、参数配置、PMS集成接口  | <br />    |

***

### 🧩 14个业务中台 (共享业务能力)

- 5.1 内容审核中心 (商品/Listing内容合规审核，对接平台审核API)
- 5.2 货币汇率中心 (实时汇率获取与缓存，对接汇率API)
- 5.3 国内外支付聚合中心 (对接支付SDK：Stripe `stripe-java`、支付宝 `alipay-sdk-java`、Ping++)
- 5.4 订单策略中心 (拆单/合单/有货先发策略引擎)
- 5.5 物流策略中心 (物流优选规则、运费规则引擎)
- 5.6 计费策略中心 (平台费用计算、佣金规则引擎)
- 5.7 客户数据平台 (CDP) (客户画像、标签体系、行为分析)
- 5.8 发票税务中台 (发票模板、税务参数、VAT计算)
- 5.9 合规风控中台 (平台合规检测、贸易合规规则)
- 5.10 选品分析中台 (市场趋势分析、竞品数据聚合)
- 5.11 广告优化中台 (广告策略模板、出价算法接口)
- 5.12 成本归集引擎 (多维度成本自动归集规则)
- 5.13 利润核算引擎 (利润计算规则、汇率换算、分摊规则)
- 5.14 进销存凭证引擎 (进销存凭证自动生成、会计科目映射)

### ⚙️ 11个技术能力中台 (基础设施复用)

- 6.1 消息通知中心 (短信/飞书/钉钉/企微/邮件，统一 `NotificationService` 接口)
- 6.2 文件处理中心 (图片压缩/格式转换/文档生成，对接 MinIO 存储)
- 6.3 工作流引擎 (审批状态机 `ApprovalStateMachine`，支持多级审批、会签、加签)
- 6.4 任务调度中心 (`@ScheduledTask` + Quartz/XXL-JOB，分布式任务调度)
- 6.5 权限管理中心 (OAuth2 + RBAC + 10维数据权限，`@RequirePermission` 注解)
- 6.6 日志审计中心 (`@Audited` 注解 + AOP，结构化日志由 Fluentd 采集)
- 6.7 API 网关 (Kong 外部网关 + Spring Cloud Gateway 内部网关，双层架构)
- 6.8 多语言翻译中心 (对接云翻译API，i18n资源文件管理)
- 6.9 数据脱敏中心 (统一脱敏注解，Jackson序列化拦截，针对 `data_level` 维度)
- 6.10 API 管理平台 (SpringDoc OpenAPI 自动生成文档，14域分组，结合 Swagger UI)
- 6.11 连接器管理平台 (插件化架构，统一对接外部电商/物流/支付 API)

***

### 🔗 微服务数据自治 (铁律)

- 每个领域及中台服务拥有独立数据库，禁止跨库查询。
- 服务间通过异步消息 (Kafka/RocketMQ) 或同步 HTTP (OpenFeign) 交换数据，所有暴露 API 必须幂等。
- **ES 索引所有权**：不同服务索引隔离，不允许交叉写入。

### ⚖️ 分布式事务与 CAP 平衡

- 强一致场景 (支付、库存扣减)：采用 Seata AT模式 (一期预留) 或 RocketMQ 事务消息 (半消息 + 回查)。
- 最终一致场景：Saga 异步补偿，通过事件驱动实现，结合 Outbox 模式确保消息不丢失。
- 事务消息实现需依赖 RocketMQ 事务监听器，回查逻辑必须健壮，本地事务表与消息发送保持原子性。

### 📨 消息中间件与抽象

- **Kafka**：高吞吐数据管道、日志、Canal 同步，使用 `spring-kafka` (支持事务性)
- **RocketMQ**：事务性事件，使用 `rocketmq-spring-boot-starter`
- 所有微服务通过 `erp-common/event` 统一消息抽象层发送/接收，该模块内部根据配置切换中间件，业务代码隔离。
- **Spring Cloud Stream** 函数式编程模型：`Supplier` (生产)、`Function` (转换)、`Consumer` (消费)。

### 📊 数据分析组件

- Canal → Kafka → Flink (流计算) → ES/PostgreSQL/Redis
- ClickHouse：OLAP分析，通过 Flink JDBC Connector 写入
- EFK 日志体系对接审计中心

***

### 📦 多环境部署与 GitOps 策略

- **本地开发**：H2 + Embedded Redis + Embedded Kafka，零容器启动 (`erp-app` 单进程聚合)
- **测试环境**：Docker Compose 编排 (PG/Redis/Kafka/MinIO/ES/ClickHouse/Nacos)
- **生产环境**：K8s + Helm，多 values 按云平台区分 (华为云/AWS/阿里云)
- CI/CD 构建 Java 应用镜像 (基于 `eclipse-temurin:17-jre-alpine`，多阶段构建)，镜像标签禁用 `latest`
- Flyway 数据库迁移脚本按域隔离，版本号严格递增

### 🧪 本地基础设施详情 (Windows 开发)

所有组件列表：

- **JDK 17**：Eclipse Temurin，`JAVA_HOME` 指向 D 盘安装目录
- **Maven**：`M2_HOME` 与本地仓库重定向至 `D:\Project\erp\.m2`
- **Docker Compose**：PG/Redis/Kafka/MinIO/ES/ClickHouse/Nacos 一键启动
- **H2 本地模式**：无需安装 PG，`erp-app` 可零依赖启动
- 所有路径锁定 D 盘，C 盘零写入

### 🔒 依赖管理与安全

- 新增 Maven 依赖需经审批，版本号统一在父 POM `<dependencyManagement>` 管理，子模块禁止指定版本
- 安全规范：MyBatis 参数化查询，禁用字符串拼接 SQL；JWT 密钥由环境变量注入，禁止硬编码
- **Spotless** 代码格式化强制执行，CI 阶段 `mvn spotless:check` 不通过则构建失败
- **EditorConfig** 统一缩进/换行/编码规范

***

### 📊 当前进展

| 阶段       | 状态     | 完成项                                                                                                                                      |
| -------- | ------ | ---------------------------------------------------------------------------------------------------------------------------------------- |
| P0 工程基线  | ✅ 完成   | 18模块骨架、DDD分层、SC组件、EditorConfig、异常体系、租户上下文、日志、Docker Compose、Flyway、DDL、API路径规范、OpenAPI分组、测试基线、.gitignore、Dockerfile、ADR                  |
| P1 技术中台  | ✅ 完成   | Spring Security+JWT、RBAC、10维数据权限、PermissionAspect、审计AOP、Kafka事件发布、Outbox模式、Redis缓存、ES搜索、MinIO文件、通知服务、审批状态机、调度框架、Sentinel、Seata预留、PMS集成接口 |
| P2 核心域业务 | 🔄 进行中 | IAM/PDM/SOM/OMS/SCM/WMS/FBA/TMS/CRM/FMS/BI/ADS 域业务逻辑实现                                                                                   |
| P3 集成测试  | ⏳ 待开始  | 端到端测试、PMS集成测试                                                                                                                            |
| P4 部署上线  | ⏳ 待开始  | K8s部署、CI/CD、监控                                                                                                                           |

***

### 🔒 代码规范 (铁律)

- **统一响应**：所有API返回 `Result<T>`，成功用 `Result.ok(data)`，失败用 `Result.fail(code, message)`
- **异常体系**：业务异常统一抛 `BizException(code, message)`，由 `GlobalExceptionHandler` 捕获
- **实体基类**：`BaseEntity`(id/createdAt/updatedAt) 和 `TenantBaseEntity`(+tenantId)
- **权限注解**：`@RequirePermission("iam:user:write")` 控制功能权限
- **审计注解**：`@Audited(action="CREATE", module="pdm")` 自动记录操作日志
- **禁止事项**：禁止跨域直接依赖、禁止硬编码租户ID、禁止跳过审批直接写入

***

### ✍️ AI 输出规范 (Java 专版)

1. 功能分析先四色建模，再定义接口 (OpenAPI)，最后生成 DDD 分层代码骨架。
2. 异步消息指明使用的 topic 和中间件 (Kafka/RocketMQ)。
3. 代码输出需遵循 Spring Boot 最佳实践 (依赖注入、构造器注入、`@Transactional` 边界控制)。
4. 所有示例代码使用 Java 17 特性 (record、sealed class、text block、switch 表达式)。
5. 数据库操作使用 MyBatis-Plus，禁止手写 SQL 除非性能优化需要。
6. 新增 API 路径必须遵循 `/{service-name}/api/{direction}/v1/{resource}` 规范。
7. 结尾声明："所有路径锁定 D 盘，C 盘零写入，Maven 依赖新增需审批"。

***

### 📚 参考文档

1. `D:\Project\erp\跨境电商ERP系统详细设计说明书V11.md` — 核心设计文档
2. `D:\Project\erp\跨境电商ERP-技术实现方案V3.md` — 技术实现方案
3. `D:\Project\erp\跨境电商ERP-需求规格说明书V4.md` — 需求规格
4. `D:\Project\erp\跨境电商ERP系统——任务清单.md` — 218任务9阶段
5. `D:\Project\erp\跨境电商ERP系统——任务清单-验收标准.md` — 验收标准
6. `D:\Project\fms\PMS-ERP接口矩阵.md` — PMS集成接口
7. `D:\Project\fms\PMS-ERP权限矩阵.md` — PMS权限矩阵
8. `D:\Project\fms\PMS-ERP事件与数据同步矩阵.md` — 事件同步
9. `D:\Project\fms\PMS-ERP数据主权与主数据边界说明.md` — 数据主权
10. `D:\Project\fms\PMS建议池与ERP审批执行状态机设计.md` — 审批状态机
11. `D:\Project\fms\跨境电商AI选品系统PMS详细设计说明书V11.md` — PMS设计
12. `D:\Project\fms\PMS-ERP14域验收标准.md` — 14域验收

***

*本文件由跨境电商ERP全栈虚拟专家团队联合制定，所有 AI 编程工具必须严格遵守。*
