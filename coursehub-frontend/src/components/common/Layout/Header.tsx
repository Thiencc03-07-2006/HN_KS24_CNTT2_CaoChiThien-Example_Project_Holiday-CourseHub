import React, { useState } from 'react';
import { Menu, Button, Space, Dropdown, Typography, Drawer } from 'antd';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { Avatar } from '../UI/Avatar';
import {
  CompassOutlined,
  HeartOutlined,
  BookOutlined,
  LogoutOutlined,
  UserOutlined,
  MenuOutlined,
  FlagOutlined,
} from '@ant-design/icons';

const { Text } = Typography;

export const Header: React.FC = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileOpen, setMobileOpen] = useState(false);

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  const isInstructor = user?.roles.includes('ROLE_INSTRUCTOR');
  const isAdmin = user?.roles.includes('ROLE_ADMIN');

  const userDropdownItems = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: 'Hồ sơ cá nhân',
      onClick: () => navigate('/profile'),
    },
    {
      key: 'wishlist',
      icon: <FlagOutlined />,
      label: 'Danh sách mong muốn',
      onClick: () => navigate('/wishlist'),
    },
    {
      key: 'favorites',
      icon: <HeartOutlined />,
      label: 'Khóa học yêu thích',
      onClick: () => navigate('/favorites'),
    },
    {
      type: 'divider' as const,
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: 'Đăng xuất',
      danger: true,
      onClick: handleLogout,
    },
  ];

  const navItems = [
    {
      key: '/courses',
      icon: <CompassOutlined />,
      label: <Link to="/courses">Khám phá</Link>,
    },
    ...(user
      ? [
        {
          key: '/dashboard',
          icon: <BookOutlined />,
          label: <Link to="/dashboard">Học tập</Link>,
        },
      ]
      : []),
  ];

  return (
    <header
      style={{
        width: '100%',
        maxWidth: '100%',
        boxSizing: 'border-box',
        background: '#FFFFFF',
        borderBottom: '1px solid var(--border-color)',
        boxShadow: '0 1px 4px rgba(0,0,0,0.06)',
        position: 'sticky',
        top: 0,
        left: 0,
        right: 0,
        zIndex: 1000,
        height: '64px',
        display: 'flex',
        alignItems: 'center',
        padding: '0 24px',
      }}
    >
      {/* Inner wrapper — constrained width */}
      <div
        style={{
          width: '100%',
          maxWidth: '1400px',
          margin: '0 auto',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          gap: '16px',
        }}
      >
        {/* Logo + Desktop Nav */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '40px', flex: 1, minWidth: 0 }}>
          <Link to="/" style={{ display: 'flex', alignItems: 'center', textDecoration: 'none', flexShrink: 0 }}>
            <Text
              strong
              style={{
                color: 'var(--primary)',
                fontSize: '22px',
                letterSpacing: '-0.5px',
                fontFamily: 'Inter, sans-serif',
                fontWeight: 800,
              }}
            >
              CourseHub
            </Text>
          </Link>

          {/* Desktop menu */}
          <div className="hidden-mobile">
            <Menu
              mode="horizontal"
              selectedKeys={[location.pathname]}
              style={{
                border: 'none',
                background: 'transparent',
                fontWeight: 500,
              }}
              items={navItems}
            />
          </div>
        </div>

        {/* Right Side Actions */}
        <Space size="small" style={{ flexShrink: 0 }}>
          {user ? (
            <>
              {isInstructor && (
                <Button
                  type="text"
                  onClick={() => {
                    if (location.pathname.startsWith('/instructor')) {
                      navigate('/dashboard');
                    } else {
                      navigate('/instructor/dashboard');
                    }
                  }}
                  style={{ color: 'var(--primary)', fontWeight: 600 }}
                  className="hidden-mobile"
                >
                  {location.pathname.startsWith('/instructor') ? 'Học viên' : 'Giảng viên'}
                </Button>
              )}

              {isAdmin && (
                <Button
                  size="small"
                  onClick={() => navigate('/admin/dashboard')}
                  style={{ color: '#dc2626', borderColor: '#dc2626', borderRadius: 8, fontWeight: 600 }}
                  className="hidden-mobile"
                >
                  Admin
                </Button>
              )}

              <Dropdown menu={{ items: userDropdownItems }} placement="bottomRight" trigger={['click']}>
                <Space style={{ cursor: 'pointer', padding: '4px 8px', borderRadius: 8, transition: 'background 0.2s' }}
                  onMouseEnter={(e) => (e.currentTarget.style.background = '#F3F4F6')}
                  onMouseLeave={(e) => (e.currentTarget.style.background = 'transparent')}
                >
                  <Avatar src={user.avatarUrl} size="medium" />
                  <Text
                    style={{ color: 'var(--text-body)', fontWeight: 500, maxWidth: 120 }}
                    ellipsis
                    className="hidden-mobile"
                  >
                    {user.fullName}
                  </Text>
                </Space>
              </Dropdown>
            </>
          ) : (
            <>
              <Button
                type="text"
                onClick={() => navigate('/login')}
                style={{ fontWeight: 500, color: 'var(--text-body)' }}
              >
                Đăng nhập
              </Button>
              <Button
                type="primary"
                onClick={() => navigate('/register')}
                style={{ borderRadius: 8, fontWeight: 600, background: 'var(--primary)', borderColor: 'var(--primary)' }}
              >
                Đăng ký
              </Button>
            </>
          )}

          {/* Mobile hamburger */}
          <Button
            type="text"
            icon={<MenuOutlined />}
            onClick={() => setMobileOpen(true)}
            style={{ display: 'none' }}
            className="show-mobile"
            id="mobile-menu-btn"
          />
        </Space>
      </div>

      {/* Mobile Drawer */}
      <Drawer
        title={<Text strong style={{ color: 'var(--primary)', fontSize: 18 }}>CourseHub</Text>}
        placement="right"
        onClose={() => setMobileOpen(false)}
        open={mobileOpen}
        size="default"
        styles={{ body: { padding: 0 } }}
      >
        <Menu
          mode="inline"
          selectedKeys={[location.pathname]}
          items={navItems}
          onClick={() => setMobileOpen(false)}
        />
        <div style={{ padding: '16px' }}>
          {user ? (
            <Space orientation="vertical" style={{ width: '100%' }}>
              {isInstructor && (
                <Button
                  block
                  type="default"
                  onClick={() => { navigate('/instructor/dashboard'); setMobileOpen(false); }}
                >
                  Giảng viên
                </Button>
              )}
              <Button block type="primary" danger onClick={() => { handleLogout(); setMobileOpen(false); }}>
                Đăng xuất
              </Button>
            </Space>
          ) : (
            <Space orientation="vertical" style={{ width: '100%' }}>
              <Button block onClick={() => { navigate('/login'); setMobileOpen(false); }}>
                Đăng nhập
              </Button>
              <Button block type="primary" onClick={() => { navigate('/register'); setMobileOpen(false); }}>
                Đăng ký
              </Button>
            </Space>
          )}
        </div>
      </Drawer>
    </header>
  );
};
