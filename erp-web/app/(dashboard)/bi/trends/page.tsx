'use client';

import { useState, useEffect, useCallback } from 'react';
import { Card, Typography, Row, Col, Select, Space, Button, Table, Tag } from 'antd';
import { biApi } from '@/lib/api';
import type { CockpitTrend, RankingData } from '@/types';
import ReactEChartsCore from 'echarts-for-react';
import * as echarts from 'echarts/core';
import { LineChart, BarChart } from 'echarts/charts';
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';

echarts.use([LineChart, BarChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer]);

const { Title } = Typography;

export default function TrendsPage() {
  const [trends, setTrends] = useState<CockpitTrend[]>([]);
  const [rankings, setRankings] = useState<RankingData[]>([]);
  const [loading, setLoading] = useState(false);
  const [metricCode, setMetricCode] = useState<string>('');

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const [trendData, rankData] = await Promise.all([
        biApi.listTrends(metricCode || undefined),
        biApi.listRankings(),
      ]);
      setTrends(trendData || []);
      setRankings(rankData || []);
    } catch {
      setTrends([]);
      setRankings([]);
    } finally {
      setLoading(false);
    }
  }, [metricCode]);

  useEffect(() => { fetchData(); }, [fetchData]);

  const trendOption = trends.length > 0 ? {
    tooltip: { trigger: 'axis' as const },
    legend: { data: ['实际值', '目标值'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category' as const, data: trends[0]?.dataPoints?.map((p) => p.timestamp.substring(5, 10)) || [] },
    yAxis: { type: 'value' as const },
    series: [
      { name: '实际值', type: 'line', smooth: true, data: trends[0]?.dataPoints?.map((p) => p.value) || [], itemStyle: { color: '#1890ff' } },
      { name: '目标值', type: 'line', smooth: true, lineStyle: { type: 'dashed' as const }, data: trends[0]?.dataPoints?.map((p) => p.targetValue) || [], itemStyle: { color: '#faad14' } },
    ],
  } : {
    tooltip: { trigger: 'axis' as const },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category' as const, data: ['W1', 'W2', 'W3', 'W4'] },
    yAxis: { type: 'value' as const },
    series: [
      { name: 'GMV', type: 'line', smooth: true, data: [120000, 135000, 128000, 142000], itemStyle: { color: '#1890ff' } },
      { name: '目标', type: 'line', lineStyle: { type: 'dashed' as const }, data: [130000, 130000, 130000, 130000], itemStyle: { color: '#faad14' } },
    ],
  };

  const rankingColumns = [
    { title: '排名类型', dataIndex: 'rankingType', key: 'rankingType', render: (v: string) => <Tag color="blue">{v}</Tag> },
    { title: '维度', dataIndex: 'dimension', key: 'dimension' },
    { title: 'TOP项目', key: 'items', render: (_: unknown, r: RankingData) => r.items?.slice(0, 3).map((i) => <Tag key={i.rankKey}>{i.label}: {i.value}</Tag>) },
    { title: '生成时间', dataIndex: 'generatedAt', key: 'generatedAt', render: (v: string) => v ? new Date(v).toLocaleString() : '-' },
  ];

  return (
    <div>
      <Title level={4} style={{ marginBottom: 16 }}>趋势分析</Title>
      <Card style={{ borderRadius: 8, marginBottom: 16 }}>
        <Space>
          <Select placeholder="选择指标" allowClear style={{ width: 200 }} value={metricCode || undefined} onChange={setMetricCode}
            options={[{ value: 'GMV', label: 'GMV' }, { value: 'ORDER_COUNT', label: '订单量' }, { value: 'PROFIT_RATE', label: '利润率' }, { value: 'CUSTOMER_COUNT', label: '客户数' }]} />
          <Button type="primary" onClick={fetchData}>查询</Button>
        </Space>
      </Card>
      <Row gutter={[16, 16]}>
        <Col xs={24} lg={16}>
          <Card title="趋势图" style={{ borderRadius: 8 }}>
            <ReactEChartsCore echarts={echarts} option={trendOption} style={{ height: 400 }} />
          </Card>
        </Col>
        <Col xs={24} lg={8}>
          <Card title="排行榜" style={{ borderRadius: 8 }}>
            <Table rowKey="rankingId" columns={rankingColumns} dataSource={rankings} size="small" pagination={false} />
          </Card>
        </Col>
      </Row>
    </div>
  );
}
