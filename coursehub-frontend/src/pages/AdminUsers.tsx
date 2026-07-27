import React, { useEffect, useState } from 'react';
import { Table, Input, Select, Button, Space, Card, Modal, Descriptions, Tag, Switch, message, Typography, Form } from 'antd';
import { SearchOutlined, EyeOutlined, PlusOutlined } from '@ant-design/icons';
import axiosInstance from '../api/axiosInstance';

const { Title, Text } = Typography;
const { Option } = Select;

interface UserRecord {
  id: string;
  email: string;
  fullName: string;
  phoneNumber?: string;
  avatarUrl?: string;
  bio?: string;
  status: string;
  roles: string[];
  createdAt: string;
  instructorProfile?: any;
}

const AdminUsers: React.FC = () => {
  const [users, setUsers] = useState<UserRecord[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);

  // Search & Filter state
  const [keyword, setKeyword] = useState('');
  const [status, setStatus] = useState<string | undefined>(undefined);
  const [role, setRole] = useState<string | undefined>(undefined);

  // Detail Modal state
  const [selectedUser, setSelectedUser] = useState<UserRecord | null>(null);
  const [modalOpen, setModalOpen] = useState(false);

  // Role addition state
  const [roleModalOpen, setRoleModalOpen] = useState(false);
  const [selectedUserForRole, setSelectedUserForRole] = useState<UserRecord | null>(null);
  const [newRole, setNewRole] = useState<string | undefined>(undefined);
  const [roleError, setRoleError] = useState('');


  const fetchUsers = async () => {
    setLoading(true);
    try {
      const params: any = {
        page,
        size: pageSize,
      };
      if (keyword) params.keyword = keyword;
      if (status) params.status = status;
      if (role) params.role = role;

      const res = await axiosInstance.get('/admin/users', { params });
      if (res.data?.success && res.data?.data) {
        setUsers(res.data.data.content);
        setTotal(res.data.data.totalElements);
      }
    } catch (err: any) {
      message.error('Không thể tải danh sách người dùng.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, [page, pageSize, status, role]);

  const handleSearch = () => {
    setPage(0);
    fetchUsers();
  };

  const handleStatusToggle = async (userId: string, checked: boolean) => {
    const newStatus = checked ? 'ACTIVE' : 'BANNED';
    try {
      await axiosInstance.put(`/admin/users/${userId}/status`, null, {
        params: { status: newStatus },
      });
      message.success(checked ? 'Đã kích hoạt tài khoản!' : 'Đã khóa tài khoản!');
      fetchUsers();
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Không thể cập nhật trạng thái người dùng.');
    }
  };

  const handleAddRole = async () => {
    if (!selectedUserForRole || !newRole) {
      setRoleError('Trường này là bắt buộc');
      return;
    }
    setRoleError('');
    try {
      await axiosInstance.post(`/admin/users/${selectedUserForRole.id}/roles`, null, {
        params: { role: newRole }
      });
      message.success('Đã thêm vai trò thành công!');
      setRoleModalOpen(false);
      setNewRole(undefined);
      setSelectedUserForRole(null);
      fetchUsers();
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Không thể thêm vai trò.');
    }
  };


  const handleRemoveRole = async (userId: string, roleName: string) => {
    try {
      await axiosInstance.delete(`/admin/users/${userId}/roles/${roleName}`);
      message.success('Đã xóa vai trò thành công!');
      fetchUsers();
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Không thể xóa vai trò.');
    }
  };

  const viewDetails = (user: UserRecord) => {
    setSelectedUser(user);
    setModalOpen(true);
  };

  const columns = [
    {
      title: 'Họ tên',
      dataIndex: 'fullName',
      key: 'fullName',
      render: (text: string) => <Text strong style={{ color: '#333333' }}>{text}</Text>,
    },
    {
      title: 'Email',
      dataIndex: 'email',
      key: 'email',
      render: (email: string) => <Text style={{ color: '#a3b1cc' }}>{email}</Text>,
    },
    {
      title: 'Quyền',
      dataIndex: 'roles',
      key: 'roles',
      render: (roles: string[], record: UserRecord) => (
        <Space wrap>
          {roles.map((r) => {
            let color = 'blue';
            if (r === 'ROLE_ADMIN') color = 'volcano';
            if (r === 'ROLE_INSTRUCTOR') color = 'green';
            return (
              <Tag
                color={color}
                key={r}
                closable={r !== 'ROLE_ADMIN'}
                onClose={() => handleRemoveRole(record.id, r)}
                style={{ marginBottom: 4 }}
              >
                {r.replace('ROLE_', '')}
              </Tag>
            );
          })}
          <Button
            type="dashed"
            size="small"
            icon={<PlusOutlined />}
            onClick={() => {
              setSelectedUserForRole(record);
              setNewRole(undefined);
              setRoleError('');
              setRoleModalOpen(true);
            }}

            style={{
              background: 'transparent',
              color: '#389e0d',
              borderColor: '#389e0d',
              fontSize: '12px',
              padding: '0 8px',
              height: '22px',
              display: 'inline-flex',
              alignItems: 'center'
            }}
          >
            Thêm
          </Button>
        </Space>
      ),
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      key: 'status',
      render: (userStatus: string, record: UserRecord) => {
        const isActive = userStatus === 'ACTIVE';
        return (
          <Space>
            <Switch
              checked={isActive}
              onChange={(checked) => handleStatusToggle(record.id, checked)}
              disabled={record.roles.includes('ROLE_ADMIN')} // Don't allow banning admin accounts
            />
            <Tag color={isActive ? 'success' : 'error'}>{userStatus}</Tag>
          </Space>
        );
      },
    },
    {
      title: 'Ngày tạo',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (date: string) => <Text style={{ color: '#8c9db5' }}>{new Date(date).toLocaleDateString('vi-VN')}</Text>,
    },
    {
      title: 'Hành động',
      key: 'action',
      render: (_: any, record: UserRecord) => (
        <Button
          type="primary"
          icon={<EyeOutlined />}
          onClick={() => viewDetails(record)}
        >
          Chi tiết
        </Button>
      ),
    },
  ];

  return (
    <div>
      <Title level={3} style={{ color: '#fff', marginBottom: 24 }}>Quản lý Người dùng</Title>

      {/* Filter Row */}
      <Card style={{
        background: '#FFFFFF',
        border: '1px solid #E5E7EB',
        borderRadius: 12,
        marginBottom: 24,
        boxShadow: '0 4px 12px rgba(0,0,0,0.06)'
      }}>
        <Space wrap size="middle">
          <Input
            placeholder="Tìm theo tên hoặc email..."
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            onPressEnter={handleSearch}
            style={{ width: 250, background: '#FFFFFF', color: '#111827', border: '1px solid #E5E7EB' }}
            suffix={<SearchOutlined style={{ color: '#8c9db5' }} />}
          />
          <Select
            placeholder="Lọc trạng thái"
            style={{ width: 150 }}
            allowClear
            onChange={(val) => setStatus(val)}
          >
            <Option value="ACTIVE">Hoạt động</Option>
            <Option value="BANNED">Đang khóa</Option>
            <Option value="PENDING_VERIFICATION">Chờ xác thực</Option>
          </Select>
          <Select
            placeholder="Lọc vai trò"
            style={{ width: 150 }}
            allowClear
            onChange={(val) => setRole(val)}
          >
            <Option value="ROLE_STUDENT">Học viên</Option>
            <Option value="ROLE_INSTRUCTOR">Giảng viên</Option>
            <Option value="ROLE_ADMIN">Quản trị viên</Option>
          </Select>
          <Button type="primary" onClick={handleSearch}>Lọc dữ liệu</Button>
        </Space>
      </Card>

      {/* User Table */}
      <Table
        dataSource={users}
        columns={columns}
        rowKey="id"
        loading={loading}
        pagination={{
          current: page + 1,
          pageSize,
          total,
          onChange: (p, size) => {
            setPage(p - 1);
            setPageSize(size);
          },
          showSizeChanger: true,
          style: { color: '#fff' }
        }}
        style={{
          background: '#FFFFFF',
          borderRadius: 12,
          overflow: 'hidden',
          border: '1px solid #E5E7EB',
          boxShadow: '0 4px 12px rgba(0,0,0,0.06)'
        }}
      />

      {/* Detail Modal */}
      <Modal
        title="Thông tin chi tiết Người dùng"
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        footer={[
          <Button key="close" onClick={() => setModalOpen(false)}>Đóng</Button>
        ]}
        width={600}
      >
        {selectedUser && (
          <Descriptions bordered column={1} size="small" style={{ marginTop: 16 }}>
            <Descriptions.Item label="Họ và tên">{selectedUser.fullName}</Descriptions.Item>
            <Descriptions.Item label="Email">{selectedUser.email}</Descriptions.Item>
            <Descriptions.Item label="Số điện thoại">{selectedUser.phoneNumber || 'Chưa cung cấp'}</Descriptions.Item>
            <Descriptions.Item label="Tiểu sử">{selectedUser.bio || 'Trống'}</Descriptions.Item>
            <Descriptions.Item label="Trạng thái">
              <Tag color={selectedUser.status === 'ACTIVE' ? 'success' : 'error'}>{selectedUser.status}</Tag>
            </Descriptions.Item>
            <Descriptions.Item label="Vai trò">
              <Space>
                {selectedUser.roles.map((r) => <Tag color="blue" key={r}>{r}</Tag>)}
              </Space>
            </Descriptions.Item>
            <Descriptions.Item label="Ngày đăng ký">
              {new Date(selectedUser.createdAt).toLocaleString('vi-VN')}
            </Descriptions.Item>
            {selectedUser.instructorProfile && (
              <>
                <Descriptions.Item label="Slogan giảng viên">{selectedUser.instructorProfile.headline}</Descriptions.Item>
                <Descriptions.Item label="Giới thiệu giảng viên">{selectedUser.instructorProfile.detailedBio}</Descriptions.Item>
                <Descriptions.Item label="Website">{selectedUser.instructorProfile.websiteUrl}</Descriptions.Item>
              </>
            )}
          </Descriptions>
        )}
      </Modal>

      {/* Role Selection Modal */}
      <Modal
        title="Thêm Vai trò mới"
        open={roleModalOpen}
        onOk={handleAddRole}
        onCancel={() => {
          setRoleModalOpen(false);
          setSelectedUserForRole(null);
          setNewRole(undefined);
        }}
        okText="Xác nhận thêm"
        cancelText="Hủy"
        width={400}
      >
        <Form layout="vertical" style={{ marginTop: 16 }} noValidate>
          <Form.Item label={<span>Chọn vai trò muốn thêm cho người dùng <strong>{selectedUserForRole?.fullName}</strong>:</span>}>
            <Select
              placeholder="Chọn vai trò"
              style={{ width: '100%' }}
              value={newRole}
              onChange={(val) => setNewRole(val)}
            >
              <Option value="ROLE_STUDENT">ROLE_STUDENT (Học viên)</Option>
              <Option value="ROLE_INSTRUCTOR">ROLE_INSTRUCTOR (Giảng viên)</Option>
              <Option value="ROLE_MODERATOR">ROLE_MODERATOR (Kiểm duyệt viên)</Option>
            </Select>
            {roleError && <div className="input-error">{roleError}</div>}
          </Form.Item>
        </Form>
      </Modal>

    </div>
  );
};

export default AdminUsers;
