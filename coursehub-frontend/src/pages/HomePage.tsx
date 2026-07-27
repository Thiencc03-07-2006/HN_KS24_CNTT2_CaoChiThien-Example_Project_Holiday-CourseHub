import React from 'react';
import { Typography, Row, Col, Space, Tag } from 'antd';
import { useNavigate } from 'react-router-dom';
import {
  CompassOutlined, TrophyOutlined, UserOutlined, AppstoreOutlined,
  ArrowRightOutlined, StarFilled,
} from '@ant-design/icons';
import { Header } from '../components/common/Layout/Header';
import { Footer } from '../components/common/Layout/Footer';
import { Button } from '../components/common/UI/Button';

const { Title, Paragraph, Text } = Typography;

const FEATURES = [
  {
    icon: <TrophyOutlined style={{ fontSize: 32, color: '#2563EB' }} />,
    title: 'Chứng chỉ uy tín',
    desc: 'Hoàn thành 100% khóa học để nhận chứng chỉ PDF tự động, được công nhận trên toàn quốc.',
  },
  {
    icon: <UserOutlined style={{ fontSize: 32, color: '#2563EB' }} />,
    title: 'Giảng viên chuyên môn',
    desc: 'Học từ kỹ sư công nghệ, nhà thiết kế chuyên nghiệp và chuyên gia đầu ngành.',
  },
  {
    icon: <CompassOutlined style={{ fontSize: 32, color: '#2563EB' }} />,
    title: 'Học mọi lúc, mọi nơi',
    desc: 'Video chất lượng cao, tài liệu PDF và câu hỏi trắc nghiệm tự chấm điểm.',
  },
];

const CATEGORIES = [
  { name: 'Công nghệ thông tin & Lập trình', count: '120 khóa học', icon: <CompassOutlined style={{ fontSize: 28, color: '#2563EB' }} />, color: '#EFF6FF' },
  { name: 'Thiết kế đồ họa & UI/UX', count: '85 khóa học', icon: <AppstoreOutlined style={{ fontSize: 28, color: '#7C3AED' }} />, color: '#F5F3FF' },
  { name: 'Kinh doanh & Marketing', count: '94 khóa học', icon: <TrophyOutlined style={{ fontSize: 28, color: '#059669' }} />, color: '#F0FDF4' },
  { name: 'Ngoại ngữ & Đời sống', count: '63 khóa học', icon: <UserOutlined style={{ fontSize: 28, color: '#D97706' }} />, color: '#FFFBEB' },
];

const STATS = [
  { value: '500+', label: 'Khóa học' },
  { value: '50K+', label: 'Học viên' },
  { value: '200+', label: 'Giảng viên' },
  { value: '4.8', label: 'Đánh giá trung bình' },
];

const HomePage: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div
      style={{
        background: '#FFFFFF',
        minHeight: '100vh',
        display: 'flex',
        flexDirection: 'column',
        width: '100%',
        maxWidth: '100%',
        overflowX: 'hidden',
      }}
    >
      <Header />

      {/* ====== HERO SECTION ====== */}
      <section
        style={{
          background: 'linear-gradient(135deg, #EFF6FF 0%, #DBEAFE 60%, #E0E7FF 100%)',
          padding: 'clamp(48px, 8vw, 96px) 24px',
          textAlign: 'center',
          position: 'relative',
          overflow: 'hidden',
        }}
      >
        {/* Background decoration */}
        <div style={{
          position: 'absolute', top: -60, right: -60, width: 300, height: 300,
          borderRadius: '50%', background: 'rgba(37,99,235,0.06)', pointerEvents: 'none',
        }} />
        <div style={{
          position: 'absolute', bottom: -80, left: -40, width: 240, height: 240,
          borderRadius: '50%', background: 'rgba(124,58,237,0.04)', pointerEvents: 'none',
        }} />

        <div style={{ maxWidth: 800, margin: '0 auto', position: 'relative' }}>
          <Tag color="blue" style={{ marginBottom: 20, fontSize: 12, fontWeight: 600, padding: '5px 14px', borderRadius: 20 }}>
            🚀 Nền tảng học trực tuyến hàng đầu Việt Nam
          </Tag>
          <Title
            style={{
              color: '#111827',
              fontSize: 'clamp(28px, 5vw, 52px)',
              fontWeight: 800,
              lineHeight: 1.15,
              letterSpacing: '-0.02em',
              marginBottom: 20,
            }}
          >
            Nâng Cao Kỹ Năng Của Bạn Với{' '}
            <span style={{ color: '#2563EB' }}>CourseHub</span>
          </Title>
          <Paragraph
            style={{
              fontSize: 'clamp(15px, 2vw, 18px)',
              color: '#6B7280',
              marginBottom: 36,
              lineHeight: 1.7,
              maxWidth: 600,
              marginLeft: 'auto',
              marginRight: 'auto',
            }}
          >
            Học tập không giới hạn với hàng trăm khóa học trực tuyến chất lượng cao từ các giảng viên hàng đầu.
          </Paragraph>
          <Space size="middle" wrap style={{ justifyContent: 'center' }}>
            <Button
              type="primary"
              size="large"
              onClick={() => navigate('/courses')}
              icon={<CompassOutlined />}
              style={{
                height: 50,
                paddingLeft: 28,
                paddingRight: 28,
                fontSize: 16,
                fontWeight: 600,
                background: '#2563EB',
                borderColor: '#2563EB',
                borderRadius: 10,
              }}
            >
              Khám phá ngay
            </Button>
            <Button
              size="large"
              onClick={() => navigate('/register')}
              style={{
                height: 50,
                paddingLeft: 28,
                paddingRight: 28,
                fontSize: 16,
                borderRadius: 10,
                fontWeight: 600,
                borderColor: '#2563EB',
                color: '#2563EB',
              }}
            >
              Đăng ký miễn phí
            </Button>
          </Space>

          {/* Social proof */}
          <div style={{ marginTop: 36, display: 'flex', justifyContent: 'center', alignItems: 'center', gap: 6, flexWrap: 'wrap' }}>
            <div style={{ display: 'flex' }}>
              {[1, 2, 3, 4].map(i => (
                <div key={i} style={{
                  width: 28, height: 28, borderRadius: '50%',
                  background: `hsl(${210 + i * 30}, 70%, 60%)`,
                  border: '2px solid #FFFFFF',
                  marginLeft: i > 1 ? -8 : 0,
                }} />
              ))}
            </div>
            <Text style={{ color: '#6B7280', fontSize: 14 }}>
              <Text strong style={{ color: '#111827' }}>50,000+</Text> học viên đã tin tưởng
            </Text>
            <div style={{ display: 'flex', gap: 2 }}>
              {[1, 2, 3, 4, 5].map(i => <StarFilled key={i} style={{ color: '#F59E0B', fontSize: 13 }} />)}
            </div>
          </div>
        </div>
      </section>

      {/* ====== STATS BAR ====== */}
      <section style={{ background: '#2563EB', padding: '28px 24px' }}>
        <div style={{ maxWidth: 1200, margin: '0 auto' }}>
          <Row gutter={[24, 16]} justify="center">
            {STATS.map((stat, idx) => (
              <Col xs={12} sm={6} key={idx} style={{ textAlign: 'center' }}>
                <Text strong style={{ color: '#FFFFFF', fontSize: 'clamp(20px, 3vw, 32px)', display: 'block', lineHeight: 1.2 }}>
                  {stat.value}
                </Text>
                <Text style={{ color: 'rgba(255,255,255,0.8)', fontSize: 13 }}>{stat.label}</Text>
              </Col>
            ))}
          </Row>
        </div>
      </section>

      {/* ====== MAIN CONTENT ====== */}
      <main style={{ flex: 1, padding: 'clamp(40px, 6vw, 72px) 24px', boxSizing: 'border-box' }}>
        <div style={{ maxWidth: 1200, margin: '0 auto' }}>

          {/* WHY SECTION */}
          <div style={{ textAlign: 'center', marginBottom: 48 }}>
            <Tag color="blue" style={{ marginBottom: 12, fontWeight: 600, borderRadius: 20, padding: '4px 12px' }}>
              Tại sao chọn chúng tôi
            </Tag>
            <Title level={2} style={{ color: '#111827', fontWeight: 800, margin: '0 0 12px' }}>
              Tại sao chọn CourseHub?
            </Title>
            <Paragraph style={{ color: '#6B7280', maxWidth: 560, margin: '0 auto', fontSize: 15 }}>
              Chúng tôi cung cấp trải nghiệm học tập tốt nhất với công nghệ và nội dung chất lượng cao.
            </Paragraph>
          </div>

          <Row gutter={[24, 24]} style={{ marginBottom: 72 }}>
            {FEATURES.map((feat, idx) => (
              <Col xs={24} md={8} key={idx}>
                <div
                  style={{
                    background: '#FFFFFF',
                    borderRadius: 16,
                    border: '1px solid #E5E7EB',
                    boxShadow: '0 4px 12px rgba(0,0,0,0.06)',
                    padding: '32px 28px',
                    textAlign: 'center',
                    height: '100%',
                    transition: 'all 0.2s ease',
                    cursor: 'default',
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.transform = 'translateY(-4px)';
                    e.currentTarget.style.boxShadow = '0 12px 28px rgba(0,0,0,0.1)';
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.transform = 'translateY(0)';
                    e.currentTarget.style.boxShadow = '0 4px 12px rgba(0,0,0,0.06)';
                  }}
                >
                  <div style={{
                    width: 64, height: 64, borderRadius: 16,
                    background: '#EFF6FF',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    margin: '0 auto 20px',
                  }}>
                    {feat.icon}
                  </div>
                  <Title level={4} style={{ color: '#111827', marginBottom: 12 }}>{feat.title}</Title>
                  <Paragraph style={{ color: '#6B7280', lineHeight: 1.6, margin: 0, fontSize: 14 }}>
                    {feat.desc}
                  </Paragraph>
                </div>
              </Col>
            ))}
          </Row>

          {/* CATEGORIES */}
          <div style={{ textAlign: 'center', marginBottom: 40 }}>
            <Title level={2} style={{ color: '#111827', fontWeight: 800, margin: '0 0 12px' }}>
              Các chủ đề hàng đầu
            </Title>
            <Paragraph style={{ color: '#6B7280', margin: 0, fontSize: 15 }}>
              Khám phá hàng trăm khóa học trong nhiều lĩnh vực khác nhau
            </Paragraph>
          </div>

          <Row gutter={[20, 20]} style={{ marginBottom: 64 }}>
            {CATEGORIES.map((cat, idx) => (
              <Col xs={24} sm={12} md={6} key={idx}>
                <div
                  style={{
                    background: cat.color,
                    borderRadius: 16,
                    border: '1px solid #E5E7EB',
                    padding: '28px 20px',
                    textAlign: 'center',
                    cursor: 'pointer',
                    transition: 'all 0.2s ease',
                    height: '100%',
                  }}
                  onClick={() => navigate('/courses')}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.transform = 'translateY(-4px)';
                    e.currentTarget.style.boxShadow = '0 10px 24px rgba(0,0,0,0.1)';
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.transform = 'translateY(0)';
                    e.currentTarget.style.boxShadow = 'none';
                  }}
                >
                  <div style={{ marginBottom: 16 }}>{cat.icon}</div>
                  <Text strong style={{ color: '#111827', display: 'block', fontSize: 14, marginBottom: 6, lineHeight: 1.4 }}>
                    {cat.name}
                  </Text>
                  <Text type="secondary" style={{ fontSize: 13 }}>{cat.count}</Text>
                </div>
              </Col>
            ))}
          </Row>

          {/* CTA Banner */}
          <div style={{
            background: 'linear-gradient(135deg, #2563EB 0%, #1D4ED8 100%)',
            borderRadius: 20,
            padding: 'clamp(32px, 5vw, 56px)',
            textAlign: 'center',
            position: 'relative',
            overflow: 'hidden',
          }}>
            <div style={{
              position: 'absolute', top: -40, right: -40, width: 200, height: 200,
              borderRadius: '50%', background: 'rgba(255,255,255,0.06)', pointerEvents: 'none',
            }} />
            <Title level={2} style={{ color: '#FFFFFF', marginBottom: 12, fontWeight: 800 }}>
              Sẵn sàng bắt đầu hành trình học tập?
            </Title>
            <Paragraph style={{ color: 'rgba(255,255,255,0.85)', fontSize: 16, marginBottom: 28 }}>
              Đăng ký ngay hôm nay và tiếp cận hàng trăm khóa học chất lượng cao.
            </Paragraph>
            <Space size="middle" wrap style={{ justifyContent: 'center' }}>
              <Button
                size="large"
                onClick={() => navigate('/register')}
                style={{
                  background: '#FFFFFF', color: '#2563EB',
                  borderColor: '#FFFFFF', fontWeight: 700,
                  height: 48, paddingLeft: 28, paddingRight: 28,
                  borderRadius: 10,
                }}
              >
                Đăng ký miễn phí <ArrowRightOutlined />
              </Button>
              <Button
                size="large"
                type="text"
                onClick={() => navigate('/courses')}
                style={{ color: '#FFFFFF', fontWeight: 600, height: 48 }}
              >
                Khám phá khóa học
              </Button>
            </Space>
          </div>

        </div>
      </main>

      <Footer />
    </div>
  );
};

export default HomePage;
