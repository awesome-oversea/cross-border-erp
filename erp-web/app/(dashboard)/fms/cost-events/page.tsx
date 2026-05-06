'use client';

import { useState, useEffect, useCallback } from 'react';
import { Table, Card, Typography, Tag, Select, Space, Button, Input, DatePicker, Badge } from 'antd';
import { SearchOutlined } from '@ant-design/icons';
import { fmsApi } from '@/lib/api';
import type { CostEvent } from '@/types';

const { Title } = Typography;

const eventTypeMap: Record<string, { color: string; text: string }> = {
  PURCHASE: { color: 'blue', text: '采购成本' },
  SHIPPING: { color: 'cyan', text: '物流成本' },
  PLATFORM_FEE: { color: 'orange', text: '平台费用' },
  ADVERTISING: { color: 'purple', text: '广告费用' },
  WAREHOUSE: { color: 'geekblue', text: '仓储费用' },
  TAX: { color: 'red', text: '税费' },
  OTHER: { color: 'default', text: '其他' },
};

export default function CostEventsPage() {
  const [events, setEvents] = useState<CostEvent[]>([]);
  const [loading, setLoading] = useState(false);
  const [eventType, setEventType] = useState<string>('');

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const params: Record<string, string> = {};
      if (eventType) params.eventType = eventType;
      const data = await fmsApi.listCostEvents(params);
      setEvents(data || []);
    } catch {
      setEvents([]);
    } finally {
      setLoading(false);
    }
  }, [eventType]);

  useEffect(() => { fetchData(); }, [fetchData]);

  const columns = [
    { title: '事件类型', dataIndex: 'eventType', key: 'eventType', render: (v: string) => <Tag color={eventTypeMap[v]?.color || 'default'}>{eventTypeMap[v]?.text || v}</Tag> },
    { title: '来源类型', dataIndex: 'sourceType', key: 'sourceType', render: (v: string) => <Tag>{v}</Tag> },
    { title: '来源ID', dataIndex: 'sourceId', key: 'sourceId' },
    { title: '金额', dataIndex: 'amount', key: 'amount', render: (v: number, r: CostEvent) => `${r.currency} ${v?.toFixed(2)}` },
    { title: '币种', dataIndex: 'currency', key: 'currency' },
    { title: '发生时间', dataIndex: 'occurredAt', key: 'occurredAt', render: (v: string) => v ? new Date(v).toLocaleString() : '-' },
  ];

  return (
    <div>
      <Title level={4} style={{ marginBottom: 16 }}>成本事件</Title>
      <Card style={{ borderRadius: 8, marginBottom: 16 }}>
        <Space wrap>
          <Input placeholder="搜索来源ID" prefix={<SearchOutlined />} style={{ width: 240 }} />
          <Select placeholder="事件类型" allowClear style={{ width: 160 }} value={eventType || undefined} onChange={setEventType}
            options={Object.entries(eventTypeMap).map(([k, v]) => ({ value: k, label: v.text }))} />
          <Button type="primary" onClick={fetchData}>查询</Button>
        </Space>
      </Card>
      <Card style={{ borderRadius: 8 }}>
        <Table rowKey="eventId" columns={columns} dataSource={events} loading={loading} size="middle"
          pagination={{ pageSize: 20, showTotal: (t) => `共 ${t} 条` }} />
      </Card>
    </div>
  );
}
