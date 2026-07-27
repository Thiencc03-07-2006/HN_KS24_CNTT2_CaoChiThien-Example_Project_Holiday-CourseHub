import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Row, Col, Typography, Space, Card as AntdCard, message } from 'antd';
import { GlobalOutlined, LinkedinOutlined } from '@ant-design/icons';
import axiosInstance from '../api/axiosInstance';
import { Header } from '../components/common/Layout/Header';
import { Footer } from '../components/common/Layout/Footer';
import { Card } from '../components/common/UI/Card';
import { Avatar } from '../components/common/UI/Avatar';
import { Loading } from '../components/common/UI/Loading';

const { Title, Text, Paragraph } = Typography;

const InstructorProfilePage: React.FC = () => {
  const { instructorId } = useParams<{ instructorId: string }>();
  const navigate = useNavigate();

  const [profile, setProfile] = useState<any>(null);
  const [courses, setCourses] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchInstructorPublicData = async () => {
      try {
        const res = await axiosInstance.get(`/instructors/public/${instructorId}`);
        if (res.data?.success && res.data?.data) {
          setProfile(res.data.data);
        }
        
        // Fetch courses of this instructor
        const courseRes = await axiosInstance.get(`/courses/search`, {
          params: { page: 0, size: 8 }
        });
        if (courseRes.data?.success && courseRes.data?.data) {
          // Filter by instructor just in case search doesn't support instructorId directly
          const allCourses = courseRes.data.data.content || [];
          const filtered = allCourses.filter((c: any) => c.instructor.id === instructorId);
          setCourses(filtered);
        }
      } catch (err) {
        message.error('Không thể tải thông tin hồ sơ giảng viên.');
      } finally {
        setLoading(false);
      }
    };
    fetchInstructorPublicData();
  }, [instructorId]);

  if (loading) {
    return <Loading message="Đang tải hồ sơ giảng viên..." />;
  }

  const instProfile = profile?.instructorProfile;

  return (
    <div style={{ background: '#F0F4F8', minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <Header />

      <div style={{ flex: 1, padding: '40px 24px' }}>
        <div style={{ maxWidth: 1000, margin: '0 auto' }}>
          <Row gutter={[24, 24]}>
            {/* Left Column: Avatar & Basic Meta */}
            <Col xs={24} md={8}>
              <Card style={{ textAlign: 'center', padding: '16px' }}>
                <Avatar src={profile?.avatarUrl} size={120} style={{ marginBottom: '16px' }} />
                <Title level={3} style={{ color: 'var(--text-color)', margin: '4px 0' }}>
                  {profile?.fullName}
                </Title>
                <Text type="secondary" style={{ display: 'block', marginBottom: '16px' }}>
                  {instProfile?.headline || 'Giảng viên chuyên môn'}
                </Text>

                <Space style={{ marginTop: 16 }}>
                  {instProfile?.websiteUrl && (
                    <a href={instProfile.websiteUrl} target="_blank" rel="noopener noreferrer">
                      <GlobalOutlined style={{ fontSize: '20px', color: 'var(--primary-color)' }} />
                    </a>
                  )}
                  {instProfile?.linkedinUrl && (
                    <a href={instProfile.linkedinUrl} target="_blank" rel="noopener noreferrer">
                      <LinkedinOutlined style={{ fontSize: '20px', color: 'var(--primary-color)' }} />
                    </a>
                  )}
                </Space>
              </Card>
            </Col>

            {/* Right Column: Stats & Detailed Biography */}
            <Col xs={24} md={16}>
              <Space orientation="vertical" size="large" style={{ width: '100%' }}>
                {/* Stats row */}
                <Row gutter={16}>
                  <Col span={8}>
                    <Card style={{ textAlign: 'center', padding: '8px' }}>
                      <Text style={{ display: 'block', fontSize: 13, opacity: 0.8 }}>Học viên</Text>
                      <Text strong style={{ fontSize: 20 }}>{instProfile?.totalStudents || 0}</Text>
                    </Card>
                  </Col>
                  <Col span={8}>
                    <Card style={{ textAlign: 'center', padding: '8px' }}>
                      <Text style={{ display: 'block', fontSize: 13, opacity: 0.8 }}>Khóa học</Text>
                      <Text strong style={{ fontSize: 20 }}>{instProfile?.totalCourses || 0}</Text>
                    </Card>
                  </Col>
                  <Col span={8}>
                    <Card style={{ textAlign: 'center', padding: '8px' }}>
                      <Text style={{ display: 'block', fontSize: 13, opacity: 0.8 }}>Đánh giá</Text>
                      <Text strong style={{ fontSize: 20 }}>{instProfile?.averageRating?.toFixed(1) || '0.0'}</Text>
                    </Card>
                  </Col>
                </Row>

                {/* About me */}
                <Card title="Về tôi">
                  <Paragraph style={{ whiteSpace: 'pre-line', color: 'var(--text-color)', lineHeight: 1.6 }}>
                    {instProfile?.detailedBio || 'Giảng viên chưa cập nhật tiểu sử chi tiết.'}
                  </Paragraph>
                </Card>

                {/* Courses List */}
                <Card title="Khóa học đang giảng dạy">
                  {courses.length === 0 ? (
                    <Text type="secondary">Chưa có khóa học nào được xuất bản công khai.</Text>
                  ) : (
                    <Row gutter={[16, 16]}>
                      {courses.map((c) => (
                        <Col xs={24} sm={12} key={c.id}>
                          <AntdCard
                            hoverable
                            size="small"
                            cover={<img alt={c.title} src={c.thumbnailUrl} style={{ height: 120, objectFit: 'cover' }} />}
                            onClick={() => navigate(`/courses/${c.slug}`)}
                          >
                            <Text strong ellipsis style={{ display: 'block', color: 'var(--text-color)' }}>
                              {c.title}
                            </Text>
                            <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '8px' }}>
                              <Text type="secondary">{c.level}</Text>
                              <Text strong style={{ color: 'var(--primary-color)' }}>
                                {c.price === 0 ? 'Miễn phí' : new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(c.price)}
                              </Text>
                            </div>
                          </AntdCard>
                        </Col>
                      ))}
                    </Row>
                  )}
                </Card>
              </Space>
            </Col>
          </Row>
        </div>
      </div>

      <Footer />
    </div>
  );
};

export default InstructorProfilePage;
