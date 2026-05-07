'use client';

import { useState } from 'react';
import { Table, Button, Tag, Space, message, Card, Typography, Select, Input, Progress } from 'antd';
import { SyncOutlined, SearchOutlined, AlertOutlined } from '@ant-design/icons';
import { usePageApi } from '@/lib/hooks';
import { somApi } from '@/lib/api';
import type { ListingMonitor, PageParams } from '@/types';

const { Title } = Typography;

const healthMap: Record<string, { color: string; text: string }> = {
  HEALTHY: { color: 'green', text: '健康' },
  WARNING: { color: 'orange', text: '警告' },
  CRITICAL: { color: 'red', text: '危险' },
  UNKNOWN: { color: 'default', text: '未知' },
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
    { title: 'Listing', key: 'listing', render: (_: unknown, r: ListingMonitor) => (
      <div>
        <div style={{ fontWeight: 500 }}>{r.sku || r.listingId}</div>
        <div style={{ fontSize: 12, color: '#999' }}>{r.asin || ''}</div>
      </div>
    )},
    { title: '健康状态', dataIndex: 'healthStatus', key: 'healthStatus', render: (v: string) => {
      const s = healthMap[v] || { color: 'default', text: v };
      return <Tag color={s.color}>{s.text}</Tag>;
    }},
    { title: 'Buy Box', dataIndex: 'buyBoxOwner', key: 'buyBoxOwner', render: (v: string) => v || '-' },
    { title: 'Buy Box价格', dataIndex: 'buyBoxPrice', key: 'buyBoxPrice', render: (v: number) => v != null ? `$${v.toFixed(2)}` : '-' },
    { title: '我的价格', dataIndex: 'ourPrice', key: 'ourPrice', render: (v: number) => v != null ? `$${v.toFixed(2)}` : '-' },
    { title: '竞争者数', dataIndex: 'competitorCount', key: 'competitorCount', render: (v: number) => v ?? '-' },
    { title: '评分', dataIndex: 'rating', key: 'rating', render: (v: number) => v != null ? (
      <Progress percent={v * 20} size="small" format={() => v.toFixed(1)} />
    ) : '-' },
    { title: '评论数', dataIndex: 'reviewCount', key: 'reviewCount', render: (v: number) => v ?? '-' },
    { title: '操作', key: 'action', width: 100, render: (_: unknown, record: ListingMonitor) => (
      <Space>
        <Button type="link" size="small" icon={<SyncOutlined />} onClick={() => handleRefresh(record.listingId)}>刷新</Button>
        <Button type="link" size="small" onClick={() => {
          window.location.href = `/som/listings?listingId=${record.listingId}`;
        }}>详情</Button>
      </Space>
    )},
  ];

  return (
    <div>
      <Card>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
          <Title level={4} style={{ margin: 0 }}>Listing监控</Title>
          <Space>
            <Input placeholder="搜索SKU/ASIN" prefix={<SearchOutlined />} allowClear
              style={{ width: 200 }} onChange={(e) => setParams({ ...params, keyword: e.target.value || undefined })} />
            <Select placeholder="健康状态" allowClear style={{ width: 120 }}
              options={Object.entries(healthMap).map(([k, v]) => ({ value: k, label: v.text }))}
              onChange={(v) => setParams({ ...params, healthStatus: v })} />
            <Button icon={<AlertOutlined />}>异常导出</Button>
          </Space>
        </div>
        <Table
          rowKey="monitorId"
          dataSource={data?.list || []}
          columns={columns}
          scroll={{ x: 1100 }}
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
