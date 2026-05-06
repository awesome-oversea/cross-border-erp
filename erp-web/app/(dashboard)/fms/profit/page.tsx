'use client';

import { Card, Typography, Row, Col, Statistic, Table, Tag } from 'antd';
import { DollarOutlined, ArrowUpOutlined, ArrowDownOutlined } from '@ant-design/icons';
import ReactEChartsCore from 'echarts-for-react';
import * as echarts from 'echarts/core';
import { LineChart, BarChart } from 'echarts/charts';
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';

echarts.use([LineChart, BarChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer]);

const { Title } = Typography;

const profitData = [
  { key: '1', month: '2026-01', revenue: 125000, cost: 87500, profit: 37500, margin: '30.0%' },
  { key: '2', month: '2026-02', revenue: 138000, cost: 93840, profit: 44160, margin: '32.0%' },
  { key: '3', month: '2026-03', revenue: 152000, cost: 98800, profit: 53200, margin: '35.0%' },
  { key: '4', month: '2026-04', revenue: 145000, cost: 101500, profit: 43500, margin: '30.0%' },
];

const profitTrendOption = {
  tooltip: { trigger: 'axis' as const },
  legend: { data: ['收入', '成本', '利润'] },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'category' as const, data: ['2026-01', '2026-02', '2026-03', '2026-04'] },
  yAxis: { type: 'value' as const, name: 'USD' },
  series: [
    { name: '收入', type: 'bar', data: [125000, 138000, 152000, 145000], itemStyle: { color: '#1890ff' } },
    { name: '成本', type: 'bar', data: [87500, 93840, 98800, 101500], itemStyle: { color: '#ff4d4f' } },
    { name: '利润', type: 'line', data: [37500, 44160, 53200, 43500], itemStyle: { color: '#52c41a' }, lineStyle: { width: 3 } },
  ],
};

export default function ProfitPage() {
  const columns = [
    { title: '月份', dataIndex: 'month', key: 'month' },
    { title: '收入', dataIndex: 'revenue', key: 'revenue', render: (v: number) => `$${v.toLocaleString()}` },
    { title: '成本', dataIndex: 'cost', key: 'cost', render: (v: number) => `$${v.toLocaleString()}` },
    { title: '利润', dataIndex: 'profit', key: 'profit', render: (v: number) => <span style={{ color: v >= 0 ? '#52c41a' : '#ff4d4f', fontWeight: 600 }}>${v.toLocaleString()}</span> },
    { title: '利润率', dataIndex: 'margin', key: 'margin', render: (v: string) => <Tag color={parseFloat(v) >= 30 ? 'green' : 'orange'}>{v}</Tag> },
  ];

  return (
    <div>
      <Title level={4} style={{ marginBottom: 16 }}>利润报表</Title>
      <Row gutter={[16, 16]}>
        <Col xs={24} sm={8}>
          <Card style={{ borderRadius: 8 }}>
            <Statistic title="本月收入" value={145000} prefix="$" valueStyle={{ color: '#1890ff' }} />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card style={{ borderRadius: 8 }}>
            <Statistic title="本月成本" value={101500} prefix="$" valueStyle={{ color: '#ff4d4f' }} />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card style={{ borderRadius: 8 }}>
            <Statistic title="本月利润" value={43500} prefix="$" valueStyle={{ color: '#52c41a' }}
              suffix={<span style={{ fontSize: 14 }}><ArrowUpOutlined /> 30%</span>} />
          </Card>
        </Col>
      </Row>
      <Card title="利润趋势" style={{ marginTop: 16, borderRadius: 8 }}>
        <ReactEChartsCore echarts={echarts} option={profitTrendOption} style={{ height: 360 }} />
      </Card>
      <Card title="月度明细" style={{ marginTop: 16, borderRadius: 8 }}>
        <Table columns={columns} dataSource={profitData} pagination={false} size="middle" />
      </Card>
    </div>
  );
}
