import React from 'react';
import { ConfigProvider, theme } from 'antd';
import { AuthProvider } from './context/AuthContext';
import AppRouter from './AppRouter';

const App: React.FC = () => {
  return (
    <ConfigProvider
      theme={{
        algorithm: theme.defaultAlgorithm,
        token: {
          colorPrimary: '#1677ff',
          borderRadius: 8,

          // Light theme
          colorBgContainer: '#FFFFFF',
          colorBgLayout: '#F8FAFC',

          colorBorder: '#E5E7EB',

          colorTextBase: '#111827',
        },
      }}
    >
      <AuthProvider>
        <AppRouter />
      </AuthProvider>
    </ConfigProvider>
  );
};

export default App;
