import React, { useState } from 'react';
import { Layout } from 'antd';
import { Outlet } from 'react-router-dom';
import { Header } from '../components/common/Layout/Header';
import { Sidebar } from '../components/common/Layout/Sidebar';
import { Footer } from '../components/common/Layout/Footer';

const { Content } = Layout;

const StudentLayout: React.FC = () => {
  const [collapsed, setCollapsed] = useState(false);

  return (
    <Layout
      style={{
        minHeight: '100vh',
        background: 'var(--bg-primary)',
        width: '100%',
        maxWidth: '100%',
        overflowX: 'hidden',
      }}
    >
      <Header />
      <Layout style={{ flex: 1 }}>
        <Sidebar collapsed={collapsed} onCollapse={setCollapsed} />
        <Layout
          style={{
            display: 'flex',
            flexDirection: 'column',
            minWidth: 0,
            flex: 1,
          }}
        >
          <Content
            style={{
              margin: '24px',
              padding: '28px',
              background: '#FFFFFF',
              borderRadius: '16px',
              border: '1px solid var(--border-color)',
              boxShadow: '0 2px 8px rgba(0,0,0,0.04)',
              color: 'var(--text-color)',
              minHeight: '400px',
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

export default StudentLayout;
