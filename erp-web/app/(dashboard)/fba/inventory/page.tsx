'use client';

import { useState } from 'react';
import { Table, Card, Typography, Tag, Space, Select, Input, Badge, Button, Tabs, Modal, Form, InputNumber, message } from 'antd';
import { SearchOutlined } from '@ant-design/icons';
import { usePageApi } from '@/lib/hooks';
import { fbaApi } from '@/lib/api';
import type { FbaInventory, FbaRemoval, PageParams } from '@/types';

const { Title } = Typography;

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
    { title: 'Seller SKU', dataIndex: 'sellerSku', key: 'sellerSku' },
    { title: 'FBA库存', dataIndex: 'fbaStock', key: 'fbaStock', render: (v: number) => (
      <span style={{ fontWeight: 600, color: v < 10 ? '#ff4d4f' : undefined }}>{v ?? 0}</span>
    )},
    { title: '在途数量', dataIndex: 'inboundQty', key: 'inboundQty' },
    { title: '转运数量', dataIndex: 'transferQty', key: 'transferQty' },
    { title: '日均销量', dataIndex: 'dailySales', key: 'dailySales', render: (v: number) => v?.toFixed(1) },
    { title: '可售天数', dataIndex: 'daysOfSupply', key: 'daysOfSupply', render: (v: number) => (
      <Badge count={v} style={{ backgroundColor: v < 14 ? '#ff4d4f' : v < 30 ? '#faad14' : '#52c41a' }} />
    )},
    { title: '仓库', dataIndex: 'fc', key: 'fc', render: (v: string) => <Tag>{v}</Tag> },
    { title: '操作', key: 'action', render: (_: unknown, r: FbaInventory) => (
      <Space>
        <Button type="link" size="small" onClick={() => {
          window.location.href = `/wms/inventory?keyword=${r.sellerSku}`;
        }}>本地库存</Button>
      </Space>
    )},
  ];

  const removalColumns = [
    { title: '移除订单号', dataIndex: 'removalId', key: 'removalId' },
    { title: '处置方式', dataIndex: 'removalType', key: 'removalType', render: (v: string) => <Tag color={v === 'RETURN' ? 'blue' : 'red'}>{v === 'RETURN' ? '退回' : '销毁'}</Tag> },
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: string) => <Tag color={v === 'PENDING' ? 'orange' : v === 'COMPLETED' ? 'green' : 'blue'}>{v}</Tag> },
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
                    <Input placeholder="搜索SKU" prefix={<SearchOutlined />} allowClear style={{ width: 200 }}
                      onChange={(e) => setInvParams({ ...invParams, keyword: e.target.value || undefined })} />
                    <Button onClick={() => fbaApi.generateRestockSuggestions()}>生成补货建议</Button>
                  </Space>
                </div>
                <Table rowKey="skuId" columns={inventoryColumns} dataSource={inventory?.list || []} scroll={{ x: 900 }}
                  pagination={{ current: invParams.page, pageSize: invParams.size, total: inventory?.total || 0, onChange: (page, size) => setInvParams({ ...invParams, page, size }), showTotal: (total) => `共 ${total} 条` }} />
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
                <Table rowKey="removalId" columns={removalColumns} dataSource={removals?.list || []}
                  pagination={{ current: removalParams.page, pageSize: removalParams.size, total: removals?.total || 0, onChange: (page, size) => setRemovalParams({ ...removalParams, page, size }), showTotal: (total) => `共 ${total} 条` }} />
              </>
            ),
          },
        ]} />
      </Card>
      <Modal title="创建移除订单" open={removalOpen} onOk={handleCreateRemoval} onCancel={() => setRemovalOpen(false)} width={480}>
        <Form form={removalForm} layout="vertical">
          <Form.Item name="storeId" label="店铺" rules={[{ required: true }]}>
            <Select options={[{ value: 'store-001', label: 'Amazon US' }, { value: 'store-002', label: 'Amazon EU' }]} />
          </Form.Item>
          <Form.Item name="removalType" label="处置方式" rules={[{ required: true }]}>
            <Select options={[{ value: 'RETURN', label: '退回' }, { value: 'DISPOSE', label: '销毁' }]} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
