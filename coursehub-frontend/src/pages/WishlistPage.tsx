import React, { useEffect, useState } from 'react';
import { Row, Col, Typography, message, Rate, Space, Empty } from 'antd';
import { DeleteOutlined, CompassOutlined, CalendarOutlined, UserOutlined, EyeOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { wishlistService } from '../services/wishlistService';
import { Card } from '../components/common/UI/Card';
import { Button } from '../components/common/UI/Button';
import { Avatar } from '../components/common/UI/Avatar';
import { Loading } from '../components/common/UI/Loading';

const { Title, Text } = Typography;

const WishlistPage: React.FC = () => {
  const [courses, setCourses] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  const fetchWishlist = async () => {
    try {
      const res = await wishlistService.getMyWishlist();
      if (res?.success && res?.data) {
        setCourses(res.data);
      }
    } catch (err) {
      message.error('Không thể tải danh sách mong muốn.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchWishlist();
  }, []);

  const handleRemove = async (courseId: string, e: React.MouseEvent) => {
    e.stopPropagation();
    try {
      await wishlistService.removeFromWishlist(courseId);
      message.success('Đã xóa khóa học khỏi danh sách mong muốn.');
      fetchWishlist();
    } catch (err) {
      message.error('Không thể xóa khóa học.');
    }
  };

  if (loading) {
    return <Loading message="Đang tải danh sách mong muốn..." />;
  }

  return (
    <div style={{ padding: '4px 0' }}>
      <Title level={3} style={{ color: 'var(--text-color)', marginBottom: 24 }}>
        Danh sách khóa học mong muốn
      </Title>

      {courses.length === 0 ? (
        <Card style={{ padding: '60px 24px', textAlign: 'center' }}>
          <Empty 
            image={Empty.PRESENTED_IMAGE_SIMPLE}
            description={
              <span style={{ color: 'var(--text-muted)', fontSize: 15 }}>
                Danh sách khóa học mong muốn của bạn đang trống.
              </span>
            } 
          />
          <Button
            type="primary"
            icon={<CompassOutlined />}
            onClick={() => navigate('/courses')}
            style={{ marginTop: '24px', background: 'var(--primary)', borderColor: 'var(--primary)' }}
          >
            Khám phá các khóa học ngay
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

                <div style={{ display: 'flex', gap: 8, borderTop: '1px solid #F1F5F9', paddingTop: 12 }}>
                  <Button 
                    type="default" 
                    icon={<EyeOutlined />} 
                    style={{ flex: 1, padding: 0 }} 
                    onClick={() => navigate(`/courses/${course.slug}`)}
                  >
                    Xem
                  </Button>
                  <Button 
                    type="primary" 
                    style={{ flex: 1, padding: 0, background: 'var(--primary)', borderColor: 'var(--primary)' }} 
                    onClick={() => navigate(`/courses/${course.slug}`)}
                  >
                    Đăng ký
                  </Button>
                </div>
              </Card>
            </Col>
          ))}
        </Row>
      )}
    </div>
  );
};

export default WishlistPage;
