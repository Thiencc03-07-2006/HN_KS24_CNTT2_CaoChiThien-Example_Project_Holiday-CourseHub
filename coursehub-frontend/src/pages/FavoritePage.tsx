import React, { useEffect, useState } from 'react';
import { Row, Col, Typography, message, Rate, Space } from 'antd';
import { DeleteOutlined, CompassOutlined, CalendarOutlined, UserOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { favoriteService } from '../services/favoriteService';
import { Card } from '../components/common/UI/Card';
import { Button } from '../components/common/UI/Button';
import { Avatar } from '../components/common/UI/Avatar';
import { Loading } from '../components/common/UI/Loading';
import { EmptyState } from '../components/common/UI/EmptyState';

const { Title, Text } = Typography;

const FavoritePage: React.FC = () => {
  const [courses, setCourses] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  const fetchFavorites = async () => {
    try {
      const res = await favoriteService.getMyFavorites();
      if (res?.success && res?.data) {
        setCourses(res.data);
      }
    } catch (err) {
      message.error('Không thể tải danh sách yêu thích.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchFavorites();
  }, []);

  const handleRemove = async (courseId: string, e: React.MouseEvent) => {
    e.stopPropagation();
    try {
      await favoriteService.removeFavorite(courseId);
      message.success('Đã xóa khóa học khỏi danh sách yêu thích.');
      fetchFavorites();
    } catch (err) {
      message.error('Không thể xóa khóa học.');
    }
  };

  if (loading) {
    return <Loading message="Đang tải danh sách yêu thích..." />;
  }

  return (
    <div>
      <Title level={3} style={{ color: 'var(--text-color)', marginBottom: 24 }}>
        Khóa học yêu thích
      </Title>

      {courses.length === 0 ? (
        <Card style={{ textAlign: 'center', padding: '40px 0' }}>
          <EmptyState description="Danh sách khóa học yêu thích của bạn đang trống." />
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
        <Row gutter={[20, 20]}>
          {courses.map((course) => (
            <Col xs={24} sm={12} md={12} lg={8} xl={6} key={course.id}>
              <Card
                hoverable
                cover={
                  <div style={{ position: 'relative' }}>
                    <img
                      alt={course.title}
                      src={course.thumbnailUrl || 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?q=80&w=600'}
                      style={{ height: 160, width: '100%', objectFit: 'cover' }}
                      onClick={() => navigate(`/courses/${course.slug}`)}
                    />
                    <div style={{ position: 'absolute', top: 10, right: 10 }}>
                      <Button
                        type="primary"
                        danger
                        shape="circle"
                        icon={<DeleteOutlined />}
                        onClick={(e) => handleRemove(course.id, e)}
                        style={{ boxShadow: '0 2px 8px rgba(0,0,0,0.15)' }}
                      />
                    </div>
                  </div>
                }
              >
                <div onClick={() => navigate(`/courses/${course.slug}`)} style={{ cursor: 'pointer' }}>
                  <Text strong ellipsis style={{ color: 'var(--text-color)', fontSize: 16, display: 'block', marginBottom: 6 }}>
                    {course.title}
                  </Text>
                  
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}>
                    <Avatar src={course.instructor?.avatarUrl} icon={<UserOutlined />} size="small" />
                    <Text type="secondary" style={{ fontSize: 13 }} ellipsis>
                      {course.instructor?.fullName}
                    </Text>
                  </div>

                  <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginBottom: 8 }}>
                    <Rate disabled allowHalf value={course.averageRating || 0} style={{ fontSize: 12, color: '#fadb14' }} />
                    <Text type="secondary" style={{ fontSize: 12 }}>
                      ({course.totalReviews || 0})
                    </Text>
                  </div>

                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
                    <Text style={{ fontSize: 13, opacity: 0.8 }}>
                      👥 {course.totalStudents || 0} học viên
                    </Text>
                    <Text strong style={{ color: 'var(--primary-color)', fontSize: 16 }}>
                      {course.price === 0
                        ? 'Miễn phí'
                        : new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(course.price)}
                    </Text>
                  </div>

                  <Space style={{ fontSize: 11, opacity: 0.6, marginBottom: 16, display: 'flex', alignItems: 'center' }}>
                    <CalendarOutlined />
                    <span>Lưu ngày: {new Date(course.createdAt || Date.now()).toLocaleDateString('vi-VN')}</span>
                  </Space>
                </div>
              </Card>
            </Col>
          ))}
        </Row>
      )}
    </div>
  );
};

export default FavoritePage;
