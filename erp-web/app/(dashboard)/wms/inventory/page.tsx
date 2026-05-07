'use client';

import { useState } from 'react';
import { Table, Card, Typography, Tag, Badge, Select, Space, Button, Input, Modal, Form, InputNumber, message } from 'antd';
import { SearchOutlined, WarningOutlined, PlusOutlined } from '@ant-design/icons';
import { usePageApi } from '@/lib/hooks';
import { wmsApi } from '@/lib/api';
import type { InventoryBalance, PageParams } from '@/types';

const { Title } = Typography;

export default function InventoryPage() {
  const [params, setParams] = useState<PageParams & Record<string, unknown>>({ page: 1, size: 20 });
  const [adjustOpen, setAdjustOpen] = useState(false);
  const [adjustSku, setAdjustSku] = useState<InventoryBalance | null>(null);
  const [adjustForm] = Form.useForm();
  const { data, mutate } = usePageApi<InventoryBalance>('/wms/api/in/v1/inventory', params);

  const handleAdjust = async () => {
    try {
      const values = await adjustForm.validateFields();
      await wmsApi.adjustInventory({
        skuId: adjustSku!.skuId,
        warehouseId: adjustSku!.warehouseId,
        quantity: values.quantity,
        reason: values.reason,
      });
      message.success('库存调整成功');
      setAdjustOpen(false);
      mutate();
    } catch { message.error('调整失败'); }
  };

  const columns = [
    { title: 'Seller SKU', dataIndex: 'sellerSku', key: 'sellerSku', render: (v: string) => (
      <a href={`/pdm/products?sku=${v}`}>{v}</a>
    )},
    { title: '仓库', dataIndex: 'warehouseId', key: 'warehouseId', render: (v: string) => (
      <a href={`/wms/warehouses?id=${v}`}>{v}</a>
    )},
    { title: '可用库存', dataIndex: 'available', key: 'available',
      render: (v: number) => <span style={{ color: v > 20 ? '#52c41a' : v > 0 ? '#faad14' : '#ff4d4f', fontWeight: 600 }}>{v}</span>,
    },
    { title: '预留库存', dataIndex: 'reserved', key: 'reserved', render: (v: number) => <Tag color="orange">{v}</Tag> },
    { title: '在途库存', dataIndex: 'inTransit', key: 'inTransit', render: (v: number) => <Tag color="blue">{v}</Tag> },
    { title: '冻结库存', dataIndex: 'frozen', key: 'frozen', render: (v: number) => <Tag color="red">{v}</Tag> },
    { title: '库存状态', key: 'status', render: (_: unknown, r: InventoryBalance) => {
      if (r.available <= 0) return <Badge status="error" text="缺货" />;
      if (r.available <= 20) return <Badge status="warning" text="低库存" />;
      return <Badge status="success" text="正常" />;
    }},
    { title: '操作', key: 'action', render: (_: unknown, r: InventoryBalance) => (
      <Space>
        <Button type="link" size="small" onClick={() => {
          setAdjustSku(r);
          adjustForm.resetFields();
          setAdjustOpen(true);
        }}>调整</Button>
        <Button type="link" size="small" onClick={() => {
          window.location.href = `/fba/inventory?sku=${r.sellerSku}`;
        }}>FBA库存</Button>
        <Button type="link" size="small" onClick={() => {
          window.location.href = `/scm/purchase-orders?sku=${r.sellerSku}`;
        }}>采购单</Button>
      </Space>
    )},
  ];

  return (
    <div>
      <Card>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
          <Title level={4} style={{ margin: 0 }}>库存台账</Title>
          <Space>
            <Input placeholder="搜索SKU" prefix={<SearchOutlined />} allowClear style={{ width: 220 }}
              onChange={(e) => setParams({ ...params, keyword: e.target.value || undefined })} />
            <Select placeholder="选择仓库" allowClear style={{ width: 180 }
            } onChange={(v) => setParams({ ...params, warehouseId: v })} />
            <Select placeholder="库存状态" allowClear style={{ width: 120 }}
              options={[{ value: 'LOW', label: '低库存' }, { value: 'OUT', label: '缺货' }, { value: 'NORMAL', label: '正常' }]}
              onChange={(v) => setParams({ ...params, stockStatus: v })} />
          </Space>
        </div>
        <Table
          rowKey={(r: InventoryBalance) => `${r.skuId}-${r.warehouseId}`}
          columns={columns}
          dataSource={data?.list || []}
          scroll={{ x: 1000 }}
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

      <Modal title={`库存调整 - ${adjustSku?.sellerSku || ''}`} open={adjustOpen} onOk={handleAdjust} onCancel={() => setAdjustOpen(false)} width={480}>
        <Form form={adjustForm} layout="vertical">
          <Form.Item name="quantity" label="调整数量(正数增加/负数减少)" rules={[{ required: true }]}>
            <InputNumber style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="reason" label="调整原因" rules={[{ required: true }]}>
            <Input.TextArea rows={3} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
