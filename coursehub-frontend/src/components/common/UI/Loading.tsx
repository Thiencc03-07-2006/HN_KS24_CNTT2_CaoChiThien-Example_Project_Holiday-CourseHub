import React from 'react';
import { Spin } from 'antd';
import { LoadingOutlined } from '@ant-design/icons';

export const Loading: React.FC<{ message?: string }> = ({ message = 'Đang tải dữ liệu...' }) => {
  const antIcon = <LoadingOutlined style={{ fontSize: 32, color: 'var(--primary-color)' }} spin />;
  return (
    <div style={{ display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center', minHeight: '200px', width: '100%' }}>
      <Spin indicator={antIcon} />
      <span style={{ marginTop: '12px', color: 'var(--text-color)', fontSize: '14px' }}>{message}</span>
    </div>
  );
};
