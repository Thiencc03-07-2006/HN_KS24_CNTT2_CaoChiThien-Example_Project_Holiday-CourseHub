import React from 'react';
import { Result, Button } from 'antd';
import { useNavigate } from 'react-router-dom';

export const PageNotFound: React.FC = () => {
  const navigate = useNavigate();
  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', background: '#fff' }}>
      <Result
        status="404"
        title="404"
        subTitle="Xin lỗi, trang bạn đang tìm kiếm không tồn tại."
        extra={
          <Button type="primary" size="large" onClick={() => navigate('/')}>
            Quay lại Trang chủ
          </Button>
        }
        style={{ color: '#000000' }}
      />
    </div>
  );
};

export const AccessDenied: React.FC = () => {
  const navigate = useNavigate();
  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', background: '#0a0f1d' }}>
      <Result
        status="403"
        title="403"
        subTitle="Truy cập bị từ chối. Bạn không có quyền truy cập trang này."
        extra={
          <Button type="primary" size="large" onClick={() => navigate('/')}>
            Quay lại Trang chủ
          </Button>
        }
      />
    </div>
  );
};

export const ServerError: React.FC = () => {
  const navigate = useNavigate();
  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', background: '#0a0f1d' }}>
      <Result
        status="500"
        title="500"
        subTitle="Đã có lỗi hệ thống xảy ra. Vui lòng thử lại sau."
        extra={
          <Button type="primary" size="large" onClick={() => navigate('/')}>
            Quay lại Trang chủ
          </Button>
        }
      />
    </div>
  );
};
