import React, { useEffect, useState } from 'react';
import { Table, Button, Form, Input, Modal, Space, Popconfirm, message, Typography, Tag, TreeSelect } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, FolderOpenOutlined } from '@ant-design/icons';
import axiosInstance from '../api/axiosInstance';

const { Title, Text } = Typography;

interface CategoryRecord {
  id: number;
  name: string;
  slug: string;
  icon?: string;
  description?: string;
  parentId?: number;
  children?: CategoryRecord[];
  courseCount: number;
}

const AdminCategories: React.FC = () => {
  const [categories, setCategories] = useState<CategoryRecord[]>([]);
  const [loading, setLoading] = useState(false);

  // Form & Modal state
  const [modalOpen, setModalOpen] = useState(false);
  const [form] = Form.useForm();
  const [editingId, setEditingId] = useState<number | null>(null);

  const fetchCategories = async () => {
    setLoading(true);
    try {
      const res = await axiosInstance.get('/categories');
      if (res.data?.success && res.data?.data) {
        setCategories(res.data.data);
      }
    } catch (err: any) {
      message.error('Không thể tải danh mục.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCategories();
  }, []);

  const handleOpenCreate = () => {
    setEditingId(null);
    form.resetFields();
    setModalOpen(true);
  };

  const handleOpenEdit = (record: CategoryRecord) => {
    setEditingId(record.id);
    form.setFieldsValue({
      name: record.name,
      slug: record.slug,
      icon: record.icon,
      description: record.description,
      parentId: record.parentId,
    });
    setModalOpen(true);
  };

  const handleDelete = async (id: number) => {
    try {
      await axiosInstance.delete(`/admin/categories/${id}`);
      message.success('Xóa danh mục thành công!');
      fetchCategories();
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Không thể xóa danh mục. Vui lòng kiểm tra xem danh mục có khóa học hoặc danh mục con hay không.');
    }
  };

  const handleSave = async (values: any) => {
    try {
      if (editingId) {
        await axiosInstance.put(`/admin/categories/${editingId}`, values);
        message.success('Cập nhật danh mục thành công!');
      } else {
        await axiosInstance.post('/admin/categories', values);
        message.success('Tạo danh mục mới thành công!');
      }
      setModalOpen(false);
      fetchCategories();
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Không thể lưu danh mục.');
    }
  };

  const findNodeById = (id: number, items: CategoryRecord[]): CategoryRecord | null => {
    for (const item of items) {
      if (item.id === id) return item;
      if (item.children) {
        const found = findNodeById(id, item.children);
        if (found) return found;
      }
    }
    return null;
  };

  const getDescendantIdsFromNode = (node: CategoryRecord): number[] => {
    const ids: number[] = [];
    const traverse = (n: CategoryRecord) => {
      if (n.children) {
        n.children.forEach(child => {
          ids.push(child.id);
          traverse(child);
        });
      }
    };
    traverse(node);
    return ids;
  };

  const buildTreeData = (items: CategoryRecord[], currentId: number | null, descendantIds: number[]): any[] => {
    return items.map(item => {
      const isDisabled = currentId !== null && (item.id === currentId || descendantIds.includes(item.id));
      return {
        title: item.name,
        value: item.id,
        key: item.id,
        disabled: isDisabled,
        children: item.children && item.children.length > 0 
          ? buildTreeData(item.children, currentId, descendantIds) 
          : undefined
      };
    });
  };

  const columns = [
    {
      title: 'Tên danh mục',
      dataIndex: 'name',
      key: 'name',
      render: (text: string) => (
        <Space>
          <FolderOpenOutlined style={{ color: '#faad14' }} />
          <Text strong>{text}</Text>
        </Space>
      ),
    },
    {
      title: 'Slug',
      dataIndex: 'slug',
      key: 'slug',
      render: (slug: string) => <Text style={{ color: '#a3b1cc' }}>{slug}</Text>,
    },
    {
      title: 'Icon',
      dataIndex: 'icon',
      key: 'icon',
      render: (icon: string) => <Text style={{ color: '#8c9db5' }}>{icon || '-'}</Text>,
    },
    {
      title: 'Số khóa học',
      dataIndex: 'courseCount',
      key: 'courseCount',
      render: (count: number) => <Tag color="blue">{count} khóa học</Tag>,
    },
    {
      title: 'Hành động',
      key: 'action',
      render: (_: any, record: CategoryRecord) => (
        <Space size="middle">
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => handleOpenEdit(record)}
          >
            Sửa
          </Button>
          <Popconfirm
            title="Bạn có chắc chắn muốn xóa danh mục này?"
            okText="Xóa"
            cancelText="Hủy"
            onConfirm={() => handleDelete(record.id)}
          >
            <Button
              type="link"
              danger
              icon={<DeleteOutlined />}
            >
              Xóa
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  const currentEditingNode = editingId ? findNodeById(editingId, categories) : null;
  const descendantIds = currentEditingNode ? getDescendantIdsFromNode(currentEditingNode) : [];
  const treeData = buildTreeData(categories, editingId, descendantIds);

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <Title level={3} style={{ color: '#333333', margin: 0 }}>Quản lý Danh mục</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleOpenCreate}>
          Thêm danh mục
        </Button>
      </div>

      <Table
        dataSource={categories}
        columns={columns}
        rowKey="id"
        loading={loading}
        pagination={false}
        defaultExpandAllRows
        style={{
          overflow: 'hidden',
          background: '#FFFFFF',
          borderRadius: 12,
          border: '1px solid #E5E7EB',
          boxShadow: '0 4px 12px rgba(0,0,0,0.06)'
        }}
      />

      <Modal
        title={editingId ? 'Sửa Danh mục' : 'Thêm Danh mục mới'}
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        onOk={() => form.submit()}
        okText="Lưu lại"
        cancelText="Hủy"
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSave}
          style={{ marginTop: 16 }}
          noValidate
        >
          <Form.Item
            label="Tên danh mục"
            name="name"
            rules={[
              { required: true, message: 'Tên danh mục không được để trống' },
              { min: 2, max: 100, message: 'Tên danh mục từ 2 đến 100 ký tự' }
            ]}
          >
            <Input placeholder="Ví dụ: Lập trình Java" />
          </Form.Item>


          <Form.Item
            label="Slug (Đường dẫn thân thiện)"
            name="slug"
            help="Để trống để tự động tạo từ tên danh mục"
            rules={[{ max: 120, message: 'Slug tối đa 120 ký tự' }]}
          >
            <Input placeholder="Ví dụ: lap-trinh-java" />
          </Form.Item>

          <Form.Item
            label="Danh mục cha (Tùy chọn)"
            name="parentId"
            rules={[
              {
                validator: async (_, value) => {
                  if (value && editingId) {
                    if (value === editingId) {
                      return Promise.reject(new Error('Danh mục không được phép chọn chính nó làm cha!'));
                    }
                    if (descendantIds.includes(value)) {
                      return Promise.reject(new Error('Không được phép chọn một danh mục con/cháu của chính nó làm danh mục cha!'));
                    }
                  }
                  return Promise.resolve();
                }
              }
            ]}
          >
            <TreeSelect
              placeholder="Chọn danh mục cha"
              allowClear
              treeData={treeData}
              treeDefaultExpandAll
              style={{ width: '100%' }}
            />
          </Form.Item>

          <Form.Item
            label="Tên Icon (Tùy chọn)"
            name="icon"
            rules={[{ max: 100, message: 'Icon tối đa 100 ký tự' }]}
          >
            <Input placeholder="Ví dụ: code, laptop, database..." />
          </Form.Item>

          <Form.Item
            label="Mô tả danh mục"
            name="description"
            rules={[{ max: 255, message: 'Mô tả tối đa 255 ký tự' }]}
          >
            <Input.TextArea placeholder="Mô tả ngắn gọn về danh mục..." rows={3} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default AdminCategories;
