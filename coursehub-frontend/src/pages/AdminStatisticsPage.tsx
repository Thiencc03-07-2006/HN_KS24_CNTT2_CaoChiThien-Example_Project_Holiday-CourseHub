import React, { useEffect, useState } from 'react';
import { Row, Col, Typography, message } from 'antd';
import { UserOutlined, BookOutlined, DollarOutlined, SolutionOutlined } from '@ant-design/icons';
import { dashboardService } from '../services/dashboardService';
import { Card } from '../components/common/UI/Card';
import { Loading } from '../components/common/UI/Loading';

const { Title, Text } = Typography;

const AdminStatisticsPage: React.FC = () => {
  const [stats, setStats] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const res = await dashboardService.getAdminStatistics();
        if (res?.success && res?.data) {
          setStats(res.data);
        }
      } catch (err) {
        message.error('Không thể tải thống kê hệ thống.');
      } finally {
        setLoading(false);
      }
    };
    fetchStats();
  }, []);

  if (loading) {
    return <Loading message="Đang tải dữ liệu thống kê hệ thống..." />;
  }

  return (
    <div>
      <Title level={3} style={{ color: 'var(--text-color)', marginBottom: 24 }}>
        Thống kê hệ thống
      </Title>

      {stats && (
        <Row gutter={[16, 16]}>
          <Col xs={24} sm={12} md={8}>
            <Card style={{ textAlign: 'center', padding: '16px' }}>
              <UserOutlined style={{ fontSize: '32px', color: 'var(--primary-color)', marginBottom: '12px' }} />
              <div>
                <Text style={{ fontSize: '14px', display: 'block', opacity: 0.8 }}>Tổng số tài khoản</Text>
                <Title level={2} style={{ margin: '4px 0', color: 'var(--text-color)' }}>
                  {stats.totalUsers}
                </Title>
              </div>
            </Card>
          </Col>

          <Col xs={24} sm={12} md={8}>
            <Card style={{ textAlign: 'center', padding: '16px' }}>
              <SolutionOutlined style={{ fontSize: '32px', color: 'var(--primary-color)', marginBottom: '12px' }} />
              <div>
                <Text style={{ fontSize: '14px', display: 'block', opacity: 0.8 }}>Tổng số học viên</Text>
                <Title level={2} style={{ margin: '4px 0', color: 'var(--text-color)' }}>
                  {stats.totalStudents}
                </Title>
              </div>
            </Card>
          </Col>

          <Col xs={24} sm={12} md={8}>
            <Card style={{ textAlign: 'center', padding: '16px' }}>
              <SolutionOutlined style={{ fontSize: '32px', color: 'var(--primary-color)', marginBottom: '12px' }} />
              <div>
                <Text style={{ fontSize: '14px', display: 'block', opacity: 0.8 }}>Tổng số giảng viên</Text>
                <Title level={2} style={{ margin: '4px 0', color: 'var(--text-color)' }}>
                  {stats.totalInstructor}
                </Title>
              </div>
            </Card>
          </Col>

          <Col xs={24} sm={12} md={8}>
            <Card style={{ textAlign: 'center', padding: '16px' }}>
              <BookOutlined style={{ fontSize: '32px', color: 'var(--primary-color)', marginBottom: '12px' }} />
              <div>
                <Text style={{ fontSize: '14px', display: 'block', opacity: 0.8 }}>Tổng số khóa học</Text>
                <Title level={2} style={{ margin: '4px 0', color: 'var(--text-color)' }}>
                  {stats.totalCourses}
                </Title>
              </div>
            </Card>
          </Col>

          <Col xs={24} sm={12} md={8}>
            <Card style={{ textAlign: 'center', padding: '16px' }}>
              <BookOutlined style={{ fontSize: '32px', color: 'var(--primary-color)', marginBottom: '12px' }} />
              <div>
                <Text style={{ fontSize: '14px', display: 'block', opacity: 0.8 }}>Tổng lượt đăng ký</Text>
                <Title level={2} style={{ margin: '4px 0', color: 'var(--text-color)' }}>
                  {stats.totalEnrollments}
                </Title>
              </div>
            </Card>
          </Col>

          <Col xs={24} sm={12} md={8}>
            <Card style={{ textAlign: 'center', padding: '16px' }}>
              <DollarOutlined style={{ fontSize: '32px', color: 'var(--primary-color)', marginBottom: '12px' }} />
              <div>
                <Text style={{ fontSize: '14px', display: 'block', opacity: 0.8 }}>Tổng doanh thu gộp</Text>
                <Title level={2} style={{ margin: '4px 0', color: 'var(--primary-color)', fontWeight: 700 }}>
                  {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(stats.totalRevenue)}
                </Title>
              </div>
            </Card>
          </Col>
        </Row>
      )}
    </div>
  );
};

export default AdminStatisticsPage;
