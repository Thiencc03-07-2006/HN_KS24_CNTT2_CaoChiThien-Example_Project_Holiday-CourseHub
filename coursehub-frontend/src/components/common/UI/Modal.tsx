import React from 'react';
import { Modal as AntdModal } from 'antd';
import type { ModalProps } from 'antd';

export const Modal: React.FC<ModalProps> = ({ style, ...props }) => {
  return (
    <AntdModal
      style={{
        borderRadius: '8px',
        overflow: 'hidden',
        ...style,
      }}
      {...props}
    />
  );
};
