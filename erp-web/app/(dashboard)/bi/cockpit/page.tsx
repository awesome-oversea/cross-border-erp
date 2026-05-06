'use client';

import { Row, Col, Card, Statistic, Typography, Table, Tag, Badge, Progress } from 'antd';
import {
  ShoppingOutlined, DollarOutlined, UserOutlined, RiseOutlined,
  ArrowUpOutlined, ArrowDownOutlined,
} from '@ant-design/icons';
import ReactEChartsCore from 'echarts-for-react';
import * as echarts from 'echarts/core';
import { LineChart, BarChart, PieChart } from 'echarts/charts';
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';

echarts.use([LineChart, BarChart, PieChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer]);

const { Title } = Typography;

const kpiCards = [
  { title: 'GMV', value: 542800, unit: 'USD', trend: 15.2, icon: <DollarOutlined />, color: '#1890ff' },
  { title: '订单量', value: 3842, unit: '单', trend: 8.7, icon: <ShoppingOutlined />, color: '#52c41a' },
  { title: '活跃客户', value: 1256, unit: '人', trend: 3.4, icon: <UserOutlined />, color: '#722ed1' },
  { title: '利润率', value: 32.5, unit: '%', trend: -1.2, icon: <RiseOutlined />, color: '#fa8c16' },
];

const revenueTrendOption = {
  tooltip: { trigger: 'axis' as const },
  legend: { data: ['GMV', '净利润'] },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'category' as const, data: ['1月', '2月', '3月', '4月', '5月'] },
  yAxis: { type: 'value' as const, name: 'USD' },
  series: [
    { name: 'GMV', type: 'line', smooth: true, data: [420000, 450000, 480000, 510000, 542800], areaStyle: { opacity: 0.1 }, itemStyle: { color: '#1890ff' } },
    { name: '净利润', type: 'line', smooth: true, data: [126000, 139500, 153600, 163200, 176410], areaStyle: { opacity: 0.1 }, itemStyle: { color: '#52c41a' } },
  ],
};

const platformPieOption = {
  tooltip: { trigger: 'item' },
  series: [{
    type: 'pie', radius: ['35%', '65%'],
    data: [
      { value: 245000, name: 'Amazon', itemStyle: { color: '#ff9900' } },
      { value: 135000, name: 'Shopify', itemStyle: { color: '#96bf48' } },
      { value: 82000, name: 'eBay', itemStyle: { color: '#86b817' } },
      { value: 52000, name: 'Shopee', itemStyle: { color: '#ee4d2d' } },
      { value: 28800, name: '其他', itemStyle: { color: '#8c8c8c' } },
    ],
    label: { formatter: '{b}\n{d}%' },
  }],
};

const topProducts = [
  { key: '1', rank: 1, name: 'Wireless Earbuds Pro', sku: 'SKU-001', sales: 1280, revenue: 38400 },
  { key: '2', rank: 2, name: 'LED Desk Lamp', sku: 'SKU-002', sales: 856, revenue: 38520 },
  { key: '3', rank: 3, name: 'Portable Charger', sku: 'SKU-004', sales: 720, revenue: 25920 },
  { key: '4', rank: 4, name: 'Yoga Mat Premium', sku: 'SKU-003', sales: 540, revenue: 12150 },
  { key: '5', rank: 5, name: 'Water Bottle', sku: 'SKU-005', sales: 480, revenue: 8640 },
];

export default function CockpitPage() {
  const productColumns = [
    { title: '排名', dataIndex: 'rank', key: 'rank', render: (v: number) => <Badge count={v} style={{ backgroundColor: v <= 3 ? '#faad14' : '#8c8c8c' }} /> },
    { title: '产品', dataIndex: 'name', key: 'name' },
    { title: 'SKU', dataIndex: 'sku', key: 'sku' },
    { title: '销量', dataIndex: 'sales', key: 'sales' },
    { title: '收入', dataIndex: 'revenue', key: 'revenue', render: (v: number) => `$${v.toLocaleString()}` },
  ];

  return (
    <div>
      <Title level={4} style={{ marginBottom: 24 }}>经营驾驶舱</Title>
      <Row gutter={[16, 16]}>
        {kpiCards.map((kpi) => (
          <Col xs={24} sm={12} lg={6} key={kpi.title}>
            <Card hoverable style={{ borderRadius: 8 }}>
              <Statistic
                title={<span style={{ fontSize: 14 }}>{kpi.title}</span>}
                value={kpi.value}
                suffix={kpi.unit}
                prefix={<span style={{ color: kpi.color, marginRight: 8 }}>{kpi.icon}</span>}
                valueStyle={{ fontSize: 28, fontWeight: 700 }}
              />
              <div style={{ marginTop: 8 }}>
                {kpi.trend > 0 ? (
                  <span style={{ color: '#52c41a', fontSize: 13 }}><ArrowUpOutlined /> {kpi.trend}% 环比</span>
                ) : (
                  <span style={{ color: '#ff4d4f', fontSize: 13 }}><ArrowDownOutlined /> {Math.abs(kpi.trend)}% 环比</span>
                )}
              </div>
            </Card>
          </Col>
        ))}
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} lg={16}>
          <Card title="收入趋势" style={{ borderRadius: 8 }}>
            <ReactEChartsCore echarts={echarts} option={revenueTrendOption} style={{ height: 340 }} />
          </Card>
        </Col>
        <Col xs={24} lg={8}>
          <Card title="平台分布" style={{ borderRadius: 8 }}>
            <ReactEChartsCore echarts={echarts} option={platformPieOption} style={{ height: 340 }} />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} lg={12}>
          <Card title="热销产品TOP5" style={{ borderRadius: 8 }}>
            <Table columns={productColumns} dataSource={topProducts} pagination={false} size="small" />
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title="关键指标达成" style={{ borderRadius: 8 }}>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 20, padding: '8px 0' }}>
              {[
                { label: '月度GMV目标', value: 542800, target: 600000 },
                { label: '新客获取', value: 320, target: 400 },
                { label: '库存周转率', value: 4.2, target: 5 },
                { label: '客户满意度', value: 92, target: 95 },
              ].map((item) => (
                <div key={item.label}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
                    <span>{item.label}</span>
                    <span style={{ fontWeight: 600 }}>{item.value} / {item.target}</span>
                  </div>
                  <Progress percent={Math.round((item.value / item.target) * 100)} strokeColor={item.value / item.target >= 0.9 ? '#52c41a' : item.value / item.target >= 0.7 ? '#faad14' : '#ff4d4f'} />
                </div>
              ))}
            </div>
          </Card>
        </Col>
      </Row>
    </div>
  );
}
