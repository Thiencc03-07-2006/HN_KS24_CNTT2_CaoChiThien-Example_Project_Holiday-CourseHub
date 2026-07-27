import React, { useEffect, useState } from 'react';
import { Typography, Form, message } from 'antd';
import { useAuth } from '../context/AuthContext';
import axiosInstance from '../api/axiosInstance';
import { Card } from '../components/common/UI/Card';
import { Button } from '../components/common/UI/Button';
import { Input } from '../components/common/UI/Input';
import { Loading } from '../components/common/UI/Loading';

const { Title, Paragraph } = Typography;

const InstructorProfileSettingsPage: React.FC = () => {
  const { refreshProfile } = useAuth();
  const [profile, setProfile] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [form] = Form.useForm();

  const fetchProfile = async () => {
    try {
      const res = await axiosInstance.get('/users/me');
      if (res.data?.success && res.data?.data) {
        setProfile(res.data.data.instructorProfile);
        form.setFieldsValue({
          headline: res.data.data.instructorProfile?.headline || '',
          detailedBio: res.data.data.instructorProfile?.detailedBio || '',
          websiteUrl: res.data.data.instructorProfile?.websiteUrl || '',
          linkedinUrl: res.data.data.instructorProfile?.linkedinUrl || '',
        });
      }
    } catch (err) {
      message.error('Không thể tải hồ sơ giảng viên.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProfile();
  }, []);

  const handleSave = async (values: any) => {
    try {
      if (profile) {
        // Update profile
        await axiosInstance.put('/instructor/profile', values);
        message.success('Cập nhật hồ sơ giảng viên thành công.');
      } else {
        // Register
        await axiosInstance.post('/instructor/register', values);
        message.success('Đăng ký làm giảng viên thành công! Hãy tải lại trang để chuyển đổi vai trò.');
        refreshProfile();
      }
      fetchProfile();
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Có lỗi xảy ra khi lưu thông tin.');
    }
  };

  if (loading) {
    return <Loading message="Đang tải thông tin giảng viên..." />;
  }

  return (
    <div style={{ maxWidth: 700, margin: '0 auto' }}>
      <Title level={3} style={{ color: 'var(--text-color)', marginBottom: 24 }}>
        {profile ? 'Hồ sơ Giảng viên' : 'Đăng ký trở thành Giảng viên'}
      </Title>

      <Card>
        {!profile && (
          <Paragraph style={{ color: 'var(--text-color)', opacity: 0.8, marginBottom: '24px' }}>
            Bằng cách đăng ký trở thành giảng viên, bạn có thể tạo khóa học của riêng mình, thiết lập giáo trình chương học, tải lên tài liệu video, bài đọc PDF, và kiếm doanh thu từ học viên đăng ký học.
          </Paragraph>
        )}

        <Form form={form} layout="vertical" onFinish={handleSave} noValidate>
          <Form.Item
            label="Tiêu đề nghề nghiệp (Headline)"
            name="headline"
            rules={[
              { required: true, message: 'Tiêu đề không được để trống' },
              { min: 10, max: 80, message: 'Tiêu đề giảng viên từ 10 đến 80 ký tự' }
            ]}
          >
            <Input placeholder="Ví dụ: Kỹ sư phần mềm cao cấp tại Google / Giảng viên Khoa CNTT" />
          </Form.Item>

          <Form.Item
            label="Tiểu sử chi tiết (Biography)"
            name="detailedBio"
            rules={[
              { required: true, message: 'Tiểu sử chuyên môn không được để trống' },
              { min: 100, message: 'Tiểu sử chuyên môn tối thiểu 100 ký tự' }
            ]}
          >
            <Input.TextArea placeholder="Hãy mô tả chi tiết kinh nghiệm lập trình hoặc giảng dạy của bạn để thu hút học viên học tập..." rows={6} />
          </Form.Item>


          <Form.Item
            label="Website cá nhân / Portfolio"
            name="websiteUrl"
            rules={[
              { max: 255, message: 'Website URL tối đa 255 ký tự' }
            ]}
          >
            <Input placeholder="https://mywebsite.com" />
          </Form.Item>

          <Form.Item
            label="LinkedIn URL"
            name="linkedinUrl"
            rules={[
              { max: 255, message: 'LinkedIn URL tối đa 255 ký tự' }
            ]}
          >
            <Input placeholder="https://linkedin.com/in/username" />
          </Form.Item>

          <Form.Item style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: 0, marginTop: 16 }}>
            <Button type="primary" htmlType="submit">
              {profile ? 'Cập nhật hồ sơ' : 'Đăng ký ngay'}
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default InstructorProfileSettingsPage;
