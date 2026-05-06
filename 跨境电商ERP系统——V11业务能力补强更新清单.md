# 跨境电商ERP系统——V11详细设计说明书业务能力补强更新清单

> 基于原始需求规格说明书V3、需求分析说明书、详细设计说明书V3/V51/V6/V7等文档，与V11详细设计说明书逐域对比，识别并补齐98项缺失业务能力。

---

## 一、更新总览

| 维度 | 更新内容 | 涉及范围 |
|---|---|---|
| 领域概述+核心功能 | 14域职责描述扩展、核心实体补充、功能模块表新增 | 全部14域 |
| 数据库表 | 新增30+张数据库表 | 全部14域 |
| API接口 | 新增80+个API接口 | 全部14域 |
| 实体关系图 | IAM域新增Position、ObjectPermission实体 | IAM域 |

---

## 二、各域补强明细

### 2.1 工作台域 (DASHBOARD) ★[AI看板]

| 序号 | 补强项 | 原始状态 | 补强内容 | 来源文档 |
|---|---|---|---|---|
| 1 | 常用功能入口 | 缺失 | 用户自定义快捷方式、高频功能一键直达、最近访问记录 | 需求规格V3 |
| 2 | 工作流通知聚合 | 缺失 | 审批通知、异常告警、库存预警、订单异常、授权失效等多类型工作流通知统一聚合展示，按紧急程度分级 | 需求规格V3 |
| 3 | 个人中心扩展 | 简略 | 补充仓库人员KPI、开发人员KPI等 | 需求规格V3 |
| 4 | 电子日历扩展 | 简略 | 新增促销日历、备货日历 | 需求规格V3 |

**新增API接口**：4个（常用功能入口GET/PUT、工作流通知聚合GET、日历GET）

---

### 2.2 组织权限域 (IAM)

| 序号 | 补强项 | 原始状态 | 补强内容 | 来源文档 |
|---|---|---|---|---|
| 5 | 岗位管理 | 缺失 | 岗位定义、岗位层级、岗位与部门关联、岗位人员分配 | 需求规格V3、设计V7 |
| 6 | 对象级权限 | 缺失 | 产品/Listing/广告等业务对象级别的精细权限控制，支持用户自定义产品角色（查看全部或仅与自己相关的产品） | 需求规格V3、设计V51 |

**新增核心实体**：Position、ObjectPermission

**新增数据库表**：positions、object_permissions（users表新增position_id字段）

**新增API接口**：5个（岗位CRUD、对象级权限设置/查询）

**实体关系图更新**：User实体新增position_id字段，新增Position和ObjectPermission两个实体框

---

### 2.3 产品开发域 (PDM) ★[AI选品]

| 序号 | 补强项 | 原始状态 | 补强内容 | 来源文档 |
|---|---|---|---|---|
| 7 | 1688选品铺货 | 缺失 | 1688精选货源一键铺货、1688产品采集与选品对接 | 需求规格V3 |
| 8 | 出单产品模式 | 缺失 | 先上架销售、出单后再关联或生成SKU的出单产品模式 | 需求规格V3 |
| 9 | 供应商平台新品推荐 | 缺失 | 供应商在供应商平台推送新品供卖家选品 | 需求规格V3 |
| 10 | 品牌管理 | 缺失 | 品牌信息维护、品牌授权管理、刊登时快速选择品牌 | 需求规格V3 |
| 11 | UPC管理 | 缺失 | 导入UPC/EAN码池，刊登时自动调用未使用码 | 需求规格V3 |
| 12 | 敏感词库 | 缺失 | 维护各平台敏感词，文案编辑或刊登时自动提醒 | 需求规格V3 |
| 13 | 标题库/图片库 | 缺失 | 维护标题库和图片库，刊登时系统自动随机调用 | 需求规格V3 |
| 14 | 组合产品管理 | 缺失 | 支持加工、组合(Bundle)、变体产品管理 | 需求规格V3 |
| 15 | 包材管理 | 缺失 | 维护物流包材成本，发货后自动计入订单利润 | 需求规格V3 |
| 16 | 自定义物流属性 | 缺失 | 自定义物流属性，用于订单审核规则匹配运输方式 | 需求规格V3 |
| 17 | 平台产品限价 | 缺失 | 防止多店铺同站点内部价格战，设置平台产品限价 | 需求规格V3 |
| 18 | 产品权限控制 | 缺失 | 用户自定义产品角色（数据权限），可查看全部或仅与自己相关的产品 | 需求规格V3 |
| 19 | 产品问题记录 | 缺失 | 汇集质检、客服售后、评论中记录的质量问题，支持分析优化 | 需求规格V3 |
| 20 | 开发人员KPI | 缺失 | 开发人员开发SKU数、开发效率、产品出单率、销售总利润、KPI管理和提成计算 | 需求规格V3 |

**新增核心实体**：Brand、UPCPool、SensitiveWord、TitleLibrary、ImageLibrary、BundleProduct、PackagingMaterial、ProductIssue、PlatformPriceLimit

**新增数据库表**：brands、upc_pool、sensitive_words、title_library、image_library、bundle_products、packaging_materials、product_issues、platform_price_limits、developer_kpis（products表新增logistics_attrs字段）

**新增API接口**：18个

---

### 2.4 销售运营域 (SOM)

| 序号 | 补强项 | 原始状态 | 补强内容 | 来源文档 |
|---|---|---|---|---|
| 21 | Listing定时上下架 | 缺失 | 自动定时上下架、手动批量下架/删除 | 需求规格V3 |
| 22 | Listing价格计算器 | 缺失 | 使用价格计算器自动计算售价，综合考虑成本、佣金、运费、利润率 | 需求规格V3 |
| 23 | Buybox采集与调价 | 缺失 | 采集Buybox信息，自动调价提升Buybox获得率 | 需求规格V3 |
| 24 | 跟卖监控 | 缺失 | 跟卖监控与提醒，自动检测跟卖者并通知 | 需求规格V3 |
| 25 | 平衡库存策略 | 缺失 | 修改可售数，使用平衡库存策略，自动补货和平衡库存 | 需求规格V3 |
| 26 | 账号健康监控 | 缺失 | 多平台账号健康安全状态同步监控，异常告警 | 需求规格V3 |
| 27 | 销售小组 | 缺失 | 销售小组设置，便于分组管理和统计业绩 | 需求规格V3 |
| 28 | Reviews同步 | 缺失 | 通过Reviews发现产品问题，记录质量投诉归类，与PDM质量问题联动 | 需求规格V3 |
| 29 | 滞销品清库 | 缺失 | 为滞销品设置降价清库计划，自动执行清库策略 | 需求规格V3 |
| 30 | 刊登效率报表 | 缺失 | 统计刊登效率和出单效果报表，优化刊登策略 | 需求规格V3 |

**新增核心实体**：BuyboxMonitor、HijackAlert、SalesTeam、ListingPriceCalculator

**新增数据库表**：listing_price_calculators、buybox_monitors、hijack_alerts、sales_teams（listings表新增auto_schedule字段）

**新增API接口**：11个

---

### 2.5 广告管理域 (ADS) ★[AI优化]

| 序号 | 补强项 | 原始状态 | 补强内容 | 来源文档 |
|---|---|---|---|---|
| 31 | 否定关键词管理 | 简略 | 关键词管理扩展否定关键词管理（避免无效点击） | 需求规格V3 |
| 32 | 搜索词提炼 | 缺失 | 从用户搜索词报告中提炼优质搜索词添加到广告活动，自动发现高转化搜索词 | 需求规格V3 |
| 33 | 广告仪表盘 | 缺失 | 仪表盘多维度查看广告活动表现和概况，支持按活动/组/关键词/ASIN维度 | 需求规格V3 |
| 34 | 分组分析视图 | 简略 | 广告活动自由组合管理扩展分组分析视图 | 需求规格V3 |

**新增API接口**：已包含在ADS域A/B/C类接口中

---

### 2.6 订单域 (OMS) ★[AI风控]

| 序号 | 补强项 | 原始状态 | 补强内容 | 来源文档 |
|---|---|---|---|---|
| 35 | 订单三层分离 | 缺失 | 渠道订单（保留原文）、ERP销售单、履约单/包裹单三层分离，各层独立管理 | 需求规格V3、设计V51 |
| 36 | 批量导入订单 | 缺失 | 无API渠道的批量导入订单 | 需求规格V3 |
| 37 | 拆单/合单 | 缺失 | 订单拆单、合单、有货先发等操作 | 需求规格V3 |
| 38 | 物流申报规则 | 缺失 | 物流申报规则自动申报并获取追踪号 | 需求规格V3 |
| 39 | 平台标记发货 | 缺失 | 平台自动标记发货规则，系统自动将追踪号上传到平台 | 需求规格V3 |
| 40 | 插头国标规则 | 缺失 | 按收货国家自动匹配对应插头规格SKU | 需求规格V3 |
| 41 | 分拣口设置 | 缺失 | 配合智能分拣硬件自动按物流渠道分拣 | 需求规格V3 |
| 42 | 运输统计 | 缺失 | 自动统计发货包裹数和运费，比照估算运费与实际运费差异 | 需求规格V3 |
| 43 | 自定义发票 | 缺失 | 自定义发票设置，配置打印模板 | 需求规格V3 |

**新增核心实体**：ChannelOrder、SalesOrder、FulfillmentOrder、Package

**新增数据库表**：channel_orders、sales_orders、fulfillment_orders、packages、order_split_rules、logistics_declaration_rules、platform_ship_rules、plug_adapter_rules、sorting_ports、custom_invoice_templates（原orders表拆分为4层）

**新增API接口**：13个

---

### 2.7 供应链域 (SCM) ★[AI补货]

| 序号 | 补强项 | 原始状态 | 补强内容 | 来源文档 |
|---|---|---|---|---|
| 44 | 5种采购模式 | 缺失 | 市场采购、工厂采购、天猫淘宝网络采购、1688采购、采购原料自主加工 | 需求规格V3 |
| 45 | 1688采购 | 缺失 | 真实在系统内操作1688下单付款；支持自动下单和批量付款 | 需求规格V3 |
| 46 | 采购审核流程 | 缺失 | 按采购类型设置多级下单和付款审核条件，支持自动审核 | 需求规格V3 |
| 47 | 采购合同 | 缺失 | 打印采购合同，合同模板可自定义 | 需求规格V3 |
| 48 | 供应商平台 | 缺失 | 供应商线上接单、回复备货、打印采购条码 | 需求规格V3 |
| 49 | 下单产品跟单 | 缺失 | 可视化跟单，汇总下单、收货、质检、入库、不良品退回数量 | 需求规格V3 |
| 50 | 销单入库核对 | 缺失 | 查看每次销单质检入库详情及时间 | 需求规格V3 |
| 51 | 本地仓备货分析 | 缺失 | 根据平台自发货销量、本地库存、采购在途，按规则计算补货建议，一键生成本地仓备货计划单 | 需求规格V3 |
| 52 | FBA补货建议 | 缺失 | 多种业务场景，自定义日均销量、备货时效、销量去噪规则；计算建议采购量、发货日 | 需求规格V3 |
| 53 | 海外仓备货分析 | 缺失 | 根据全平台销量、海外仓库存、本地仓库存、在途数量，按备货规则计算补货数量 | 需求规格V3 |
| 54 | Temu/Shein供应商模式 | 缺失 | 支持Temu、Shein、Amazon供应商模式下的备货单、采购、库存管理 | 需求规格V3 |
| 55 | 供应商平台新品推荐 | 缺失 | 供应商在供应商平台推送新品供卖家选品 | 需求规格V3 |

**新增核心实体**：SupplierPlatform、PurchaseTracking、RestockPlan、PurchaseContract

**新增数据库表**：purchase_contracts、purchase_tracking、purchase_writeoffs、restock_plans、supplier_platform_products（purchase_orders表purchase_type字段扩展枚举值）

**新增API接口**：11个

---

### 2.8 仓储域 (WMS) ★[AI预测]

| 序号 | 补强项 | 原始状态 | 补强内容 | 来源文档 |
|---|---|---|---|---|
| 56 | 库存事务账 | 缺失 | 每次库存变化基于业务来源记录流水，包含在手、预占、可售、在途、不良库存五类状态 | 需求规格V3、设计V51 |
| 57 | 销单管理 | 缺失 | 根据收货数据销单或扫描快递单号快速销单，打印进货单和采购下单条码 | 需求规格V3 |
| 58 | 库存变动场景扩展 | 简略 | 办公借用、美工拍照、拍卖、销毁、盘盈入库等场景 | 需求规格V3 |
| 59 | 不良品退货管理 | 缺失 | 按采购回复处理（入库、作废、退货），线上流程化管理 | 需求规格V3 |
| 60 | 产品返修管理 | 缺失 | 入库后发现不良品申请返修，出库、维修、重新质检入库全流程 | 需求规格V3 |
| 61 | 供应商回购/换货 | 缺失 | 滞销品退货退款，仅换货不退款处理 | 需求规格V3 |
| 62 | 出货质检管理 | 缺失 | 拣货后打包前再检，记录发货前不良品 | 需求规格V3 |
| 63 | PDA移动作业 | 缺失 | PDA扫描条码质检、PDA移动盘点、PDA入库操作 | 需求规格V3 |
| 64 | 自定义标签模板 | 缺失 | 产品条码、采购条码、拣货单模板、包裹发票等自定义打印模板 | 需求规格V3 |
| 65 | FBA库存管理 | 缺失 | 查看EFN/NARF库存和库龄，统一多仓库存视图 | 需求规格V3 |
| 66 | Removal订单 | 缺失 | FBA退回/销毁/清货处理 | 需求规格V3 |
| 67 | 库位分组统计 | 缺失 | 统计各型号库位占用比例，优化空间利用 | 需求规格V3 |

**新增核心实体**：InventoryTransaction、WriteOffOrder、DefectiveReturn、ProductRepair、OutboundQC、RemovalOrder、LabelTemplate

**新增数据库表**：inventory_transactions、writeoff_orders、outbound_qc_records、defective_returns、product_repairs、removal_orders、label_templates（inventories表字段扩展：on_hand_qty/in_transit_qty；storage_locations新增location_group）

**新增API接口**：13个

---

### 2.9 FBA/海外仓域 (FBA)

| 序号 | 补强项 | 原始状态 | 补强内容 | 来源文档 |
|---|---|---|---|---|
| 68 | FBA补货建议 | 缺失 | 自定义日均销量、备货时效、销量去噪规则；计算建议采购量、本地发FBA量、海外仓发FBA量、发货日 | 需求规格V3 |
| 69 | 备货购物车 | 缺失 | 加入备货购物车确认后一键生成FBA货件计划单 | 需求规格V3 |
| 70 | 三种FBA备货计划创建模式 | 缺失 | 同步Amazon后台/加载shipmentID/先创建无ID计划 | 需求规格V3 |
| 71 | 发货打包SOP | 缺失 | 扫描SKU查看特殊包装要求，按SOP流程打包 | 需求规格V3 |
| 72 | 智能设备集成 | 缺失 | 支持电子秤传输、智能设备自动称重量体积 | 需求规格V3 |
| 73 | FBA头程异常处理 | 缺失 | 对接Amazon运输中以及仓内破损/丢失/退货/移除/共享库存五大异常场景，自动生成头程批次 | 需求规格V3 |

**新增核心实体**：FBARestockSuggestion、RestockCart、FBAShipmentException

**新增数据库表**：fba_restock_suggestions、restock_carts、fba_shipment_exceptions（fba_inbound_plans新增create_mode字段；fba_inventories新增efn_qty/narf_qty/age_days字段）

**新增API接口**：8个

---

### 2.10 物流域 (TMS)

| 序号 | 补强项 | 原始状态 | 补强内容 | 来源文档 |
|---|---|---|---|---|
| 74 | 发货后修改运费 | 缺失 | 发货后录入实际运费，精准核算成本 | 需求规格V3 |
| 75 | 物流严选 | 缺失 | 展示优质跨境物流服务商，提供多样选择 | 需求规格V3 |
| 76 | 轨迹跟踪扩展 | 简略 | 对接2000+物流渠道 | 需求规格V3 |
| 77 | 绩效分析扩展 | 简略 | 上网时效分析、物流商对比分析 | 需求规格V3 |

**新增数据库表**：carrier_performance（shipments表新增estimated_cost/actual_cost字段；carriers表新增featured字段；trackings表新增channel字段）

**新增API接口**：3个

---

### 2.11 客服售后域 (CRM) ★[AI情感]

| 序号 | 补强项 | 原始状态 | 补强内容 | 来源文档 |
|---|---|---|---|---|
| 78 | 邮件智能分配 | 缺失 | 邮件规则：智能分配客服，责任到人，权限分明 | 需求规格V3 |
| 79 | 假期自动回复 | 缺失 | 假期自动回复设置，支持自定义假期时间段和回复内容 | 需求规格V3 |
| 80 | 请求评论 | 缺失 | 自动排除退款/差评订单，多维度筛选请求评论 | 需求规格V3 |
| 81 | 邮件营销 | 缺失 | 自动推广邮件、邮件营销活动管理 | 需求规格V3 |
| 82 | 新人回复审核 | 缺失 | 新人回复审核流程，确保服务质量 | 需求规格V3 |
| 83 | 移动端处理 | 缺失 | 移动端处理邮件，随时随地响应客户 | 需求规格V3 |
| 84 | 退款报告 | 缺失 | API接口处理退款，自动生成退款报告 | 需求规格V3 |
| 85 | 质量问题记录 | 缺失 | 与PDM/WMS质量问题联动，记录并归类产品质量问题 | 需求规格V3 |

**新增核心实体**：EmailRule、ReviewRequest、QualityIssue

**新增数据库表**：email_rules、vacation_auto_replies、review_requests、email_campaigns、quality_issues（messages表新增language字段）

**新增API接口**：11个

---

### 2.12 财务域 (FMS) ★[AI成本归集]

| 序号 | 补强项 | 原始状态 | 补强内容 | 来源文档 |
|---|---|---|---|---|
| 86 | FBA头程异常处理 | 缺失 | 对接Amazon运输中以及仓内破损/丢失/退货/移除/共享库存五大异常场景，自动生成头程批次 | 需求规格V3 |
| 87 | 运费池管理 | 缺失 | 追溯其他平台头程/二程费用及分摊，精准核算物流成本 | 需求规格V3 |
| 88 | 平台回款扩展 | 简略 | Amazon追款功能 | 需求规格V3 |
| 89 | 外汇管理扩展 | 简略 | 系统自动同步银联汇率 | 需求规格V3 |

**新增数据库表**：fba_first_leg_exceptions、shipping_cost_pools

**新增API接口**：已包含在FMS域A/B/C类接口中

---

### 2.13 商业智能域 (BI) ★[KPI]

| 序号 | 补强项 | 原始状态 | 补强内容 | 来源文档 |
|---|---|---|---|---|
| 90 | 即时销量 | 缺失 | 即时销量实时看板 | 需求规格V3 |
| 91 | 产品表现 | 缺失 | 针对单个产品从表现明细、销售分布等11项核心指标进行深度分析 | 需求规格V3 |
| 92 | 运营监控 | 缺失 | 设置监控指标（销量/价格/Buybox/库存/星级评论异常），自动监管异常并通过短信/邮件/钉钉提醒 | 需求规格V3 |
| 93 | FBA货件分析 | 缺失 | FBA货件时效、成本、异常分析 | 需求规格V3 |
| 94 | 自定义报告 | 缺失 | 用户自定义报告构建，灵活选择维度和指标 | 需求规格V3 |
| 95 | 开发人员提成报表 | 缺失 | 开发人员开发SKU数、出单率、销售利润、KPI提成计算 | 需求规格V3 |
| 96 | 报表体系 | 缺失 | 500+业务报表，覆盖产品开发、采购、发货、销量、利润、库存、客服、运营等各业务节点 | 需求规格V3 |

**新增核心实体**：CustomReport

**新增数据库表**：custom_reports、developer_commission_reports

**新增API接口**：7个

---

### 2.14 系统设置域 (SYS)

| 序号 | 补强项 | 原始状态 | 补强内容 | 来源文档 |
|---|---|---|---|---|
| 97 | 平台授权 | 缺失 | 多平台API授权管理（Amazon/Shopify/TikTok/Walmart/eBay等），Token刷新、授权状态监控 | 需求规格V3 |
| 98 | 数据导入导出 | 缺失 | 批量数据导入导出、模板管理、导入校验规则 | 需求规格V3 |
| 99 | 打印模板 | 缺失 | 自定义打印模板管理（采购条码/产品条码/拣货单/包裹发票等） | 需求规格V3 |
| 100 | PMS集成 | 缺失 | PMS建议池接口、审批状态机、数据同步 | 设计V6/V7 |

**新增数据库表**：platform_authorizations、data_import_exports、print_templates

**新增API接口**：7个

---

## 三、关键架构增强说明

### 3.1 OMS订单三层分离架构

```
渠道订单(ChannelOrder) → ERP销售单(SalesOrder) → 履约单(FulfillmentOrder) → 包裹单(Package)
     (保留原文)              (统一视图)              (仓库执行)            (物流追踪)
```

- 渠道订单保留平台原始数据，不做翻译
- ERP销售单统一各平台差异，提供标准视图
- 履约单面向仓库执行，包含配货/拣货/打包
- 包裹单面向物流追踪，包含承运商/追踪号/重量

### 3.2 SCM五采购模式

| 模式 | 说明 | 特殊处理 |
|---|---|---|
| 市场采购 | 传统市场现场采购 | 手动录入 |
| 工厂采购 | 工厂直接下单 | 合同管理 |
| 天猫淘宝网络采购 | 电商平台采购 | 在线下单 |
| 1688采购 | 1688平台采购 | 系统内操作1688下单付款 |
| 采购原料自主加工 | 原料采购+加工 | 加工订单关联 |

### 3.3 WMS库存事务账五类状态

| 状态 | 说明 | 业务场景 |
|---|---|---|
| 在手(on_hand) | 仓库实际持有 | 入库确认后增加 |
| 预占(reserved) | 已分配未出库 | 订单分配后预占 |
| 可售(available) | 可供销售 | 在手-预占 |
| 在途(in_transit) | 运输中 | 调拨在途、FBA在途 |
| 不良(damaged) | 不可销售 | 质检不良品 |

### 3.4 PDM产品权限控制（对象级权限）

```
用户 → 对象级权限(ObjectPermission) → 资源类型(product/listing/ad) → 资源ID → 权限列表
```

- 支持用户自定义产品角色
- 可查看全部产品或仅与自己相关的产品
- 与IAM数据权限互补，实现精细化权限控制

### 3.5 FBA补货购物车链路

```
FBA补货建议 → 加入备货购物车 → 确认生成 → FBA货件计划单
(智能计算)    (批量选择)      (一键操作)    (执行发货)
```

---

## 四、数据库表新增汇总

| 域 | 新增表名 | 说明 |
|---|---|---|
| IAM | positions | 岗位信息 |
| IAM | object_permissions | 对象级权限 |
| PDM | brands | 品牌管理 |
| PDM | upc_pool | UPC/EAN码池 |
| PDM | sensitive_words | 敏感词库 |
| PDM | title_library | 标题库 |
| PDM | image_library | 图片库 |
| PDM | bundle_products | 组合产品 |
| PDM | packaging_materials | 包材管理 |
| PDM | product_issues | 产品问题记录 |
| PDM | platform_price_limits | 平台产品限价 |
| PDM | developer_kpis | 开发人员KPI |
| SOM | listing_price_calculators | Listing价格计算器 |
| SOM | buybox_monitors | Buybox采集 |
| SOM | hijack_alerts | 跟卖监控 |
| SOM | sales_teams | 销售小组 |
| OMS | channel_orders | 渠道订单 |
| OMS | sales_orders | ERP销售单 |
| OMS | fulfillment_orders | 履约单 |
| OMS | packages | 包裹单 |
| OMS | order_split_rules | 拆单合单规则 |
| OMS | logistics_declaration_rules | 物流申报规则 |
| OMS | platform_ship_rules | 平台标记发货规则 |
| OMS | plug_adapter_rules | 插头国标规则 |
| OMS | sorting_ports | 分拣口设置 |
| OMS | custom_invoice_templates | 自定义发票模板 |
| SCM | purchase_contracts | 采购合同 |
| SCM | purchase_tracking | 采购跟单 |
| SCM | purchase_writeoffs | 销单入库 |
| SCM | restock_plans | 备货计划 |
| SCM | supplier_platform_products | 供应商平台新品 |
| WMS | inventory_transactions | 库存事务账 |
| WMS | writeoff_orders | 销单 |
| WMS | outbound_qc_records | 出货质检记录 |
| WMS | defective_returns | 不良品退货 |
| WMS | product_repairs | 产品返修 |
| WMS | removal_orders | FBA Removal订单 |
| WMS | label_templates | 自定义标签模板 |
| FBA | fba_restock_suggestions | FBA补货建议 |
| FBA | restock_carts | 备货购物车 |
| FBA | fba_shipment_exceptions | FBA头程异常 |
| TMS | carrier_performance | 物流商绩效 |
| CRM | email_rules | 邮件分配规则 |
| CRM | vacation_auto_replies | 假期自动回复 |
| CRM | review_requests | 请求评论 |
| CRM | email_campaigns | 邮件营销 |
| CRM | quality_issues | 质量问题记录 |
| FMS | fba_first_leg_exceptions | FBA头程异常 |
| FMS | shipping_cost_pools | 运费池 |
| BI | custom_reports | 自定义报告 |
| BI | developer_commission_reports | 开发人员提成报表 |
| SYS | platform_authorizations | 平台授权 |
| SYS | data_import_exports | 数据导入导出 |
| SYS | print_templates | 打印模板 |

**合计新增数据库表：53张**

---

## 五、API接口新增汇总

| 域 | 新增接口数 | 关键接口 |
|---|---|---|
| DASHBOARD | 4 | quick-entries、notifications/aggregate、calendar |
| IAM | 5 | positions CRUD、object-permissions |
| PDM | 18 | brands、upc-pool、sensitive-words、title-library、image-library、bundle-products、packaging-materials、product-issues、platform-price-limits、developer-kpis |
| SOM | 11 | buybox-monitors、hijack-alerts、sales-teams、listing schedule、batch-delist、listing-price-calculator、account-health、clearance-plans |
| ADS | — | 已包含在A/B/C类接口中 |
| OMS | 13 | orders/import、channel-orders、sales-orders、fulfillment-orders、packages、split、merge、logistics-declaration-rules、platform-ship-rules、plug-adapter-rules、sorting-ports、shipping-statistics、custom-invoice-templates |
| SCM | 11 | 1688-order、1688-pay、purchase-tracking、purchase-writeoffs、purchase-contracts、restock-plans(local/fba/overseas)、supplier-platform/products |
| WMS | 13 | inventory-transactions、writeoff-orders、outbound-qc、defective-returns、product-repairs、removal-orders、label-templates、storage-location-groups |
| FBA | 8 | sync-amazon、load-shipment-id、restock-suggestions、restock-cart、shipment-exceptions、packing-sop |
| TMS | 3 | actual-cost、carriers/featured、carrier-performance/comparison |
| CRM | 11 | email-rules、vacation-auto-replies、review-requests、email-campaigns、quality-issues、refund-reports |
| FMS | — | 已包含在A/B/C类接口中 |
| BI | 7 | realtime-sales、product-performance、operation-monitor、fba-shipment-analysis、custom-reports、developer-commission |
| SYS | 7 | platform-authorizations、data-import-exports、print-templates |

**合计新增API接口：111个**

---

*本清单由跨境电商ERP全栈虚拟专家团队审核确认，所有补强项均已更新至V11详细设计说明书。*
