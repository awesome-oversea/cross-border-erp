'use client';

import { useState } from 'react';
import { Table, Button, Modal, Form, Input, Select, Tag, Space, message, Card, Typography, Badge, DatePicker } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { usePageApi } from '@/lib/hooks';
import { crmApi } from '@/lib/api';
import type { Campaign, PageParams } from '@/types';

const { Title } = Typography;
const { RangePicker } = DatePicker;

const channelMap: Record<string, { color: string; text: string }> = {
  EMAIL: { color: 'blue', text: '邮件' }, SMS: { color: 'green', text: '短信' },
  WECHAT: { color: 'cyan', text: '微信' }, WHATSAPP: { color: 'geekblue', text: 'WhatsApp' },
};

const statusMap: Record<string, { color: string; text: string }> = {
  DRAFT: { color: 'default', text: '草稿' }, SCHEDULED: { color: 'orange', text: '已排期' },
  RUNNING: { color: 'blue', text: '进行中' }, COMPLETED: { color: 'green', text: '已完成' },
};

export default function CampaignsPage() {
  const [params, setParams] = useState<PageParams & Record<string, unknown>>({ page: 1, size: 20 });
  const [modalOpen, setModalOpen] = useState(false);
  const [form] = Form.useForm();
  const { data, mutate } = usePageApi<Campaign>('/crm/api/in/v1/campaigns', params);

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      await crmApi.createCampaign(values);
      message.success('营销活动创建成功');
      setModalOpen(false);
      mutate();
    } catch { message.error('操作失败'); }
  };

  const columns = [
    { title: '活动名称', dataIndex: 'name', key: 'name' },
    { title: '渠道', dataIndex: 'channel', key: 'channel', render: (v: string) => <Tag color={channelMap[v]?.color || 'default'}>{channelMap[v]?.text || v}</Tag> },
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: string) => <Badge color={statusMap[v]?.color || 'default'} text={statusMap[v]?.text || v} /> },
    { title: '目标客户数', dataIndex: 'targetCount', key: 'targetCount' },
    { title: '已发送', dataIndex: 'sentCount', key: 'sentCount' },
    { title: '转化率', dataIndex: 'conversionRate', key: 'conversionRate', render: (v: number) => v != null ? `${(v * 100).toFixed(1)}%` : '-' },
    { title: '开始日期', dataIndex: 'startDate', key: 'startDate', render: (v: string) => v ? new Date(v).toLocaleDateString() : '-' },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={4} style={{ margin: 0 }}>营销活动</Title>
        <Space>
          <Select placeholder="渠道" allowClear style={{ width: 120 }}
            options={Object.entries(channelMap).map(([k, v]) => ({ value: k, label: v.text }))}
            onChange={(v) => setParams({ ...params, channel: v })} />
          <Button type="primary" icon={<PlusOutlined />} onClick={() => { form.resetFields(); setModalOpen(true); }}>新建活动</Button>
        </Space>
      </div>
      <Card style={{ borderRadius: 8 }}>
        <Table rowKey="campaignId" columns={columns} dataSource={data?.list || []} size="middle"
          pagination={{ current: params.page, pageSize: params.size, total: data?.total || 0, onChange: (page, size) => setParams({ ...params, page, size }) }} />
      </Card>
      <Modal title="新建营销活动" open={modalOpen} onOk={handleSubmit} onCancel={() => setModalOpen(false)} width={560}>
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="活动名称" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="channel" label="渠道" rules={[{ required: true }]}>
            <Select options={Object.entries(channelMap).map(([k, v]) => ({ value: k, label: v.text }))} />
          </Form.Item>
          <Form.Item name="templateId" label="消息模板"><Input /></Form.Item>
          <Form.Item name="targetSegment" label="目标客群">
            <Select options={[{ value: 'VIP', label: 'VIP客户' }, { value: 'NEW', label: '新客户' }, { value: 'INACTIVE', label: '沉睡客户' }, { value: 'ALL', label: '全部客户' }]} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
