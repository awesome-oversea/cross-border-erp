export interface Result<T> {
  code: string;
  message: string;
  data: T;
}

export interface PageResult<T> {
  list: T[];
  total: number;
  page: number;
  size: number;
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

export interface Warehouse {
  warehouseId: string;
  tenantId: string;
  name: string;
  code: string;
  type: string;
  address: string;
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

export interface Supplier {
  supplierId: string;
  tenantId: string;
  name: string;
  code: string;
  contactPerson: string;
  phone: string;
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
