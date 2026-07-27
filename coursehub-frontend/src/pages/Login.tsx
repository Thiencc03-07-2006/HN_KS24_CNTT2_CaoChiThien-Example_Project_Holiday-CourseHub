import React, { useState } from 'react';
import { Form, Checkbox, message, Typography } from 'antd';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Card } from '../components/common/UI/Card';
import { Button } from '../components/common/UI/Button';
import { Input } from '../components/common/UI/Input';

const { Title, Paragraph } = Typography;

const Login: React.FC = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);

  const onFinish = async (values: any) => {
    setLoading(true);
    try {
      await login({
        email: values.email,
        password: values.password,
      });
      message.success('Đăng nhập hệ thống thành công!');
      
      // Dynamic routing redirect based on role
      if (values.email.toLowerCase() === 'admin@coursehub.com') {
        navigate('/admin/dashboard');
      } else {
        navigate('/dashboard');
      }
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Email hoặc mật khẩu không chính xác.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', background: '#F0F4F8' }}>
      <Card style={{ width: 400, padding: '16px' }}>
        <div style={{ textAlign: 'center', marginBottom: 24 }}>
          <Title level={2} style={{ color: 'var(--text-color)', margin: 0 }}>Đăng nhập</Title>
          <Paragraph style={{ color: 'var(--text-color)', opacity: 0.8, marginTop: 8 }}>
            Chào mừng bạn quay lại với CourseHub.
          </Paragraph>
        </div>

        <Form layout="vertical" onFinish={onFinish} requiredMark={false} noValidate>
          <Form.Item
            label={<span style={{ color: 'var(--text-color)', fontWeight: 500 }}>Địa chỉ email</span>}
            name="email"
            rules={[
              { required: true, message: 'Email không được để trống' },
              { type: 'email', message: 'Email không đúng định dạng' }
            ]}
          >
            <Input size="large" placeholder="ten@email.com" />
          </Form.Item>

          <Form.Item
            label={<span style={{ color: 'var(--text-color)', fontWeight: 500 }}>Mật khẩu</span>}
            name="password"
            rules={[{ required: true, message: 'Mật khẩu không được để trống' }]}
          >
            <Input.Password size="large" placeholder="••••••••" />
          </Form.Item>


          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 24 }}>
            <Checkbox style={{ color: 'var(--text-color)' }}>Ghi nhớ</Checkbox>
            <Link to="/forgot-password" style={{ color: 'var(--primary-color)' }}>Quên mật khẩu?</Link>
          </div>

          <Form.Item>
            <Button type="primary" htmlType="submit" size="large" block loading={loading}>
              Đăng nhập
            </Button>
          </Form.Item>

          <div style={{ textAlign: 'center', marginTop: 16 }}>
            <span style={{ color: 'var(--text-color)', opacity: 0.8 }}>Chưa có tài khoản? </span>
            <Link to="/register" style={{ color: 'var(--primary-color)', fontWeight: 500 }}>Đăng ký ngay</Link>
          </div>

          <div style={{ textAlign: 'center', marginTop: 12 }}>
            <span style={{ color: 'var(--text-color)', opacity: 0.8 }}>Tài khoản chưa kích hoạt? </span>
            <Link to="/verify-otp" style={{ color: 'var(--primary-color)', fontWeight: 500 }}>Xác thực OTP</Link>
          </div>
        </Form>
      </Card>
    </div>
  );
};

export default Login;
