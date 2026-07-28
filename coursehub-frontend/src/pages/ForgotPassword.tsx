import React, { useState } from 'react';
import { Form, Input, Button, Card, Typography, message } from 'antd';
import { Link } from 'react-router-dom';
import axiosInstance from '../api/axiosInstance';

const { Title, Paragraph } = Typography;

const ForgotPassword: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [sent, setSent] = useState(false);

  const onFinish = async (values: { email: string }) => {
    setLoading(true);
    try {
      await axiosInstance.post('/auth/forgot-password', values);
      setSent(true);
      message.success('Đã gửi link khôi phục mật khẩu. Vui lòng kiểm tra email.');
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Gửi yêu cầu khôi phục thất bại.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', background: 'var(--background-color)' }}>
      <Card style={{ width: 400, borderRadius: 12, boxShadow: '0 8px 24px rgba(0,0,0,0.3)', border: '1px solid #e5e7eb', background: '#fff' }}>
        <div style={{ textAlign: 'center', marginBottom: 24 }}>
          <Title level={2} style={{ color: '#1e293b', margin: 0 }}>Quên mật khẩu</Title>
          <Paragraph style={{ color: '#64748b', marginTop: 8 }}>
            {!sent
              ? 'Nhập email liên kết với tài khoản của bạn để khôi phục mật khẩu.'
              : 'Chúng tôi đã gửi hướng dẫn đặt lại mật khẩu về email của bạn.'}
          </Paragraph>
        </div>

        {!sent ? (
          <Form layout="vertical" onFinish={onFinish} requiredMark={false} noValidate>
            <Form.Item
              label={<span style={{ color: '#334155' }}>Địa chỉ email</span>}
              name="email"
              rules={[
                { required: true, message: 'Email không được để trống' },
                { type: 'email', message: 'Email không đúng định dạng' }
              ]}
            >
              <Input size="large" placeholder="ten@email.com" />
            </Form.Item>

            <Form.Item style={{ marginTop: 32 }}>
              <Button type="primary" htmlType="submit" size="large" block loading={loading} style={{ background: '#1677ff' }}>
                Gửi mã khôi phục
              </Button>
            </Form.Item>


            <div style={{ textAlign: 'center', marginTop: 16 }}>
              <Link to="/login" style={{ color: '#1677ff' }}>Quay lại Đăng nhập</Link>
            </div>
          </Form>
        ) : (
          <div style={{ textAlign: 'center', marginTop: 24 }}>
            <Link to="/reset-password">
              <Button type="primary" size="large" block>
                Nhập token đặt lại mật khẩu
              </Button>
            </Link>
          </div>
        )}
      </Card>
    </div>
  );
};

export default ForgotPassword;
