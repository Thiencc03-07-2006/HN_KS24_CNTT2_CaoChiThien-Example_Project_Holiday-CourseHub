import React from 'react';
import { Input as AntdInput } from 'antd';
import type { InputProps } from 'antd';

export const Input = (({ style, ...props }: InputProps) => {
  return (
    <AntdInput
      style={{
        borderRadius: '8px',
        borderColor: 'var(--border-color)',
        fontSize: '14px',
        ...style,
      }}
      {...props}
    />
  );
}) as React.FC<InputProps> & {
  TextArea: typeof AntdInput.TextArea;
  Password: typeof AntdInput.Password;
};

Input.TextArea = AntdInput.TextArea;
Input.Password = AntdInput.Password;
export type { InputProps };
