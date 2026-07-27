import React, { useEffect, useState } from 'react';
import { Row, Col, Card, Statistic, Table, Avatar, Tag, Space, Typography, Spin, message, Tabs } from 'antd';
import { UserOutlined, BookOutlined, SolutionOutlined, HeartOutlined, WarningOutlined, MessageOutlined, CheckCircleOutlined, InfoCircleOutlined } from '@ant-design/icons';
import axiosInstance from '../api/axiosInstance';

const { Title, Text } = Typography;

interface StatsData {
  totalUsers: number;
  totalCourses: number;
  totalCategories: number;
  totalEnrollments: number;
  recentUsers: any[];
  recentCourses: any[];
  totalWishlist: number;
  top10FavoriteCourses: any[];
  totalCourseReports: number;
  totalCommentReports: number;
  pendingReportsCount: number;
  resolvedReportsCount: number;
}

const IconWrapper: React.FC<{ children: React.ReactNode; color: string; bgColor: string }> = ({ children, color, bgColor }) => (
  <div style={{
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    width: '40px',
    height: '40px',
    borderRadius: '8px',
    color: color,
    backgroundColor: bgColor,
    fontSize: '20px',
    marginRight: '12px'
  }}>
    {children}
  </div>
);

const AdminDashboard: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState<StatsData | null>(null);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const res = await axiosInstance.get('/admin/dashboard/stats');
        if (res.data?.success && res.data?.data) {
          setStats(res.data.data);
        }
      } catch (err: any) {
        message.error('Không thể tải số liệu thống kê dashboard.');
      } finally {
        setLoading(false);
      }
    };
    fetchStats();
  }, []);

  if (loading || !stats) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '400px' }}>
        <Spin size="large" description="Đang tải dữ liệu thống kê hệ thống..." />
      </div>
    );
  }

  const userColumns = [
    {
      title: 'Họ tên',
      dataIndex: 'fullName',
      key: 'fullName',
      render: (text: string, record: any) => (
        <Space>
          <Avatar src={record.avatarUrl} icon={<UserOutlined />} />
          <Text strong style={{ color: 'var(--text-color)' }}>{text}</Text>
        </Space>
      ),
    },
    {
      title: 'Email',
      dataIndex: 'email',
      key: 'email',
      render: (email: string) => <Text style={{ color: 'var(--text-muted)' }}>{email}</Text>,
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => {
        const color = status === 'ACTIVE' ? 'success' : 'warning';
        return <Tag color={color}>{status}</Tag>;
      },
    },
  ];

  const courseColumns = [
    {
      title: 'Tên khóa học',
      dataIndex: 'title',
      key: 'title',
      render: (text: string) => <Text strong style={{ color: 'var(--text-color)' }}>{text}</Text>,
    },
    {
      title: 'Giảng viên',
      dataIndex: 'instructor',
      key: 'instructor',
      render: (ins: any) => <Text style={{ color: 'var(--text-muted)' }}>{ins?.fullName || 'N/A'}</Text>,
    },
    {
      title: 'Giá',
      dataIndex: 'price',
      key: 'price',
      render: (price: number) => (
        <Text style={{ color: 'var(--primary)', fontWeight: 600 }}>
          {price === 0 ? 'Miễn phí' : new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price)}
        </Text>
      ),
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => {
        let color = 'geekblue';
        if (status === 'PUBLISHED') color = 'success';
        if (status === 'PENDING_REVIEW') color = 'warning';
        if (status === 'REJECTED') color = 'error';
        return <Tag color={color}>{status}</Tag>;
      },
    },
  ];

  const topFavoriteColumns = [
    {
      title: 'Tên khóa học',
      dataIndex: 'title',
      key: 'title',
      render: (text: string) => <Text strong style={{ color: 'var(--text-color)' }}>{text}</Text>,
    },
    {
      title: 'Giảng viên',
      dataIndex: 'instructor',
      key: 'instructor',
      render: (ins: any) => <Text style={{ color: 'var(--text-muted)' }}>{ins?.fullName || 'N/A'}</Text>,
    },
    {
      title: 'Học viên',
      dataIndex: 'totalStudents',
      key: 'totalStudents',
      render: (students: number) => <Text style={{ color: 'var(--text-muted)' }}>👥 {students || 0}</Text>,
    },
    {
      title: 'Học phí',
      dataIndex: 'price',
      key: 'price',
      render: (price: number) => (
        <Text style={{ color: 'var(--primary)', fontWeight: 600 }}>
          {price === 0 ? 'Miễn phí' : new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price)}
        </Text>
      ),
    },
  ];

  return (
    <div>
      <Title level={3} style={{ color: 'var(--text-color)', marginBottom: 24 }}>Tổng quan Hệ thống</Title>

      {/* Metrics Row 1: Core Metrics */}
      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} lg={6}>
          <Card style={{ background: 'var(--bg-white)', border: '1px solid var(--border-color)', borderRadius: 12, boxShadow: '0 4px 12px rgba(0,0,0,0.06)' }}>
            <Statistic
              title={<span style={{ color: 'var(--text-muted)', fontWeight: 500 }}>Tổng người dùng</span>}
              value={stats.totalUsers}
              prefix={
                <IconWrapper color="var(--primary)" bgColor="rgba(37, 99, 235, 0.1)">
                  <UserOutlined />
                </IconWrapper>
              }
              valueStyle={{ color: 'var(--text-color)', fontWeight: 700 }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card style={{ background: 'var(--bg-white)', border: '1px solid var(--border-color)', borderRadius: 12, boxShadow: '0 4px 12px rgba(0,0,0,0.06)' }}>
            <Statistic
              title={<span style={{ color: 'var(--text-muted)', fontWeight: 500 }}>Tổng khóa học</span>}
              value={stats.totalCourses}
              prefix={
                <IconWrapper color="var(--success)" bgColor="rgba(34, 197, 94, 0.1)">
                  <BookOutlined />
                </IconWrapper>
              }
              valueStyle={{ color: 'var(--text-color)', fontWeight: 700 }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card style={{ background: 'var(--bg-white)', border: '1px solid var(--border-color)', borderRadius: 12, boxShadow: '0 4px 12px rgba(0,0,0,0.06)' }}>
            <Statistic
              title={<span style={{ color: 'var(--text-muted)', fontWeight: 500 }}>Lượt đăng ký học</span>}
              value={stats.totalEnrollments}
              prefix={
                <IconWrapper color="var(--warning)" bgColor="rgba(245, 158, 11, 0.1)">
                  <SolutionOutlined />
                </IconWrapper>
              }
              valueStyle={{ color: 'var(--text-color)', fontWeight: 700 }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card style={{ background: 'var(--bg-white)', border: '1px solid var(--border-color)', borderRadius: 12, boxShadow: '0 4px 12px rgba(0,0,0,0.06)' }}>
            <Statistic
              title={<span style={{ color: 'var(--text-muted)', fontWeight: 500 }}>Lượt lưu yêu thích</span>}
              value={stats.totalWishlist || 0}
              prefix={
                <IconWrapper color="var(--danger)" bgColor="rgba(239, 68, 68, 0.1)">
                  <HeartOutlined />
                </IconWrapper>
              }
              valueStyle={{ color: 'var(--text-color)', fontWeight: 700 }}
            />
          </Card>
        </Col>
      </Row>

      {/* Metrics Row 2: Content Moderation & Reports */}
      <Title level={4} style={{ color: 'var(--text-color)', marginTop: 24, marginBottom: 16 }}>Giám sát & Báo cáo vi phạm</Title>
      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} lg={6}>
          <Card style={{ background: 'var(--bg-white)', border: '1px solid var(--border-color)', borderRadius: 12, boxShadow: '0 4px 12px rgba(0,0,0,0.06)' }}>
            <Statistic
              title={<span style={{ color: 'var(--text-muted)', fontWeight: 500 }}>Báo cáo khóa học</span>}
              value={stats.totalCourseReports || 0}
              prefix={
                <IconWrapper color="var(--danger)" bgColor="rgba(239, 68, 68, 0.1)">
                  <WarningOutlined />
                </IconWrapper>
              }
              valueStyle={{ color: 'var(--text-color)', fontWeight: 700 }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card style={{ background: 'var(--bg-white)', border: '1px solid var(--border-color)', borderRadius: 12, boxShadow: '0 4px 12px rgba(0,0,0,0.06)' }}>
            <Statistic
              title={<span style={{ color: 'var(--text-muted)', fontWeight: 500 }}>Báo cáo bình luận/đánh giá</span>}
              value={stats.totalCommentReports || 0}
              prefix={
                <IconWrapper color="#F97316" bgColor="rgba(249, 115, 22, 0.1)">
                  <MessageOutlined />
                </IconWrapper>
              }
              valueStyle={{ color: 'var(--text-color)', fontWeight: 700 }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card style={{ background: 'var(--bg-white)', border: '1px solid var(--border-color)', borderRadius: 12, boxShadow: '0 4px 12px rgba(0,0,0,0.06)' }}>
            <Statistic
              title={<span style={{ color: 'var(--text-muted)', fontWeight: 500 }}>Báo cáo chưa xử lý (Pending)</span>}
              value={stats.pendingReportsCount || 0}
              prefix={
                <IconWrapper color="var(--warning)" bgColor="rgba(245, 158, 11, 0.1)">
                  <InfoCircleOutlined />
                </IconWrapper>
              }
              valueStyle={{ color: 'var(--text-color)', fontWeight: 700 }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card style={{ background: 'var(--bg-white)', border: '1px solid var(--border-color)', borderRadius: 12, boxShadow: '0 4px 12px rgba(0,0,0,0.06)' }}>
            <Statistic
              title={<span style={{ color: 'var(--text-muted)', fontWeight: 500 }}>Báo cáo đã xử lý</span>}
              value={stats.resolvedReportsCount || 0}
              prefix={
                <IconWrapper color="var(--success)" bgColor="rgba(34, 197, 94, 0.1)">
                  <CheckCircleOutlined />
                </IconWrapper>
              }
              valueStyle={{ color: 'var(--text-color)', fontWeight: 700 }}
            />
          </Card>
        </Col>
      </Row>

      {/* Lists Section */}
      <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
        <Col xs={24} xl={12}>
          <Card
            title={
              <span style={{ color: 'var(--text-color)' }}>
                <HeartOutlined style={{ marginRight: 8, color: 'var(--danger)' }} />
                Top 10 Khóa học được yêu thích nhất
              </span>
            }
            style={{
              background: 'var(--bg-white)',
              border: '1px solid var(--border-color)',
              borderRadius: 12,
              boxShadow: '0 4px 12px rgba(0,0,0,0.06)'
            }}
          >
            <Table
              dataSource={stats.top10FavoriteCourses || []}
              columns={topFavoriteColumns}
              rowKey="id"
              pagination={false}
              size="small"
              style={{ background: 'transparent' }}
            />
          </Card>
        </Col>

        <Col xs={24} xl={12}>
          <Card
            title={<span style={{ color: 'var(--text-color)' }}>Hoạt động Hệ thống Gần đây</span>}
            style={{
              background: 'var(--bg-white)',
              border: '1px solid var(--border-color)',
              borderRadius: 12,
              boxShadow: '0 4px 12px rgba(0,0,0,0.06)'
            }}
          >
            <Tabs defaultActiveKey="users" type="line" size="small">
              <Tabs.TabPane tab="Học viên mới đăng ký" key="users">
                <Table
                  dataSource={stats.recentUsers}
                  columns={userColumns}
                  rowKey="id"
                  pagination={false}
                  size="small"
                  style={{ background: 'transparent' }}
                />
              </Tabs.TabPane>
              <Tabs.TabPane tab="Khóa học mới tạo" key="courses">
                <Table
                  dataSource={stats.recentCourses}
                  columns={courseColumns}
                  rowKey="id"
                  pagination={false}
                  size="small"
                  style={{ background: 'transparent' }}
                />
              </Tabs.TabPane>
            </Tabs>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default AdminDashboard;
