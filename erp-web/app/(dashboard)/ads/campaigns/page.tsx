'use client';

import { useState } from 'react';
import { Table, Button, Modal, Form, Input, Select, Tag, Space, message, Card, Typography, InputNumber, Statistic, Row, Col, Descriptions, Tabs } from 'antd';
import { PlusOutlined, PauseCircleOutlined, PlayCircleOutlined, SyncOutlined, SearchOutlined } from '@ant-design/icons';
import { usePageApi, useApi } from '@/lib/hooks';
import { adsApi } from '@/lib/api';
import type { AdCampaign, AdGroup, AdKeyword, PageParams } from '@/types';

const { Title } = Typography;

const campaignTypeMap: Record<string, string> = {
  SPONSORED_PRODUCTS: '商品推广',
  SPONSORED_BRANDS: '品牌推广',
  SPONSORED_DISPLAY: '展示推广',
};

const statusMap: Record<string, { color: string; text: string }> = {
  RUNNING: { color: 'green', text: '运行中' },
  PAUSED: { color: 'orange', text: '已暂停' },
  ENDED: { color: 'default', text: '已结束' },
  DRAFT: { color: 'blue', text: '草稿' },
};

export default function CampaignsPage() {
  const [params, setParams] = useState<PageParams & Record<string, unknown>>({ page: 1, size: 20 });
  const [modalOpen, setModalOpen] = useState(false);
  const [detailOpen, setDetailOpen] = useState(false);
  const [selectedCampaign, setSelectedCampaign] = useState<AdCampaign | null>(null);
  const [form] = Form.useForm();
  const { data, mutate } = usePageApi<AdCampaign>('/ads/api/in/v1/campaigns', params);
  const { data: adGroups } = useApi<AdGroup[]>(
    selectedCampaign ? `/ads/api/in/v1/campaigns/${selectedCampaign.campaignId}/groups` : null
  );

  const handleCreate = () => {
    form.resetFields();
    setModalOpen(true);
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      await adsApi.createCampaign(values);
      message.success('广告活动创建成功');
      setModalOpen(false);
      mutate();
    } catch { message.error('创建失败'); }
  };

  const handlePause = async (id: string) => {
    try { await adsApi.pauseCampaign(id); message.success('已暂停'); mutate(); }
    catch { message.error('操作失败'); }
  };

  const handleResume = async (id: string) => {
    try { await adsApi.resumeCampaign(id); message.success('已恢复'); mutate(); }
    catch { message.error('操作失败'); }
  };

  const handleSync = async () => {
    try {
      await adsApi.syncCampaignData(params.storeId as string || 'all');
      message.success('同步任务已发起');
      mutate();
    } catch { message.error('同步失败'); }
  };

  const showDetail = (record: AdCampaign) => {
    setSelectedCampaign(record);
    setDetailOpen(true);
  };

  const columns = [
    { title: '活动名称', dataIndex: 'name', key: 'name', width: 200, render: (v: string, r: AdCampaign) => (
      <a onClick={() => showDetail(r)}>{v}</a>
    )},
    { title: '类型', dataIndex: 'campaignType', key: 'campaignType', render: (v: string) => (
      <Tag>{campaignTypeMap[v] || v}</Tag>
    )},
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: string) => {
      const s = statusMap[v] || { color: 'default', text: v };
      return <Tag color={s.color}>{s.text}</Tag>;
    }},
    { title: '预算', dataIndex: 'budget', key: 'budget', render: (v: number) => `$${v?.toFixed(2) ?? '0.00'}` },
    { title: '花费', dataIndex: 'spent', key: 'spent', render: (v: number) => `$${v?.toFixed(2) ?? '0.00'}` },
    { title: 'ACoS', dataIndex: 'acos', key: 'acos', render: (v: number) => v != null ? `${(v * 100).toFixed(1)}%` : '-' },
    { title: '曝光', dataIndex: 'impressions', key: 'impressions' },
    { title: '点击', dataIndex: 'clicks', key: 'clicks' },
    { title: 'CTR', dataIndex: 'ctr', key: 'ctr', render: (v: number) => v != null ? `${(v * 100).toFixed(2)}%` : '-' },
    { title: '订单', dataIndex: 'orders', key: 'orders' },
    { title: 'ROAS', dataIndex: 'roas', key: 'roas', render: (v: number) => v != null ? v.toFixed(2) : '-' },
    { title: '操作', key: 'action', width: 150, fixed: 'right' as const, render: (_: unknown, record: AdCampaign) => (
      <Space>
        {record.status === 'RUNNING' && (
          <Button type="link" size="small" icon={<PauseCircleOutlined />} onClick={() => handlePause(record.campaignId)}>暂停</Button>
        )}
        {record.status === 'PAUSED' && (
          <Button type="link" size="small" icon={<PlayCircleOutlined />} onClick={() => handleResume(record.campaignId)}>恢复</Button>
        )}
        <Button type="link" size="small" onClick={() => showDetail(record)}>详情</Button>
      </Space>
    )},
  ];

  return (
    <div>
      <Card>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
          <Title level={4} style={{ margin: 0 }}>广告活动</Title>
          <Space>
            <Input placeholder="搜索活动名称" prefix={<SearchOutlined />} allowClear
              style={{ width: 200 }} onChange={(e) => setParams({ ...params, keyword: e.target.value || undefined })} />
            <Select placeholder="状态" allowClear style={{ width: 120 }}
              options={Object.entries(statusMap).map(([k, v]) => ({ value: k, label: v.text }))}
              onChange={(v) => setParams({ ...params, status: v })} />
            <Select placeholder="类型" allowClear style={{ width: 140 }}
              options={Object.entries(campaignTypeMap).map(([k, v]) => ({ value: k, label: v }))} />
            <Button icon={<SyncOutlined />} onClick={handleSync}>同步数据</Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>创建活动</Button>
          </Space>
        </div>
        <Table
          rowKey="campaignId"
          dataSource={data?.list || []}
          columns={columns}
          scroll={{ x: 1400 }}
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

      <Modal title="创建广告活动" open={modalOpen} onOk={handleSubmit} onCancel={() => setModalOpen(false)} width={560}>
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="活动名称" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="campaignType" label="活动类型" rules={[{ required: true }]}>
            <Select options={Object.entries(campaignTypeMap).map(([k, v]) => ({ value: k, label: v }))} />
          </Form.Item>
          <Form.Item name="budget" label="日预算" rules={[{ required: true }]}>
            <InputNumber min={1} precision={2} style={{ width: '100%' }} prefix="$" />
          </Form.Item>
          <Form.Item name="platform" label="平台">
            <Select options={[{ value: 'AMAZON', label: 'Amazon' }]} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title={`广告活动详情 - ${selectedCampaign?.name || ''}`}
        open={detailOpen}
        onCancel={() => setDetailOpen(false)}
        width={800}
        footer={null}
      >
        {selectedCampaign && (
          <Tabs items={[
            {
              key: 'overview',
              label: '概览',
              children: (
                <div>
                  <Row gutter={16} style={{ marginBottom: 24 }}>
                    <Col span={6}><Statistic title="花费" value={selectedCampaign.spent} prefix="$" precision={2} /></Col>
                    <Col span={6}><Statistic title="ACoS" value={(selectedCampaign.acos ?? 0) * 100} suffix="%" precision={1} /></Col>
                    <Col span={6}><Statistic title="ROAS" value={selectedCampaign.roas ?? 0} precision={2} /></Col>
                    <Col span={6}><Statistic title="订单" value={selectedCampaign.orders ?? 0} /></Col>
                  </Row>
                  <Descriptions column={2} bordered size="small">
                    <Descriptions.Item label="活动类型">{campaignTypeMap[selectedCampaign.campaignType]}</Descriptions.Item>
                    <Descriptions.Item label="状态">{statusMap[selectedCampaign.status]?.text}</Descriptions.Item>
                    <Descriptions.Item label="日预算">${selectedCampaign.budget?.toFixed(2)}</Descriptions.Item>
                    <Descriptions.Item label="曝光">{selectedCampaign.impressions ?? 0}</Descriptions.Item>
                    <Descriptions.Item label="点击">{selectedCampaign.clicks ?? 0}</Descriptions.Item>
                    <Descriptions.Item label="CTR">{selectedCampaign.ctr != null ? `${(selectedCampaign.ctr * 100).toFixed(2)}%` : '-'}</Descriptions.Item>
                    <Descriptions.Item label="开始日期">{selectedCampaign.startDate}</Descriptions.Item>
                    <Descriptions.Item label="结束日期">{selectedCampaign.endDate || '无'}</Descriptions.Item>
                  </Descriptions>
                </div>
              ),
            },
            {
              key: 'groups',
              label: '广告组',
              children: (
                <Table
                  rowKey="groupId"
                  dataSource={adGroups || []}
                  size="small"
                  columns={[
                    { title: '组名', dataIndex: 'groupName', key: 'groupName' },
                    { title: '状态', dataIndex: 'status', key: 'status', render: (v: string) => <Tag>{v}</Tag> },
                    { title: '默认出价', dataIndex: 'defaultBid', key: 'defaultBid', render: (v: number) => `$${v?.toFixed(2) ?? '0.00'}` },
                  ]}
                  pagination={false}
                />
              ),
            },
          ]} />
        )}
      </Modal>
    </div>
  );
}
