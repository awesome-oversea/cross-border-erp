'use client';

import { Card, Typography, Table, Tag, Input, Space, Button, Badge } from 'antd';
import { SearchOutlined } from '@ant-design/icons';

const { Title } = Typography;

const mockTracking = [
  { key: '1', trackingNo: 'DHL-2026050301', carrier: 'DHL', origin: '深圳', destination: '洛杉矶', status: 'IN_TRANSIT', lastUpdate: '2026-05-03 08:30', eta: '2026-05-08' },
  { key: '2', trackingNo: 'FEDEX-2026050205', carrier: 'FedEx', origin: '义乌', destination: '纽约', status: 'DELIVERED', lastUpdate: '2026-05-02 16:00', eta: '-' },
  { key: '3', trackingNo: 'UPS-2026050112', carrier: 'UPS', origin: '深圳', destination: '伦敦', status: 'CUSTOMS', lastUpdate: '2026-05-03 10:15', eta: '2026-05-10' },
  { key: '4', trackingNo: 'DHL-2026050408', carrier: 'DHL', origin: '广州', destination: '东京', status: 'PICKED_UP', lastUpdate: '2026-05-04 09:00', eta: '2026-05-07' },
];

const statusMap: Record<string, { color: string; text: string }> = {
  PICKED_UP: { color: 'blue', text: '已揽收' },
  IN_TRANSIT: { color: 'cyan', text: '运输中' },
  CUSTOMS: { color: 'orange', text: '清关中' },
  OUT_FOR_DELIVERY: { color: 'purple', text: '派送中' },
  DELIVERED: { color: 'green', text: '已签收' },
  EXCEPTION: { color: 'red', text: '异常' },
};

export default function TrackingPage() {
  const columns = [
    { title: '运单号', dataIndex: 'trackingNo', key: 'trackingNo', render: (v: string) => <code>{v}</code> },
    { title: '物流商', dataIndex: 'carrier', key: 'carrier', render: (v: string) => <Tag>{v}</Tag> },
    { title: '始发地', dataIndex: 'origin', key: 'origin' },
    { title: '目的地', dataIndex: 'destination', key: 'destination' },
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: string) => <Badge color={statusMap[v]?.color || 'default'} text={statusMap[v]?.text || v} /> },
    { title: '最后更新', dataIndex: 'lastUpdate', key: 'lastUpdate' },
    { title: '预计到达', dataIndex: 'eta', key: 'eta' },
  ];

  return (
    <div>
      <Title level={4} style={{ marginBottom: 16 }}>轨迹查询</Title>
      <Card style={{ borderRadius: 8, marginBottom: 16 }}>
        <Space>
          <Input placeholder="输入运单号" prefix={<SearchOutlined />} style={{ width: 300 }} />
          <Button type="primary">查询</Button>
        </Space>
      </Card>
      <Card style={{ borderRadius: 8 }}>
        <Table columns={columns} dataSource={mockTracking} size="middle" />
      </Card>
    </div>
  );
}
