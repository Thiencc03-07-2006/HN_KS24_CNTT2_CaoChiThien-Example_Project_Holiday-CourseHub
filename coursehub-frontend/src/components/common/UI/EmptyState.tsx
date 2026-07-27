import React from 'react';
import { Empty } from 'antd';

export const EmptyState: React.FC<{ description?: string }> = ({ description = 'Không tìm thấy dữ liệu phù hợp.' }) => {
  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '200px', width: '100%' }}>
      <Empty description={<span style={{ color: 'var(--text-color)' }}>{description}</span>} />
    </div>
  );
};
