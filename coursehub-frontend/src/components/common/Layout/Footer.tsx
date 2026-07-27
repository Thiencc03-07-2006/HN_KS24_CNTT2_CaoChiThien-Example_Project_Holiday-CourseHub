import React from 'react';
import { Typography, Space, Divider } from 'antd';
import { Link } from 'react-router-dom';

const { Text } = Typography;

export const Footer: React.FC = () => {
  return (
    <footer
      style={{
        width: '100%',
        maxWidth: '100%',
        boxSizing: 'border-box',
        background: '#F8FAFC',
        borderTop: '1px solid var(--border-color)',
        padding: '40px 24px 24px',
        marginTop: 'auto',
      }}
    >
      <div style={{ maxWidth: '1400px', margin: '0 auto' }}>
        {/* Top row */}
        <div
          style={{
            display: 'flex',
            flexWrap: 'wrap',
            gap: '32px',
            justifyContent: 'space-between',
            marginBottom: '32px',
          }}
        >
          {/* Brand */}
          <div style={{ maxWidth: 280 }}>
            <Text
              strong
              style={{
                color: 'var(--primary)',
                fontSize: '20px',
                fontWeight: 800,
                display: 'block',
                marginBottom: 8,
                letterSpacing: '-0.5px',
              }}
            >
              CourseHub
            </Text>
            <Text style={{ color: 'var(--text-muted)', fontSize: '13px', lineHeight: '1.6' }}>
              Nền tảng học trực tuyến chất lượng cao với hàng trăm khóa học từ các chuyên gia hàng đầu.
            </Text>
          </div>

          {/* Links */}
          <div style={{ display: 'flex', gap: '48px', flexWrap: 'wrap' }}>
            <div>
              <Text strong style={{ color: 'var(--text-body)', display: 'block', marginBottom: 12, fontSize: 13 }}>
                Khám phá
              </Text>
              <Space orientation="vertical" size={8}>
                <Link to="/courses" style={{ color: 'var(--text-muted)', fontSize: 13 }}>Tất cả khóa học</Link>
                <Link to="/courses" style={{ color: 'var(--text-muted)', fontSize: 13 }}>Lập trình</Link>
                <Link to="/courses" style={{ color: 'var(--text-muted)', fontSize: 13 }}>Thiết kế</Link>
              </Space>
            </div>
            <div>
              <Text strong style={{ color: 'var(--text-body)', display: 'block', marginBottom: 12, fontSize: 13 }}>
                Tài khoản
              </Text>
              <Space orientation="vertical" size={8}>
                <Link to="/login" style={{ color: 'var(--text-muted)', fontSize: 13 }}>Đăng nhập</Link>
                <Link to="/register" style={{ color: 'var(--text-muted)', fontSize: 13 }}>Đăng ký</Link>
                <Link to="/dashboard" style={{ color: 'var(--text-muted)', fontSize: 13 }}>Bảng điều khiển</Link>
              </Space>
            </div>
          </div>
        </div>

        <Divider style={{ margin: '0 0 16px', borderColor: 'var(--border-color)' }} />

        {/* Bottom row */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 8 }}>
          <Text style={{ color: 'var(--text-muted)', fontSize: '12px' }}>
            © 2026 CourseHub. Tất cả quyền được bảo lưu.
          </Text>
          <Text style={{ color: 'var(--text-muted)', fontSize: '12px' }}>
            Thiết kế với ❤️ cho người học Việt Nam
          </Text>
        </div>
      </div>
    </footer>
  );
};
