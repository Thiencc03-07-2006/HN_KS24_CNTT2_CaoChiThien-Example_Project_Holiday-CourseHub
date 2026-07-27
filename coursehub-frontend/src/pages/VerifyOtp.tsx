import React, { useState } from 'react';
import { Form, Input, Button, Card, Typography, message } from 'antd';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const { Title, Paragraph } = Typography;

const VerifyOtp: React.FC = () => {
  const { verifyOtp } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [loading, setLoading] = useState(false);

  // Retrieve email from registration redirect state or default empty
  const defaultEmail = location.state?.email || '';

  const onFinish = async (values: { email: string; otpCode: string }) => {
    setLoading(true);
    try {
      await verifyOtp(values.email, values.otpCode);
      message.success('Xác thực tài khoản thành công! Vui lòng đăng nhập.');
      navigate('/login');
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Xác thực OTP thất bại. Vui lòng kiểm tra lại mã.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', background: '#0a0f1d' }}>
      <Card style={{ width: 400, borderRadius: 12, boxShadow: '0 8px 24px rgba(0,0,0,0.3)', border: 'none', background: '#141b2d' }}>
        <div style={{ textAlign: 'center', marginBottom: 24 }}>
          <Title level={2} style={{ color: '#fff', margin: 0 }}>Xác thực tài khoản</Title>
          <Paragraph style={{ color: '#8c9db5', marginTop: 8 }}>
            Nhập mã OTP 6 chữ số được gửi về email của bạn.
          </Paragraph>
        </div>

        <Form layout="vertical" onFinish={onFinish} initialValues={{ email: defaultEmail }} requiredMark={false} noValidate>
          <Form.Item
            label={<span style={{ color: '#a3b1cc' }}>Email đăng ký</span>}
            name="email"
            rules={[
              { required: true, message: 'Email không được để trống' },
              { type: 'email', message: 'Email không đúng định dạng' }
            ]}
          >
            <Input size="large" placeholder="ten@email.com" style={{ background: '#1f2940', color: '#fff', border: '1px solid #2e3b52' }} />
          </Form.Item>

          <Form.Item
            label={<span style={{ color: '#a3b1cc' }}>Mã OTP (6 chữ số)</span>}
            name="otpCode"
            rules={[
              { required: true, message: 'Mã OTP không được để trống' },
              { min: 6, max: 6, message: 'Mã OTP phải có đúng 6 chữ số' }
            ]}
          >
            <Input size="large" maxLength={6} placeholder="123456" style={{ letterSpacing: '8px', textAlign: 'center', background: '#1f2940', color: '#fff', border: '1px solid #2e3b52' }} />
          </Form.Item>

          <Form.Item style={{ marginTop: 32 }}>
            <Button type="primary" htmlType="submit" size="large" block loading={loading} style={{ background: '#1677ff' }}>
              Xác thực và Kích hoạt
            </Button>
          </Form.Item>
        </Form>

      </Card>
    </div>
  );
};

export default VerifyOtp;
