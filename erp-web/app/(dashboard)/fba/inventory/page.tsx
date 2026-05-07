'use client';

import { useState } from 'react';
import { Table, Card, Typography, Tag, Space, Select, Input, Badge, Button, Tabs } from 'antd';
import { SearchOutlined } from '@ant-design/icons';
import { usePageApi } from '@/lib/hooks';
import { fbaApi } from '@/lib/api';
import type { FbaInventory, FbaRemoval, PageParams } from '@/types';
import { Modal, Form, InputNumber, message } from 'antd';

const { Title } = Typography;

const conditionMap: Record<string, { color: string; text: string }> = {
  SELLABLE: { color: 'green', text: '可售' },
  UNSELLABLE: { color: 'red', text: '不可售' },
  RESERVED: { color: 'orange', text: '预留' },
};

export default function FbaInventoryPage() {
  const [invParams, setInvParams] = useState<PageParams & Record<string, unknown>>({ page: 1, size: 20 });
  const [removalParams, setRemovalParams] = useState<PageParams & Record<string, unknown>>({ page: 1, size: 20 });
  const [removalOpen, setRemovalOpen] = useState(false);
  const [removalForm] = Form.useForm();
  const { data: inventory } = usePageApi<FbaInventory>('/fba/api/in/v1/fba-inventory', invParams);
  const { data: removals, mutate: mutateRemovals } = usePageApi<FbaRemoval>('/fba/api/in/v1/removals', removalParams);

  const handleCreateRemoval = async () => {
    try {
      const values = await removalForm.validateFields();
      await fbaApi.createRemoval(values);
      message.success('移除订单已创建');
      setRemovalOpen(false);
      mutateRemovals();
    } catch { message.error('创建失败'); }
  };

  const inventoryColumns = [
    { title: 'SKU', dataIndex: 'sku', key: 'sku' },
    { title: 'ASIN', dataIndex: 'asin', key: 'asin' },
    { title: 'FNSKU', dataIndex: 'fnsku', key: 'fnsku' },
    { title: '可售数量', dataIndex: 'fulfillableQty', key: 'fulfillableQty', render: (v: number) => (
      <span style={{ fontWeight: 600, color: v < 10 ? '#ff4d4f' : undefined }}>{v ?? 0}</span>
    )},
    { title: '预留数量', dataIndex: 'reservedQty', key: 'reservedQty' },
    { title: '在途数量', dataIndex: 'inboundQty', key: 'inboundQty' },
    { title: '不可售数量', dataIndex: 'unfulfillableQty', key: 'unfulfillableQty', render: (v: number) => v > 0 ? <Badge count={v} style={{ backgroundColor: '#ff4d4f' }} /> : 0 },
    { title: '操作', key: 'action', render: (_: unknown, r: FbaInventory) => (
      <Space>
        <Button type="link" size="small" onClick={() => {
          window.location.href = `/wms/inventory?sku=${r.sku}`;
        }}>本地库存</Button>
        {r.unfulfillableQty > 0 && (
          <Button type="link" size="small" danger onClick={() => {
            removalForm.setFieldsValue({ sku: r.sku, quantity: r.unfulfillableQty });
            setRemovalOpen(true);
          }}>创建移除</Button>
        )}
      </Space>
    )},
  ];

  const removalColumns = [
    { title: '移除订单号', dataIndex: 'removalOrderId', key: 'removalOrderId' },
    { title: 'SKU', dataIndex: 'sku', key: 'sku' },
    { title: '数量', dataIndex: 'quantity', key: 'quantity' },
    { title: '处置方式', dataIndex: 'disposition', key: 'disposition', render: (v: string) => <Tag>{v === 'RETURN' ? '退回' : v === 'DISPOSE' ? '销毁' : v}</Tag> },
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: string) => <Tag color={v === 'PENDING' ? 'orange' : v === 'COMPLETE' ? 'green' : 'blue'}>{v}</Tag> },
    { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', render: (v: string) => v ? new Date(v).toLocaleString() : '-' },
  ];

  return (
    <div>
      <Card>
        <Tabs items={[
          {
            key: 'inventory',
            label: 'FBA库存',
            children: (
              <>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
                  <Title level={4} style={{ margin: 0 }}>FBA库存</Title>
                  <Space>
                    <Input placeholder="搜索SKU/ASIN" prefix={<SearchOutlined />} allowClear style={{ width: 200 }}
                      onChange={(e) => setInvParams({ ...invParams, keyword: e.target.value || undefined })} />
                  </Space>
                </div>
                <Table
                  rowKey="inventoryId"
                  columns={inventoryColumns}
                  dataSource={inventory?.list || []}
                  scroll={{ x: 1000 }}
                  pagination={{
                    current: invParams.page,
                    pageSize: invParams.size,
                    total: inventory?.total || 0,
                    onChange: (page, size) => setInvParams({ ...invParams, page, size }),
                    showTotal: (total) => `共 ${total} 条`,
                  }}
                />
              </>
            ),
          },
          {
            key: 'removals',
            label: '移除订单',
            children: (
              <>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
                  <Title level={4} style={{ margin: 0 }}>移除订单</Title>
                  <Button onClick={() => { removalForm.resetFields(); setRemovalOpen(true); }}>创建移除</Button>
                </div>
                <Table
                  rowKey="removalId"
                  columns={removalColumns}
                  dataSource={removals?.list || []}
                  pagination={{
                    current: removalParams.page,
                    pageSize: removalParams.size,
                    total: removals?.total || 0,
                    onChange: (page, size) => setRemovalParams({ ...removalParams, page, size }),
                    showTotal: (total) => `共 ${total} 条`,
                  }}
                />
              </>
            ),
          },
        ]} />
      </Card>
      <Modal title="创建移除订单" open={removalOpen} onOk={handleCreateRemoval} onCancel={() => setRemovalOpen(false)} width={480}>
        <Form form={removalForm} layout="vertical">
          <Form.Item name="sku" label="SKU" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="quantity" label="数量" rules={[{ required: true }]}>
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="disposition" label="处置方式" rules={[{ required: true }]}>
            <Select options={[{ value: 'RETURN', label: '退回' }, { value: 'DISPOSE', label: '销毁' }]} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
