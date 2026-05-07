'use client';

import { Row, Col, Card, Statistic, Typography, Table, Tag, Badge, Spin } from 'antd';
import {
  ShoppingOutlined,
  DollarOutlined,
  HomeOutlined,
  CustomerServiceOutlined,
  ArrowUpOutlined,
  ArrowDownOutlined,
} from '@ant-design/icons';
import ReactEChartsCore from 'echarts-for-react';
import * as echarts from 'echarts/core';
import { LineChart, BarChart, PieChart } from 'echarts/charts';
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { useApi } from '@/lib/hooks';
import type { DashboardMetrics, Order } from '@/types';

echarts.use([LineChart, BarChart, PieChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer]);

const { Title } = Typography;

export default function DashboardPage() {
  const { data: metrics, isLoading: metricsLoading } = useApi<DashboardMetrics>('/dashboard/api/in/v1/metrics');
  const { data: recentOrders } = useApi<Order[]>('/dashboard/api/in/v1/recent-orders?limit=10');
  const { data: salesTrend } = useApi<Record<string, unknown>>('/dashboard/api/in/v1/sales-trend?days=7');
  const { data: platformDist } = useApi<Record<string, unknown>>('/dashboard/api/in/v1/platform-distribution');

  const stats = [
    { title: '今日订单', value: metrics?.todayOrders ?? 0, prefix: <ShoppingOutlined />, suffix: '单', trend: metrics?.orderTrend ?? 0, color: '#1890ff' },
    { title: '销售额', value: metrics?.todaySales ?? 0, prefix: <DollarOutlined />, suffix: 'USD', trend: metrics?.salesTrend ?? 0, color: '#52c41a' },
    { title: '库存预警', value: metrics?.inventoryAlerts ?? 0, prefix: <HomeOutlined />, suffix: 'SKU', trend: metrics?.inventoryAlertTrend ?? 0, color: '#faad14' },
    { title: '待处理工单', value: metrics?.pendingTickets ?? 0, prefix: <CustomerServiceOutlined />, suffix: '件', trend: metrics?.ticketTrend ?? 0, color: '#ff4d4f' },
  ];

  const orderColumns = [
    { title: '订单号', dataIndex: 'orderNo', key: 'orderNo', render: (v: string) => <a href={`/oms/orders`}>{v}</a> },
    { title: '平台', dataIndex: 'platform', key: 'platform', render: (v: string) => <Tag color="blue">{v}</Tag> },
    { title: '客户', dataIndex: 'customerName', key: 'customerName' },
    { title: '金额', dataIndex: 'totalAmount', key: 'totalAmount', render: (v: number, r: Order) => `${r.currency} ${v?.toFixed(2)}` },
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: string) => {
      const statusMap: Record<string, string> = { PENDING: '待审核', CONFIRMED: '已确认', PAID: '已付款', SHIPPED: '已发货', DELIVERED: '已签收', CANCELLED: '已取消' };
      const colorMap: Record<string, string> = { PENDING: 'orange', CONFIRMED: 'blue', PAID: 'geekblue', SHIPPED: 'cyan', DELIVERED: 'green', CANCELLED: 'red' };
      return <Badge color={colorMap[v] || 'default'} text={statusMap[v] || v} />;
    }},
  ];

  const salesTrendOption = {
    tooltip: { trigger: 'axis' as const },
    legend: { data: ['销售额', '订单数'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category' as const, data: (salesTrend as { dates?: string[] })?.dates || ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'] },
    yAxis: [{ type: 'value' as const, name: '销售额(USD)' }, { type: 'value' as const, name: '订单数' }],
    series: [
      { name: '销售额', type: 'line' as const, smooth: true, data: (salesTrend as { sales?: number[] })?.sales || [8200, 9300, 7800, 10500, 11200, 9800, 12400], itemStyle: { color: '#1890ff' } },
      { name: '订单数', type: 'bar' as const, yAxisIndex: 1, data: (salesTrend as { orders?: number[] })?.orders || [120, 145, 110, 168, 180, 155, 198], itemStyle: { color: '#52c41a', opacity: 0.6 } },
    ],
  };

  const platformData = (platformDist as { platforms?: { name: string; value: number; color: string }[] })?.platforms || [
    { value: 45, name: 'Amazon', color: '#ff9900' },
    { value: 25, name: 'Shopify', color: '#96bf48' },
    { value: 15, name: 'eBay', color: '#86b817' },
    { value: 10, name: 'Shopee', color: '#ee4d2d' },
    { value: 5, name: '其他', color: '#8c8c8c' },
  ];

  return (
    <div>
      <Title level={4} style={{ marginBottom: 24 }}>经营驾驶舱</Title>
      <Spin spinning={metricsLoading}>
        <Row gutter={[16, 16]}>
          {stats.map((s) => (
            <Col xs={24} sm={12} lg={6} key={s.title}>
              <Card hoverable style={{ borderRadius: 8 }}>
                <Statistic
                  title={s.title}
                  value={s.value}
                  prefix={<span style={{ color: s.color, marginRight: 8 }}>{s.prefix}</span>}
                  suffix={s.suffix}
                  valueStyle={{ fontSize: 28, fontWeight: 700 }}
                />
                <div style={{ marginTop: 8, fontSize: 13 }}>
                  {s.trend > 0 ? (
                    <span style={{ color: '#52c41a' }}><ArrowUpOutlined /> {s.trend}% 较昨日</span>
                  ) : (
                    <span style={{ color: '#ff4d4f' }}><ArrowDownOutlined /> {Math.abs(s.trend)}% 较昨日</span>
                  )}
                </div>
              </Card>
            </Col>
          ))}
        </Row>
      </Spin>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} lg={16}>
          <Card title="销售趋势" style={{ borderRadius: 8 }}>
            <ReactEChartsCore echarts={echarts} option={salesTrendOption} style={{ height: 320 }} />
          </Card>
        </Col>
        <Col xs={24} lg={8}>
          <Card title="平台分布" style={{ borderRadius: 8 }}>
            <ReactEChartsCore
              echarts={echarts}
              option={{
                tooltip: { trigger: 'item' },
                series: [{
                  type: 'pie',
                  radius: ['40%', '70%'],
                  data: platformData.map((p: { name: string; value: number; color: string }) => ({
                    value: p.value, name: p.name, itemStyle: { color: p.color },
                  })),
                  label: { formatter: '{b}: {d}%' },
                }],
              }}
              style={{ height: 320 }}
            />
          </Card>
        </Col>
      </Row>

      <Card title="最近订单" style={{ marginTop: 16, borderRadius: 8 }}
        extra={<a href="/oms/orders">查看全部</a>}>
        <Table columns={orderColumns} dataSource={recentOrders || []} rowKey="orderId" pagination={false} size="small" />
      </Card>
    </div>
  );
}
