import React from 'react';
import { Card as AntdCard } from 'antd';
import type { CardProps } from 'antd';

export const Card: React.FC<CardProps> = ({ style, className, ...props }) => {
  return (
    <AntdCard
      className={`ch-card-hover ${className || ''}`}
      style={{
        borderRadius: '12px',
        border: '1px solid var(--border-color)',
        boxShadow: '0 4px 12px rgba(0,0,0,0.08)',
        background: '#FFFFFF',
        transition: 'all 0.2s ease',
        ...style,
      }}
      {...props}
    />
  );
};
