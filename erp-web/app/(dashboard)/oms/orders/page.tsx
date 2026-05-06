'use client';

import { useState, useEffect, useCallback } from 'react';
import { Table, Card, Typography, Tag, Space, Input, Select, DatePicker, Button, Badge, Descriptions, Modal } from 'antd';
import { SearchOutlined, EyeOutlined } from '@ant-design/icons';
import { omsApi } from '@/lib/api';
import type { Order } from '@/types';

const { Title } = Typography;
const { RangePicker } = DatePicker;

const statusMap: Record<string, { color: string; text: string }> = {
  PENDING: { color: 'orange', text: '待审核' },
  CONFIRMED: { color: 'blue', text: '已确认' },
  SHIPPED: { color: 'cyan', text: '已发货' },
  DELIVERED: { color: 'green', text: '已签收' },
  CANCELLED: { color: 'red', text: '已取消' },
  REFUNDED: { color: 'volcano', text: '已退款' },
};

export default function OrdersPage() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(false);
  const [detailOpen, setDetailOpen] = useState(false);
  const [currentOrder, setCurrentOrder] = useState<Order | null>(null);
  const [platform, setPlatform] = useState<string>('');
  const [status, setStatus] = useState<string>('');

  const fetchOrders = useCallback(async () => {
    setLoading(true);
    try {
      const params: Record<string, string> = {};
      if (platform) params.platform = platform;
      if (status) params.status = status;
      const data = await omsApi.listOrders(params);
      setOrders(data || []);
    } catch {
      setOrders([]);
    } finally {
      setLoading(false);
    }
  }, [platform, status]);

  useEffect(() => { fetchOrders(); }, [fetchOrders]);

  const showDetail = async (orderId: string) => {
    try {
      const data = await omsApi.getOrder(orderId);
      setCurrentOrder(data);
      setDetailOpen(true);
    } catch {
      message.error('获取订单详情失败');
    }
  };

  const columns = [
    { title: '订单号', dataIndex: 'orderNo', key: 'orderNo', render: (v: string) => <a>{v}</a> },
    { title: '平台', dataIndex: 'platform', key: 'platform', render: (v: string) => <Tag>{v}</Tag> },
    { title: '店铺', dataIndex: 'storeId', key: 'storeId' },
    { title: '客户', dataIndex: 'customerName', key: 'customerName' },
    { title: '金额', dataIndex: 'totalAmount', key: 'totalAmount', render: (v: number, r: Order) => `${r.currency} ${v?.toFixed(2)}` },
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: string) => {
      const s = statusMap[v] || { color: 'default', text: v };
      return <Badge color={s.color} text={s.text} />;
    }},
    { title: '下单时间', dataIndex: 'orderDate', key: 'orderDate', render: (v: string) => v ? new Date(v).toLocaleString() : '-' },
    {
      title: '操作', key: 'action',
      render: (_: unknown, record: Order) => (
        <Button type="link" icon={<EyeOutlined />} onClick={() => showDetail(record.orderId)}>详情</Button>
      ),
    },
  ];

  return (
    <div>
      <Title level={4} style={{ marginBottom: 16 }}>订单列表</Title>
      <Card style={{ borderRadius: 8, marginBottom: 16 }}>
        <Space wrap>
          <Input placeholder="搜索订单号/客户" prefix={<SearchOutlined />} style={{ width: 240 }} />
          <Select placeholder="平台" allowClear style={{ width: 140 }} value={platform || undefined} onChange={setPlatform}
            options={[{ value: 'Amazon', label: 'Amazon' }, { value: 'Shopify', label: 'Shopify' }, { value: 'eBay', label: 'eBay' }, { value: 'Shopee', label: 'Shopee' }]} />
          <Select placeholder="状态" allowClear style={{ width: 140 }} value={status || undefined} onChange={setStatus}
            options={Object.entries(statusMap).map(([k, v]) => ({ value: k, label: v.text }))} />
          <RangePicker />
          <Button type="primary" onClick={fetchOrders}>查询</Button>
        </Space>
      </Card>
      <Card style={{ borderRadius: 8 }}>
        <Table rowKey="orderId" columns={columns} dataSource={orders} loading={loading} size="middle"
          pagination={{ pageSize: 20, showSizeChanger: true, showTotal: (t) => `共 ${t} 条` }} />
      </Card>
      <Modal title="订单详情" open={detailOpen} onCancel={() => setDetailOpen(false)} footer={null} width={720}>
        {currentOrder && (
          <Descriptions bordered column={2} size="small">
            <Descriptions.Item label="订单号">{currentOrder.orderNo}</Descriptions.Item>
            <Descriptions.Item label="平台"><Tag>{currentOrder.platform}</Tag></Descriptions.Item>
            <Descriptions.Item label="店铺">{currentOrder.storeId}</Descriptions.Item>
            <Descriptions.Item label="客户">{currentOrder.customerName}</Descriptions.Item>
            <Descriptions.Item label="金额">{currentOrder.currency} {currentOrder.totalAmount?.toFixed(2)}</Descriptions.Item>
            <Descriptions.Item label="状态"><Badge color={statusMap[currentOrder.status]?.color} text={statusMap[currentOrder.status]?.text || currentOrder.status} /></Descriptions.Item>
            <Descriptions.Item label="下单时间" span={2}>{currentOrder.orderDate ? new Date(currentOrder.orderDate).toLocaleString() : '-'}</Descriptions.Item>
          </Descriptions>
        )}
      </Modal>
    </div>
  );
}

import { message } from 'antd';
