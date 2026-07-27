import React from 'react';
import { Layout, Menu } from 'antd';
import { useNavigate, useLocation } from 'react-router-dom';
import {
  BookOutlined,
  UserOutlined,
  CompassOutlined,
  SettingOutlined,
  FolderOpenOutlined,
  WarningOutlined,
  BarChartOutlined,
  HeartOutlined,
  FlagOutlined,
  DashboardOutlined,
  StarOutlined,
} from '@ant-design/icons';

const { Sider } = Layout;

interface SidebarProps {
  collapsed?: boolean;
  onCollapse?: (collapsed: boolean) => void;
}

export const Sidebar: React.FC<SidebarProps> = ({ collapsed = false, onCollapse }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const path = location.pathname;

  const getMenuItems = () => {
    if (path.startsWith('/admin')) {
      return [
        { key: '/admin/dashboard', icon: <DashboardOutlined />, label: 'Tổng quan', onClick: () => navigate('/admin/dashboard') },
        { key: '/admin/users', icon: <UserOutlined />, label: 'Người dùng', onClick: () => navigate('/admin/users') },
        { key: '/admin/categories', icon: <FolderOpenOutlined />, label: 'Danh mục', onClick: () => navigate('/admin/categories') },
        { key: '/admin/courses', icon: <BookOutlined />, label: 'Quản lý khóa học', onClick: () => navigate('/admin/courses') },
        { key: '/admin/reviews', icon: <StarOutlined />, label: 'Quản lý đánh giá', onClick: () => navigate('/admin/reviews') },
        { key: '/admin/reports', icon: <WarningOutlined />, label: 'Báo cáo vi phạm', onClick: () => navigate('/admin/reports') },
        { key: '/admin/statistics', icon: <BarChartOutlined />, label: 'Thống kê hệ thống', onClick: () => navigate('/admin/statistics') },
      ];
    }

    if (path.startsWith('/instructor')) {
      return [
        { key: '/instructor/dashboard', icon: <DashboardOutlined />, label: 'Tổng quan giảng viên', onClick: () => navigate('/instructor/dashboard') },
        { key: '/instructor/courses', icon: <BookOutlined />, label: 'Khóa học của tôi', onClick: () => navigate('/instructor/courses') },
        { key: '/instructor/reviews', icon: <StarOutlined />, label: 'Đánh giá của học viên', onClick: () => navigate('/instructor/reviews') },
        { key: '/instructor/profile', icon: <SettingOutlined />, label: 'Hồ sơ giảng viên', onClick: () => navigate('/instructor/profile') },
      ];
    }


    return [
      { key: '/dashboard', icon: <BookOutlined />, label: 'Khóa học của tôi', onClick: () => navigate('/dashboard') },
      { key: '/profile', icon: <UserOutlined />, label: 'Hồ sơ cá nhân', onClick: () => navigate('/profile') },
      { key: '/wishlist', icon: <FlagOutlined />, label: 'Danh sách mong muốn', onClick: () => navigate('/wishlist') },
      { key: '/favorites', icon: <HeartOutlined />, label: 'Khóa học yêu thích', onClick: () => navigate('/favorites') },
      { key: '/courses', icon: <CompassOutlined />, label: 'Khám phá khóa học', onClick: () => navigate('/courses') },
    ];
  };

  const selectedKey = getMenuItems().find(item => path.startsWith(item.key))?.key || path;

  return (
    <Sider
      collapsible
      collapsed={collapsed}
      onCollapse={onCollapse}
      width={240}
      theme="light"
      style={{
        borderRight: '1px solid var(--border-color)',
        background: '#FFFFFF',
        minHeight: 'calc(100vh - 64px)',
        flexShrink: 0,
      }}
    >
      <Menu
        mode="inline"
        selectedKeys={[selectedKey]}
        style={{
          height: '100%',
          borderRight: 0,
          paddingTop: '12px',
          fontSize: '14px',
          fontWeight: 500,
        }}
        items={getMenuItems()}
      />
    </Sider>
  );
};
