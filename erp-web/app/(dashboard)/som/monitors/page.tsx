'use client';

import { useState } from 'react';
import { Table, Button, Tag, Space, message, Card, Typography, Select, Input } from 'antd';
import { SyncOutlined, SearchOutlined, AlertOutlined } from '@ant-design/icons';
import { usePageApi } from '@/lib/hooks';
import { somApi } from '@/lib/api';
import type { ListingMonitor, PageParams } from '@/types';

const { Title } = Typography;

const statusMap: Record<string, { color: string; text: string }> = {
  WINNING: { color: 'green', text: '赢得Buy Box' },
  LOSING: { color: 'red', text: '失去Buy Box' },
  NO_BUY_BOX: { color: 'orange', text: '无Buy Box' },
};

export default function ListingMonitorsPage() {
  const [params, setParams] = useState<PageParams & Record<string, unknown>>({ page: 1, size: 20 });
  const { data, mutate } = usePageApi<ListingMonitor>('/som/api/in/v1/listing-monitors', params);

  const handleRefresh = async (listingId: string) => {
    try {
      await somApi.refreshListingMonitor(listingId);
      message.success('监控数据已刷新');
      mutate();
    } catch { message.error('刷新失败'); }
  };

  const columns = [
    { title: 'SKU', dataIndex: 'sellerSku', key: 'sellerSku', render: (v: string, r: ListingMonitor) => (
      <a href={`/som/listings?listingId=${r.listingId}`}>{v || r.listingId}</a>
    )},
    { title: 'Buy Box状态', dataIndex: 'status', key: 'status', render: (v: string) => {
      const s = statusMap[v] || { color: 'default', text: v };
      return <Tag color={s.color}>{s.text}</Tag>;
    }},
    { title: 'Buy Box价格', dataIndex: 'buyBoxPrice', key: 'buyBoxPrice', render: (v: number) => v != null ? `$${v.toFixed(2)}` : '-' },
    { title: 'Buy Box持有者', dataIndex: 'buyBoxOwner', key: 'buyBoxOwner', render: (v: string) => v || '-' },
    { title: '我的价格', dataIndex: 'ourPrice', key: 'ourPrice', render: (v: number) => v != null ? `$${v.toFixed(2)}` : '-' },
    { title: '价格差距', dataIndex: 'priceGap', key: 'priceGap', render: (v: number) => v != null ? (
      <span style={{ color: v > 0 ? '#ff4d4f' : v < 0 ? '#52c41a' : '#999' }}>
        {v > 0 ? '+' : ''}{v.toFixed(2)}
      </span>
    ) : '-' },
    { title: '检查时间', dataIndex: 'checkedAt', key: 'checkedAt', render: (v: string) => v ? new Date(v).toLocaleString() : '-' },
    { title: '操作', key: 'action', width: 100, render: (_: unknown, record: ListingMonitor) => (
      <Space>
        <Button type="link" size="small" icon={<SyncOutlined />} onClick={() => handleRefresh(record.listingId)}>刷新</Button>
      </Space>
    )},
  ];

  return (
    <div>
      <Card>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
          <Title level={4} style={{ margin: 0 }}>Listing监控</Title>
          <Space>
            <Input placeholder="搜索SKU" prefix={<SearchOutlined />} allowClear
              style={{ width: 200 }} onChange={(e) => setParams({ ...params, keyword: e.target.value || undefined })} />
            <Select placeholder="Buy Box状态" allowClear style={{ width: 140 }}
              options={Object.entries(statusMap).map(([k, v]) => ({ value: k, label: v.text }))}
              onChange={(v) => setParams({ ...params, status: v })} />
          </Space>
        </div>
        <Table
          rowKey="monitorId"
          dataSource={data?.list || []}
          columns={columns}
          scroll={{ x: 900 }}
          pagination={{
            current: params.page,
            pageSize: params.size,
            total: data?.total || 0,
            onChange: (page, size) => setParams({ ...params, page, size }),
            showSizeChanger: true,
            showTotal: (total) => `共 ${total} 条`,
          }}
        />
      </Card>
    </div>
  );
}
