import React from 'react';
import { Pagination as AntdPagination } from 'antd';
import type { PaginationProps } from 'antd';

export const Pagination: React.FC<PaginationProps> = (props) => {
  return (
    <AntdPagination
      style={{
        display: 'flex',
        justifyContent: 'center',
        marginTop: '24px',
        ...props.style,
      }}
      {...props}
    />
  );
};
