'use client';

import { Card, Typography, Table, Tag, Badge, Row, Col, Statistic, Progress, Button, Modal, Form, Input, InputNumber, Select, Space, message } from 'antd';
import { PlusOutlined, RiseOutlined, FallOutlined } from '@ant-design/icons';
import ReactEChartsCore from 'echarts-for-react';
import * as echarts from 'echarts/core';
import { LineChart, BarChart } from 'echarts/charts';
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';

echarts.use([LineChart, BarChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer]);

const { Title } = Typography;

const campaigns = [
  { key: '1', campaignId: 'AD-001', name: 'Spring Sale - Earbuds', platform: 'Amazon', status: 'RUNNING', budget: 500, spent: 342, acos: 18.5, impressions: 45000, clicks: 1200, orders: 85 },
  { key: '2', campaignId: 'AD-002', name: 'Brand Awareness - Lamps', platform: 'Amazon', status: 'RUNNING', budget: 300, spent: 210, acos: 22.3, impressions: 32000, clicks: 890, orders: 52 },
  { key: '3', campaignId: 'AD-003', name: 'Clearance - Chargers', platform: 'Shopify', status: 'PAUSED', budget: 200, spent: 180, acos: 35.1, impressions: 18000, clicks: 560, orders: 18 },
  { key: '4', campaignId: 'AD-004', name: 'New Launch - Bottles', platform: 'Amazon', status: 'RUNNING', budget: 400, spent: 95, acos: 12.8, impressions: 22000, clicks: 680, orders: 42 },
];

const statusMap: Record<string, { color: string; text: string }> = {
  RUNNING: { color: 'green', text: '投放中' },
  PAUSED: { color: 'orange', text: '已暂停' },
  ENDED: { color: 'default', text: '已结束' },
  DRAFT: { color: 'blue', text: '草稿' },
};

const adSpendOption = {
  tooltip: { trigger: 'axis' as const },
  legend: { data: ['花费', '销售额', 'ACOS'] },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'category' as const, data: ['W1', 'W2', 'W3', 'W4'] },
  yAxis: [{ type: 'value' as const, name: 'USD' }, { type: 'value' as const, name: 'ACOS%', min: 0, max: 50 }],
  series: [
    { name: '花费', type: 'bar', data: [680, 750, 820, 827], itemStyle: { color: '#1890ff' } },
    { name: '销售额', type: 'bar', data: [3200, 3800, 4100, 4500], itemStyle: { color: '#52c41a' } },
    { name: 'ACOS', type: 'line', yAxisIndex: 1, data: [21.2, 19.7, 20.0, 18.4], itemStyle: { color: '#ff4d4f' } },
  ],
};

export default function AdsPage() {
  const columns = [
    { title: '广告ID', dataIndex: 'campaignId', key: 'campaignId' },
    { title: '广告名称', dataIndex: 'name', key: 'name', ellipsis: true },
    { title: '平台', dataIndex: 'platform', key: 'platform', render: (v: string) => <Tag>{v}</Tag> },
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: string) => <Badge color={statusMap[v]?.color || 'default'} text={statusMap[v]?.text || v} /> },
    { title: '预算/花费', key: 'budget', render: (_: unknown, r: typeof campaigns[0]) => `$${r.spent} / $${r.budget}` },
    { title: 'ACOS', dataIndex: 'acos', key: 'acos', render: (v: number) => <span style={{ color: v <= 20 ? '#52c41a' : v <= 30 ? '#faad14' : '#ff4d4f', fontWeight: 600 }}>{v}%</span> },
    { title: '曝光', dataIndex: 'impressions', key: 'impressions', render: (v: number) => v.toLocaleString() },
    { title: '点击', dataIndex: 'clicks', key: 'clicks' },
    { title: '订单', dataIndex: 'orders', key: 'orders' },
  ];

  return (
    <div>
      <Title level={4} style={{ marginBottom: 16 }}>广告管理</Title>
      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col xs={24} sm={6}>
          <Card style={{ borderRadius: 8 }}>
            <Statistic title="总花费" value={827} prefix="$" valueStyle={{ color: '#1890ff' }} />
          </Card>
        </Col>
        <Col xs={24} sm={6}>
          <Card style={{ borderRadius: 8 }}>
            <Statistic title="总销售额" value={4500} prefix="$" valueStyle={{ color: '#52c41a' }} />
          </Card>
        </Col>
        <Col xs={24} sm={6}>
          <Card style={{ borderRadius: 8 }}>
            <Statistic title="平均ACOS" value={18.4} suffix="%" valueStyle={{ color: '#52c41a' }} prefix={<FallOutlined />} />
          </Card>
        </Col>
        <Col xs={24} sm={6}>
          <Card style={{ borderRadius: 8 }}>
            <Statistic title="投放中广告" value={3} suffix="个" valueStyle={{ color: '#722ed1' }} />
          </Card>
        </Col>
      </Row>
      <Card title="广告花费趋势" style={{ borderRadius: 8, marginBottom: 16 }}>
        <ReactEChartsCore echarts={echarts} option={adSpendOption} style={{ height: 320 }} />
      </Card>
      <Card title="广告活动列表" style={{ borderRadius: 8 }}>
        <Table columns={columns} dataSource={campaigns} size="middle" pagination={false} />
      </Card>
    </div>
  );
}
