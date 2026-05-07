'use client';

import { useState } from 'react';
import { Card, Typography, Table, Tag, Input, Space, Button, Badge, Descriptions, Modal, Timeline, Select } from 'antd';
import { SearchOutlined, EnvironmentOutlined } from '@ant-design/icons';
import { usePageApi, useApi } from '@/lib/hooks';
import { tmsApi } from '@/lib/api';
import type { ShipmentTracking, PageParams } from '@/types';

const { Title } = Typography;

const statusMap: Record<string, { color: string; text: string }> = {
  PICKED_UP: { color: 'blue', text: '已揽收' },
  IN_TRANSIT: { color: 'cyan', text: '运输中' },
  CUSTOMS: { color: 'orange', text: '清关中' },
  OUT_FOR_DELIVERY: { color: 'purple', text: '派送中' },
  DELIVERED: { color: 'green', text: '已签收' },
  EXCEPTION: { color: 'red', text: '异常' },
};

export default function TrackingPage() {
  const [params, setParams] = useState<PageParams & Record<string, unknown>>({ page: 1, size: 20 });
  const [searchNo, setSearchNo] = useState('');
  const [detailOpen, setDetailOpen] = useState(false);
  const [selectedTracking, setSelectedTracking] = useState<ShipmentTracking | null>(null);
  const { data, mutate } = usePageApi<ShipmentTracking>('/tms/api/in/v1/trackings', params);

  const handleSearch = async () => {
    if (!searchNo) return;
    try {
      const tracking = await tmsApi.getShipmentTracking(searchNo);
      setSelectedTracking(tracking);
      setDetailOpen(true);
    } catch {
      setSelectedTracking(null);
    }
  };

  const showDetail = async (trackingNo: string) => {
    try {
      const tracking = await tmsApi.getShipmentTracking(trackingNo);
      setSelectedTracking(tracking);
      setDetailOpen(true);
    } catch {}
  };

  const columns = [
    { title: '运单号', dataIndex: 'trackingNo', key: 'trackingNo', render: (v: string) => (
      <a onClick={() => showDetail(v)}><code>{v}</code></a>
    )},
    { title: '物流商', dataIndex: 'carrierName', key: 'carrierName', render: (v: string) => <Tag>{v}</Tag> },
    { title: '始发地', dataIndex: 'origin', key: 'origin' },
    { title: '目的地', dataIndex: 'destination', key: 'destination' },
    { title: '状态', dataIndex: 'status', key: 'status', render: (v: string) => <Badge color={statusMap[v]?.color || 'default'} text={statusMap[v]?.text || v} /> },
    { title: '最后更新', dataIndex: 'lastEventTime', key: 'lastEventTime', render: (v: string) => v ? new Date(v).toLocaleString() : '-' },
    { title: '预计到达', dataIndex: 'eta', key: 'eta', render: (v: string) => v || '-' },
    { title: '操作', key: 'action', render: (_: unknown, r: ShipmentTracking) => (
      <Space>
        <Button type="link" size="small" icon={<EnvironmentOutlined />} onClick={() => showDetail(r.trackingNo)}>详情</Button>
        {r.shipmentId && (
          <Button type="link" size="small" onClick={() => {
            window.location.href = `/fba/shipments?shipmentId=${r.shipmentId}`;
          }}>关联货件</Button>
        )}
      </Space>
    )},
  ];

  return (
    <div>
      <Card>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
          <Title level={4} style={{ margin: 0 }}>轨迹查询</Title>
          <Space>
            <Input placeholder="输入运单号查询" prefix={<SearchOutlined />} style={{ width: 300 }}
              value={searchNo} onChange={(e) => setSearchNo(e.target.value)}
              onPressEnter={handleSearch} />
            <Button type="primary" onClick={handleSearch}>查询</Button>
            <Select placeholder="状态" allowClear style={{ width: 120 }}
              options={Object.entries(statusMap).map(([k, v]) => ({ value: k, label: v.text }))}
              onChange={(v) => setParams({ ...params, status: v })} />
          </Space>
        </div>
        <Table
          rowKey="trackingId"
          columns={columns}
          dataSource={data?.list || []}
          pagination={{
            current: params.page,
            pageSize: params.size,
            total: data?.total || 0,
            onChange: (page, size) => setParams({ ...params, page, size }),
            showTotal: (total) => `共 ${total} 条`,
          }}
        />
      </Card>

      <Modal
        title={`物流轨迹 - ${selectedTracking?.trackingNo || ''}`}
        open={detailOpen}
        onCancel={() => setDetailOpen(false)}
        footer={null}
        width={700}
      >
        {selectedTracking && (
          <>
            <Descriptions bordered column={2} size="small" style={{ marginBottom: 24 }}>
              <Descriptions.Item label="运单号">{selectedTracking.trackingNo}</Descriptions.Item>
              <Descriptions.Item label="物流商">{selectedTracking.carrierName}</Descriptions.Item>
              <Descriptions.Item label="始发地">{selectedTracking.origin}</Descriptions.Item>
              <Descriptions.Item label="目的地">{selectedTracking.destination}</Descriptions.Item>
              <Descriptions.Item label="状态">
                <Badge color={statusMap[selectedTracking.status]?.color} text={statusMap[selectedTracking.status]?.text || selectedTracking.status} />
              </Descriptions.Item>
              <Descriptions.Item label="预计到达">{selectedTracking.eta || '-'}</Descriptions.Item>
            </Descriptions>
            <Title level={5}>物流轨迹</Title>
            <Timeline
              items={(selectedTracking.events || []).map((e: { eventTime: string; location: string; description: string }) => ({
                children: (
                  <div>
                    <div style={{ fontWeight: 500 }}>{e.description}</div>
                    <div style={{ fontSize: 12, color: '#999' }}>{e.location} · {new Date(e.eventTime).toLocaleString()}</div>
                  </div>
                ),
              }))}
            />
          </>
        )}
      </Modal>
    </div>
  );
}
