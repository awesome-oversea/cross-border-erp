'use client';

import { useState } from 'react';
import { Table, Card, Typography, Tag, Space, Input, Select } from 'antd';
import { usePageApi } from '@/lib/hooks';
import type { Customer, PageParams } from '@/types';

const { Title } = Typography;

const levelColors: Record<string, string> = { VIP: 'gold', NORMAL: 'blue', NEW: 'green' };

export default function CustomersPage() {
  const [params, setParams] = useState<PageParams & Record<string, unknown>>({ page: 1, size: 20 });
  const { data } = usePageApi<Customer>('/crm/api/in/v1/customers', params);

  const columns = [
    { title: '客户名', dataIndex: 'name', key: 'name' },
    { title: '邮箱', dataIndex: 'email', key: 'email' },
    { title: '国家', dataIndex: 'countryCode', key: 'countryCode', render: (v: string) => <Tag>{v}</Tag> },
    { title: '平台', dataIndex: 'platform', key: 'platform', render: (v: string) => <Tag color="blue">{v}</Tag> },
    { title: '累计消费', dataIndex: 'totalSpent', key: 'totalSpent', render: (v: number) => v?.toFixed(2) },
    { title: '订单数', dataIndex: 'totalOrders', key: 'totalOrders' },
    { title: '最近下单', dataIndex: 'lastOrderAt', key: 'lastOrderAt', render: (v: string) => v ? new Date(v).toLocaleDateString() : '-' },
    { title: '工单', key: 'tickets', render: (_: unknown, r: Customer) => <a href={`/crm/tickets?customerId=${r.customerId}`}>查看</a> },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={4} style={{ margin: 0 }}>客户管理</Title>
        <Space>
          <Input placeholder="搜索客户" allowClear style={{ width: 200 }}
            onChange={(e) => setParams({ ...params, keyword: e.target.value || undefined })} />
          <Select placeholder="平台" allowClear style={{ width: 120 }}
            options={[{ value: 'AMAZON', label: 'Amazon' }, { value: 'SHOPIFY', label: 'Shopify' }, { value: 'EBAY', label: 'eBay' }]}
            onChange={(v) => setParams({ ...params, platform: v })} />
        </Space>
      </div>
      <Card style={{ borderRadius: 8 }}>
        <Table rowKey="customerId" columns={columns} dataSource={data?.list || []} size="middle"
          pagination={{ current: params.page, pageSize: params.size, total: data?.total || 0, onChange: (page, size) => setParams({ ...params, page, size }) }} />
      </Card>
    </div>
  );
}
