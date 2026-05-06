'use client';

import { useState, useEffect, useCallback } from 'react';
import { Table, Button, Modal, Form, Input, Tag, Space, message, Card, Typography, Badge } from 'antd';
import { PlusOutlined, SendOutlined } from '@ant-design/icons';
import { crmApi } from '@/lib/api';
import type { EmailCampaign } from '@/types';

const { Title } = Typography;

const statusMap: Record<string, { color: string; text: string }> = {
  DRAFT: { color: 'default', text: '草稿' },
  SCHEDULED: { color: 'blue', text: '已排期' },
  SENDING: { color: 'orange', text: '发送中' },
  SENT: { color: 'green', text: '已发送' },
  CANCELLED: { color: 'red', text: '已取消' },
};

export default function EmailCampaignsPage() {
  const [campaigns, setCampaigns] = useState<EmailCampaign[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [form] = Form.useForm();

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const data = await crmApi.listEmailCampaigns();
      setCampaigns(data || []);
    } catch {
      setCampaigns([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchData(); }, [fetchData]);

  const handleCreate = async () => {
    try {
      const values = await form.validateFields();
      await crmApi.createEmailCampaign(values);
      message.success('营销活动创建成功');
      setModalOpen(false);
      fetchData();
    } catch {
      message.error('操作失败');
    }
  };

  const handleSend = async (id: string) => {
    try {
      await crmApi.sendEmailCampaign(id);
      message.success('营销邮件已发送');
      fetchData();
    } catch {
      message.error('发送失败');
    }
  };

  const columns = [
    { title: '活动名称', dataIndex: 'name', key: 'name' },
    { title: '主题', dataIndex: 'subject', key: 'subject', ellipsis: true },
    { title: '目标客群', dataIndex: 'targetSegment', key: 'targetSegment', render: (v: string) => <Tag color="purple">{v}</Tag> },
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: string) => <Badge color={statusMap[v]?.color || 'default'} text={statusMap[v]?.text || v} /> },
    { title: '发送时间', dataIndex: 'sentAt', key: 'sentAt', render: (v: string) => v ? new Date(v).toLocaleString() : '-' },
    {
      title: '操作', key: 'action',
      render: (_: unknown, record: EmailCampaign) => (
        <Space>
          {record.status === 'DRAFT' && (
            <>
              <Button size="small" type="primary" icon={<SendOutlined />} onClick={() => handleSend(record.campaignId)}>发送</Button>
            </>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={4} style={{ margin: 0 }}>邮件营销</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => { form.resetFields(); setModalOpen(true); }}>新建活动</Button>
      </div>
      <Card style={{ borderRadius: 8 }}>
        <Table rowKey="campaignId" columns={columns} dataSource={campaigns} loading={loading} size="middle" />
      </Card>
      <Modal title="新建营销活动" open={modalOpen} onOk={handleCreate} onCancel={() => setModalOpen(false)} width={600}>
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="活动名称" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="subject" label="邮件主题" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="content" label="邮件内容" rules={[{ required: true }]}>
            <Input.TextArea rows={6} placeholder="支持HTML模板" />
          </Form.Item>
          <Form.Item name="targetSegment" label="目标客群" rules={[{ required: true }]}>
            <Input placeholder="如: VIP客户, 新注册用户, 流失用户" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
