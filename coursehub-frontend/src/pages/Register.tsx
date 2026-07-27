import React, { useState } from 'react';
import { Form, message, Typography } from 'antd';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Card } from '../components/common/UI/Card';
import { Button } from '../components/common/UI/Button';
import { Input } from '../components/common/UI/Input';

const { Title, Paragraph } = Typography;

const Register: React.FC = () => {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);

  const onFinish = async (values: any) => {
    setLoading(true);
    try {
      await register({
        fullName: values.fullName,
        email: values.email,
        password: values.password,
        confirmPassword: values.confirmPassword,
      });
      message.success('Đăng ký tài khoản thành công! Một mã OTP đã được gửi tới email của bạn.');
      navigate('/verify-otp', { state: { email: values.email } });
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Đăng ký tài khoản thất bại. Email có thể đã được sử dụng.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', background: '#F0F4F8', padding: '40px 0' }}>
      <Card style={{ width: 440, padding: '16px' }}>
        <div style={{ textAlign: 'center', marginBottom: 24 }}>
          <Title level={2} style={{ color: 'var(--text-color)', margin: 0 }}>Đăng ký tài khoản</Title>
          <Paragraph style={{ color: 'var(--text-color)', opacity: 0.8, marginTop: 8 }}>
            Tạo tài khoản mới để bắt đầu hành trình học tập cùng CourseHub.
          </Paragraph>
        </div>

        <Form layout="vertical" onFinish={onFinish} requiredMark={false} noValidate>
          <Form.Item
            label={<span style={{ color: 'var(--text-color)', fontWeight: 500 }}>Họ và tên</span>}
            name="fullName"
            rules={[
              { required: true, message: 'Họ và tên không được để trống' },
              { min: 2, max: 100, message: 'Họ và tên từ 2 đến 100 ký tự' },
              {
                pattern: /^[^0-9`~!@#$%^&*()_\-+=\[\]{}|;:',.<>?/\\"]*$/,
                message: 'Họ tên không được chứa ký tự đặc biệt hoặc chữ số'
              }
            ]}
          >
            <Input size="large" placeholder="Nguyễn Văn A" />
          </Form.Item>

          <Form.Item
            label={<span style={{ color: 'var(--text-color)', fontWeight: 500 }}>Địa chỉ email</span>}
            name="email"
            rules={[
              { required: true, message: 'Email không được để trống' },
              { type: 'email', message: 'Email không đúng định dạng' },
              { max: 150, message: 'Email tối đa 150 ký tự' }
            ]}
          >
            <Input size="large" placeholder="ten@email.com" />
          </Form.Item>

          <Form.Item
            label={<span style={{ color: 'var(--text-color)', fontWeight: 500 }}>Mật khẩu</span>}
            name="password"
            rules={[
              { required: true, message: 'Mật khẩu không được để trống' },
              { min: 8, message: 'Mật khẩu tối thiểu 8 ký tự' },
              {
                pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/,
                message: 'Mật khẩu phải chứa ít nhất 1 chữ hoa, 1 chữ thường, 1 chữ số và 1 ký tự đặc biệt'
              }
            ]}
          >
            <Input.Password size="large" placeholder="Mật khẩu bảo mật" />
          </Form.Item>

          <Form.Item
            label={<span style={{ color: 'var(--text-color)', fontWeight: 500 }}>Xác nhận mật khẩu</span>}
            name="confirmPassword"
            dependencies={['password']}
            rules={[
              { required: true, message: 'Trường này là bắt buộc' },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue('password') === value) {
                    return Promise.resolve();
                  }
                  return Promise.reject(new Error('Mật khẩu xác nhận không khớp!'));
                },
              }),
            ]}
          >
            <Input.Password size="large" placeholder="Nhập lại mật khẩu" />
          </Form.Item>


          <Form.Item style={{ marginTop: 32 }}>
            <Button type="primary" htmlType="submit" size="large" block loading={loading}>
              Đăng ký tài khoản
            </Button>
          </Form.Item>

          <div style={{ textAlign: 'center', marginTop: 16 }}>
            <span style={{ color: 'var(--text-color)', opacity: 0.8 }}>Đã có tài khoản? </span>
            <Link to="/login" style={{ color: 'var(--primary-color)', fontWeight: 500 }}>Đăng nhập ngay</Link>
          </div>
        </Form>
      </Card>
    </div>
  );
};

export default Register;
