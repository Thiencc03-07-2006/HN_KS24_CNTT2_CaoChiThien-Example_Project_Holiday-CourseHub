import React, { useEffect, useState } from 'react';
import { Row, Col, Progress, Typography, message } from 'antd';
import { CompassOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../api/axiosInstance';
import { Card } from '../components/common/UI/Card';
import { Button } from '../components/common/UI/Button';
import { Avatar } from '../components/common/UI/Avatar';
import { Loading } from '../components/common/UI/Loading';
import { EmptyState } from '../components/common/UI/EmptyState';

const { Title, Paragraph, Text } = Typography;

interface Enrollment {
  id: string;
  enrollmentDate: string;
  progressPercent: number;
  status: string;
  course: {
    id: string;
    title: string;
    slug: string;
    shortDescription: string;
    thumbnailUrl?: string;
    instructor: {
      fullName: string;
      avatarUrl?: string;
    };
  };
}

const StudentDashboard: React.FC = () => {
  const [enrollments, setEnrollments] = useState<Enrollment[]>([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchEnrollments = async () => {
      try {
        const res = await axiosInstance.get('/enrollments/me');
        if (res.data?.success && res.data?.data) {
          setEnrollments(res.data.data.content || []);
        }
      } catch (err: any) {
        message.error('Không thể tải danh sách khóa học đã đăng ký.');
      } finally {
        setLoading(false);
      }
    };
    fetchEnrollments();
  }, []);

  if (loading) {
    return <Loading message="Đang tải danh sách khóa học của bạn..." />;
  }

  return (
    <div>
      <Title level={3} style={{ color: 'var(--text-color)', marginBottom: 24 }}>
        Khóa học của tôi
      </Title>

      {enrollments.length === 0 ? (
        <Card style={{ textAlign: 'center', padding: '40px 0' }}>
          <EmptyState description="Bạn chưa đăng ký khóa học nào." />
          <Button
            type="primary"
            icon={<CompassOutlined />}
            onClick={() => navigate('/courses')}
            style={{ marginTop: '16px' }}
          >
            Khám phá khóa học ngay
          </Button>
        </Card>
      ) : (
        <Row gutter={[16, 16]}>
          {enrollments.map((item) => (
            <Col xs={24} sm={12} md={12} lg={8} xl={6} key={item.id}>
              <Card
                hoverable
                cover={
                  <img
                    alt={item.course.title}
                    src={item.course.thumbnailUrl || 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?q=80&w=600'}
                    style={{ height: 160, objectFit: 'cover' }}
                  />
                }
                onClick={() => navigate(`/courses/${item.course.slug}`)}
              >
                <div style={{ minHeight: 70 }}>
                  <Text strong ellipsis style={{ color: 'var(--text-color)', fontSize: 16, display: 'block' }}>
                    {item.course.title}
                  </Text>
                  <Paragraph ellipsis={{ rows: 2 }} style={{ color: 'var(--text-color)', opacity: 0.7, fontSize: 13, marginTop: 4 }}>
                    {item.course.shortDescription}
                  </Paragraph>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', margin: '12px 0' }}>
                  <Avatar src={item.course.instructor.avatarUrl} size="small" />
                  <Text style={{ color: 'var(--text-color)', opacity: 0.8, fontSize: 12, marginLeft: 8 }}>
                    {item.course.instructor.fullName}
                  </Text>
                </div>

                <div style={{ borderTop: '1px solid var(--border-color)', paddingTop: 12, marginTop: 12 }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
                    <Text style={{ color: 'var(--text-color)', opacity: 0.6, fontSize: 12 }}>Tiến độ</Text>
                    <Text style={{ color: 'var(--text-color)', fontSize: 12, fontWeight: 'bold' }}>
                      {item.progressPercent}%
                    </Text>
                  </div>
                  <Progress percent={item.progressPercent} showInfo={false} size="small" strokeColor="var(--primary-color)" />
                </div>
              </Card>
            </Col>
          ))}
        </Row>
      )}
    </div>
  );
};

export default StudentDashboard;
