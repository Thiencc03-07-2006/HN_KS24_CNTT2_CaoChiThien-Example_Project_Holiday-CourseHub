import React, { useState } from 'react';
import { Form, Input, Button, Card, Typography, message } from 'antd';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../api/axiosInstance';

const { Title, Paragraph } = Typography;

const ResetPassword: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);

  const onFinish = async (values: any) => {
    setLoading(true);
    try {
      await axiosInstance.post('/auth/reset-password', {
        token: values.token,
        newPassword: values.newPassword,
        confirmPassword: values.confirmPassword,
      });
      message.success('Đặt lại mật khẩu thành công! Vui lòng đăng nhập lại.');
      navigate('/login');
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Token không hợp lệ hoặc đã hết hạn.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', background: '#0a0f1d' }}>
      <Card style={{ width: 400, borderRadius: 12, boxShadow: '0 8px 24px rgba(0,0,0,0.3)', border: 'none', background: '#141b2d' }}>
        <div style={{ textAlign: 'center', marginBottom: 24 }}>
          <Title level={2} style={{ color: '#fff', margin: 0 }}>Đặt lại mật khẩu</Title>
          <Paragraph style={{ color: '#8c9db5', marginTop: 8 }}>
            Nhập token nhận được trong email và mật khẩu mới của bạn.
          </Paragraph>
        </div>

        <Form layout="vertical" onFinish={onFinish} requiredMark={false} noValidate>
          <Form.Item
            label={<span style={{ color: '#a3b1cc' }}>Mã Token xác nhận</span>}
            name="token"
            rules={[{ required: true, message: 'Token không được để trống' }]}
          >
            <Input size="large" placeholder="Nhập token UUID nhận được..." style={{ background: '#1f2940', color: '#fff', border: '1px solid #2e3b52' }} />
          </Form.Item>

          <Form.Item
            label={<span style={{ color: '#a3b1cc' }}>Mật khẩu mới</span>}
            name="newPassword"
            rules={[
              { required: true, message: 'Mật khẩu mới không được để trống' },
              { min: 8, message: 'Mật khẩu tối thiểu 8 ký tự' },
              {
                pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/,
                message: 'Mật khẩu phải chứa ít nhất 1 chữ hoa, 1 chữ thường, 1 chữ số và 1 ký tự đặc biệt'
              }
            ]}
          >
            <Input.Password size="large" placeholder="Tối thiểu 8 ký tự" style={{ background: '#1f2940', color: '#fff', border: '1px solid #2e3b52' }} />
          </Form.Item>

          <Form.Item
            label={<span style={{ color: '#a3b1cc' }}>Xác nhận mật khẩu</span>}
            name="confirmPassword"
            dependencies={['newPassword']}
            rules={[
              { required: true, message: 'Xác nhận mật khẩu không được để trống' },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue('newPassword') === value) {
                    return Promise.resolve();
                  }
                  return Promise.reject(new Error('Mật khẩu xác nhận không khớp!'));
                },
              }),
            ]}
          >
            <Input.Password size="large" placeholder="Nhập lại mật khẩu mới" style={{ background: '#1f2940', color: '#fff', border: '1px solid #2e3b52' }} />
          </Form.Item>

          <Form.Item style={{ marginTop: 32 }}>
            <Button type="primary" htmlType="submit" size="large" block loading={loading} style={{ background: '#1677ff' }}>
              Cập nhật Mật khẩu
            </Button>
          </Form.Item>
        </Form>

      </Card>
    </div>
  );
};

export default ResetPassword;
