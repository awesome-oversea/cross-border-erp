'use client';

import { useState } from 'react';
import { Table, Button, Modal, Form, Input, InputNumber, Select, Tag, Space, message, Card, Typography, Badge, Tabs } from 'antd';
import { PlusOutlined, SyncOutlined } from '@ant-design/icons';
import { usePageApi } from '@/lib/hooks';
import { fbaApi } from '@/lib/api';
import type { ShipmentPlan, FbaShipment, FbaInventory, FbaRemoval, PageParams } from '@/types';

const { Title } = Typography;

const planStatusMap: Record<string, { color: string; text: string }> = {
  DRAFT: { color: 'default', text: '草稿' },
  SUBMITTED: { color: 'blue', text: '已提交' },
  IN_PRODUCTION: { color: 'orange', text: '生产中' },
  SHIPPED: { color: 'cyan', text: '已发货' },
  RECEIVED: { color: 'green', text: '已接收' },
  CANCELLED: { color: 'red', text: '已取消' },
};

const shipmentStatusMap: Record<string, { color: string; text: string }> = {
  WORKING: { color: 'default', text: '准备中' },
  SHIPPED: { color: 'blue', text: '已发货' },
  IN_TRANSIT: { color: 'cyan', text: '运输中' },
  DELIVERED: { color: 'green', text: '已送达' },
  CHECKED_IN: { color: 'geekblue', text: '已入库' },
  RECEIVING: { color: 'purple', text: '接收中' },
  CLOSED: { color: 'default', text: '已关闭' },
  CANCELLED: { color: 'red', text: '已取消' },
};

export default function ShipmentsPage() {
  const [planParams, setPlanParams] = useState<PageParams & Record<string, unknown>>({ page: 1, size: 20 });
  const [shipmentParams, setShipmentParams] = useState<PageParams & Record<string, unknown>>({ page: 1, size: 20 });
  const [modalOpen, setModalOpen] = useState(false);
  const [form] = Form.useForm();
  const { data: plans, mutate: mutatePlans } = usePageApi<ShipmentPlan>('/fba/api/in/v1/shipments', planParams);
  const { data: shipments } = usePageApi<FbaShipment>('/fba/api/in/v1/fba-shipments', shipmentParams);

  const handleCreate = async () => {
    try {
      const values = await form.validateFields();
      await fbaApi.createShipmentPlan(values);
      message.success('备货计划创建成功');
      setModalOpen(false);
      mutatePlans();
    } catch { message.error('操作失败'); }
  };

  const planColumns = [
    { title: 'Seller SKU', dataIndex: 'sellerSku', key: 'sellerSku' },
    { title: '目的仓库', dataIndex: 'destinationFc', key: 'destinationFc', render: (v: string) => <Tag color="blue">{v}</Tag> },
    { title: '建议数量', dataIndex: 'suggestedQuantity', key: 'suggestedQuantity', render: (v: number) => <span style={{ fontWeight: 600 }}>{v}</span> },
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: string) => <Badge color={planStatusMap[v]?.color || 'default'} text={planStatusMap[v]?.text || v} /> },
    { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', render: (v: string) => v ? new Date(v).toLocaleString() : '-' },
    { title: '操作', key: 'action', render: (_: unknown, r: ShipmentPlan) => (
      <Space>
        <Button type="link" size="small" onClick={() => {
          window.location.href = `/wms/inventory?sku=${r.sellerSku}`;
        }}>查看库存</Button>
      </Space>
    )},
  ];

  const shipmentColumns = [
    { title: '货件ID', dataIndex: 'shipmentId', key: 'shipmentId' },
    { title: 'FBA Shipment ID', dataIndex: 'fbaShipmentId', key: 'fbaShipmentId' },
    { title: '目的仓', dataIndex: 'destinationFc', key: 'destinationFc', render: (v: string) => <Tag>{v}</Tag> },
    { title: 'SKU数', dataIndex: 'skuCount', key: 'skuCount' },
    { title: '总数量', dataIndex: 'totalQuantity', key: 'totalQuantity' },
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: string) => <Badge color={shipmentStatusMap[v]?.color || 'default'} text={shipmentStatusMap[v]?.text || v} /> },
    { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', render: (v: string) => v ? new Date(v).toLocaleString() : '-' },
    { title: '操作', key: 'action', render: (_: unknown, r: FbaShipment) => (
      <Space>
        <Button type="link" size="small" onClick={() => {
          window.location.href = `/tms/tracking?shipmentId=${r.shipmentId}`;
        }}>物流追踪</Button>
      </Space>
    )},
  ];

  return (
    <div>
      <Card>
        <Tabs items={[
          {
            key: 'plans',
            label: '备货计划',
            children: (
              <>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
                  <Title level={4} style={{ margin: 0 }}>备货计划</Title>
                  <Button type="primary" icon={<PlusOutlined />} onClick={() => { form.resetFields(); setModalOpen(true); }}>新建计划</Button>
                </div>
                <Table
                  rowKey="planId"
                  columns={planColumns}
                  dataSource={plans?.list || []}
                  pagination={{
                    current: planParams.page,
                    pageSize: planParams.size,
                    total: plans?.total || 0,
                    onChange: (page, size) => setPlanParams({ ...planParams, page, size }),
                    showTotal: (total) => `共 ${total} 条`,
                  }}
                />
              </>
            ),
          },
          {
            key: 'shipments',
            label: 'FBA货件',
            children: (
              <>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
                  <Title level={4} style={{ margin: 0 }}>FBA货件</Title>
                  <Space>
                    <Select placeholder="状态" allowClear style={{ width: 120 }}
                      options={Object.entries(shipmentStatusMap).map(([k, v]) => ({ value: k, label: v.text }))}
                      onChange={(v) => setShipmentParams({ ...shipmentParams, status: v })} />
                  </Space>
                </div>
                <Table
                  rowKey="shipmentId"
                  columns={shipmentColumns}
                  dataSource={shipments?.list || []}
                  pagination={{
                    current: shipmentParams.page,
                    pageSize: shipmentParams.size,
                    total: shipments?.total || 0,
                    onChange: (page, size) => setShipmentParams({ ...shipmentParams, page, size }),
                    showTotal: (total) => `共 ${total} 条`,
                  }}
                />
              </>
            ),
          },
        ]} />
      </Card>
      <Modal title="新建备货计划" open={modalOpen} onOk={handleCreate} onCancel={() => setModalOpen(false)} width={480}>
        <Form form={form} layout="vertical">
          <Form.Item name="sellerSku" label="Seller SKU" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="destinationFc" label="目的仓库" rules={[{ required: true }]}>
            <Select options={[{ value: 'US-SEA1', label: '美国西雅图仓' }, { value: 'US-LAX1', label: '美国洛杉矶仓' }, { value: 'EU-FRA1', label: '德国法兰克福仓' }, { value: 'JP-TYO1', label: '日本东京仓' }]} />
          </Form.Item>
          <Form.Item name="suggestedQuantity" label="建议数量" rules={[{ required: true }]}>
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
