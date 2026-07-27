import { Table as AntdTable } from 'antd';
import type { TableProps } from 'antd';

export const Table = <RecordType extends object = any>(props: TableProps<RecordType>) => {
  return (
    <AntdTable
      bordered
      style={{
        borderRadius: '8px',
        ...props.style,
      }}
      {...props}
    />
  );
};
