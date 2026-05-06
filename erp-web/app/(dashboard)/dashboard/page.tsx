'use client';

import { Row, Col, Card, Statistic, Typography, Table, Tag, Space, Badge } from 'antd';
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
import { LineChart, BarChart } from 'echarts/charts';
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';

echarts.use([LineChart, BarChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer]);

const { Title } = Typography;

const stats = [
  { title: '今日订单', value: 1284, prefix: <ShoppingOutlined />, suffix: '单', trend: 12.5, color: '#1890ff' },
  { title: '销售额', value: 89420, prefix: <DollarOutlined />, suffix: 'USD', trend: 8.3, color: '#52c41a' },
  { title: '库存预警', value: 23, prefix: <HomeOutlined />, suffix: 'SKU', trend: -5.2, color: '#faad14' },
  { title: '待处理工单', value: 15, prefix: <CustomerServiceOutlined />, suffix: '件', trend: -2.1, color: '#ff4d4f' },
];

const recentOrders = [
  { key: '1', orderNo: 'OM-20260502-001', platform: 'Amazon', customer: 'John D.', amount: 89.99, status: '已发货' },
  { key: '2', orderNo: 'OM-20260502-002', platform: 'Shopify', customer: 'Lisa M.', amount: 156.50, status: '待发货' },
  { key: '3', orderNo: 'OM-20260502-003', platform: 'eBay', customer: 'Wang W.', amount: 42.00, status: '待审核' },
  { key: '4', orderNo: 'OM-20260502-004', platform: 'Amazon', customer: 'Hans K.', amount: 234.80, status: '已发货' },
  { key: '5', orderNo: 'OM-20260502-005', platform: 'Shopee', customer: 'Tan A.', amount: 67.30, status: '待发货' },
];

const orderColumns = [
  { title: '订单号', dataIndex: 'orderNo', key: 'orderNo' },
  { title: '平台', dataIndex: 'platform', key: 'platform', render: (v: string) => <Tag>{v}</Tag> },
  { title: '客户', dataIndex: 'customer', key: 'customer' },
  { title: '金额', dataIndex: 'amount', key: 'amount', render: (v: number) => `$${v.toFixed(2)}` },
  { title: '状态', dataIndex: 'status', key: 'status', render: (v: string) => {
    const colorMap: Record<string, string> = { '已发货': 'green', '待发货': 'blue', '待审核': 'orange' };
    return <Badge color={colorMap[v] || 'default'} text={v} />;
  }},
];

const salesTrendOption = {
  tooltip: { trigger: 'axis' as const },
  legend: { data: ['销售额', '订单数'] },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'category' as const, data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'] },
  yAxis: [{ type: 'value' as const, name: '销售额(USD)' }, { type: 'value' as const, name: '订单数' }],
  series: [
    { name: '销售额', type: 'line' as const, smooth: true, data: [8200, 9300, 7800, 10500, 11200, 9800, 12400], itemStyle: { color: '#1890ff' } },
    { name: '订单数', type: 'bar' as const, yAxisIndex: 1, data: [120, 145, 110, 168, 180, 155, 198], itemStyle: { color: '#52c41a', opacity: 0.6 } },
  ],
};

export default function DashboardPage() {
  return (
    <div>
      <Title level={4} style={{ marginBottom: 24 }}>经营驾驶舱</Title>
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
                  data: [
                    { value: 45, name: 'Amazon', itemStyle: { color: '#ff9900' } },
                    { value: 25, name: 'Shopify', itemStyle: { color: '#96bf48' } },
                    { value: 15, name: 'eBay', itemStyle: { color: '#86b817' } },
                    { value: 10, name: 'Shopee', itemStyle: { color: '#ee4d2d' } },
                    { value: 5, name: '其他', itemStyle: { color: '#8c8c8c' } },
                  ],
                  label: { formatter: '{b}: {d}%' },
                }],
              }}
              style={{ height: 320 }}
            />
          </Card>
        </Col>
      </Row>

      <Card title="最近订单" style={{ marginTop: 16, borderRadius: 8 }}>
        <Table columns={orderColumns} dataSource={recentOrders} pagination={false} size="small" />
      </Card>
    </div>
  );
}
