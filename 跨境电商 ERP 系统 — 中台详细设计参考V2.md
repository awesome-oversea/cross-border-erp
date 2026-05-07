# 跨境电商ERP系统——中台详细设计说明书（强化版 V2.0）

> **版本**：V2.0  
> **创建日期**：2026-05-08  
> **文档状态**：正式版  
> **编制依据**：  
> - 跨境电商ERP系统详细设计说明书V11  
> - 跨境电商ERP-需求规格说明书V4  
> - 跨境电商ERP-技术实现方案V4  
> - ERP中台模块与开源项目可视化架构图  
> - 中台业务/技术/工作流/自动化开源项目参考表  
> - 主流开源社区最佳实践

---

## 目录

1. [总体设计原则与架构分层](#1-总体设计原则与架构分层)
2. [技术栈选型与开源生态](#2-技术栈选型与开源生态)
3. [DDD分层与模块结构规范](#3-ddd分层与模块结构规范)
4. [业务中台详细设计](#4-业务中台详细设计)
   - 4.1 内容审核中心
   - 4.2 货币汇率中心
   - 4.3 国内外支付聚合中心
   - 4.4 订单策略中心
   - 4.5 物流策略中心
   - 4.6 计费策略中心
   - 4.7 客户数据平台(CDP)
   - 4.8 发票税务中台
   - 4.9 合规风控中台
   - 4.10 选品分析中台
   - 4.11 广告优化中台
   - 4.12 成本归集引擎
   - 4.13 利润核算引擎
   - 4.14 进销存凭证引擎
5. [技术能力中台详细设计](#5-技术能力中台详细设计)
   - 5.1 消息通知中心
   - 5.2 文件处理中心
   - 5.3 工作流引擎
   - 5.4 任务调度中心
   - 5.5 权限管理中心
   - 5.6 日志审计中心
   - 5.7 API网关
   - 5.8 多语言翻译中心
   - 5.9 数据脱敏中心
   - 5.10 API管理平台
   - 5.11 连接器管理平台
6. [中台集成与数据流转规范](#6-中台集成与数据流转规范)
7. [高可用与容灾设计](#7-高可用与容灾设计)
8. [扩展性与插件化机制](#8-扩展性与插件化机制)
9. [开源项目映射与选型建议](#9-开源项目映射与选型建议)
10. [可视化架构图参考](#10-可视化架构图参考)

---

## 1. 总体设计原则与架构分层

### 1.1 设计原则

| 原则                     | 说明                                                         | 开源参考                        |
| ------------------------ | ------------------------------------------------------------ | ------------------------------- |
| **模块化与分层**         | 业务中台和技术中台独立部署，模块接口清晰，避免跨域数据库直接访问 | COLA架构、mall-cloud            |
| **DDD分层**              | `interfaces → application → domain ← infrastructure`，domain层不依赖Spring | 美团DDD实践、Axon Framework     |
| **事件驱动与数据一致性** | 核心业务使用Kafka + Outbox模式，关键业务强一致性，非关键业务最终一致性 | Debezium、Kafka Connect         |
| **PMS AI集成**           | PMS仅生成建议（Recommendation/Draft/InsightCard），ERP审批后执行，ERP作为业务数据真相源 | Temporal Saga模式               |
| **多租户与安全**         | 租户隔离（逻辑多Schema方案），对象级权限控制（RBAC+数据权限+审计） | Apache ShardingSphere、Keycloak |
| **可观测性**             | 全链路日志（EFK）、指标监控（Prometheus/Grafana）、链路追踪（OpenTelemetry/Jaeger） | OpenTelemetry、Micrometer       |
| **插件化可扩展**         | 通过策略模式、SPI机制实现算法/渠道可插拔                     | Spring Plugin、Dubbo SPI        |
| **无状态设计**           | 业务中台服务本身无状态，状态下沉至Redis/DB                   | 12-Factor App                   |
| **开源优先**             | 优先采用Apache基金会或CNCF基金会项目，确保社区活跃与低商业风险 | Apache Airflow、Kafka、Sentinel |

### 1.2 架构分层全景

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           展示层 (Presentation)                              │
│  Next.js 14+ 管理后台 / PDA仓库作业(React Native) / 开放API(SwaggerUI)       │
│  大屏可视化(ECharts) / 移动端App / Web浏览器                                 │
└──────────────────────────────────┬──────────────────────────────────────────┘
                                   │
┌──────────────────────────────────▼──────────────────────────────────────────┐
│                           网关层 (Gateway)                                   │
│  Kong(外部: SSL终止/WAF/域名路由/全局限流/访问控制)                           │
│         → SCG(内部: JWT认证/授权/租户Header注入/限流熔断/灰度路由)           │
└──────────────────────────────────┬──────────────────────────────────────────┘
                                   │
┌──────────────────────────────────▼──────────────────────────────────────────┐
│                       业务域层 (14大业务域)                                   │
│  DASHBOARD │ IAM │ PDM │ SOM │ ADS │ OMS │ SCM │ WMS │ FBA │ TMS │ CRM │ FMS │ BI │ SYS │
│  经营看板  │组织/用户│选品/产品│店铺/Listing│广告投放│订单处理│采购/供应商│入库/出库│FBA库存│物流调度│客服工单│数据分析│系统配置│
│  KPI监控  │角色/权限│开发/生命周期│定价/促销│优化/归因│履约/履约│计划/协同│库存管理│海外仓管理│轨迹/时效│成本/评价│报表/报表│字典/参数│
│  推荐: Nacos│Grafana│Cassandra│Spree│Saleor│Redash│Medusa│Odoo│OpenWMS│Stock5│GoFreight│Frappe ERPNext│ClickHouse│Apollo│
└──────────────────────────────────┬──────────────────────────────────────────┘
                                   │
┌──────────────────────────────────▼──────────────────────────────────────────┐
│                       业务中台层 (14大业务中台)                                │
│  内容审核 │ 货币汇率 │ 支付聚合 │ 订单策略 │ 物流策略 │ 计费策略 │ CDP │ 发票税务 │
│  合规风控 │ 选品分析 │ 广告优化 │ 成本归集 │ 利润核算 │ 进销存凭证 │             │
│  推荐: OpenNLP │ FXRateLib │ Apache APISIX │ Drools │ Apache Camel │ Brooks │ Apache Flink │ Flipt │
│        Python+Pandas │ Apache Airflow │ Kurenets │ Frappe │ ClickHouse │ Apache Doris │ Odoo │
└──────────────────────────────────┬──────────────────────────────────────────┘
                                   │
┌──────────────────────────────────▼──────────────────────────────────────────┐
│                       技术中台层 (11大技术中台)                                │
│  消息通知 │ 文件处理 │ 工作流引擎 │ 任务调度 │ 权限管理 │ 日志审计 │ API网关 │
│  多语言翻译 │ 数据脱敏 │ API管理平台 │ 连接器管理平台 │                         │
│  推荐: Apache RocketMQ │ Kafka │ Apache Tika │ Apache Camunda │ XXL-JOB │ Keycloak │
│        Event Store │ Kong │ Apache ESI │ Nacos │ Sentinel │                   │
└──────────────────────────────────┬──────────────────────────────────────────┘
                                   │
┌──────────────────────────────────▼──────────────────────────────────────────┐
│                       事件驱动层 (Event Layer)                                │
│  Apache Kafka / RocketMQ + Outbox Pattern + CloudEvents + EventStore        │
│  Canal → Kafka → Flink → ClickHouse/Elasticsearch/Redis                     │
└──────────────────────────────────┬──────────────────────────────────────────┘
                                   │
┌──────────────────────────────────▼──────────────────────────────────────────┐
│                       连接器层 (Connector Layer)                              │
│  电商平台: Amazon SP-API │ Shopify API │ TikTok Shop API │ 1688 API         │
│  物流: DHL │ FedEx │ UPS │ 燕文 │ 4PX                                       │
│  支付: PayPal │ Stripe │ Payoneer │ 连连 │ 万里汇                           │
│  仓储: Amazon FBA │ ShipBob │ Deliverr                                        │
│  智能服务: PMS AI连接器                                                        │
└──────────────────────────────────┬──────────────────────────────────────────┘
                                   │
┌──────────────────────────────────▼──────────────────────────────────────────┐
│                       数据层 (Data Layer)                                     │
│  PostgreSQL 15+ │ Redis 7.x │ Elasticsearch 8.x │ MinIO │ ClickHouse │ Canal │
│  PostgreSQL → Canal → Kafka → Flink → ClickHouse/ES/Redis                    │
└─────────────────────────────────────────────────────────────────────────────┘

                          ┌──────────────────────────┐
                          │   PMS智能系统闭环         │
                          │                          │
                          │  PMS生成建议              │
                          │         ↓                │
                          │  建议接入ERP              │
                          │         ↓                │
                          │  人工审批执行             │
                          │         ↓                │
                          │  执行结果回流             │
                          │         ↓                │
                          │  PMS学习优化             │
                          │  推荐: TensorFlow        │
                          └──────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                          支撑工具层                                           │
│  容器编排: Docker + Kubernetes + Helm                                        │
│  CI/CD: GitLab CI / GitHub Actions / ArgoCD                                  │
│  监控告警: Prometheus + Grafana + AlertManager                               │
│  链路追踪: OpenTelemetry + Jaeger / Zipkin                                   │
│  日志集中: EFK (Elasticsearch + Fluentd + Kibana)                            │
│  服务网格: Istio + Envoy                                                     │
│  配置中心: Nacos / Apollo                                                    │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. 技术栈选型与开源生态

### 2.1 后端核心技术栈

| 层级          | 技术选型              | 版本         | 用途                 | 开源协议   |
| ------------- | --------------------- | ------------ | -------------------- | ---------- |
| 基础框架      | Spring Boot           | 3.2.x        | 应用基线             | Apache-2.0 |
| 微服务框架    | Spring Cloud          | 2023.x       | 服务治理             | Apache-2.0 |
| 服务注册/配置 | Nacos                 | 2.3.x        | 注册中心+配置中心    | Apache-2.0 |
| 熔断限流      | Sentinel              | 1.8.x        | 限流/熔断/降级       | Apache-2.0 |
| ORM           | MyBatis-Plus          | 3.5.x        | 多租户+逻辑删除+分页 | Apache-2.0 |
| 安全          | Spring Security + JWT | 6.x / 0.12.5 | 认证鉴权             | Apache-2.0 |
| 消息队列      | Apache Kafka          | 3.6+         | 高吞吐事件驱动       | Apache-2.0 |
| 事务消息      | Apache RocketMQ       | 5.x          | 分布式事务消息       | Apache-2.0 |
| 长事务编排    | Temporal Java SDK     | 1.x          | Saga模式、重试、补偿 | MIT        |
| 工作流        | Flowable / Camunda    | 7.x          | BPMN工作流引擎       | Apache-2.0 |
| 批处理        | Spring Batch          | 5.x          | 账单清洗、利润计算   | Apache-2.0 |
| 任务调度      | XXL-JOB               | 2.4.x        | 分布式任务调度       | GPLv3      |
| API文档       | SpringDoc OpenAPI     | 2.x          | 14域分组文档         | Apache-2.0 |

### 2.2 数据存储与分析

| 组件          | 版本   | 用途               | 开源协议        |
| ------------- | ------ | ------------------ | --------------- |
| PostgreSQL    | 15+    | 主数据库           | PostgreSQL      |
| Redis         | 7.x    | 缓存/分布式锁/会话 | BSD             |
| Elasticsearch | 8.x    | 全文搜索/日志存储  | Elastic License |
| MinIO         | latest | S3兼容对象存储     | AGPL-3.0        |
| ClickHouse    | latest | OLAP分析           | Apache-2.0      |
| Canal         | 1.1.x  | PostgreSQL变更捕获 | Apache-2.0      |

### 2.3 前端技术栈

| 层级     | 技术选型     | 版本 | 用途           |
| -------- | ------------ | ---- | -------------- |
| 框架     | Next.js      | 14+  | App Router     |
| UI库     | React        | 18+  | 组件化         |
| 类型     | TypeScript   | 5.x  | 类型安全       |
| 组件库   | Ant Design   | 5.x  | 企业级UI       |
| 状态管理 | Zustand      | -    | 轻量状态       |
| 请求     | Axios + SWR  | -    | 数据请求       |
| 图表     | ECharts      | 5.x  | 数据可视化     |
| PDA      | React Native | -    | 移动端仓库作业 |

### 2.4 运维与监控

| 组件                        | 用途           | 开源协议   |
| --------------------------- | -------------- | ---------- |
| Docker + Kubernetes         | 容器化部署编排 | Apache-2.0 |
| Helm                        | K8s包管理      | Apache-2.0 |
| Prometheus + Grafana        | 监控告警可视化 | Apache-2.0 |
| OpenTelemetry + Jaeger      | 分布式链路追踪 | Apache-2.0 |
| EFK (ES + Fluentd + Kibana) | 日志采集查询   | 多个协议   |
| Istio                       | 服务网格       | Apache-2.0 |
| ArgoCD                      | GitOps部署     | Apache-2.0 |

---

## 3. DDD分层与模块结构规范

### 3.1 标准模块结构

```
erp-platform/
├── erp-common/                          # 公共模块
│   ├── erp-common-core/                 # 核心工具类
│   ├── erp-common-security/             # 安全工具类
│   ├── erp-common-mybatis/              # MyBatis配置
│   ├── erp-common-redis/                # Redis配置
│   ├── erp-common-kafka/                # Kafka配置
│   ├── erp-common-es/                   # ES配置
│   ├── erp-common-minio/                # MinIO配置
│   └── erp-common-openapi/              # OpenAPI配置
├── erp-gateway/                         # Spring Cloud Gateway
├── erp-domain-xxx/                      # 各业务域
│   └── src/main/java/com/aidotnet/erp/xxx/
│       ├── interfaces/                  # 接口层
│       │   ├── rest/                    # REST Controller
│       │   ├── dto/                     # 数据传输对象
│       │   └── assembler/               # DTO↔领域对象转换
│       ├── application/                 # 应用层
│       │   ├── service/                 # 用例协调、事务编排
│       │   └── command/                 # 命令对象
│       ├── domain/                      # 领域层（纯Java，不依赖Spring）
│       │   ├── model/                   # 聚合根、实体、值对象
│       │   │   ├── aggregate/           # 聚合根
│       │   │   ├── entity/              # 实体
│       │   │   └── vo/                  # 值对象
│       │   ├── service/                 # 领域服务
│       │   ├── event/                   # 领域事件
│       │   ├── repository/              # 仓储接口
│       │   └── gateway/                 # 外部网关接口
│       └── infrastructure/              # 基础设施层
│           ├── persistence/             # MyBatis Mapper实现
│           ├── messaging/               # 消息发送实现
│           ├── client/                  # 外部API客户端
│           └── config/                  # Spring配置
└── erp-starter/                         # 启动模块
```

### 3.2 模块依赖规则

```
interfaces → application → domain ← infrastructure
```

- domain层不依赖任何Spring框架
- infrastructure实现domain层定义的接口
- 域间调用通过application层接口，禁止直接调用infrastructure

---

## 4. 业务中台详细设计

各业务中台在设计时充分参考了开源社区的最佳实践，并在模块描述中标注了推荐的开源项目。详细的开源项目对照表请参见第9章。

### 4.1 内容审核中心

**设计概述**  
统一管理产品文案、图片、Listing内容的多语言审核与合规检查，引入**策略器**和**规则引擎**分离机制，支持热更新审核规则。

**推荐开源**：OpenNLP、Drools、TensorFlow、ToolGood.Words、houbb/sensitive-word

#### 4.1.1 架构组件

```
┌───────────────────────────────────────────────────────────────────┐
│                    内容审核中心 (content-review)                    │
├───────────────────────────────────────────────────────────────────┤
│  interfaces (REST)                                                 │
│  ├─ ContentReviewController                                       │
│  └─ dto/ReviewRequest, ReviewResult                              │
├───────────────────────────────────────────────────────────────────┤
│  application                                                       │
│  ├─ ContentReviewService (审核编排)                                 │
│  ├─ ReviewRuleEngine (规则引擎接口，适配Drools或自研)                │
│  └─ ReviewChannel (策略：自动通过/自动拒绝/人工复审)                │
├───────────────────────────────────────────────────────────────────┤
│  domain                                                            │
│  ├─ ReviewContent (聚合根)                                         │
│  ├─ SensitiveWordDetector (领域服务，封装敏感词检测工具)              │
│  ├─ ImageModeration (领域服务，图像审核)                             │
│  └─ AuditLog (实体)                                               │
├───────────────────────────────────────────────────────────────────┤
│  infrastructure                                                    │
│  ├─ persistence (MyBatis)                                         │
│  ├─ client/SensitiveWordClient (HTTP调用或本地库)                   │
│  ├─ client/ImageModerationClient (PMS图像审核或第三方)              │
│  └─ messaging (发布审核完成事件)                                    │
└───────────────────────────────────────────────────────────────────┘
```

#### 4.1.2 核心聚合

```java
@dataclass
class ReviewContent:
    """审核内容聚合根"""
    review_id: str
    tenant_id: str
    target_type: str        // productDescription/image/listingTitle
    source_type: str        // PDM/SOM/CRM
    source_id: str
    content: str
    language: str
    channel_config: Dict
    status: ReviewStatus    // pending/auto_approved/auto_rejected/manual_review/approved/rejected
    rule_results: List[RuleResult]
    ai_score: Optional[float]
    ai_suggestion: Optional[str]
    created_at: datetime
    reviewed_at: Optional[datetime]
    reviewer_id: Optional[str]

    def can_auto_approve(self) -> bool:
        return all(r.result == 'pass' for r in self.rule_results)

    def need_manual_review(self) -> bool:
        return any(r.severity == 'high' for r in self.rule_results if r.result == 'hit')
```

#### 4.1.3 核心功能

| 功能           | 说明                                 | 实现方式                                            |
| -------------- | ------------------------------------ | --------------------------------------------------- |
| **文案审核**   | 敏感词检测、违禁词过滤、商标侵权检查 | 基于ToolGood.Words/sensitive-word词库，支持多语言   |
| **图片审核**   | 图片合规检查、水印检测、版权检查     | 调用PMS图像审核API或第三方（阿里云/腾讯云内容安全） |
| **多语言审核** | 翻译质量检查、文化禁忌检测           | 集成多语言翻译中心术语库校验                        |
| **审核流程**   | 自动审核 + 人工复审                  | 基于规则引擎(Drools)配置自动化策略                  |

#### 4.1.4 审核规则模型扩展

- `review_rules` 表字段扩展：
  - `rule_content` JSONB，支持Drools DRL脚本或Groovy表达式
  - `version` 整型，版本号
  - `status` 枚举 (draft/active/archived)
  - `effective_date` 日期，生效时间
- 规则热加载机制：通过Nacos配置监听实现规则动态刷新

#### 4.1.5 核心接口

| 方法 | 路径                                             | 说明                       |
| ---- | ------------------------------------------------ | -------------------------- |
| POST | `/sys/api/v1/content-review/submit`              | 提交内容审核，支持批量     |
| GET  | `/sys/api/v1/content-review/{id}/result`         | 获取审核结果               |
| GET  | `/sys/api/v1/content-review/rules`               | 获取审核规则（含版本管理） |
| POST | `/sys/api/v1/content-review/rules`               | 创建审核规则               |
| POST | `/sys/api/v1/content-review/rules/test`          | 规则测试沙箱               |
| PUT  | `/sys/api/v1/content-review/rules/{id}/activate` | 激活规则版本               |
| GET  | `/sys/api/v1/content-review/rules/{id}/versions` | 查看规则历史版本           |

---

### 4.2 货币汇率中心

**设计概述**  
提供实时汇率查询、历史汇率、自动汇率更新、汇率快照管理、多数据源汇率服务。增加多数据源适配器模式，支持自动切换与降级。

**推荐开源**：FXRateLib、OpenExchangeRates API、Apache Camel (集成)、XXL-JOB（定时采集）

#### 4.2.1 架构组件

```
┌───────────────────────────────────────────────────────────────────┐
│                    货币汇率中心 (forex)                              │
├───────────────────────────────────────────────────────────────────┤
│  interfaces                                                        │
│  ├─ ForexRateController                                           │
│  └─ dto/RateQueryRequest, BatchConvertRequest                    │
├───────────────────────────────────────────────────────────────────┤
│  application                                                       │
│  ├─ ForexRateService (汇率查询与换算)                               │
│  ├─ ForexRateSyncService (定时同步调度)                             │
│  └─ RateValidationService (多源交叉验证)                            │
├───────────────────────────────────────────────────────────────────┤
│  domain                                                            │
│  ├─ ForexRate (聚合根)                                             │
│  ├─ RateSnapshot (值对象，历史汇率快照)                              │
│  ├─ CurrencyPair (值对象)                                          │
│  ├─ RateSource 接口 (SPI，汇率源抽象)                               │
│  │   ├─ UnionPayRateSource (银联)                                  │
│  │   ├─ XERateSource (XE)                                          │
│  │   ├─ CentralBankRateSource (央行)                                │
│  │   └─ AliPayRateSource (支付宝)                                   │
│  ├─ ForexGainLossCalculator (领域服务，汇兑损益计算)                 │
│  └─ RateUpdated (领域事件)                                          │
├───────────────────────────────────────────────────────────────────┤
│  infrastructure                                                    │
│  ├─ persistence/MyBatis                                           │
│  ├─ client/UnionPayClient, XEClient 等                             │
│  ├─ cache/RedisRateCache (多源交叉验证)                              │
│  └─ messaging (发布汇率更新事件)                                    │
└───────────────────────────────────────────────────────────────────┘
```

#### 4.2.2 核心聚合

```java
@dataclass
class ForexRate:
    """汇率聚合根"""
    rate_id: str
    tenant_id: str
    from_currency: str       // USD
    to_currency: str         // CNY
    rate: Decimal
    source: str              // unionpay/xe/cb/alipay
    effective_date: date
    created_at: datetime

@dataclass
class RateSource(ABC):
    """汇率源抽象接口"""
    source_name: str
    priority: int
    weight: float

    @abstractmethod
    async def fetch_rate(self, from_currency: str, to_currency: str) -> Optional[Decimal]:
        pass

    def is_available(self) -> bool:
        pass
```

#### 4.2.3 核心功能

| 功能         | 说明                                                  | 实现方式                                |
| ------------ | ----------------------------------------------------- | --------------------------------------- |
| **实时汇率** | 对接银联/央行/XE等多数据源，提供实时汇率查询          | RateSource SPI，支持多源动态切换        |
| **历史汇率** | 历史汇率数据存储与查询，支持按日期范围查询            | RateSnapshot按期归档                    |
| **汇率快照** | 按生效时间管理汇率快照，确保财务核算汇率可追溯        | Optimistic Locking (version字段)        |
| **自动更新** | 定时更新汇率数据（每日/每小时），支持多数据源交叉验证 | XXL-JOB定时调度 + RateValidationService |
| **汇率换算** | 批量汇率换算服务，支持多币种同时换算                  | 并行流处理，Redis缓存热点               |
| **汇兑损益** | 自动计算汇兑损益，关联业务单据                        | ForexGainLossCalculator                 |
| **风险预警** | 汇率波动超阈值自动告警，支持自定义预警规则            | Sentinel熔断 + AlertManager通知         |

#### 4.2.4 数据一致性

- 汇率快照写入时采用`Optimistic Locking (version)`防止并发覆盖
- 快照历史表保留所有变更，不可物理删除
- 财务核算必须引用快照ID，确保可追溯

#### 4.2.5 核心接口

| 方法 | 路径                                                    | 说明                      |
| ---- | ------------------------------------------------------- | ------------------------- |
| GET  | `/fms/api/v1/forex/rates/{from}/{to}`                   | 获取实时汇率              |
| GET  | `/fms/api/v1/forex/rates/{from}/{to}/snapshot?date=xxx` | 获取指定日期汇率快照      |
| GET  | `/fms/api/v1/forex/history`                             | 获取历史汇率              |
| POST | `/fms/api/v1/forex/convert`                             | 批量汇率换算              |
| POST | `/fms/api/v1/forex/convert/batch`                       | 大批量换算（异步）        |
| POST | `/fms/api/v1/forex/gain-loss/calculate`                 | 计算汇兑损益              |
| GET  | `/fms/api/v1/forex/risk-alert`                          | 获取汇率风险预警          |
| POST | `/fms/api/v1/forex/rates/sync`                          | 手动触发汇率同步          |
| POST | `/fms/api/v1/forex/rate-control`                        | 管理界面配置源优先级/权重 |

---

### 4.3 国内外支付聚合中心

**设计概述**  
聚合多种支付渠道，提供统一的支付、退款、对账、结汇提现接口。采用**支付路由**模式，支持多支付服务商动态添加。

**推荐开源**：IJPay、Apache APISIX、Seata、Spring Batch

#### 4.3.1 架构组件

```
┌───────────────────────────────────────────────────────────────────┐
│                  国内外支付聚合中心 (payment)                        │
├───────────────────────────────────────────────────────────────────┤
│  interfaces                                                        │
│  ├─ PaymentController (统一支付入口)                                │
│  ├─ CallbackController (统一回调入口)                               │
│  └─ dto/PaymentRequest, RefundRequest, BatchPayRequest            │
├───────────────────────────────────────────────────────────────────┤
│  application                                                       │
│  ├─ PaymentService (支付编排)                                       │
│  ├─ RefundService (退款编排)                                        │
│  ├─ ReconciliationService (对账服务)                                │
│  └─ BatchPayService (批量付款编排)                                  │
├───────────────────────────────────────────────────────────────────┤
│  domain                                                            │
│  ├─ PaymentOrder (聚合根)                                          │
│  │   └─ 状态: INIT → PAYING → SUCCESS/FAIL → REFUNDING → REFUNDED │
│  ├─ RefundOrder (聚合根)                                           │
│  ├─ PaymentChannel 接口 (SPI，支付渠道抽象)                        │
│  │   ├─ PayoneerChannel                                           │
│  │   ├─ LianLianChannel                                           │
│  │   ├─ WorldFirstChannel                                         │
│  │   ├─ PayPalChannel                                             │
│  │   ├─ StripeChannel                                             │
│  │   ├─ AliPayChannel                                             │
│  │   └─ WeChatPayChannel                                          │
│  ├─ SettlementOrder (结算单聚合根)                                  │
│  └─ PaymentCompleted, RefundCompleted (领域事件)                    │
├───────────────────────────────────────────────────────────────────┤
│  infrastructure                                                    │
│  ├─ persistence/MyBatis                                           │
│  ├─ client/PayoneerClient, LianLianClient 等                       │
│  ├─ batch/PaymentBatchProcessor (Spring Batch)                    │
│  └─ messaging (发布支付事件)                                        │
└───────────────────────────────────────────────────────────────────┘
```

#### 4.3.2 核心聚合

```java
@dataclass
class PaymentOrder:
    """支付单聚合根"""
    payment_id: str
    tenant_id: str
    out_trade_no: str          // 商户订单号，幂等键
    channel: str               // payoneer/lianlian/paypal
    amount: Decimal
    currency: str
    status: PaymentStatus
    channel_trade_no: Optional[str]
    callback_data: Optional[Dict]
    created_at: datetime
    paid_at: Optional[datetime]

    def can_refund(self, refund_amount: Decimal) -> bool:
        return self.status in [PaymentStatus.SUCCESS, PaymentStatus.PARTIAL_REFUND]

@dataclass
abstract class PaymentChannel:
    """支付渠道抽象接口"""
    @abstractmethod
    async def create_payment(self, order: PaymentOrder) -> ChannelResult:
        pass

    @abstractmethod
    async def query_payment(self, out_trade_no: str) -> PaymentStatus:
        pass

    @abstractmethod
    async def refund(self, refund: RefundOrder) -> ChannelResult:
        pass

    @abstractmethod
    async def verify_callback(self, data: Dict) -> bool:
        pass

    @abstractmethod
    async def download_bill(self, date: date) -> str:
        pass
```

#### 4.3.3 核心功能

| 功能             | 说明                                                     | 实现方式                             |
| ---------------- | -------------------------------------------------------- | ------------------------------------ |
| **支付渠道管理** | Payoneer/连连/万里汇/支付宝/微信/PayPal/Stripe等渠道接入 | PaymentChannel SPI + 动态注册        |
| **统一支付**     | 标准化支付接口，屏蔽渠道差异                             | PaymentService编排                   |
| **退款处理**     | 统一退款流程，支持部分退款                               | RefundService + 退款状态机           |
| **批量付款**     | 供应商/物流商批量付款，关联付款审批流                    | BatchPayService + XXL-JOB            |
| **自动对账**     | 支付流水与银行对账，差异自动标记                         | Spring Batch + ReconciliationService |
| **结汇提现**     | 对接Payoneer/连连/万里汇，一站式管理                     | SettlementService                    |
| **Amazon追款**   | Amazon平台追款功能                                       | 调用Amazon SP-API                    |

#### 4.3.4 安全与幂等设计

- 每个支付请求携带`outTradeNo`保障幂等，相同`outTradeNo`返回同一支付单
- 回调验签使用渠道SDK密钥库加密
- 分布式锁（Redis Redisson）防止并发回调导致重复处理
- 所有支付操作记录审计日志

#### 4.3.5 核心接口

| 方法 | 路径                                      | 说明                       |
| ---- | ----------------------------------------- | -------------------------- |
| POST | `/fms/api/v1/payment/pay`                 | 发起支付                   |
| POST | `/fms/api/v1/payment/refund`              | 发起退款                   |
| POST | `/fms/api/v1/payment/batch-pay`           | 批量付款                   |
| POST | `/fms/api/v1/payment/callback/{channel}`  | 统一回调入口               |
| GET  | `/fms/api/v1/payment/channels`            | 获取支付渠道列表           |
| GET  | `/fms/api/v1/payment/reconciliation`      | 触发对账任务，返回差异报告 |
| POST | `/fms/api/v1/payment/settlement/withdraw` | 结汇提现                   |
| GET  | `/fms/api/v1/payment/balance`             | 查询账户余额               |
| POST | `/fms/api/v1/payment/amazon-claim`        | Amazon追款                 |

---

### 4.4 订单策略中心

**设计概述**  
管理订单审核策略、分仓策略、物流优选策略等。将策略从硬编码中分离，设计**策略决策表**和**规则流**。

**推荐开源**：Drools、Easy Rules、QLExpress、Apache Camel

#### 4.4.1 架构组件

```
┌───────────────────────────────────────────────────────────────────┐
│                    订单策略中心 (order-strategy)                     │
├───────────────────────────────────────────────────────────────────┤
│  interfaces                                                        │
│  ├─ OrderStrategyController                                       │
│  └─ dto/EvaluateRequest, StrategyConfigDTO                        │
├───────────────────────────────────────────────────────────────────┤
│  application                                                       │
│  ├─ OrderStrategyService (策略编排)                                 │
│  ├─ StrategyEvaluateService (策略评估执行)                          │
│  └─ StrategySimulateService (策略模拟)                              │
├───────────────────────────────────────────────────────────────────┤
│  domain                                                            │
│  ├─ StrategyDefinition (聚合根)                                    │
│  │   ├─ strategy_id: str                                          │
│  │   ├─ strategy_type: str   # audit/warehouse/logistics          │
│  │   ├─ rules: List[Rule]    # JSONB存储                          │
│  │   ├─ priority: int                                             │
│  │   ├─ version: int                                              │
│  │   └─ is_active: bool                                           │
│  ├─ Rule (值对象)                                                  │
│  │   ├─ condition: str      # Drools/QLExpress表达式              │
│  │   ├─ action: str                                               │
│  │   └─ priority: int                                             │
│  ├─ DecisionLog (实体，记录每次策略命中)                             │
│  └─ StrategyChanged (领域事件)                                     │
├───────────────────────────────────────────────────────────────────┤
│  infrastructure                                                    │
│  ├─ persistence/MyBatis                                           │
│  ├─ engine/DroolsRuleEngine (规则引擎实现)                          │
│  ├─ engine/QLExpressEngine (轻量规则引擎实现)                       │
│  └─ cache/RedisStrategyCache (策略缓存)                             │
└───────────────────────────────────────────────────────────────────┘
```

#### 4.4.2 核心聚合

```java
@dataclass
class StrategyDefinition:
    strategy_id: str
    tenant_id: str
    strategy_type: str       // audit/warehouse/logistics
    name: str
    rules: List[Rule]
    priority: int
    version: int
    is_active: bool
    created_at: datetime
    updated_at: datetime

@dataclass
class Rule:
    rule_id: str
    condition: str           // QLExpress/Drools表达式
    action: ActionConfig
    priority: int
    description: str

    def evaluate(self, context: Dict) -> bool:
        pass
```

#### 4.4.3 核心功能

| 功能         | 说明                               | 实现方式                            |
| ------------ | ---------------------------------- | ----------------------------------- |
| **审核策略** | 风控规则、利润阈值、自动审核规则   | Drools/QLExpress规则引擎            |
| **分仓策略** | 基于库存、距离、成本的分仓规则     | 多维度评分模型                      |
| **物流优选** | 基于时效、成本、服务质量的物流选择 | 责任链+权重模型                     |
| **策略配置** | 可视化策略配置界面                 | 前端拖拽式规则编辑器（基于AntV X6） |
| **策略模拟** | 批量订单模拟，用于策略调优         | StrategySimulateService             |
| **策略版本** | 策略版本管理，支持回滚             | 每次发布新增一条记录                |

#### 4.4.4 核心接口

| 方法 | 路径                                         | 说明                         |
| ---- | -------------------------------------------- | ---------------------------- |
| GET  | `/oms/api/v1/order-strategies`               | 获取策略列表                 |
| POST | `/oms/api/v1/order-strategies`               | 创建策略                     |
| PUT  | `/oms/api/v1/order-strategies/{id}`          | 更新策略                     |
| POST | `/oms/api/v1/order-strategies/{id}/activate` | 激活策略版本                 |
| POST | `/oms/api/v1/order-strategies/evaluate`      | 传入订单上下文，返回匹配结果 |
| POST | `/oms/api/v1/order-strategies/simulate`      | 批量订单模拟                 |
| GET  | `/oms/api/v1/order-strategies/{id}/versions` | 查看策略历史版本             |
| POST | `/oms/api/v1/order-strategies/{id}/rollback` | 策略回滚                     |

---

### 4.5 物流策略中心

**设计概述**  
管理物流商选择策略、运费计算策略、物流时效策略。采用**可插拔计算引擎**，每个物流商为独立计算器。

**推荐开源**：Apache Camel、Easy Rules、Apache Flink（实时计算）

#### 4.5.1 架构组件

```
┌───────────────────────────────────────────────────────────────────┐
│                  物流策略中心 (logistics-strategy)                   │
├───────────────────────────────────────────────────────────────────┤
│  interfaces                                                        │
│  ├─ LogisticsStrategyController                                   │
│  └─ dto/SelectBestRequest, RateCalculateRequest                  │
├───────────────────────────────────────────────────────────────────┤
│  application                                                       │
│  ├─ LogisticsStrategyService (策略编排)                             │
│  ├─ RateCalculateService (运费计算)                                 │
│  └─ TransitTimePredictService (时效预测)                            │
├───────────────────────────────────────────────────────────────────┤
│  domain                                                            │
│  ├─ LogisticsRule (聚合根)                                         │
│  ├─ LogisticsCalculator 接口 (SPI)                                  │
│  │   ├─ DHLCalculator, FedExCalculator, UPSCalculator, etc.       │
│  ├─ RateResult, TransitTimeResult (值对象)                          │
│  └─ LogisticsOptimized (领域事件)                                   │
├───────────────────────────────────────────────────────────────────┤
│  infrastructure                                                    │
│  ├─ persistence/MyBatis                                           │
│  ├─ client/DHLClient, FedExClient 等                               │
│  ├─ engine/LogisticsCalculatorChain (责任链)                       │
│  └─ cache/RedisRateCache (运费缓存)                                 │
└───────────────────────────────────────────────────────────────────┘
```

#### 4.5.2 核心接口

| 方法 | 路径                                     | 说明                             |
| ---- | ---------------------------------------- | -------------------------------- |
| POST | `/tms/api/v1/logistics/select-best`      | 根据包裹信息返回排序后的物流方案 |
| POST | `/tms/api/v1/logistics/calculate-rate`   | 多物流商运费对比计算             |
| POST | `/tms/api/v1/logistics/predict-transit`  | 时效预估（集成PMS模型）          |
| POST | `/tms/api/v1/logistics/feedback`         | 接收实际物流绩效，调整策略权重   |
| GET  | `/tms/api/v1/logistics/strategy/weights` | 查看当前各物流商权重系数         |

---

### 4.6 计费策略中心

**设计概述**  
管理平台费用计算、佣金计算、仓储费用计算、物流费用计算、费用模拟。采用**计算引擎与规则分离**架构，支持复杂表达式配置。

**推荐开源**：AviatorScript、Brooks、Apache Flink

#### 4.6.1 核心模型

```java
@dataclass
class FeeRule:
    rule_id: str
    tenant_id: str
    fee_type: str           // platform_commission/storage/logistics/advertising
    platform: Optional[str]
    expression: str         // Aviator表达式
    unit_price: Optional[Decimal]
    conditions: Dict
    version: int
    is_active: bool
    effective_date: date
    created_at: datetime

class FeeCalculator:
    def calculate(self, rule: FeeRule, context: Dict) -> Decimal:
        pass

class FeeSimulator:
    def simulate(self, rules: List[FeeRule], scenario: Dict) -> SimulationResult:
        pass
```

#### 4.6.2 核心接口

| 方法 | 路径                                         | 说明                         |
| ---- | -------------------------------------------- | ---------------------------- |
| GET  | `/fms/api/v1/billing/platform-fees`          | 获取平台费用规则             |
| POST | `/fms/api/v1/billing/calculate`              | 实时计算费用                 |
| POST | `/fms/api/v1/billing/simulate`               | 费用模拟/利润预估            |
| POST | `/fms/api/v1/billing/template`               | 管理计费模板（支持DSL/YAML） |
| PUT  | `/fms/api/v1/billing/template/{id}/activate` | 激活模板版本                 |
| GET  | `/fms/api/v1/billing/warehouse-fees`         | 获取仓储费率                 |
| GET  | `/fms/api/v1/billing/freight-pool`           | 获取运费池                   |
| POST | `/fms/api/v1/billing/freight-allocate`       | 运费分摊计算                 |
| GET  | `/fms/api/v1/billing/packaging-costs`        | 获取包材成本                 |
| POST | `/fms/api/v1/billing/fba-head-cost`          | FBA头程费用计算              |

---

### 4.7 客户数据平台 (CDP)

**设计概述**  
整合多渠道客户数据，构建客户画像，支持精准营销。采用**OneID**设计，跨平台合并客户身份。

**推荐开源**：Apache Unomi、RudderStack、Keycloak、ClickHouse

#### 4.7.1 核心模型

```java
@dataclass
class CustomerProfile:
    profile_id: str
    tenant_id: str
    one_id: str
    identity_links: List[IdentityLink]
    name: str
    email: Optional[str]
    phone: Optional[str]
    country: str
    segments: List[str]
    tags: List[Tag]
    rfm_score: Optional[RFMScore]
    total_orders: int
    total_spent: Decimal
    blacklisted: bool
    created_at: datetime
    updated_at: datetime

@dataclass
class Segment:
    segment_id: str
    name: str
    conditions: Dict
    is_dynamic: bool
    member_count: int
    created_at: datetime
```

#### 4.7.2 核心接口

| 方法 | 路径                                       | 说明                  |
| ---- | ------------------------------------------ | --------------------- |
| GET  | `/crm/api/v1/cdp/customers/{id}/profile`   | 获取客户画像          |
| POST | `/crm/api/v1/cdp/identity/resolve`         | 融合身份（OneID关联） |
| GET  | `/crm/api/v1/cdp/segments`                 | 获取客户分群          |
| POST | `/crm/api/v1/cdp/segments`                 | 创建客户分群          |
| POST | `/crm/api/v1/cdp/segments/calculate`       | 触发分群计算（异步）  |
| PUT  | `/crm/api/v1/cdp/customers/{id}/tags`      | 更新客户标签          |
| POST | `/crm/api/v1/cdp/customers/{id}/blacklist` | 加入/移出黑名单       |

---

### 4.8 发票税务中台

**设计概述**  
管理多国发票生成、税务计算、VAT/GST合规申报、发票模板管理、第三方税务服务集成。

**推荐开源**：Flipt、Avalara SDK、JasperReports、Frappe

#### 4.8.1 第三方税务服务集成设计

| 服务商  | 优先级 | 用途                  | 集成方式               |
| ------- | ------ | --------------------- | ---------------------- |
| Avalara | P0     | 实时税率计算、VAT验证 | TaxServiceProvider SPI |
| TaxJar  | P1     | 美国销售税计算        | TaxServiceProvider SPI |
| 欧税通  | P2     | 欧盟VAT申报           | TaxServiceProvider SPI |

**降级策略**：当第三方服务不可用时，使用本地缓存的最近一次成功计算的税率（TTL 24小时），并触发告警。

#### 4.8.2 核心接口

| 方法 | 路径                                 | 说明                                 |
| ---- | ------------------------------------ | ------------------------------------ |
| POST | `/fms/api/v1/invoice/generate`       | 生成发票（PDF）                      |
| GET  | `/fms/api/v1/invoice/tax-rates`      | 获取税率                             |
| GET  | `/fms/api/v1/invoice/list`           | 获取发票列表                         |
| GET  | `/fms/api/v1/invoice/templates`      | 获取发票模板列表                     |
| POST | `/fms/api/v1/invoice/templates`      | 创建发票模板                         |
| PUT  | `/fms/api/v1/invoice/{id}/void`      | 作废发票                             |
| POST | `/fms/api/v1/invoice/{id}/red-flush` | 红冲发票                             |
| POST | `/fms/api/v1/tax/calculate`          | 税费计算（底层调用第三方，支持缓存） |
| GET  | `/fms/api/v1/tax/filing-data`        | 获取申报数据                         |
| POST | `/fms/api/v1/tax/validate-vat`       | 验证VAT号有效性                      |

---

### 4.9 合规风控中台

**设计概述**  
提供平台合规检查、知识产权保护、贸易合规验证、产品合规管理、VAT税务合规、反欺诈风控。

**推荐开源**：Drools、QLExpress、Apache Flink（实时风控）、Sentinel

#### 4.9.1 多层风控体系

```
实时规则引擎 (Drools/QLExpress) → 异步模型评分 (PMS AI) → 人工审核
          ↓                              ↓                    ↓
     自动拦截/放行                   风险标记              最终裁决
```

#### 4.9.2 核心模型

```java
@dataclass
class RiskAssessment:
    assessment_id: str
    tenant_id: str
    target_type: str        // order/listing/supplier/product
    target_id: str
    risk_level: RiskLevel   // low/medium/high/critical
    rule_results: List[RuleResult]
    ai_score: Optional[float]
    blacklist_hits: List[str]
    final_decision: str     // approved/rejected/manual_review
    assessed_at: datetime
    reviewed_by: Optional[str]
    reviewed_at: Optional[datetime]
```

#### 4.9.3 反欺诈检测能力

| 检测项   | 说明             | 实现方式               |
| -------- | ---------------- | ---------------------- |
| 盗卡检测 | 异常支付行为识别 | 规则引擎 + PMS评分     |
| 退货滥用 | 高频退货账号标记 | 统计模型               |
| 虚假物流 | 物流信息异常识别 | TMS轨迹分析            |
| 异常IP   | 代理/IP段识别    | ip2region + 公共黑名单 |

#### 4.9.4 核心接口

| 方法 | 路径                                      | 说明             |
| ---- | ----------------------------------------- | ---------------- |
| POST | `/fms/api/v1/compliance/check`            | 合规检查         |
| GET  | `/fms/api/v1/compliance/rules`            | 获取合规规则     |
| POST | `/fms/api/v1/compliance/product-cert`     | 产品认证校验     |
| GET  | `/fms/api/v1/compliance/vat/status`       | VAT合规状态查询  |
| POST | `/fms/api/v1/compliance/vat/filing-alert` | VAT申报预警      |
| POST | `/fms/api/v1/risk/assess`                 | 风险评估（实时） |
| POST | `/fms/api/v1/risk/fraud-detect`           | 反欺诈检测       |
| GET  | `/fms/api/v1/risk/blacklist`              | 买家黑名单查询   |
| POST | `/fms/api/v1/risk/blacklist`              | 管理黑名单       |

---

### 4.10 选品分析中台

**设计概述**  
提供市场分析、竞品分析、趋势分析，支持PMS选品决策。利用PMS提供的AI选品建议，结合ERP历史销售和库存数据，形成**选品工作台**。

**推荐开源**：Python+Pandas、Jupyter Notebook、TensorFlow、Kuberflow、Apache Kylin

#### 4.10.1 核心模型

```java
@dataclass
class SelectionAnalysis:
    analysis_id: str
    tenant_id: str
    product_name: str
    category: str
    market_analysis: Dict
    competitor_analysis: Dict
    trend_analysis: Dict
    profit_simulation: Dict
    pms_score: float
    pms_confidence: float
    recommendation_status: str
    created_at: datetime
```

#### 4.10.2 核心接口

| 方法 | 路径                                        | 说明                      |
| ---- | ------------------------------------------- | ------------------------- |
| GET  | `/pdm/api/v1/selection/market-analysis`     | 获取市场分析              |
| GET  | `/pdm/api/v1/selection/competitor-analysis` | 获取竞品分析              |
| POST | `/pdm/api/v1/selection/profit-simulation`   | 利润模拟                  |
| GET  | `/pdm/api/v1/selection/recommendations`     | 获取选品推荐（含PMS评分） |
| POST | `/pdm/api/v1/selection/custom-analysis`     | 自定义分析维度            |

---

### 4.11 广告优化中台

**设计概述**  
提供广告投放优化、关键词优化、预算分配建议。基于**反馈控制**的自动调价引擎，接收PMS出价建议。

**推荐开源**：Apache Airflow、Kuberflow、TensorFlow

#### 4.11.1 核心组件

```java
@dataclass
class AdOptimization:
    optimization_id: str
    tenant_id: str
    campaign_id: str
    optimization_type: str     // bid_adjustment/budget_adjustment/keyword_harvesting
    source: str                // pms/manual
    suggested_actions: List[Action]
    status: OptimizationStatus
    experiment_id: Optional[str]
    before_values: Dict
    after_values: Optional[Dict]
    pms_reason: str
    can_rollback: bool
    executed_at: Optional[datetime]

class AdBidAdjustmentEngine:
    def execute(self, optimization: AdOptimization) -> None:
        pass
    def rollback(self, optimization: AdOptimization) -> None:
        pass

class ExperimentService:
    def create_experiment(self, campaign_id: str, variants: List[Dict]) -> str:
        pass
    def get_experiment_result(self, experiment_id: str) -> ExperimentResult:
        pass
```

#### 4.11.2 核心接口

| 方法 | 路径                                               | 说明            |
| ---- | -------------------------------------------------- | --------------- |
| GET  | `/ads/api/v1/optimization/suggestions`             | 获取优化建议    |
| POST | `/ads/api/v1/optimization/budget-allocate`         | 预算分配        |
| GET  | `/ads/api/v1/optimization/performance`             | 获取效果分析    |
| POST | `/ads/api/v1/optimization/pms/execute`             | 执行PMS优化指令 |
| POST | `/ads/api/v1/optimization/pms/{logId}/rollback`    | 回滚PMS操作     |
| POST | `/ads/api/v1/optimization/experiments`             | 创建A/B测试     |
| GET  | `/ads/api/v1/optimization/experiments/{id}/result` | 获取A/B测试结果 |

---

### 4.12 成本归集引擎

**设计概述**  
将各业务域产生的成本行为沉淀为标准成本事件，实现成本事件化归集、成本分解、成本分摊。采用**事件溯源**模式。

**推荐开源**：Apache Flink、Event Store、ClickHouse

#### 4.12.1 数据流架构

```
各域业务操作 → Outbox发布CostEvent → Kafka → Flink聚合 → 成本分解表(ClickHouse) → 利润引擎
                                ↓
                         原始事件存储(PostgreSQL，只增不改)
```

#### 4.12.2 8类成本事件

| 序号 | 成本事件类型   | 说明                            | 来源域      | 对应枚举            |
| ---- | -------------- | ------------------------------- | ----------- | ------------------- |
| 1    | PURCHASE       | 商品采购成本                    | SCM         | purchase_order      |
| 2    | LOGISTICS      | 头程物流成本                    | TMS         | shipment            |
| 3    | STORAGE        | 仓储与操作费                    | WMS/FBA     | warehouse_fee       |
| 4    | PLATFORM       | 平台佣金                        | 平台账单    | platform_commission |
| 5    | ADVERTISING    | 广告成本                        | ADS         | ad_campaign         |
| 6    | PAYMENT        | 支付手续费                      | 支付账单    | payment_fee         |
| 7    | TAIL_LOGISTICS | 尾程物流成本                    | TMS         | tail_freight        |
| 8    | OTHER          | 退款损耗/补发成本/汇兑损益/税费 | OMS/CRM/FMS | other               |

#### 4.12.3 核心聚合

```java
@dataclass
class CostEvent:
    event_id: str
    tenant_id: str
    event_type: CostEventType
    source_type: str
    source_id: str
    amount: Decimal
    currency: str
    exchange_rate: Optional[Decimal]
    amount_cny: Optional[Decimal]
    sku: Optional[str]
    asin: Optional[str]
    order_id: Optional[str]
    business_date: date
    settled: bool
    raw_data: Optional[Dict]
    created_at: datetime

class CostBreakdown:
    cost_id: str
    tenant_id: str
    asin: str
    sku: str
    period: str
    bom_cost: Decimal
    shipping_cost: Decimal
    fba_fees: Decimal
    tariff: Decimal
    advertising_cost: Decimal
    storage_cost: Decimal
    return_cost: Decimal
    labor_cost: Decimal
    other_costs: Decimal
    total_cost: Decimal
    currency: str
    created_at: datetime
```

#### 4.12.4 核心接口

| 方法 | 路径                                         | 说明             |
| ---- | -------------------------------------------- | ---------------- |
| POST | `/fms/api/v1/cost-engine/events/collect`     | 触发成本事件采集 |
| GET  | `/fms/api/v1/cost-engine/events`             | 查询成本事件     |
| POST | `/fms/api/v1/cost-engine/breakdown/generate` | 生成成本分解     |
| POST | `/fms/api/v1/cost-engine/allocate`           | 执行成本分摊     |
| POST | `/fms/api/v1/cost-engine/fifo/calculate`     | FIFO成本计算     |
| GET  | `/fms/api/v1/cost-engine/trend`              | 获取成本趋势     |
| POST | `/fms/api/v1/cost-engine/ai/detect-anomaly`  | AI成本异常检测   |

---

### 4.13 利润核算引擎

**设计概述**  
基于成本事件和收入数据，实现多维度利润核算、利润分析、利润预警。

**推荐开源**：Apache Airflow、ClickHouse、Apache Kylin

#### 4.13.1 核心聚合

```java
@dataclass
class ProfitStatement:
    statement_id: str
    tenant_id: str
    asin: str
    sku: str
    period: str
    revenue: Decimal
    refund_amount: Decimal
    net_revenue: Decimal
    total_cost: Decimal
    gross_profit: Decimal
    gross_margin: float
    operating_expenses: Decimal
    net_profit: Decimal
    net_margin: float
    roi: float
    units_sold: int
    avg_selling_price: Decimal
    currency: str
    dimension: Optional[Dict]

    def calculate_metrics(self) -> None:
        self.net_revenue = self.revenue - self.refund_amount
        self.gross_profit = self.net_revenue - self.total_cost
        self.gross_margin = float(self.gross_profit / self.net_revenue) if self.net_revenue > 0 else 0
        self.net_profit = self.gross_profit - self.operating_expenses
        self.net_margin = float(self.net_profit / self.net_revenue) if self.net_revenue > 0 else 0
        self.roi = float(self.net_profit / self.total_cost) if self.total_cost > 0 else 0
        self.avg_selling_price = self.net_revenue / self.units_sold if self.units_sold > 0 else Decimal('0')
```

#### 4.13.2 利润分析维度

| 维度         | 说明                | 核算方式         |
| ------------ | ------------------- | ---------------- |
| **订单利润** | 按订单维度核算      | 事件驱动实时计算 |
| **SKU利润**  | 按SKU/ASIN维度核算  | 定时批处理汇总   |
| **店铺利润** | 按店铺维度核算      | 定时批处理汇总   |
| **渠道利润** | 按渠道/市场维度核算 | 定时批处理汇总   |
| **人员利润** | 按运营人员维度核算  | 定时批处理汇总   |

#### 4.13.3 核心接口

| 方法 | 路径                                              | 说明           |
| ---- | ------------------------------------------------- | -------------- |
| POST | `/fms/api/v1/profit-engine/calculate`             | 触发利润核算   |
| GET  | `/fms/api/v1/profit-engine/order/{id}`            | 获取订单利润   |
| GET  | `/fms/api/v1/profit-engine/sku/{asin}`            | 获取SKU利润    |
| GET  | `/fms/api/v1/profit-engine/store`                 | 获取店铺利润   |
| GET  | `/fms/api/v1/profit-engine/channel`               | 获取渠道利润   |
| GET  | `/fms/api/v1/profit-engine/market`                | 获取市场利润   |
| GET  | `/fms/api/v1/profit-engine/dashboard`             | BI看板聚合数据 |
| POST | `/fms/api/v1/profit-engine/custom-expense/import` | 导入自定义费用 |
| GET  | `/fms/api/v1/profit-engine/alert`                 | 获取利润预警   |
| POST | `/fms/api/v1/profit-engine/ai/analyze`            | AI利润分析     |

---

### 4.14 进销存凭证引擎

**设计概述**  
基于业务单据自动生成会计分录，支持进销存凭证管理、对接金蝶/用友等财务系统。

**推荐开源**：Odoo、Frappe、Apache Kafka（事件驱动）

#### 4.14.1 核心模型

```java
@dataclass
class VoucherTemplate:
    template_id: str
    tenant_id: str
    business_type: str
    debit_account: str
    credit_account: str
    description_template: str
    is_active: bool
    created_at: datetime

@dataclass
class JournalEntry:
    entry_id: str
    tenant_id: str
    entry_type: str
    voucher_no: str
    period: str
    debit_account: str
    debit_amount: Decimal
    credit_account: str
    credit_amount: Decimal
    ref_type: str
    ref_id: str
    status: str
    created_at: datetime
    exported_at: Optional[datetime]
```

#### 4.14.2 60+业务场景支持

| 场景类别 | 典型场景                                 | 触发事件              |
| -------- | ---------------------------------------- | --------------------- |
| 采购入库 | 市场采购入库、工厂采购入库、1688采购入库 | purchase.received     |
| 销售出库 | 自发货出库、FBA出库、海外仓出库          | order.shipped         |
| 退货入库 | 买家退货入库、质检退回入库               | return.inbound        |
| 盘盈盘亏 | 盘点盈入、盘点亏出                       | inventory.adjusted    |
| 调拨     | 本地仓→FBA、FBA→海外仓                   | inventory.transferred |
| 其他     | 办公借用、美工拍照、拍卖、销毁           | inventory.movement    |

#### 4.14.3 核心接口

| 方法 | 路径                                          | 说明                     |
| ---- | --------------------------------------------- | ------------------------ |
| POST | `/fms/api/v1/voucher-engine/auto-generate`    | 自动生成凭证（事件驱动） |
| GET  | `/fms/api/v1/voucher-engine/entries`          | 查询会计分录             |
| GET  | `/fms/api/v1/voucher-engine/summary`          | 凭证汇总                 |
| POST | `/fms/api/out/v1/voucher-engine/push-kingdee` | 推送金蝶                 |
| POST | `/fms/api/out/v1/voucher-engine/push-yonyou`  | 推送用友                 |
| POST | `/fms/api/v1/voucher-engine/{id}/approve`     | 审核凭证                 |
| GET  | `/fms/api/v1/voucher-engine/inventory-cost`   | 获取库存成本核算结果     |

---

## 5. 技术能力中台详细设计

### 5.1 消息通知中心

**推荐开源**：Apache RocketMQ、Kafka、Sentinel

| 方法 | 路径                                      | 说明         |
| ---- | ----------------------------------------- | ------------ |
| POST | `/sys/api/v1/notification/send`           | 发送通知     |
| GET  | `/sys/api/v1/notification/templates`      | 获取通知模板 |
| POST | `/sys/api/v1/notification/templates`      | 创建通知模板 |
| GET  | `/sys/api/v1/notification/history`        | 获取发送记录 |
| POST | `/sys/api/v1/notification/history/resend` | 重发失败消息 |

### 5.2 文件处理中心

**推荐开源**：Apache Tika、Apache Camel

| 方法   | 路径                             | 说明                 |
| ------ | -------------------------------- | -------------------- |
| POST   | `/sys/api/v1/file/upload`        | 上传文件（支持分片） |
| GET    | `/sys/api/v1/file/{id}/download` | 下载文件             |
| GET    | `/sys/api/v1/file/{id}/preview`  | 预览文件（缩略图）   |
| DELETE | `/sys/api/v1/file/{id}`          | 删除文件             |
| POST   | `/sys/api/v1/file/process`       | 触发异步处理         |

### 5.3 工作流引擎

**推荐开源**：Apache Camunda、Flowable、Imixs-Workflow

| 方法 | 路径                                       | 说明         |
| ---- | ------------------------------------------ | ------------ |
| POST | `/sys/api/v1/workflow/definitions/deploy`  | 部署BPMN文件 |
| POST | `/sys/api/v1/workflow/instances`           | 启动流程实例 |
| GET  | `/sys/api/v1/workflow/instances/{id}`      | 获取流程状态 |
| POST | `/sys/api/v1/workflow/tasks/{id}/complete` | 完成任务节点 |

### 5.4 任务调度中心

**推荐开源**：Apache Airflow、Argo Workflows、Kestra、XXL-JOB

| 方法 | 路径                                      | 说明         |
| ---- | ----------------------------------------- | ------------ |
| POST | `/sys/api/v1/scheduler/jobs`              | 创建定时任务 |
| GET  | `/sys/api/v1/scheduler/jobs/{id}/logs`    | 获取执行日志 |
| POST | `/sys/api/v1/scheduler/jobs/{id}/trigger` | 手动触发执行 |

### 5.5 权限管理中心

**推荐开源**：Keycloak、Apache ShardingSphere、Sentinel

| 方法 | 路径                             | 说明             |
| ---- | -------------------------------- | ---------------- |
| GET  | `/iam/api/v1/auth/check`         | 权限校验         |
| POST | `/iam/api/v1/auth/data-scope`    | 管理数据权限维度 |
| POST | `/iam/api/v1/object-permissions` | 设置对象级权限   |

### 5.6 日志审计中心

**推荐开源**：Event Store、Apache Kafka

| 方法 | 路径                               | 说明                    |
| ---- | ---------------------------------- | ----------------------- |
| POST | `/sys/api/v1/audit/log`            | 记录审计日志            |
| GET  | `/sys/api/v1/audit/logs`           | 查询审计日志            |
| GET  | `/sys/api/v1/audit/logs/{traceId}` | 按traceId查询全链路日志 |

### 5.7 API网关

**推荐开源**：Kong、Spring Cloud Gateway、Nacos、Sentinel

双层网关：Kong（外部） → SCG（内部），支持动态路由、灰度发布、限流熔断。

### 5.8 多语言翻译中心

**推荐开源**：Apache ESI、Google/DeepL API

| 方法 | 路径                                | 说明       |
| ---- | ----------------------------------- | ---------- |
| POST | `/sys/api/v1/translation/translate` | 翻译文本   |
| GET  | `/sys/api/v1/translation/glossary`  | 获取术语库 |

### 5.9 数据脱敏中心

**推荐开源**：Apache ShardingSphere

| 方法 | 路径                        | 说明         |
| ---- | --------------------------- | ------------ |
| POST | `/sys/api/v1/masking/mask`  | 数据脱敏     |
| GET  | `/sys/api/v1/masking/rules` | 获取脱敏规则 |

### 5.10 API管理平台

**推荐开源**：Swagger/OpenAPI、Knife4j、Nacos

### 5.11 连接器管理平台

**推荐开源**：Apache Camel、Nacos

| 方法 | 路径                                              | 说明               |
| ---- | ------------------------------------------------- | ------------------ |
| GET  | `/sys/api/v1/connector/platforms`                 | 获取平台连接器列表 |
| POST | `/sys/api/v1/connector/platforms/{type}/register` | 注册平台连接器     |
| GET  | `/sys/api/v1/connector/health`                    | 连接器健康检查     |

---

## 6. 中台集成与数据流转规范

- API路径规范：`/{service-name}/api/v1/...`
- 事件主题规范：`erp.{domain}.{event-type}.v1`，Avro序列化
- Outbox模式保证最终一致性
- CDC同步：Canal → Kafka → Flink → ClickHouse/ES/Redis
- 批量同步：Spring Batch + XXL-JOB

---

## 7. 高可用与容灾设计

| 组件       | 高可用方案               | 最小节点 | RPO       | RTO   |
| ---------- | ------------------------ | -------- | --------- | ----- |
| PostgreSQL | Patroni主从+自动Failover | 3        | 0（同步） | <30s  |
| Redis      | 哨兵模式                 | 3        | <1s       | <15s  |
| Kafka      | 3 Broker+副本因子3       | 3        | 0         | <10s  |
| ES         | 3节点集群+副本分片       | 3        | 0         | <30s  |
| MinIO      | 4节点纠删码              | 4        | 0         | <30s  |
| 中台服务   | 多副本+HPA自动扩缩       | 2+       | 0         | <5min |

关键路径集成Sentinel，热点数据本地Caffeine缓存兜底。

备份策略：PG全量每日+WAL持续，RDB每6小时+AOF实时，MinIO跨区域实时复制。

---

## 8. 扩展性与插件化机制

- SPI扩展：支付渠道、物流商、汇率源、连接器等
- 策略模式：订单/物流/计费/广告策略动态配置
- 脚本引擎：Groovy/QLExpress/Aviator表达式热加载
- 事件驱动：新业务订阅相关Topic即可加入工作流

---

## 9. 开源项目映射与选型建议

### 9.1 开源项目评价总表

| 模块分类 | 项目名称                   | 所属组织      | Star数 | 语言       | 上手难度 | 社区活跃度 | 性能 | 协议       | 商业风险 | 适用场景                  |
| -------- | -------------------------- | ------------- | ------ | ---------- | -------- | ---------- | ---- | ---------- | -------- | ------------------------- |
| 中台业务 | Ant Financial mid-platform | Ant Financial | 50k+   | Java/Scala | ⭐⭐⭐⭐☆    | ⭐⭐⭐⭐☆      | 高   | 不公开     | 高       | 中台事件驱动参考          |
| 中台业务 | mall-cloud                 | macrozheng    | 14k+   | Java       | ⭐⭐⭐☆☆    | ⭐⭐⭐☆       | 中高 | MIT        | 低       | DDD+微服务分层            |
| 中台业务 | Apache ShardingSphere      | Apache        | 16k+   | Java       | ⭐⭐⭐☆☆    | ⭐⭐⭐⭐☆      | 高   | Apache-2.0 | 低       | 多租户/数据权限           |
| 中台业务 | Metasfresh                 | metasfresh    | 4k+    | Java       | ⭐⭐⭐☆☆    | ⭐⭐☆☆☆      | 中   | GPLv2      | 中等     | ERP后端（库存/订单/财务） |
| 中台业务 | Spree Commerce             | spree         | 8k+    | Ruby       | ⭐⭐☆☆☆    | ⭐⭐☆☆☆      | 中   | MIT        | 低       | Headless电商（商品/订单） |
| 技术中台 | Supabase                   | supabase      | 101k+  | TypeScript | ⭐⭐☆☆☆    | ⭐⭐⭐☆☆      | 高   | Apache-2.0 | 低       | 后端BaaS，中台基础框架    |
| 技术中台 | awesome-oss-saas           | vihar         | 3k+    | -          | ⭐⭐☆☆☆    | ⭐⭐☆☆☆      | 中   | MIT        | 低       | SaaS工具集合参考          |
| 技术中台 | open-source-saas           | toolworks-dev | 2k+    | -          | ⭐⭐☆☆☆    | ⭐⭐☆☆☆      | 中   | MIT        | 低       | SaaS项目集合参考          |
| 工作流   | Apache Airflow             | Apache        | 30k+   | Python     | ⭐⭐⭐☆☆    | ⭐⭐⭐⭐☆      | 高   | Apache-2.0 | 低       | 批处理/数据管道/调度      |
| 工作流   | Argo Workflows             | argoproj      | 16.6k+ | Go         | ⭐⭐⭐☆☆    | ⭐⭐⭐☆☆      | 高   | Apache-2.0 | 低       | 云原生容器任务编排        |
| 工作流   | Kestra                     | kestra-io     | 5k+    | Java       | ⭐⭐⭐☆☆    | ⭐⭐⭐☆☆      | 高   | Apache-2.0 | 低       | 工作流编排引擎            |
| 工作流   | Imixs-Workflow             | imixs         | 1k+    | Java       | ⭐⭐⭐☆☆    | ⭐⭐☆☆☆      | 中   | Apache-2.0 | 低       | BPMN工作流引擎            |
| 自动化   | n8n                        | n8n-io        | 186k+  | TypeScript | ⭐⭐☆☆☆    | ⭐⭐⭐⭐☆      | 高   | Fair-code  | 中等     | 可视化工作流自动化        |
| 自动化   | Activepieces               | Activepieces  | 21k+   | TypeScript | ⭐⭐☆☆☆    | ⭐⭐☆☆☆      | 中   | MIT        | 低       | 无代码自动化引擎          |
| 自动化   | Flowise                    | FlowiseAI     | 2k+    | TypeScript | ⭐⭐☆☆☆    | ⭐⭐☆☆☆      | 中   | MIT        | 低       | AI工作流工具              |
| 自动化   | ToolJet                    | ToolJet       | 25k+   | TypeScript | ⭐⭐☆☆☆    | ⭐⭐⭐☆       | 中   | Apache-2.0 | 低       | 低代码内部工具平台        |
| 自动化   | Hoppscotch                 | hoppscotch    | 25k+   | TypeScript | ⭐⭐☆☆☆    | ⭐⭐⭐☆       | 中   | MIT        | 低       | API联调/测试/自动化       |
| 自动化   | middleware                 | middlewarehq  | 1k+    | JavaScript | ⭐⭐☆☆☆    | ⭐⭐☆☆☆      | 中   | MIT        | 低       | 工程性能度量、DORA指标    |

### 9.2 选型原则与建议

1. **协议优先**：尽量选择 Apache-2.0 或 MIT 协议的项目，避免 GPL 传染风险。
2. **社区活跃**：Star数>5k、近期有持续更新的项目优先。
3. **技术栈匹配**：后端优先 Java 生态（Spring Cloud），数据管道可考虑 Python/Go。
4. **业务中台**：参考 mall-cloud 的 DDD 分层、Metasfresh 的库存/财务模块，ShardingSphere 处理多租户隔离。
5. **工作流**：轻量审批用 Flowable，批处理/数据管道用 Apache Airflow，云原生编排用 Argo Workflows。
6. **自动化**：内部集成用 n8n/ToolJet 快速构建，API 自动化测试用 Hoppscotch。
7. **PMS 闭环**：AI 建议仅作推荐，ERP 审批后执行，使用事件驱动（Kafka）回流结果。

---

## 10. 可视化架构图参考

本项目提供了 **ERP中台模块与开源项目可视化架构图**（PNG 图片），展示了 14 业务域 + 14 业务中台 + 11 技术中台 + PMS 闭环的完整布局，并标注了每个模块推荐的开源项目。建议作为中台设计文档的直观参考，用于规划模块依赖、事件流、数据流和技术选型。

架构图文件路径：`ERP中台模块与开源项目可视化架构图.png`

---

> **文档结束**
>
> 本文档基于 V11 详细设计、V4 需求规格、V4 技术实现方案，结合开源社区最佳实践和参考表格，构建了完整的跨境电商 ERP 系统中台详细设计。各中台模块独立演进，通过标准化接口、事件驱动和插件体系，支撑跨境业务的灵活扩展和高效运营。