'use client';

import { useState, useEffect, useCallback } from 'react';
import { Table, Button, Modal, Form, Input, Select, Tag, Space, message, Card, Typography, Badge } from 'antd';
import { PlusOutlined, CheckCircleOutlined } from '@ant-design/icons';
import { crmApi } from '@/lib/api';
import type { QualityIssue } from '@/types';

const { Title } = Typography;

const severityMap: Record<string, { color: string; text: string }> = {
  CRITICAL: { color: 'red', text: '严重' },
  MAJOR: { color: 'orange', text: '重要' },
  MINOR: { color: 'blue', text: '一般' },
};

export default function QualityIssuesPage() {
  const [issues, setIssues] = useState<QualityIssue[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [form] = Form.useForm();

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const data = await crmApi.listQualityIssues();
      setIssues(data || []);
    } catch {
      setIssues([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchData(); }, [fetchData]);

  const handleCreate = async () => {
    try {
      const values = await form.validateFields();
      await crmApi.createQualityIssue(values);
      message.success('质量问题已创建');
      setModalOpen(false);
      fetchData();
    } catch {
      message.error('操作失败');
    }
  };

  const handleResolve = async (id: string) => {
    try {
      await crmApi.resolveQualityIssue(id);
      message.success('质量问题已解决');
      fetchData();
    } catch {
      message.error('操作失败');
    }
  };

  const columns = [
    { title: '产品ID', dataIndex: 'productId', key: 'productId' },
    { title: '来源', dataIndex: 'source', key: 'source', render: (v: string) => <Tag>{v}</Tag> },
    { title: '描述', dataIndex: 'description', key: 'description', ellipsis: true },
    { title: '严重程度', dataIndex: 'severity', key: 'severity', render: (v: string) => <Badge color={severityMap[v]?.color || 'default'} text={severityMap[v]?.text || v} /> },
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: string) => <Tag color={v === 'OPEN' ? 'orange' : v === 'RESOLVED' ? 'green' : 'default'}>{v}</Tag> },
    { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', render: (v: string) => v ? new Date(v).toLocaleString() : '-' },
    {
      title: '操作', key: 'action',
      render: (_: unknown, record: QualityIssue) => (
        record.status !== 'RESOLVED' ? (
          <Button size="small" type="primary" icon={<CheckCircleOutlined />} onClick={() => handleResolve(record.issueId)}>解决</Button>
        ) : null
      ),
    },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={4} style={{ margin: 0 }}>质量问题</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => { form.resetFields(); setModalOpen(true); }}>上报问题</Button>
      </div>
      <Card style={{ borderRadius: 8 }}>
        <Table rowKey="issueId" columns={columns} dataSource={issues} loading={loading} size="middle" />
      </Card>
      <Modal title="上报质量问题" open={modalOpen} onOk={handleCreate} onCancel={() => setModalOpen(false)} width={520}>
        <Form form={form} layout="vertical">
          <Form.Item name="productId" label="产品ID" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="source" label="来源" rules={[{ required: true }]}>
            <Select options={[{ value: 'CUSTOMER', label: '客户反馈' }, { value: 'INSPECTION', label: '质检发现' }, { value: 'RETURN', label: '退货分析' }]} />
          </Form.Item>
          <Form.Item name="description" label="问题描述" rules={[{ required: true }]}>
            <Input.TextArea rows={4} />
          </Form.Item>
          <Form.Item name="severity" label="严重程度" rules={[{ required: true }]}>
            <Select options={Object.entries(severityMap).map(([k, v]) => ({ value: k, label: v.text }))} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
