import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Typography, Form, Space, message, Modal as AntdModal, List } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, ArrowLeftOutlined, PlayCircleOutlined } from '@ant-design/icons';
import axiosInstance from '../api/axiosInstance';
import { Card } from '../components/common/UI/Card';
import { Button } from '../components/common/UI/Button';
import { Input } from '../components/common/UI/Input';
import { Loading } from '../components/common/UI/Loading';

const { Title, Text } = Typography;

const InstructorChapterManagementPage: React.FC = () => {
  const { courseId } = useParams<{ courseId: string }>();
  const navigate = useNavigate();
  const [course, setCourse] = useState<any>(null);
  const [chapters, setChapters] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [form] = Form.useForm();
  const [editingChapter, setEditingChapter] = useState<any>(null);

  const fetchCourseAndChapters = async () => {
    try {
      const courseRes = await axiosInstance.get(`/instructor/courses/${courseId}`);
      if (courseRes.data?.success) {
        setCourse(courseRes.data.data);
      }
      const chapterRes = await axiosInstance.get(`/instructor/courses/${courseId}/chapters`);
      if (chapterRes.data?.success) {
        setChapters(chapterRes.data.data || []);
      }
    } catch (err) {
      message.error('Không thể tải giáo trình.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCourseAndChapters();
  }, [courseId]);

  const handleSaveChapter = async (values: any) => {
    try {
      if (editingChapter) {
        await axiosInstance.put(`/instructor/chapters/${editingChapter.id}`, {
          title: values.title,
          orderIndex: values.orderIndex || 1,
        });
        message.success('Cập nhật chương học thành công!');
      } else {
        await axiosInstance.post(`/instructor/courses/${courseId}/chapters`, {
          title: values.title,
          orderIndex: values.orderIndex || (chapters.length + 1),
        });
        message.success('Tạo chương học thành công!');
      }
      setIsModalOpen(false);
      form.resetFields();
      setEditingChapter(null);
      fetchCourseAndChapters();
    } catch (err) {
      message.error('Không thể lưu chương học.');
    }
  };

  const handleDeleteChapter = async (chapterId: string) => {
    AntdModal.confirm({
      title: 'Xác nhận xóa',
      content: 'Tất cả bài học bên trong chương này cũng sẽ bị xóa. Bạn có chắc chắn muốn xóa?',
      okText: 'Xóa',
      okType: 'danger',
      cancelText: 'Hủy',
      onOk: async () => {
        try {
          await axiosInstance.delete(`/instructor/chapters/${chapterId}`);
          message.success('Đã xóa chương học.');
          fetchCourseAndChapters();
        } catch (err) {
          message.error('Không thể xóa chương học.');
        }
      },
    });
  };

  if (loading) {
    return <Loading message="Đang tải chương học..." />;
  }

  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/instructor/courses')}>
          Quay lại danh sách
        </Button>
      </Space>

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <div>
          <Title level={3} style={{ color: 'var(--text-color)', margin: 0 }}>
            Quản lý chương học
          </Title>
          <Text type="secondary">Khóa học: {course?.title}</Text>
        </div>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={() => {
            setEditingChapter(null);
            form.resetFields();
            setIsModalOpen(true);
          }}
        >
          Thêm chương học
        </Button>
      </div>

      <Card>
        <List
          itemLayout="horizontal"
          dataSource={chapters}
          renderItem={(chapter) => (
            <List.Item
              actions={[
                <Button
                  type="link"
                  icon={<PlayCircleOutlined />}
                  onClick={() => navigate(`/instructor/courses/${courseId}/chapters/${chapter.id}/lessons`)}
                >
                  Quản lý bài học
                </Button>,
                <Button
                  type="text"
                  icon={<EditOutlined />}
                  onClick={() => {
                    setEditingChapter(chapter);
                    form.setFieldsValue({
                      title: chapter.title,
                      orderIndex: chapter.orderIndex,
                    });
                    setIsModalOpen(true);
                  }}
                />,
                <Button
                  type="text"
                  danger
                  icon={<DeleteOutlined />}
                  onClick={() => handleDeleteChapter(chapter.id)}
                />,
              ]}
            >
              <List.Item.Meta
                title={<Text strong style={{ color: 'var(--text-color)', fontSize: 16 }}>{`Chương ${chapter.orderIndex}: ${chapter.title}`}</Text>}
              />
            </List.Item>
          )}
        />
      </Card>

      {/* Chapter Save Modal */}
      <AntdModal
        title={editingChapter ? 'Chỉnh sửa chương học' : 'Thêm chương học mới'}
        open={isModalOpen}
        onCancel={() => setIsModalOpen(false)}
        footer={null}
      >
        <Form form={form} layout="vertical" onFinish={handleSaveChapter} style={{ marginTop: '16px' }} noValidate>
          <Form.Item
            label="Tiêu đề chương"
            name="title"
            rules={[
              { required: true, message: 'Tên chương học không được để trống' },
              { min: 5, max: 100, message: 'Tên chương học phải từ 5 đến 100 ký tự' }
            ]}
          >
            <Input placeholder="Ví dụ: Giới thiệu khóa học & Thiết lập môi trường" />
          </Form.Item>

          <Form.Item
            label="Số thứ tự hiển thị"
            name="orderIndex"
            rules={[
              {
                validator: (_, value) => {
                  if (value !== undefined && value !== null && value !== '') {
                    if (Number(value) < 1) {
                      return Promise.reject(new Error('Giá trị tối thiểu là 1'));
                    }
                  }
                  return Promise.resolve();
                }
              }
            ]}
          >
            <Input type="number" min={1} placeholder="Thứ tự hiển thị trong giáo trình" />
          </Form.Item>

          <Form.Item style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: 0, marginTop: 16 }}>
            <Space>
              <Button onClick={() => setIsModalOpen(false)}>Hủy</Button>
              <Button type="primary" htmlType="submit">Lưu lại</Button>
            </Space>
          </Form.Item>
        </Form>

      </AntdModal>
    </div>
  );
};

export default InstructorChapterManagementPage;
