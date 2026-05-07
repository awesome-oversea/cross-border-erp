'use client';

import { useState } from 'react';
import { Card, Typography, Row, Col, Statistic, Table, Tag, Space, Select, DatePicker, Tabs, Badge, Button } from 'antd';
import { DollarOutlined, ArrowUpOutlined, ArrowDownOutlined, SyncOutlined } from '@ant-design/icons';
import ReactEChartsCore from 'echarts-for-react';
import * as echarts from 'echarts/core';
import { LineChart, BarChart } from 'echarts/charts';
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { useApi, usePageApi } from '@/lib/hooks';
import { fmsApi } from '@/lib/api';
import type { ProfitSummary, Reconciliation, PageParams } from '@/types';

echarts.use([LineChart, BarChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer]);

const { Title } = Typography;
const { RangePicker } = DatePicker;

export default function ProfitPage() {
  const [dateRange, setDateRange] = useState<Record<string, string>>({});
  const [reconParams, setReconParams] = useState<PageParams & Record<string, unknown>>({ page: 1, size: 20 });
  const { data: profitSummaries } = useApi<ProfitSummary[]>('/fms/api/in/v1/profit/summary', dateRange);
  const { data: reconciliations, mutate: mutateRecon } = usePageApi<Reconciliation>('/fms/api/in/v1/reconciliations', reconParams);

  const latest = profitSummaries?.[0];
  const prev = profitSummaries?.[1];

  const profitTrendOption = {
    tooltip: { trigger: 'axis' as const },
    legend: { data: ['收入', '成本', '利润'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category' as const, data: profitSummaries?.map((p: ProfitSummary) => p.period) || [] },
    yAxis: { type: 'value' as const, name: 'USD' },
    series: [
      { name: '收入', type: 'bar', data: profitSummaries?.map((p: ProfitSummary) => p.revenue) || [], itemStyle: { color: '#1890ff' } },
      { name: '成本', type: 'bar', data: profitSummaries?.map((p: ProfitSummary) => p.totalCost) || [], itemStyle: { color: '#ff4d4f' } },
      { name: '利润', type: 'line', data: profitSummaries?.map((p: ProfitSummary) => p.profit) || [], itemStyle: { color: '#52c41a' }, lineStyle: { width: 3 } },
    ],
  };

  const profitColumns = [
    { title: '期间', dataIndex: 'period', key: 'period' },
    { title: '收入', dataIndex: 'revenue', key: 'revenue', render: (v: number) => `$${(v || 0).toLocaleString()}` },
    { title: '产品成本', dataIndex: 'productCost', key: 'productCost', render: (v: number) => `$${(v || 0).toLocaleString()}` },
    { title: '物流成本', dataIndex: 'logisticsCost', key: 'logisticsCost', render: (v: number) => `$${(v || 0).toLocaleString()}` },
    { title: '平台佣金', dataIndex: 'platformFee', key: 'platformFee', render: (v: number) => `$${(v || 0).toLocaleString()}` },
    { title: '广告成本', dataIndex: 'adCost', key: 'adCost', render: (v: number) => `$${(v || 0).toLocaleString()}` },
    { title: '其他成本', dataIndex: 'otherCost', key: 'otherCost', render: (v: number) => `$${(v || 0).toLocaleString()}` },
    { title: '总成本', dataIndex: 'totalCost', key: 'totalCost', render: (v: number) => `$${(v || 0).toLocaleString()}` },
    { title: '利润', dataIndex: 'profit', key: 'profit', render: (v: number) => (
      <span style={{ color: v >= 0 ? '#52c41a' : '#ff4d4f', fontWeight: 600 }}>${(v || 0).toLocaleString()}</span>
    )},
    { title: '利润率', dataIndex: 'margin', key: 'margin', render: (v: number) => (
      <Tag color={v >= 30 ? 'green' : v >= 20 ? 'orange' : 'red'}>{(v || 0).toFixed(1)}%</Tag>
    )},
  ];

  const reconColumns = [
    { title: '对账单号', dataIndex: 'reconciliationId', key: 'reconciliationId' },
    { title: '平台', dataIndex: 'platform', key: 'platform', render: (v: string) => <Tag>{v}</Tag> },
    { title: '期间', dataIndex: 'period', key: 'period' },
    { title: '系统金额', dataIndex: 'systemAmount', key: 'systemAmount', render: (v: number) => `$${(v || 0).toFixed(2)}` },
    { title: '平台金额', dataIndex: 'platformAmount', key: 'platformAmount', render: (v: number) => `$${(v || 0).toFixed(2)}` },
    { title: '差异', dataIndex: 'difference', key: 'difference', render: (v: number) => (
      <span style={{ color: v !== 0 ? '#ff4d4f' : '#52c41a' }}>${(v || 0).toFixed(2)}</span>
    )},
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: string) => (
      <Badge color={v === 'MATCHED' ? 'green' : v === 'MISMATCH' ? 'red' : 'orange'} text={v === 'MATCHED' ? '匹配' : v === 'MISMATCH' ? '不匹配' : '待处理'} />
    )},
    { title: '操作', key: 'action', render: (_: unknown, r: Reconciliation) => r.status === 'MISMATCH' ? (
      <Button type="link" size="small" onClick={async () => {
        try {
          await fmsApi.resolveReconciliation(r.reconciliationId, { resolution: '已核实' });
          mutateRecon();
        } catch {}
      }}>处理差异</Button>
    ) : null },
  ];

  return (
    <div>
      <Card>
        <Tabs items={[
          {
            key: 'profit',
            label: '利润报表',
            children: (
              <>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
                  <Title level={4} style={{ margin: 0 }}>利润报表</Title>
                  <Space>
                    <RangePicker onChange={(_, ds) => setDateRange({ startDate: ds[0], endDate: ds[1] })} />
                  </Space>
                </div>
                <Row gutter={[16, 16]}>
                  <Col xs={24} sm={8}>
                    <Card style={{ borderRadius: 8 }}>
                      <Statistic title="本期收入" value={latest?.revenue ?? 0} prefix="$" valueStyle={{ color: '#1890ff' }}
                        suffix={prev ? <span style={{ fontSize: 12 }}>
                          {latest?.revenue && prev.revenue && latest.revenue > prev.revenue ? <ArrowUpOutlined style={{ color: '#52c41a' }} /> : <ArrowDownOutlined style={{ color: '#ff4d4f' }} />}
                        </span> : null} />
                    </Card>
                  </Col>
                  <Col xs={24} sm={8}>
                    <Card style={{ borderRadius: 8 }}>
                      <Statistic title="本期成本" value={latest?.totalCost ?? 0} prefix="$" valueStyle={{ color: '#ff4d4f' }} />
                    </Card>
                  </Col>
                  <Col xs={24} sm={8}>
                    <Card style={{ borderRadius: 8 }}>
                      <Statistic title="本期利润" value={latest?.profit ?? 0} prefix="$" valueStyle={{ color: '#52c41a' }}
                        suffix={<span style={{ fontSize: 14 }}>{latest?.margin?.toFixed(1) ?? 0}%</span>} />
                    </Card>
                  </Col>
                </Row>
                <Card title="利润趋势" style={{ marginTop: 16, borderRadius: 8 }}>
                  <ReactEChartsCore echarts={echarts} option={profitTrendOption} style={{ height: 360 }} />
                </Card>
                <Card title="月度明细" style={{ marginTop: 16, borderRadius: 8 }}>
                  <Table columns={profitColumns} dataSource={profitSummaries || []} rowKey="period" pagination={false} size="middle" scroll={{ x: 1000 }} />
                </Card>
              </>
            ),
          },
          {
            key: 'reconciliation',
            label: '对账管理',
            children: (
              <>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
                  <Title level={4} style={{ margin: 0 }}>对账管理</Title>
                  <Select placeholder="状态" allowClear style={{ width: 120 }}
                    options={[{ value: 'PENDING', label: '待处理' }, { value: 'MATCHED', label: '匹配' }, { value: 'MISMATCH', label: '不匹配' }]}
                    onChange={(v) => setReconParams({ ...reconParams, status: v })} />
                </div>
                <Table
                  rowKey="reconciliationId"
                  columns={reconColumns}
                  dataSource={reconciliations?.list || []}
                  pagination={{
                    current: reconParams.page,
                    pageSize: reconParams.size,
                    total: reconciliations?.total || 0,
                    onChange: (page, size) => setReconParams({ ...reconParams, page, size }),
                    showTotal: (total) => `共 ${total} 条`,
                  }}
                />
              </>
            ),
          },
        ]} />
      </Card>
    </div>
  );
}
