import React from 'react';
import { Avatar as AntdAvatar } from 'antd';
import type { AvatarProps } from 'antd';
import { UserOutlined } from '@ant-design/icons';

export const Avatar: React.FC<AvatarProps> = (props) => {
  return (
    <AntdAvatar
      icon={<UserOutlined />}
      style={{
        backgroundColor: 'var(--secondary-color)',
        color: 'var(--primary-color)',
        ...props.style,
      }}
      {...props}
    />
  );
};
