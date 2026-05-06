'use client';

import { useState, useEffect, useCallback } from 'react';
import { Table, Card, Typography, Tag, Select, Space, Button, Badge } from 'antd';
import { biApi } from '@/lib/api';
import type { ReportDefinition } from '@/types';

const { Title } = Typography;

const typeMap: Record<string, { color: string; text: string }> = {
  DAILY: { color: 'blue', text: '日报' },
  WEEKLY: { color: 'cyan', text: '周报' },
  MONTHLY: { color: 'purple', text: '月报' },
  ADHOC: { color: 'orange', text: '临时报表' },
};

export default function ReportsPage() {
  const [reports, setReports] = useState<ReportDefinition[]>([]);
  const [loading, setLoading] = useState(false);
  const [reportType, setReportType] = useState<string>('');

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const data = await biApi.listReports(reportType || undefined);
      setReports(data || []);
    } catch {
      setReports([]);
    } finally {
      setLoading(false);
    }
  }, [reportType]);

  useEffect(() => { fetchData(); }, [fetchData]);

  const columns = [
    { title: '报表编码', dataIndex: 'reportCode', key: 'reportCode' },
    { title: '报表名称', dataIndex: 'reportName', key: 'reportName' },
    { title: '类型', dataIndex: 'reportType', key: 'reportType', render: (v: string) => <Tag color={typeMap[v]?.color || 'default'}>{typeMap[v]?.text || v}</Tag> },
    { title: '数据源', dataIndex: 'dataSource', key: 'dataSource', render: (v: string) => <Tag>{v}</Tag> },
    { title: '描述', dataIndex: 'description', key: 'description', ellipsis: true },
    {
      title: '操作', key: 'action',
      render: () => <Button type="link">查看</Button>,
    },
  ];

  return (
    <div>
      <Title level={4} style={{ marginBottom: 16 }}>报表中心</Title>
      <Card style={{ borderRadius: 8, marginBottom: 16 }}>
        <Space>
          <Select placeholder="报表类型" allowClear style={{ width: 160 }} value={reportType || undefined} onChange={setReportType}
            options={Object.entries(typeMap).map(([k, v]) => ({ value: k, label: v.text }))} />
          <Button type="primary" onClick={fetchData}>查询</Button>
        </Space>
      </Card>
      <Card style={{ borderRadius: 8 }}>
        <Table rowKey="reportId" columns={columns} dataSource={reports} loading={loading} size="middle" />
      </Card>
    </div>
  );
}
