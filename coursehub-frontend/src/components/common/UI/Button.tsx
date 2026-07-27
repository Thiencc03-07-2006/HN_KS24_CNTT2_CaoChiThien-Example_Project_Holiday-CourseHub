import React from 'react';
import { Button as AntdButton } from 'antd';
import type { ButtonProps } from 'antd';

export const Button: React.FC<ButtonProps> = ({ style, ...props }) => {
  return (
    <AntdButton
      style={{
        borderRadius: '8px',
        boxShadow: 'none',
        fontWeight: 500,
        transition: 'all 0.2s ease',
        ...style,
      }}
      {...props}
    />
  );
};
