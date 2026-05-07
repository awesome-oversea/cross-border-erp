export interface Result<T> {
  success: boolean;
  code: string;
  message: string;
  data: T;
  timestamp: string;
}

export interface PageResult<T> {
  list: T[];
  total: number;
  page: number;
  size: number;
}

export interface PageParams {
  page?: number;
  size?: number;
  sort?: string;
}

export interface AuthToken {
  token: string;
  user: User;
  tenantId: string;
}

export interface User {
  userId: string;
  tenantId: string;
  username: string;
  email: string;
  phone: string;
  departmentId: string;
  status: string;
  roles: string[];
}

export interface Role {
  roleId: string;
  tenantId: string;
  roleName: string;
  permissions: string[];
}

export interface Position {
  positionId: string;
  tenantId: string;
  name: string;
  departmentId: string;
  level: number;
  createdAt: string;
}

export interface ObjectPermission {
  permissionId: string;
  tenantId: string;
  userId: string;
  resourceType: string;
  resourceId: string;
  actions: string[];
}

export interface Product {
  productId: string;
  tenantId: string;
  spuCode: string;
  productName: string;
  category: string;
  brand: string;
  status: string;
  skus: Sku[];
  createdAt: string;
  updatedAt: string;
}

export interface Sku {
  skuId: string;
  sellerSku: string;
  barcode: string;
  weight: number;
  costPrice: number;
  status: string;
}

export interface Listing {
  listingId: string;
  tenantId: string;
  platform: string;
  storeId: string;
  sellerSku: string;
  title: string;
  asin: string;
  price: number;
  currency: string;
  status: 'ACTIVE' | 'INACTIVE' | 'DRAFT' | 'SUSPENDED';
  stock: number;
  createdAt: string;
  updatedAt: string;
}

export interface Store {
  storeId: string;
  tenantId: string;
  platform: string;
  storeName: string;
  region: string;
  status: 'ACTIVE' | 'INACTIVE';
  accessToken?: string;
  listingCount: number;
  orders: number;
  revenue: number;
  createdAt: string;
}

export interface PriceRule {
  ruleId: string;
  tenantId: string;
  ruleName: string;
  ruleType: 'FIXED' | 'PERCENTAGE' | 'DYNAMIC';
  platform: string;
  conditions: Record<string, unknown>;
  adjustment: number;
  currency: string;
  status: 'ACTIVE' | 'DRAFT' | 'DISABLED';
  priority: number;
  effectiveFrom: string;
  effectiveTo: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface ListingMonitor {
  monitorId: string;
  tenantId: string;
  listingId: string;
  sellerSku: string;
  buyBoxPrice: number | null;
  buyBoxOwner: string | null;
  ourPrice: number;
  priceGap: number | null;
  status: 'WINNING' | 'LOSING' | 'NO_BUY_BOX';
  checkedAt: string;
}

export interface Order {
  orderId: string;
  tenantId: string;
  platform: string;
  storeId: string;
  orderNo: string;
  customerName: string;
  totalAmount: number;
  currency: string;
  status: string;
  orderDate: string;
  items: OrderItem[];
}

export interface OrderItem {
  itemId: string;
  sellerSku: string;
  productName: string;
  quantity: number;
  unitPrice: number;
  currency: string;
}

export interface OrderSyncLog {
  syncId: string;
  tenantId: string;
  platform: string;
  storeId: string;
  syncType: 'FULL' | 'INCREMENTAL';
  status: 'RUNNING' | 'COMPLETED' | 'FAILED';
  totalFetched: number;
  totalCreated: number;
  totalUpdated: number;
  errorMessage: string | null;
  startedAt: string;
  completedAt: string | null;
}

export interface OrderRefund {
  refundId: string;
  tenantId: string;
  orderId: string;
  refundType: 'FULL' | 'PARTIAL';
  amount: number;
  currency: string;
  reason: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'COMPLETED';
  createdAt: string;
}

export interface Warehouse {
  warehouseId: string;
  tenantId: string;
  name: string;
  code: string;
  type: string;
  address: string;
  enabled: boolean;
}

export interface WarehouseLocation {
  locationId: string;
  warehouseId: string;
  zone: string;
  aisle: string;
  shelf: string;
  bin: string;
  locationType: string;
  enabled: boolean;
}

export interface InventoryBalance {
  skuId: string;
  sellerSku: string;
  warehouseId: string;
  available: number;
  reserved: number;
  inTransit: number;
  frozen: number;
}

export interface InventoryTransaction {
  transactionId: string;
  tenantId: string;
  warehouseId: string;
  skuId: string;
  sellerSku: string;
  transactionType: 'INBOUND' | 'OUTBOUND' | 'TRANSFER' | 'ADJUSTMENT' | 'FREEZE' | 'UNFREEZE';
  quantity: number;
  referenceType: string;
  referenceId: string;
  createdAt: string;
}

export interface StockCheck {
  checkId: string;
  tenantId: string;
  warehouseId: string;
  status: 'DRAFT' | 'IN_PROGRESS' | 'COMPLETED';
  totalItems: number;
  matchedItems: number;
  diffItems: number;
  createdAt: string;
  completedAt: string | null;
}

export interface Supplier {
  supplierId: string;
  tenantId: string;
  name: string;
  code: string;
  contactPerson: string;
  phone: string;
  email: string;
  address: string;
  leadTimeDays: number;
  minOrderQty: number;
  status: string;
}

export interface PurchaseOrder {
  poId: string;
  tenantId: string;
  poNo: string;
  supplierId: string;
  status: string;
  totalAmount: number;
  currency: string;
  items: PurchaseOrderItem[];
  createdAt: string;
}

export interface PurchaseOrderItem {
  itemId: string;
  skuId: string;
  sellerSku: string;
  quantity: number;
  unitPrice: number;
  receivedQty: number;
}

export interface PurchasePlan {
  planId: string;
  tenantId: string;
  skuId: string;
  sellerSku: string;
  suggestedQty: number;
  currentStock: number;
  avgDailySales: number;
  leadTimeDays: number;
  safetyStock: number;
  status: 'PENDING' | 'APPROVED' | 'CONVERTED' | 'CANCELLED';
  createdAt: string;
}

export interface CostEvent {
  eventId: string;
  tenantId: string;
  eventType: string;
  sourceType: string;
  sourceId: string;
  amount: number;
  currency: string;
  occurredAt: string;
}

export interface Payment {
  paymentId: string;
  tenantId: string;
  orderId: string;
  amount: number;
  currency: string;
  paymentMethod: string;
  status: 'PENDING' | 'COMPLETED' | 'FAILED' | 'REFUNDED';
  paidAt: string | null;
}

export interface Bill {
  billId: string;
  tenantId: string;
  billType: string;
  sourceType: string;
  sourceId: string;
  amount: number;
  currency: string;
  dueDate: string;
  status: 'UNPAID' | 'PARTIAL' | 'PAID' | 'OVERDUE';
  paidAmount: number;
  createdAt: string;
}

export interface Settlement {
  settlementId: string;
  tenantId: string;
  platform: string;
  storeId: string;
  settlementPeriod: string;
  totalSales: number;
  totalFees: number;
  totalRefunds: number;
  netAmount: number;
  currency: string;
  status: 'PENDING' | 'CONFIRMED' | 'DISPUTED';
  settledAt: string | null;
}

export interface ProfitSummary {
  period: string;
  revenue: number;
  cogs: number;
  shippingCost: number;
  platformFee: number;
  adCost: number;
  otherCost: number;
  grossProfit: number;
  netProfit: number;
  margin: number;
  currency: string;
}

export interface Reconciliation {
  reconciliationId: string;
  tenantId: string;
  platform: string;
  storeId: string;
  period: string;
  systemAmount: number;
  platformAmount: number;
  difference: number;
  status: 'MATCHED' | 'UNMATCHED' | 'RESOLVED';
  resolvedAt: string | null;
}

export interface Carrier {
  carrierId: string;
  tenantId: string;
  code: string;
  name: string;
  enabled: boolean;
}

export interface ShipmentPlan {
  planId: string;
  tenantId: string;
  sellerSku: string;
  destinationFc: string;
  suggestedQuantity: number;
  status: string;
  createdAt: string;
}

export interface ShipmentTracking {
  trackingId: string;
  tenantId: string;
  trackingNo: string;
  carrier: string;
  carrierCode: string;
  orderId: string;
  origin: string;
  destination: string;
  status: 'PICKED_UP' | 'IN_TRANSIT' | 'CUSTOMS' | 'OUT_FOR_DELIVERY' | 'DELIVERED' | 'EXCEPTION';
  events: TrackingEvent[];
  eta: string | null;
  lastUpdate: string;
}

export interface TrackingEvent {
  timestamp: string;
  location: string;
  description: string;
  status: string;
}

export interface ShippingBatch {
  batchId: string;
  tenantId: string;
  warehouseId: string;
  carrierId: string;
  totalPackages: number;
  totalWeight: number;
  status: 'DRAFT' | 'CONFIRMED' | 'SHIPPED';
  createdAt: string;
  shippedAt: string | null;
}

export interface FbaShipment {
  shipmentId: string;
  tenantId: string;
  storeId: string;
  shipmentIdFba: string;
  destinationFc: string;
  status: 'DRAFT' | 'SUBMITTED' | 'IN_PRODUCTION' | 'SHIPPED' | 'RECEIVED' | 'CANCELLED';
  items: FbaShipmentItem[];
  createdAt: string;
}

export interface FbaShipmentItem {
  itemId: string;
  sellerSku: string;
  quantityShipped: number;
  quantityReceived: number;
}

export interface FbaInventory {
  skuId: string;
  sellerSku: string;
  fbaStock: number;
  inboundQty: number;
  transferQty: number;
  dailySales: number;
  daysOfSupply: number;
  fc: string;
}

export interface FbaRemoval {
  removalId: string;
  tenantId: string;
  storeId: string;
  removalType: 'DISPOSE' | 'RETURN';
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'CANCELLED';
  items: FbaRemovalItem[];
  createdAt: string;
}

export interface FbaRemovalItem {
  itemId: string;
  sellerSku: string;
  quantity: number;
}

export interface AdCampaign {
  campaignId: string;
  tenantId: string;
  platform: string;
  storeId: string;
  name: string;
  campaignType: 'SPONSORED_PRODUCTS' | 'SPONSORED_BRANDS' | 'SPONSORED_DISPLAY';
  status: 'RUNNING' | 'PAUSED' | 'ENDED' | 'DRAFT';
  budget: number;
  spent: number;
  currency: string;
  acos: number;
  impressions: number;
  clicks: number;
  ctr: number;
  orders: number;
  roas: number;
  startDate: string;
  endDate: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface AdGroup {
  groupId: string;
  campaignId: string;
  tenantId: string;
  name: string;
  defaultBid: number;
  status: 'ENABLED' | 'PAUSED' | 'ARCHIVED';
}

export interface AdKeyword {
  keywordId: string;
  groupId: string;
  campaignId: string;
  tenantId: string;
  keyword: string;
  matchType: 'BROAD' | 'PHRASE' | 'EXACT';
  bid: number;
  status: 'ENABLED' | 'PAUSED' | 'ARCHIVED';
}

export interface AdStrategy {
  strategyId: string;
  tenantId: string;
  strategyName: string;
  strategyType: 'BID_OPTIMIZATION' | 'BUDGET_ALLOCATION' | 'KEYWORD_DISCOVERY';
  rules: Record<string, unknown>;
  status: 'ACTIVE' | 'DRAFT' | 'DISABLED';
  createdAt: string;
}

export interface Customer {
  customerId: string;
  tenantId: string;
  name: string;
  email: string;
  phone: string;
  countryCode: string;
  platform: string;
  storeId: string;
  totalOrders: number;
  totalSpent: number;
  lastOrderAt: string | null;
  tags: CustomerTag[];
  createdAt: string;
  updatedAt: string;
}

export interface CustomerTag {
  tagId: string;
  customerId: string;
  tagName: string;
  tagValue: string;
}

export interface ServiceTicket {
  ticketId: string;
  tenantId: string;
  customerId: string;
  subject: string;
  description: string;
  assignee: string | null;
  status: 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED';
  resolution: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface EmailRule {
  ruleId: string;
  tenantId: string;
  conditions: Record<string, unknown>;
  assignTo: string;
  priority: number;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface EmailCampaign {
  campaignId: string;
  tenantId: string;
  name: string;
  subject: string;
  content: string;
  targetSegment: string;
  status: string;
  sentAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface QualityIssue {
  issueId: string;
  tenantId: string;
  productId: string;
  source: string;
  description: string;
  severity: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface KpiMetric {
  kpiId: string;
  tenantId: string;
  kpiCode: string;
  kpiName: string;
  category: string;
  value: number;
  targetValue: number;
  unit: string;
  status: 'ON_TRACK' | 'AT_RISK' | 'OFF_TRACK' | 'NOT_STARTED';
  measuredAt: string;
}

export interface KpiAlert {
  alertId: string;
  tenantId: string;
  kpiCode: string;
  kpiName: string;
  value: number;
  targetValue: number;
  deviationRate: number;
  status: string;
  severity: string;
  message: string;
  createdAt: string;
}

export interface AlertRule {
  ruleId: string;
  tenantId: string;
  ruleName: string;
  metricCode: string;
  domain: string;
  condition: string;
  threshold: string;
  severity: string;
  enabled: boolean;
  notifyChannel: string;
  notifyTargets: string;
}

export interface ReportDefinition {
  reportId: string;
  tenantId: string;
  reportCode: string;
  reportName: string;
  reportType: string;
  dataSource: string;
  description: string;
}

export interface CockpitTrend {
  trendId: string;
  tenantId: string;
  metricCode: string;
  metricName: string;
  period: string;
  dataPoints: { timestamp: string; value: number; targetValue: number }[];
  generatedAt: string;
}

export interface RankingData {
  rankingId: string;
  tenantId: string;
  rankingType: string;
  dimension: string;
  items: { rankKey: string; label: string; value: number; rank: number }[];
  generatedAt: string;
}

export interface DashboardMetrics {
  todayOrders: number;
  todayRevenue: number;
  inventoryAlerts: number;
  pendingTickets: number;
  orderTrend: number;
  revenueTrend: number;
  inventoryTrend: number;
  ticketTrend: number;
}

export interface WebhookEndpoint {
  endpointId: string;
  tenantId: string;
  name: string;
  url: string;
  eventType: string;
  status: string;
  retryCount: number;
  timeoutSeconds: number;
}

export interface SysConfig {
  configId: string;
  tenantId: string;
  configKey: string;
  configValue: string;
  category: string;
  description: string;
  editable: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface BusinessRule {
  ruleId: string;
  tenantId: string;
  ruleType: string;
  ruleName: string;
  version: number;
  contentJson: string;
  status: 'ACTIVE' | 'DRAFT' | 'DISABLED';
  lastExecuted: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface ApprovalFlow {
  flowId: string;
  tenantId: string;
  flowName: string;
  flowType: string;
  steps: ApprovalStep[];
  status: 'ACTIVE' | 'DRAFT' | 'DISABLED';
  createdAt: string;
}

export interface ApprovalStep {
  stepOrder: number;
  stepName: string;
  approverType: 'USER' | 'ROLE' | 'POSITION';
  approverId: string;
  action: 'APPROVE' | 'REJECT' | 'REVIEW';
}

export interface Connector {
  connectorId: string;
  tenantId: string;
  connectorType: string;
  name: string;
  platform: string;
  config: Record<string, unknown>;
  status: 'CONNECTED' | 'DISCONNECTED' | 'ERROR';
  lastSyncAt: string | null;
  createdAt: string;
}

export interface ComplianceRule {
  ruleId: string;
  tenantId: string;
  ruleName: string;
  ruleType: 'EXPORT_CONTROL' | 'SANCTIONS' | 'PRODUCT_COMPLIANCE' | 'DATA_PRIVACY';
  conditions: Record<string, unknown>;
  action: 'BLOCK' | 'WARN' | 'REVIEW';
  status: 'ACTIVE' | 'DRAFT' | 'DISABLED';
  createdAt: string;
}
