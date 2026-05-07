'use client';

import { useState } from 'react';
import { Table, Card, Typography, Tag, Select, Space, Button } from 'antd';
import { usePageApi } from '@/lib/hooks';
import type { ReportDefinition, PageParams } from '@/types';

const { Title } = Typography;

const typeMap: Record<string, { color: string; text: string }> = {
  DAILY: { color: 'blue', text: '日报' },
  WEEKLY: { color: 'cyan', text: '周报' },
  MONTHLY: { color: 'purple', text: '月报' },
  ADHOC: { color: 'orange', text: '临时报表' },
};

export default function ReportsPage() {
  const [params, setParams] = useState<PageParams & Record<string, unknown>>({ page: 1, size: 20 });
  const { data } = usePageApi<ReportDefinition>('/bi/api/in/v1/reports', params);

  const columns = [
    { title: '报表编码', dataIndex: 'reportCode', key: 'reportCode' },
    { title: '报表名称', dataIndex: 'reportName', key: 'reportName' },
    { title: '类型', dataIndex: 'reportType', key: 'reportType', render: (v: string) => <Tag color={typeMap[v]?.color || 'default'}>{typeMap[v]?.text || v}</Tag> },
    { title: '数据源', dataIndex: 'dataSource', key: 'dataSource', render: (v: string) => <Tag>{v}</Tag> },
    { title: '描述', dataIndex: 'description', key: 'description', ellipsis: true },
    { title: '操作', key: 'action', render: () => <Button type="link">查看</Button> },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={4} style={{ margin: 0 }}>报表中心</Title>
        <Select placeholder="报表类型" allowClear style={{ width: 160 }}
          options={Object.entries(typeMap).map(([k, v]) => ({ value: k, label: v.text }))}
          onChange={(v) => setParams({ ...params, reportType: v })} />
      </div>
      <Card style={{ borderRadius: 8 }}>
        <Table rowKey="reportId" columns={columns} dataSource={data?.list || []} size="middle"
          pagination={{ current: params.page, pageSize: params.size, total: data?.total || 0, onChange: (page, size) => setParams({ ...params, page, size }) }} />
      </Card>
    </div>
  );
}
