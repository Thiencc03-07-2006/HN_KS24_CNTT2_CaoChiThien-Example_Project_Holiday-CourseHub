import React, { useEffect, useState } from 'react';
import { Row, Col, Typography, Tag, Space, message, Modal as AntdModal, Form, TreeSelect, Upload } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, SendOutlined, UploadOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { courseService } from '../services/courseService';
import axiosInstance from '../api/axiosInstance';
import { Card } from '../components/common/UI/Card';
import { Button } from '../components/common/UI/Button';
import { Input } from '../components/common/UI/Input';
import { Select } from '../components/common/UI/Select';
import { Loading } from '../components/common/UI/Loading';
import { EmptyState } from '../components/common/UI/EmptyState';

const { Title, Text, Paragraph } = Typography;
const { Option } = Select;

const InstructorCourseManagementPage: React.FC = () => {
  const [courses, setCourses] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [categories, setCategories] = useState<any[]>([]);
  const [form] = Form.useForm();
  const navigate = useNavigate();

  const thumbnailUrl = Form.useWatch('thumbnailUrl', form);

  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [editingCourse, setEditingCourse] = useState<any>(null);
  const [editForm] = Form.useForm();
  const [uploadingThumbnail, setUploadingThumbnail] = useState(false);

  const editThumbnailUrl = Form.useWatch('thumbnailUrl', editForm);

  const fetchCourses = async () => {
    try {
      const res = await courseService.getInstructorCourses();
      if (res?.success && res?.data) {
        setCourses(res.data.content || []);
      }
    } catch (err) {
      message.error('Không thể tải danh sách khóa học.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCourses();
    const fetchCategories = async () => {
      try {
        const res = await axiosInstance.get('/categories');
        if (res.data?.success && res.data?.data) {
          setCategories(res.data.data);
        }
      } catch (err) {
        console.warn('Failed to load categories');
      }
    };
    fetchCategories();
  }, []);

  const handleCreateCourse = async (values: any) => {
    try {
      const res = await courseService.createCourse({
        title: values.title,
        shortDescription: values.shortDescription,
        description: values.description,
        price: values.price,
        level: values.level,
        language: values.language || 'Vietnamese',
        categoryId: values.categoryId,
        thumbnailUrl: values.thumbnailUrl || null,
      });
      if (res?.success) {
        message.success('Tạo khóa học thành công!');
        setIsModalOpen(false);
        form.resetFields();
        fetchCourses();
      }
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Không thể tạo khóa học mới.');
    }
  };

  const handleDelete = async (courseId: string) => {
    AntdModal.confirm({
      title: 'Xác nhận xóa',
      content: 'Bạn có chắc chắn muốn xóa khóa học này? Hành động này không thể hoàn tác.',
      okText: 'Xóa',
      okType: 'danger',
      cancelText: 'Hủy',
      onOk: async () => {
        try {
          await courseService.deleteCourse(courseId);
          message.success('Đã xóa khóa học thành công.');
          fetchCourses();
        } catch (err: any) {
          message.error(err.response?.data?.message || 'Không thể xóa khóa học.');
        }
      },
    });
  };

  const handleSubmitReview = async (courseId: string) => {
    try {
      await courseService.submitForReview(courseId);
      message.success('Đã gửi khóa học cho Admin xét duyệt.');
      fetchCourses();
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Không thể gửi duyệt. Vui lòng kiểm tra lại cấu trúc khóa học.');
    }
  };

  const handleResubmitReview = async (courseId: string) => {
    try {
      await courseService.resubmitForReview(courseId);
      message.success('Đã gửi lại khóa học cho Admin xét duyệt.');
      fetchCourses();
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Không thể gửi duyệt lại. Vui lòng kiểm tra lại cấu trúc khóa học.');
    }
  };

  const handleOpenEditModal = (course: any) => {
    setEditingCourse(course);
    editForm.setFieldsValue({
      title: course.title,
      shortDescription: course.shortDescription,
      description: course.description,
      thumbnailUrl: course.thumbnailUrl,
      price: course.price,
      categoryId: course.category?.id,
      level: course.level,
      language: course.language || 'Vietnamese',
    });
    setIsEditModalOpen(true);
  };

  const handleUpdateCourse = async (values: any) => {
    try {
      const res = await courseService.updateCourse(editingCourse.id, {
        title: values.title,
        shortDescription: values.shortDescription,
        description: values.description,
        price: values.price,
        level: values.level,
        language: values.language || 'Vietnamese',
        categoryId: values.categoryId,
        thumbnailUrl: values.thumbnailUrl || null,
      });
      if (res?.success) {
        message.success('Cập nhật khóa học thành công!');
        setIsEditModalOpen(false);
        setEditingCourse(null);
        editForm.resetFields();
        fetchCourses();
      }
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Không thể cập nhật khóa học.');
    }
  };

  const handleThumbnailUpload = async (info: any) => {
    const file = info.file;
    if (!file) return;
    setUploadingThumbnail(true);
    try {
      const res = await courseService.uploadThumbnail(editingCourse.id, file);
      if (res.success && res.data) {
        editForm.setFieldsValue({ thumbnailUrl: res.data });
        message.success('Upload ảnh bìa thành công!');
        setEditingCourse((prev: any) => prev ? { ...prev, thumbnailUrl: res.data } : null);
      }
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Upload ảnh bìa thất bại (chỉ nhận JPG/PNG tối đa 2MB).');
    } finally {
      setUploadingThumbnail(false);
    }
  };

  const buildTreeData = (items: any[]): any[] => {
    return items.map(item => ({
      title: item.name,
      value: item.id,
      key: item.id,
      children: item.children && item.children.length > 0 ? buildTreeData(item.children) : undefined
    }));
  };

  if (loading) {
    return <Loading message="Đang tải danh sách khóa học..." />;
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <Title level={3} style={{ color: 'var(--text-color)', margin: 0 }}>
          Quản lý khóa học của tôi
        </Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setIsModalOpen(true)}>
          Tạo khóa học mới
        </Button>
      </div>

      {courses.length === 0 ? (
        <Card style={{ padding: '40px 0', textAlign: 'center' }}>
          <EmptyState description="Bạn chưa tạo khóa học nào." />
        </Card>
      ) : (
        <Row gutter={[16, 16]}>
          {courses.map((course) => (
            <Col xs={24} sm={12} md={12} lg={8} xl={6} key={course.id}>
              <Card
                cover={
                  <img
                    alt={course.title}
                    src={course.thumbnailUrl || 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?q=80&w=600'}
                    style={{ height: 160, objectFit: 'cover' }}
                  />
                }
              >
                <div style={{ minHeight: 70 }}>
                  <Text strong ellipsis style={{ color: 'var(--text-color)', fontSize: 16, display: 'block' }}>
                    {course.title}
                  </Text>
                  <Paragraph ellipsis={{ rows: 2 }} style={{ color: 'var(--text-color)', opacity: 0.7, fontSize: 13, marginTop: 4 }}>
                    {course.shortDescription}
                  </Paragraph>
                </div>

                {course.status === 'REJECTED' && course.rejectReason && (
                  <div style={{ margin: '8px 0', padding: '8px 12px', backgroundColor: '#fff2f0', border: '1px solid #ffa39e', borderRadius: '4px', textAlign: 'left' }}>
                    <Text type="danger" strong style={{ fontSize: 13, display: 'block' }}>Khóa học bị từ chối</Text>
                    <Text type="secondary" style={{ fontSize: 12, display: 'block', whiteSpace: 'pre-wrap' }}>
                      Lý do: {course.rejectReason}
                    </Text>
                  </div>
                )}

                {(course.status === 'BLOCKED' || course.status === 'BLOCKED_EDITED') && course.blockedReason && (
                  <div style={{ margin: '8px 0', padding: '8px 12px', backgroundColor: '#fff2f0', border: '1px solid #ffa39e', borderRadius: '4px', textAlign: 'left' }}>
                    <Text type="danger" strong style={{ fontSize: 13, display: 'block' }}>Khóa học bị chặn</Text>
                    <Text type="secondary" style={{ fontSize: 12, display: 'block', whiteSpace: 'pre-wrap' }}>
                      Lý do: {course.blockedReason}
                    </Text>
                  </div>
                )}

                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', margin: '12px 0' }}>
                  <Tag color={course.status === 'PUBLISHED' ? 'green' : course.status === 'PENDING_REVIEW' ? 'orange' : course.status === 'REJECTED' ? 'error' : (course.status === 'BLOCKED' || course.status === 'BLOCKED_EDITED') ? 'red' : 'blue'}>
                    {course.status}
                  </Tag>
                  <Text strong style={{ color: 'var(--primary-color)' }}>
                    {course.price === 0 ? 'Miễn phí' : new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(course.price)}
                  </Text>
                </div>

                <div style={{ borderTop: '1px solid var(--border-color)', paddingTop: 12, display: 'flex', justifyContent: 'space-between', gap: '8px', flexWrap: 'wrap' }}>
                  <Space>
                    <Button
                      type="primary"
                      size="small"
                      icon={<EditOutlined />}
                      onClick={() => navigate(`/instructor/courses/${course.id}/chapters`)}
                    >
                      Chương trình
                    </Button>
                    {(course.status === 'DRAFT' || course.status === 'REJECTED' || course.status === 'BLOCKED' || course.status === 'BLOCKED_EDITED') && (
                      <Button
                        type="default"
                        size="small"
                        icon={<EditOutlined />}
                        onClick={() => handleOpenEditModal(course)}
                      >
                        Sửa thông tin
                      </Button>
                    )}
                  </Space>
                  <Space>
                    {course.status === 'DRAFT' && (
                      <Button
                        type="default"
                        size="small"
                        icon={<SendOutlined />}
                        onClick={() => handleSubmitReview(course.id)}
                      >
                        Gửi duyệt
                      </Button>
                    )}
                    {(course.status === 'REJECTED' || course.status === 'BLOCKED') && (
                      <Button
                        type="default"
                        size="small"
                        icon={<SendOutlined />}
                        onClick={() => handleResubmitReview(course.id)}
                      >
                        Gửi duyệt lại
                      </Button>
                    )}
                    <Button
                      type="text"
                      danger
                      size="small"
                      icon={<DeleteOutlined />}
                      onClick={() => handleDelete(course.id)}
                    />
                  </Space>
                </div>
              </Card>
            </Col>
          ))}
        </Row>
      )}

      {/* Create Course Modal */}
      <AntdModal
        title="Tạo khóa học mới"
        open={isModalOpen}
        onCancel={() => setIsModalOpen(false)}
        footer={null}
        width={600}
      >
        <Form form={form} layout="vertical" onFinish={handleCreateCourse} style={{ marginTop: '16px' }} noValidate>
          <Form.Item
            label="Tiêu đề khóa học"
            name="title"
            rules={[
              { required: true, message: 'Tiêu đề khóa học không được để trống' },
              { min: 10, max: 200, message: 'Tiêu đề từ 10 đến 200 ký tự' }
            ]}
          >
            <Input placeholder="Ví dụ: Lập trình Java từ cơ bản đến nâng cao" />
          </Form.Item>

          <Form.Item
            label="Mô tả ngắn"
            name="shortDescription"
            rules={[
              { required: true, message: 'Mô tả ngắn không được để trống' },
              { min: 20, max: 500, message: 'Mô tả ngắn từ 20 đến 500 ký tự' }
            ]}
          >
            <Input placeholder="Mô tả tóm tắt nội dung khóa học" />
          </Form.Item>

          <Form.Item label="Mô tả chi tiết" name="description" rules={[{ required: true, message: 'Mô tả chi tiết không được để trống' }]}>
            <Input.TextArea placeholder="Mô tả chi tiết giáo trình và kết quả đạt được" rows={4} />
          </Form.Item>

          <Form.Item 
            label="Thumbnail URL" 
            name="thumbnailUrl" 
            rules={[{ type: 'url', message: 'Vui lòng nhập một URL hình ảnh hợp lệ!' }]}
          >
            <Input placeholder="Ví dụ: https://images.unsplash.com/... (Để trống nếu không có)" />
          </Form.Item>

          {thumbnailUrl && (
            <div style={{ marginBottom: 16, textAlign: 'center', background: 'var(--card-bg)', padding: 12, borderRadius: 8, border: '1px solid var(--border-color)' }}>
              <Text type="secondary" style={{ display: 'block', marginBottom: 8, color: 'var(--text-color)' }}>Xem trước hình thu nhỏ:</Text>
              <img 
                src={thumbnailUrl} 
                alt="Thumbnail Preview" 
                style={{ maxWidth: '100%', maxHeight: 150, borderRadius: 8, objectFit: 'cover' }}
                onError={(e) => {
                  (e.target as HTMLImageElement).style.display = 'none';
                }}
              />
            </div>
          )}

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                label="Giá bán (VND)"
                name="price"
                rules={[
                  { required: true, message: 'Giá khóa học không được để trống' },
                  {
                    validator: (_, value) => {
                      if (value !== undefined && value !== null && value !== '') {
                        if (Number(value) < 0) {
                          return Promise.reject(new Error('Giá khóa học phải >= 0'));
                        }
                      }
                      return Promise.resolve();
                    }
                  }
                ]}
              >
                <Input type="number" min={0} placeholder="Nhập 0 nếu miễn phí" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item label="Danh mục" name="categoryId" rules={[{ required: true, message: 'Danh mục không được để trống' }]}>
                <TreeSelect
                  placeholder="Chọn danh mục"
                  allowClear
                  treeData={buildTreeData(categories)}
                  treeDefaultExpandAll
                  size="large"
                  style={{ width: '100%' }}
                />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item label="Cấp độ" name="level" rules={[{ required: true, message: 'Cấp độ không được để trống' }]}>
                <Select placeholder="Cấp độ khóa học">
                  <Option value="BEGINNER">Cơ bản (Beginner)</Option>
                  <Option value="INTERMEDIATE">Trung cấp (Intermediate)</Option>
                  <Option value="ADVANCED">Nâng cao (Advanced)</Option>
                  <Option value="ALL_LEVELS">Mọi cấp độ (All levels)</Option>
                </Select>
              </Form.Item>
            </Col>

            <Col span={12}>
              <Form.Item
                label="Ngôn ngữ giảng dạy"
                name="language"
                initialValue="Vietnamese"
                rules={[{ max: 50, message: 'Ngôn ngữ tối đa 50 ký tự' }]}
              >
                <Select placeholder="Ngôn ngữ">
                  <Option value="Vietnamese">Tiếng Việt</Option>
                  <Option value="English">Tiếng Anh</Option>
                </Select>
              </Form.Item>
            </Col>
          </Row>

          <Form.Item style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: 0, marginTop: 16 }}>
            <Space>
              <Button onClick={() => setIsModalOpen(false)}>Hủy</Button>
              <Button type="primary" htmlType="submit">Tạo khóa học</Button>
            </Space>
          </Form.Item>
        </Form>
      </AntdModal>

      {/* Edit Course Modal */}
      <AntdModal
        title="Chỉnh sửa thông tin khóa học"
        open={isEditModalOpen}
        onCancel={() => {
          setIsEditModalOpen(false);
          setEditingCourse(null);
          editForm.resetFields();
        }}
        footer={null}
        width={600}
      >
        <Form form={editForm} layout="vertical" onFinish={handleUpdateCourse} style={{ marginTop: '16px' }} noValidate>
          <Form.Item
            label="Tiêu đề khóa học"
            name="title"
            rules={[
              { required: true, message: 'Tiêu đề khóa học không được để trống' },
              { min: 10, max: 200, message: 'Tiêu đề từ 10 đến 200 ký tự' }
            ]}
          >
            <Input placeholder="Ví dụ: Lập trình Java từ cơ bản đến nâng cao" />
          </Form.Item>

          <Form.Item
            label="Mô tả ngắn"
            name="shortDescription"
            rules={[
              { required: true, message: 'Mô tả ngắn không được để trống' },
              { min: 20, max: 500, message: 'Mô tả ngắn từ 20 đến 500 ký tự' }
            ]}
          >
            <Input placeholder="Mô tả tóm tắt nội dung khóa học" />
          </Form.Item>

          <Form.Item label="Mô tả chi tiết" name="description" rules={[{ required: true, message: 'Mô tả chi tiết không được để trống' }]}>
            <Input.TextArea placeholder="Mô tả chi tiết giáo trình và kết quả đạt được" rows={4} />
          </Form.Item>

          <Row gutter={16}>
            <Col span={16}>
              <Form.Item 
                label="Thumbnail URL" 
                name="thumbnailUrl" 
                rules={[{ type: 'url', message: 'Vui lòng nhập một URL hình ảnh hợp lệ!' }]}
              >
                <Input placeholder="Ví dụ: https://images.unsplash.com/... (Hoặc upload file bên phải)" />
              </Form.Item>
            </Col>
            <Col span={8} style={{ display: 'flex', alignItems: 'flex-end', paddingBottom: 24 }}>
              <Upload
                showUploadList={false}
                beforeUpload={() => false}
                onChange={handleThumbnailUpload}
                accept="image/*"
              >
                <Button icon={<UploadOutlined />} loading={uploadingThumbnail} style={{ width: '100%' }}>
                  Tải ảnh lên
                </Button>
              </Upload>
            </Col>
          </Row>

          {editThumbnailUrl && (
            <div style={{ marginBottom: 16, textAlign: 'center', background: 'var(--card-bg)', padding: 12, borderRadius: 8, border: '1px solid var(--border-color)' }}>
              <Text type="secondary" style={{ display: 'block', marginBottom: 8, color: 'var(--text-color)' }}>Xem trước hình thu nhỏ:</Text>
              <img 
                src={editThumbnailUrl} 
                alt="Thumbnail Preview" 
                style={{ maxWidth: '100%', maxHeight: 150, borderRadius: 8, objectFit: 'cover' }}
                onError={(e) => {
                  (e.target as HTMLImageElement).style.display = 'none';
                }}
              />
            </div>
          )}

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                label="Giá bán (VND)"
                name="price"
                rules={[
                  { required: true, message: 'Giá khóa học không được để trống' },
                  {
                    validator: (_, value) => {
                      if (value !== undefined && value !== null && value !== '') {
                        if (Number(value) < 0) {
                          return Promise.reject(new Error('Giá khóa học phải >= 0'));
                        }
                      }
                      return Promise.resolve();
                    }
                  }
                ]}
              >
                <Input type="number" min={0} placeholder="Nhập 0 nếu miễn phí" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item label="Danh mục" name="categoryId" rules={[{ required: true, message: 'Danh mục không được để trống' }]}>
                <TreeSelect
                  placeholder="Chọn danh mục"
                  allowClear
                  treeData={buildTreeData(categories)}
                  treeDefaultExpandAll
                  size="large"
                  style={{ width: '100%' }}
                />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item label="Cấp độ" name="level" rules={[{ required: true, message: 'Cấp độ không được để trống' }]}>
                <Select placeholder="Cấp độ khóa học">
                  <Option value="BEGINNER">Cơ bản (Beginner)</Option>
                  <Option value="INTERMEDIATE">Trung cấp (Intermediate)</Option>
                  <Option value="ADVANCED">Nâng cao (Advanced)</Option>
                  <Option value="ALL_LEVELS">Mọi cấp độ (All levels)</Option>
                </Select>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                label="Ngôn ngữ giảng dạy"
                name="language"
                rules={[{ max: 50, message: 'Ngôn ngữ tối đa 50 ký tự' }]}
              >

                <Select placeholder="Ngôn ngữ">
                  <Option value="Vietnamese">Tiếng Việt</Option>
                  <Option value="English">Tiếng Anh</Option>
                </Select>
              </Form.Item>
            </Col>
          </Row>

          <Form.Item style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: 0, marginTop: 16 }}>
            <Space>
              <Button onClick={() => {
                setIsEditModalOpen(false);
                setEditingCourse(null);
                editForm.resetFields();
              }}>Hủy</Button>
              <Button type="primary" htmlType="submit">Lưu thay đổi</Button>
            </Space>
          </Form.Item>
        </Form>
      </AntdModal>
    </div>
  );
};

export default InstructorCourseManagementPage;
