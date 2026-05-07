'use client';

import { useState } from 'react';
import { Table, Card, Typography, Tag, Space, Select, Badge } from 'antd';
import { usePageApi } from '@/lib/hooks';
import type { QualityInspection, PageParams } from '@/types';

const { Title } = Typography;

const resultMap: Record<string, { color: string; text: string }> = {
  PASSED: { color: 'green', text: '合格' }, FAILED: { color: 'red', text: '不合格' }, PENDING: { color: 'orange', text: '待检' },
};

export default function QualityPage() {
  const [params, setParams] = useState<PageParams & Record<string, unknown>>({ page: 1, size: 20 });
  const { data } = usePageApi<QualityInspection>('/crm/api/in/v1/quality-inspections', params);

  const columns = [
    { title: '质检单号', dataIndex: 'inspectionNo', key: 'inspectionNo' },
    { title: '关联订单', dataIndex: 'orderId', key: 'orderId', render: (v: string) => <a href={`/oms/orders?orderId=${v}`}>{v}</a> },
    { title: '产品', dataIndex: 'productId', key: 'productId', render: (v: string) => <a href={`/pdm/products?productId=${v}`}>{v}</a> },
    { title: '质检结果', dataIndex: 'result', key: 'result', render: (v: string) => <Badge color={resultMap[v]?.color || 'default'} text={resultMap[v]?.text || v} /> },
    { title: '缺陷数', dataIndex: 'defectCount', key: 'defectCount' },
    { title: '质检员', dataIndex: 'inspector', key: 'inspector' },
    { title: '日期', dataIndex: 'inspectionDate', key: 'inspectionDate', render: (v: string) => v ? new Date(v).toLocaleDateString() : '-' },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={4} style={{ margin: 0 }}>质检管理</Title>
        <Select placeholder="质检结果" allowClear style={{ width: 120 }}
          options={Object.entries(resultMap).map(([k, v]) => ({ value: k, label: v.text }))}
          onChange={(v) => setParams({ ...params, result: v })} />
      </div>
      <Card style={{ borderRadius: 8 }}>
        <Table rowKey="inspectionId" columns={columns} dataSource={data?.list || []} size="middle"
          pagination={{ current: params.page, pageSize: params.size, total: data?.total || 0, onChange: (page, size) => setParams({ ...params, page, size }) }} />
      </Card>
    </div>
  );
}
