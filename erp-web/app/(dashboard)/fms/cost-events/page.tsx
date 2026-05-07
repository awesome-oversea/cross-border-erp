'use client';

import { useState } from 'react';
import { Table, Card, Typography, Tag, Space, Select, Badge } from 'antd';
import { usePageApi } from '@/lib/hooks';
import type { CostEvent, PageParams } from '@/types';

const { Title } = Typography;

const typeMap: Record<string, { color: string; text: string }> = {
  COMMISSION: { color: 'orange', text: '佣金' }, SHIPPING: { color: 'blue', text: '运费' },
  ADVERTISING: { color: 'magenta', text: '广告' }, STORAGE: { color: 'cyan', text: '仓储' },
  RETURN: { color: 'red', text: '退货' }, OTHER: { color: 'default', text: '其他' },
};

export default function CostEventsPage() {
  const [params, setParams] = useState<PageParams & Record<string, unknown>>({ page: 1, size: 20 });
  const { data } = usePageApi<CostEvent>('/fms/api/in/v1/cost-events', params);

  const columns = [
    { title: '费用编号', dataIndex: 'costEventId', key: 'costEventId' },
    { title: '类型', dataIndex: 'costType', key: 'costType', render: (v: string) => <Tag color={typeMap[v]?.color || 'default'}>{typeMap[v]?.text || v}</Tag> },
    { title: '金额', dataIndex: 'amount', key: 'amount', render: (v: number, r: CostEvent) => `${r.currency} ${v?.toFixed(2)}` },
    { title: '关联订单', dataIndex: 'orderId', key: 'orderId', render: (v: string) => v ? <a href={`/oms/orders?orderId=${v}`}>{v}</a> : '-' },
    { title: '关联广告', dataIndex: 'campaignId', key: 'campaignId', render: (v: string) => v ? <a href={`/ads/campaigns?campaignId=${v}`}>{v}</a> : '-' },
    { title: '日期', dataIndex: 'eventDate', key: 'eventDate', render: (v: string) => v ? new Date(v).toLocaleDateString() : '-' },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={4} style={{ margin: 0 }}>费用事件</Title>
        <Space>
          <Select placeholder="费用类型" allowClear style={{ width: 140 }}
            options={Object.entries(typeMap).map(([k, v]) => ({ value: k, label: v.text }))}
            onChange={(v) => setParams({ ...params, costType: v })} />
        </Space>
      </div>
      <Card style={{ borderRadius: 8 }}>
        <Table rowKey="costEventId" columns={columns} dataSource={data?.list || []} size="middle"
          pagination={{ current: params.page, pageSize: params.size, total: data?.total || 0, onChange: (page, size) => setParams({ ...params, page, size }) }} />
      </Card>
    </div>
  );
}
