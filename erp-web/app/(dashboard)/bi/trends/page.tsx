'use client';

import { useState } from 'react';
import { Table, Card, Typography, Tag, Space, Select, DatePicker } from 'antd';
import { usePageApi } from '@/lib/hooks';
import type { TrendData, PageParams } from '@/types';

const { Title } = Typography;
const { RangePicker } = DatePicker;

const metricMap: Record<string, string> = {
  REVENUE: '营收', ORDER_COUNT: '订单量', PROFIT: '利润',
  CONVERSION_RATE: '转化率', AD_SPEND: '广告花费', RETURN_RATE: '退货率',
};

export default function TrendsPage() {
  const [params, setParams] = useState<PageParams & Record<string, unknown>>({ page: 1, size: 30 });
  const { data } = usePageApi<TrendData>('/bi/api/in/v1/trends', params);

  const columns = [
    { title: '日期', dataIndex: 'date', key: 'date' },
    { title: '指标', dataIndex: 'metric', key: 'metric', render: (v: string) => <Tag color="blue">{metricMap[v] || v}</Tag> },
    { title: '数值', dataIndex: 'value', key: 'value', render: (v: number) => typeof v === 'number' ? v.toFixed(2) : v },
    { title: '环比', dataIndex: 'momChange', key: 'momChange', render: (v: number) => (
      <span style={{ color: v > 0 ? '#52c41a' : v < 0 ? '#ff4d4f' : '#999' }}>
        {v > 0 ? '↑' : v < 0 ? '↓' : '-'}{v != null ? `${Math.abs(v * 100).toFixed(1)}%` : '-'}
      </span>
    )},
    { title: '同比', dataIndex: 'yoyChange', key: 'yoyChange', render: (v: number) => (
      <span style={{ color: v > 0 ? '#52c41a' : v < 0 ? '#ff4d4f' : '#999' }}>
        {v > 0 ? '↑' : v < 0 ? '↓' : '-'}{v != null ? `${Math.abs(v * 100).toFixed(1)}%` : '-'}
      </span>
    )},
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={4} style={{ margin: 0 }}>趋势分析</Title>
        <Space>
          <Select placeholder="指标" allowClear style={{ width: 140 }}
            options={Object.entries(metricMap).map(([k, v]) => ({ value: k, label: v }))}
            onChange={(v) => setParams({ ...params, metric: v })} />
        </Space>
      </div>
      <Card style={{ borderRadius: 8 }}>
        <Table rowKey={(r: TrendData) => `${r.date}-${r.metric}`} columns={columns} dataSource={data?.list || []} size="middle"
          pagination={{ current: params.page, pageSize: params.size, total: data?.total || 0, onChange: (page, size) => setParams({ ...params, page, size }) }} />
      </Card>
    </div>
  );
}
