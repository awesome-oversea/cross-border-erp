'use client';

import { useState } from 'react';
import { Table, Card, Typography, Tag, Space, Button, Badge } from 'antd';
import { usePageApi } from '@/lib/hooks';
import type { Permission, PageParams } from '@/types';

const { Title } = Typography;

const domainColors: Record<string, string> = {
  order: 'blue', product: 'green', inventory: 'orange', finance: 'gold',
  customer: 'purple', logistics: 'cyan', system: 'red', report: 'geekblue', ads: 'magenta',
};

export default function PermissionsPage() {
  const [params, setParams] = useState<PageParams & Record<string, unknown>>({ page: 1, size: 50 });
  const { data } = usePageApi<Permission>('/iam/api/in/v1/permissions', params);

  const columns = [
    { title: '权限编码', dataIndex: 'code', key: 'code', render: (v: string) => <code style={{ fontSize: 13 }}>{v}</code> },
    { title: '权限名称', dataIndex: 'name', key: 'name' },
    { title: '域', dataIndex: 'domain', key: 'domain', render: (v: string) => <Tag color={domainColors[v] || 'default'}>{v}</Tag> },
    { title: '类型', dataIndex: 'type', key: 'type', render: (v: string) => <Badge color={v === 'WRITE' ? 'red' : 'blue'} text={v} /> },
    { title: '描述', dataIndex: 'description', key: 'description', ellipsis: true },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={4} style={{ margin: 0 }}>权限管理</Title>
        <Space>
          <Tag color="blue">READ 只读</Tag>
          <Tag color="red">WRITE 写入</Tag>
        </Space>
      </div>
      <Card style={{ borderRadius: 8 }}>
        <Table rowKey="permissionId" columns={columns} dataSource={data?.list || []} size="middle"
          pagination={{ current: params.page, pageSize: params.size, total: data?.total || 0, onChange: (page, size) => setParams({ ...params, page, size }) }} />
      </Card>
    </div>
  );
}
