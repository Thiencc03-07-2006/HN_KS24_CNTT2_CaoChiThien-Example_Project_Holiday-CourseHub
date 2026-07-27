import React from 'react';
import { Select as AntdSelect } from 'antd';
import type { SelectProps } from 'antd';

export const Select = (({ style, ...props }: SelectProps) => {
  return (
    <AntdSelect
      style={{
        borderRadius: '6px',
        minWidth: '120px',
        ...style,
      }}
      {...props}
    />
  );
}) as React.FC<SelectProps> & { Option: typeof AntdSelect.Option };

Select.Option = AntdSelect.Option;
export type { SelectProps };
