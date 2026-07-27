import React, { useState, useEffect } from 'react';
import { Form, Input, Button, Card, Avatar, Upload, Typography, message, Space } from 'antd';
import { UserOutlined, UploadOutlined } from '@ant-design/icons';
import { useAuth } from '../context/AuthContext';
import axiosInstance from '../api/axiosInstance';

const { Title, Text } = Typography;

const UserProfile: React.FC = () => {
  const { user, updateProfile, refreshProfile } = useAuth();
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [avatarUrl, setAvatarUrl] = useState(user?.avatarUrl || '');

  useEffect(() => {
    if (user) {
      form.setFieldsValue({
        fullName: user.fullName,
        phoneNumber: '', // placeholder, fetch detail if needed
        bio: '',
      });
      setAvatarUrl(user.avatarUrl || '');
      
      // Fetch full profile info on load to get phone number / bio
      const fetchFullProfile = async () => {
        try {
          const res = await axiosInstance.get('/users/me');
          if (res.data?.success && res.data?.data) {
            const data = res.data.data;
            form.setFieldsValue({
              fullName: data.fullName,
              phoneNumber: data.phoneNumber,
              bio: data.bio,
            });
          }
        } catch (err) {
          console.warn('Could not load full profile details');
        }
      };
      fetchFullProfile();
    }
  }, [user, form]);

  const onFinish = async (values: any) => {
    setLoading(true);
    try {
      await updateProfile(values.fullName, values.phoneNumber, values.bio);
      message.success('Cập nhật hồ sơ cá nhân thành công!');
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Cập nhật hồ sơ thất bại.');
    } finally {
      setLoading(false);
    }
  };

  const handleAvatarUpload = async (info: any) => {
    const file = info.file;
    const formData = new FormData();
    formData.append('file', file);

    setUploading(true);
    try {
      const res = await axiosInstance.post('/users/me/avatar', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      if (res.data?.success && res.data?.data) {
        setAvatarUrl(res.data.data);
        message.success('Cập nhật ảnh đại diện thành công!');
        await refreshProfile();
      }
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Upload ảnh đại diện thất bại (tối đa 2MB, định dạng JPG/PNG).');
    } finally {
      setUploading(false);
    }
  };

  return (
    <div style={{ maxWidth: 800, margin: '0 auto' }}>
      <Title level={3} style={{ color: 'var(--text-color)', marginBottom: 24 }}>Hồ sơ cá nhân</Title>

      <Card style={{ background: 'var(--bg-white)', border: '1px solid var(--border-color)', borderRadius: 12, boxShadow: '0 4px 12px rgba(0,0,0,0.06)', marginBottom: 24 }}>
        <Space size="large" align="center" style={{ width: '100%', justifyContent: 'center', flexDirection: 'column', padding: '16px 0' }}>
          <Avatar size={100} src={avatarUrl} icon={<UserOutlined />} style={{ border: '3px solid var(--primary)' }} />
          <Upload
            accept="image/*"
            showUploadList={false}
            beforeUpload={() => false} // Prevent auto upload
            onChange={handleAvatarUpload}
          >
            <Button icon={<UploadOutlined />} loading={uploading}>
              Tải lên ảnh mới
            </Button>
          </Upload>
          <Text style={{ color: 'var(--text-muted)', fontSize: 12 }}>Định dạng hỗ trợ: JPG, PNG. Dung lượng tối đa 2MB.</Text>
        </Space>
      </Card>

      <Card style={{ background: 'var(--bg-white)', border: '1px solid var(--border-color)', borderRadius: 12, boxShadow: '0 4px 12px rgba(0,0,0,0.06)' }}>
        <Form
          form={form}
          layout="vertical"
          onFinish={onFinish}
          requiredMark={false}
          noValidate
        >
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
            <Input size="large" />
          </Form.Item>


          <Form.Item
            label={<span style={{ color: 'var(--text-color)', fontWeight: 500 }}>Số điện thoại</span>}
            name="phoneNumber"
            rules={[
              {
                pattern: /^(0[3|5|7|8|9])+([0-9]{8})$/,
                message: 'Số điện thoại không hợp lệ (phải là số VN 10 chữ số)'
              }
            ]}
          >
            <Input size="large" />
          </Form.Item>

          <Form.Item
            label={<span style={{ color: 'var(--text-color)', fontWeight: 500 }}>Tiêu sử bản thân</span>}
            name="bio"
            rules={[
              { max: 500, message: 'Tiểu sử tối đa 500 ký tự' }
            ]}
          >
            <Input.TextArea rows={4} placeholder="Giới thiệu bản thân..." />
          </Form.Item>

          <Form.Item style={{ marginTop: 24 }}>
            <Button type="primary" htmlType="submit" size="large" loading={loading}>
              Lưu thay đổi
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default UserProfile;
