import React, { useState } from 'react';
import { Layout } from 'antd';
import { Outlet } from 'react-router-dom';
import { Header } from '../components/common/Layout/Header';
import { Sidebar } from '../components/common/Layout/Sidebar';
import { Footer } from '../components/common/Layout/Footer';

const { Content } = Layout;

const AdminLayout: React.FC = () => {
  const [collapsed, setCollapsed] = useState(false);

  return (
    <Layout style={{ minHeight: '100vh', background: '#F0F4F8' }}>
      <Header />
      <Layout>
        <Sidebar collapsed={collapsed} onCollapse={setCollapsed} />
        <Layout style={{ display: 'flex', flexDirection: 'column' }}>
          <Content
            style={{
              margin: '24px',
              padding: '24px',
              background: '#FFFFFF',
              borderRadius: '8px',
              minHeight: '280px',
              border: '1px solid var(--border-color)',
              color: 'var(--text-color)',
            }}
          >
            <Outlet />
          </Content>
          <Footer />
        </Layout>
      </Layout>
    </Layout>
  );
};

export default AdminLayout;
