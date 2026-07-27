import React, { useEffect, useState } from 'react';
import { Table, Button, Space, Modal, Input, Tag, message, Typography, Descriptions, Tabs, Collapse, List, Avatar, Rate, Select, Row, Col, Card, Form } from 'antd';
import { EyeOutlined, LockOutlined, UnlockOutlined, SearchOutlined, ReloadOutlined, StarFilled, QuestionCircleOutlined, CheckCircleFilled, FileTextOutlined, VideoCameraOutlined, BookOutlined, CheckOutlined, CloseOutlined } from '@ant-design/icons';
import axiosInstance from '../api/axiosInstance';

const { Title, Text } = Typography;
const { TabPane } = Tabs;
const { Panel } = Collapse;
const { Option } = Select;

interface InstructorInfo {
  id: string;
  fullName: string;
  avatarUrl?: string;
  email?: string;
}

interface CategoryInfo {
  id: number;
  name: string;
  slug: string;
}

interface Answer {
  id: string;
  content: string;
  orderIndex: number;
  isCorrect: boolean;
}

interface Question {
  id: string;
  content: string;
  questionType: string;
  points: number;
  orderIndex: number;
  explanation?: string;
  answers: Answer[];
}

interface LessonDetail {
  id: string;
  title: string;
  orderIndex: number;
  lessonType: string;
  isPreview: boolean;
  resourceUrl?: string;
  durationSeconds?: number;
  textContent?: string;
  isDownloadable?: boolean;
  questions?: Question[];
}

interface ChapterDetail {
  id: string;
  title: string;
  orderIndex: number;
  lessons: LessonDetail[];
}

interface Review {
  id: string;
  studentId: string;
  studentName: string;
  studentAvatar?: string;
  rating: number;
  comment: string;
  createdAt: string;
}

interface CourseDetail {
  id: string;
  title: string;
  slug: string;
  shortDescription: string;
  description: string;
  price: number;
  thumbnailUrl?: string;
  promoVideoUrl?: string;
  level: string;
  language: string;
  status: string;
  averageRating: number;
  totalReviews: number;
  enrollmentCount: number;
  createdAt: string;
  updatedAt: string;
  blockedReason?: string;
  blockedBy?: {
    id: string;
    fullName: string;
    email?: string;
    avatarUrl?: string;
  } | string;
  blockedAt?: string;
  rejectReason?: string;
  rejectedBy?: string;
  rejectedAt?: string;
  instructor: InstructorInfo;
  category: CategoryInfo;
  chapters: ChapterDetail[];
  reviews: Review[];
}

interface CourseSummary {
  id: string;
  title: string;
  thumbnail?: string;
  instructor: string;
  status: string;
  createdAt: string;
  enrollmentCount: number;
  rating: number;
  blockedReason?: string;
  rejectReason?: string;
}

const AdminCourses: React.FC = () => {
  const [courses, setCourses] = useState<CourseSummary[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);

  // Filters state
  const [searchKeyword, setSearchKeyword] = useState('');
  const [searchStatus, setSearchStatus] = useState<string | undefined>(undefined);
  const [searchInstructor, setSearchInstructor] = useState('');
  const [searchCategory, setSearchCategory] = useState('');

  // Detail modal state
  const [detailOpen, setDetailOpen] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [selectedCourse, setSelectedCourse] = useState<CourseDetail | null>(null);

  // Block modal state
  const [blockOpen, setBlockOpen] = useState(false);
  const [blockReason, setBlockReason] = useState('');
  const [blockTargetId, setBlockTargetId] = useState<string | null>(null);
  const [submittingBlock, setSubmittingBlock] = useState(false);
  const [blockReasonError, setBlockReasonError] = useState('');


  // Review (Approve/Reject) modal state
  const [noteOpen, setNoteOpen] = useState(false);
  const [reviewNote, setReviewNote] = useState('');
  const [actionType, setActionType] = useState<'APPROVE' | 'REJECT' | null>(null);
  const [actionCourseId, setActionCourseId] = useState<string | null>(null);
  const [submittingReview, setSubmittingReview] = useState(false);

  // Reason viewer state
  const [reasonModalOpen, setReasonModalOpen] = useState(false);
  const [reasonModalTitle, setReasonModalTitle] = useState('');
  const [reasonModalContent, setReasonModalContent] = useState('');

  const showReason = (title: string, content: string) => {
    setReasonModalTitle(title);
    setReasonModalContent(content || 'Không có lý do chi tiết.');
    setReasonModalOpen(true);
  };

  const fetchCourses = async () => {
    setLoading(true);
    try {
      const res = await axiosInstance.get('/admin/courses', {
        params: {
          page,
          size: pageSize,
          keyword: searchKeyword || undefined,
          status: searchStatus || undefined,
          instructor: searchInstructor || undefined,
          category: searchCategory || undefined,
        },
      });
      if (res.data?.success && res.data?.data) {
        setCourses(res.data.data.content);
        setTotal(res.data.data.totalElements);
      }
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Không thể tải danh sách khóa học.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCourses();
  }, [page, pageSize]);

  const handleResetFilters = () => {
    setSearchKeyword('');
    setSearchStatus(undefined);
    setSearchInstructor('');
    setSearchCategory('');
    setPage(0);
  };

  const handleViewDetails = async (courseId: string) => {
    setDetailLoading(true);
    setDetailOpen(true);
    try {
      const res = await axiosInstance.get(`/admin/courses/${courseId}`);
      if (res.data?.success && res.data?.data) {
        setSelectedCourse(res.data.data);
      }
    } catch (err: any) {
      message.error('Không thể tải chi tiết khóa học.');
      setDetailOpen(false);
    } finally {
      setDetailLoading(false);
    }
  };

  const handleOpenBlockModal = (courseId: string) => {
    setBlockTargetId(courseId);
    setBlockReason('');
    setBlockReasonError('');
    setBlockOpen(true);
  };

  const handleBlockCourse = async () => {
    if (!blockTargetId || !blockReason.trim()) {
      setBlockReasonError('Lý do chặn không được để trống');
      return;
    }
    setBlockReasonError('');
    setSubmittingBlock(true);
    try {
      await axiosInstance.put(`/admin/courses/${blockTargetId}/block`, {
        reason: blockReason,
      });
      message.success('Chặn khóa học thành công.');
      setBlockOpen(false);
      fetchCourses();
      if (selectedCourse && selectedCourse.id === blockTargetId) {
        handleViewDetails(blockTargetId);
      }
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Chặn khóa học thất bại.');
    } finally {
      setSubmittingBlock(false);
    }
  };


  const handleUnblockCourse = (courseId: string) => {
    Modal.confirm({
      title: 'Xác nhận bỏ chặn khóa học',
      content: 'Bạn có chắc chắn muốn bỏ chặn khóa học này? Khóa học sẽ trở về trạng thái hiển thị công khai.',
      okText: 'Bỏ chặn',
      cancelText: 'Hủy',
      okType: 'primary',
      onOk: async () => {
        try {
          await axiosInstance.put(`/admin/courses/${courseId}/unblock`);
          message.success('Bỏ chặn khóa học thành công.');
          fetchCourses();
          if (selectedCourse && selectedCourse.id === courseId) {
            handleViewDetails(courseId);
          }
        } catch (err: any) {
          message.error(err.response?.data?.message || 'Bỏ chặn khóa học thất bại.');
        }
      },
    });
  };

  const handleOpenReviewNote = (courseId: string, type: 'APPROVE' | 'REJECT') => {
    setActionCourseId(courseId);
    setActionType(type);
    setReviewNote(type === 'APPROVE' ? 'Nội dung đạt yêu cầu.' : 'Nội dung chưa đạt yêu cầu.');
    setNoteOpen(true);
  };

  const submitReview = async () => {
    if (!actionCourseId || !actionType) return;
    setSubmittingReview(true);
    try {
      const path = actionType === 'APPROVE' ? 'approve' : 'reject';
      await axiosInstance.put(`/admin/courses/${actionCourseId}/${path}`, null, {
        params: { note: reviewNote },
      });
      message.success(actionType === 'APPROVE' ? 'Đã duyệt khóa học thành công!' : 'Đã từ chối duyệt khóa học!');
      setNoteOpen(false);
      setDetailOpen(false);
      fetchCourses();
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Xét duyệt thất bại.');
    } finally {
      setSubmittingReview(false);
    }
  };

  const columns = [
    {
      title: 'Thumbnail',
      dataIndex: 'thumbnail',
      key: 'thumbnail',
      render: (url: string) => (
        <img
          src={url || 'https://via.placeholder.com/120x80?text=No+Image'}
          alt="thumbnail"
          style={{ width: '80px', height: '50px', objectFit: 'cover', borderRadius: '4px', border: '1px solid #e8e8e8' }}
        />
      ),
    },
    {
      title: 'Tên khóa học',
      dataIndex: 'title',
      key: 'title',
      render: (text: string) => <Text strong style={{ color: '#002140' }}>{text}</Text>,
    },
    {
      title: 'Giảng viên',
      dataIndex: 'instructor',
      key: 'instructor',
      render: (instructor: string) => <Text>{instructor}</Text>,
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => {
        let color = 'blue';
        let label = status;
        if (status === 'ACTIVE') color = 'success';
        if (status === 'BLOCKED') color = 'red';
        if (status === 'BLOCKED_EDITED') {
          color = 'orange';
          label = 'BLOCKED_EDITED';
        }
        if (status === 'PENDING') color = 'warning';
        if (status === 'REJECTED') color = 'error';
        return (
          <Space direction="vertical" size={2}>
            <Tag color={color} style={{ fontWeight: 'bold' }}>
              {label}
            </Tag>
            {status === 'BLOCKED_EDITED' && (
              <Tag color="purple" style={{ fontSize: '10px' }}>
                Đã cập nhật - Đợi xét duyệt
              </Tag>
            )}
          </Space>
        );
      },
    },
    {
      title: 'Ngày tạo',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (dateStr: string) => {
        if (!dateStr) return '';
        const d = new Date(dateStr);
        return `${String(d.getDate()).padStart(2, '0')}/${String(d.getMonth() + 1).padStart(2, '0')}/${d.getFullYear()}`;
      },
    },
    {
      title: 'Đăng ký',
      dataIndex: 'enrollmentCount',
      key: 'enrollmentCount',
      align: 'center' as const,
      render: (count: number) => <Text>{count || 0}</Text>,
    },
    {
      title: 'Rating',
      dataIndex: 'rating',
      key: 'rating',
      render: (rating: number) => (
        <Space size={4}>
          <StarFilled style={{ color: '#fadb14' }} />
          <Text>{rating ? Number(rating).toFixed(1) : '0.0'}</Text>
        </Space>
      ),
    },
    {
      title: 'Hành động',
      key: 'action',
      render: (_: any, record: CourseSummary) => (
        <Space size="middle">
          <Button
            type="primary"
            icon={<EyeOutlined />}
            onClick={() => handleViewDetails(record.id)}
            style={{ backgroundColor: '#1890ff', borderColor: '#1890ff' }}
          >
            Chi tiết
          </Button>

          {record.status === 'PENDING' && (
            <>
              <Button
                type="primary"
                style={{ backgroundColor: '#52c41a', borderColor: '#52c41a' }}
                icon={<CheckOutlined />}
                onClick={() => handleOpenReviewNote(record.id, 'APPROVE')}
              >
                Duyệt
              </Button>
              <Button
                type="primary"
                danger
                icon={<CloseOutlined />}
                onClick={() => handleOpenReviewNote(record.id, 'REJECT')}
              >
                Từ chối
              </Button>
            </>
          )}

          {record.status === 'ACTIVE' && (
            <Button
              type="primary"
              danger
              icon={<LockOutlined />}
              onClick={() => handleOpenBlockModal(record.id)}
            >
              Chặn
            </Button>
          )}

          {record.status === 'REJECTED' && (
            <Button
              type="default"
              icon={<EyeOutlined />}
              onClick={() => showReason('Lý do từ chối duyệt khóa học', record.rejectReason || '')}
            >
              Xem lý do từ chối
            </Button>
          )}

          {(record.status === 'BLOCKED' || record.status === 'BLOCKED_EDITED') && (
            <>
              <Button
                type="default"
                icon={<EyeOutlined />}
                onClick={() => showReason('Lý do chặn khóa học', record.blockedReason || '')}
              >
                Xem lý do bị chặn
              </Button>
              <Button
                type="primary"
                style={{ backgroundColor: '#52c41a', borderColor: '#52c41a' }}
                icon={<UnlockOutlined />}
                onClick={() => handleUnblockCourse(record.id)}
              >
                Bỏ chặn
              </Button>
            </>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: '24px', background: '#f0f2f5', minHeight: '100vh' }}>
      <Card bordered={false} style={{ marginBottom: '24px', borderRadius: '8px' }}>
        <Title level={3} style={{ marginBottom: '24px', color: '#002140' }}>
          Quản Lý Khóa Học
        </Title>
        <Row gutter={[16, 16]} align="middle">
          <Col xs={24} sm={12} md={6}>
            <Input
              placeholder="Tìm theo tên/mô tả..."
              prefix={<SearchOutlined style={{ color: '#bfbfbf' }} />}
              value={searchKeyword}
              onChange={(e) => setSearchKeyword(e.target.value)}
              onPressEnter={fetchCourses}
            />
          </Col>
          <Col xs={24} sm={12} md={6}>
            <Select
              placeholder="Chọn trạng thái"
              style={{ width: '100%' }}
              value={searchStatus}
              onChange={setSearchStatus}
              allowClear
            >
              <Option value="ACTIVE">ACTIVE (Công khai)</Option>
              <Option value="BLOCKED">BLOCKED (Bị chặn)</Option>
              <Option value="PENDING">PENDING (Chờ duyệt)</Option>
              <Option value="REJECTED">REJECTED (Bị từ chối)</Option>
              <Option value="DRAFT">DRAFT (Bản nháp)</Option>
            </Select>
          </Col>
          <Col xs={24} sm={12} md={6}>
            <Input
              placeholder="Tìm theo giảng viên..."
              value={searchInstructor}
              onChange={(e) => setSearchInstructor(e.target.value)}
              onPressEnter={fetchCourses}
            />
          </Col>
          <Col xs={24} sm={12} md={6}>
            <Input
              placeholder="Tìm theo danh mục..."
              value={searchCategory}
              onChange={(e) => setSearchCategory(e.target.value)}
              onPressEnter={fetchCourses}
            />
          </Col>
          <Col xs={24} style={{ textAlign: 'right' }}>
            <Space>
              <Button icon={<ReloadOutlined />} onClick={handleResetFilters}>
                Làm mới
              </Button>
              <Button type="primary" icon={<SearchOutlined />} onClick={fetchCourses} style={{ backgroundColor: '#1890ff', borderColor: '#1890ff' }}>
                Tìm kiếm
              </Button>
            </Space>
          </Col>
        </Row>
      </Card>

      <Table
        dataSource={courses}
        columns={columns}
        rowKey="id"
        loading={loading}
        pagination={{
          current: page + 1,
          pageSize,
          total,
          onChange: (p, size) => {
            setPage(p - 1);
            setPageSize(size);
          },
        }}
        style={{
          overflow: 'hidden',
          background: '#FFFFFF',
          borderRadius: 8,
          border: '1px solid #E5E7EB',
          boxShadow: '0 4px 12px rgba(0,0,0,0.06)'
        }}
      />

      {/* Detail Modal */}
      <Modal
        title={
          <Space>
            <BookOutlined style={{ color: '#1890ff' }} />
            <span>Chi tiết khóa học: {selectedCourse?.title}</span>
          </Space>
        }
        open={detailOpen}
        onCancel={() => setDetailOpen(false)}
        width={950}
        footer={
          selectedCourse?.status === 'PENDING'
            ? [
              <Button key="close" onClick={() => setDetailOpen(false)}>
                Đóng
              </Button>,
              <Button
                key="reject"
                type="primary"
                danger
                icon={<CloseOutlined />}
                onClick={() => handleOpenReviewNote(selectedCourse.id, 'REJECT')}
              >
                Từ chối duyệt
              </Button>,
              <Button
                key="approve"
                type="primary"
                style={{ backgroundColor: '#52c41a', borderColor: '#52c41a' }}
                icon={<CheckOutlined />}
                onClick={() => handleOpenReviewNote(selectedCourse.id, 'APPROVE')}
              >
                Duyệt xuất bản
              </Button>,
            ]
            : selectedCourse?.status === 'ACTIVE'
              ? [
                <Button key="close" onClick={() => setDetailOpen(false)}>
                  Đóng
                </Button>,
                <Button
                  key="block"
                  type="primary"
                  danger
                  icon={<LockOutlined />}
                  onClick={() => handleOpenBlockModal(selectedCourse.id)}
                >
                  Chặn khóa học
                </Button>,
              ]
              : (selectedCourse?.status === 'BLOCKED' || selectedCourse?.status === 'BLOCKED_EDITED')
                ? [
                  <Button key="close" onClick={() => setDetailOpen(false)}>
                    Đóng
                  </Button>,
                  <Button
                    key="unblock"
                    type="primary"
                    style={{ backgroundColor: '#52c41a', borderColor: '#52c41a' }}
                    icon={<UnlockOutlined />}
                    onClick={() => handleUnblockCourse(selectedCourse.id)}
                  >
                    Bỏ chặn khóa học
                  </Button>,
                ]
                : [
                  <Button key="close" onClick={() => setDetailOpen(false)}>
                    Đóng
                  </Button>,
                ]
        }
        loading={detailLoading}
      >
        {selectedCourse && (
          <Tabs defaultActiveKey="info" style={{ marginTop: '12px' }}>
            <TabPane tab="Thông tin cơ bản" key="info">
              <Row gutter={[24, 24]}>
                <Col span={24}>
                  {selectedCourse.thumbnailUrl && (
                    <img
                      src={selectedCourse.thumbnailUrl}
                      alt="banner"
                      style={{ width: '100%', maxHeight: '300px', objectFit: 'cover', borderRadius: '8px', marginBottom: '16px' }}
                    />
                  )}
                </Col>
                <Col span={24}>
                  <Descriptions bordered column={2}>
                    <Descriptions.Item label="Tên khóa học" span={2}>
                      <Text strong>{selectedCourse.title}</Text>
                    </Descriptions.Item>
                    <Descriptions.Item label="Mô tả ngắn" span={2}>
                      {selectedCourse.shortDescription}
                    </Descriptions.Item>
                    <Descriptions.Item label="Mô tả chi tiết" span={2}>
                      <div style={{ maxHeight: '150px', overflowY: 'auto', background: '#f5f5f5', padding: '12px', borderRadius: '4px', whiteSpace: 'pre-wrap' }}>
                        {selectedCourse.description}
                      </div>
                    </Descriptions.Item>
                    <Descriptions.Item label="Giảng viên">
                      {selectedCourse.instructor?.fullName} ({selectedCourse.instructor?.email})
                    </Descriptions.Item>
                    <Descriptions.Item label="Danh mục">
                      {selectedCourse.category?.name}
                    </Descriptions.Item>
                    <Descriptions.Item label="Cấp độ">
                      {selectedCourse.level}
                    </Descriptions.Item>
                    <Descriptions.Item label="Ngôn ngữ">
                      {selectedCourse.language}
                    </Descriptions.Item>
                    <Descriptions.Item label="Giá">
                      <Text type="danger" strong>
                        {selectedCourse.price === 0
                          ? 'Miễn phí'
                          : new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(selectedCourse.price)}
                      </Text>
                    </Descriptions.Item>
                    <Descriptions.Item label="Trạng thái">
                      <Tag color={selectedCourse.status === 'ACTIVE' ? 'success' : selectedCourse.status === 'BLOCKED' ? 'red' : selectedCourse.status === 'BLOCKED_EDITED' ? 'orange' : selectedCourse.status === 'PENDING' ? 'warning' : 'error'}>
                        {selectedCourse.status}
                      </Tag>
                    </Descriptions.Item>
                    <Descriptions.Item label="Ngày tạo">
                      {new Date(selectedCourse.createdAt).toLocaleString('vi-VN')}
                    </Descriptions.Item>
                    <Descriptions.Item label="Đăng ký">
                      {selectedCourse.enrollmentCount} học viên
                    </Descriptions.Item>
                  </Descriptions>
                </Col>

                {(selectedCourse.status === 'BLOCKED' || selectedCourse.status === 'BLOCKED_EDITED') && (
                  <Col span={24}>
                    <Card
                      title={<span style={{ color: '#cf1322' }}>Thông tin chặn khóa học</span>}
                      size="small"
                      headStyle={{ backgroundColor: '#fff1f0', borderColor: '#ffa39e' }}
                      style={{ borderColor: '#ffa39e', backgroundColor: '#fff2f0' }}
                    >
                      <Descriptions column={1}>
                        <Descriptions.Item label="Lý do chặn">
                          <Text strong style={{ color: '#cf1322' }}>{selectedCourse.blockedReason}</Text>
                        </Descriptions.Item>
                        <Descriptions.Item label="Người thực hiện">
                          {selectedCourse.blockedBy && typeof selectedCourse.blockedBy === 'object'
                            ? selectedCourse.blockedBy.fullName || 'Không xác định'
                            : selectedCourse.blockedBy || 'Không xác định'}
                        </Descriptions.Item>
                        <Descriptions.Item label="Thời gian chặn">
                          {selectedCourse.blockedAt ? new Date(selectedCourse.blockedAt).toLocaleString('vi-VN') : ''}
                        </Descriptions.Item>
                      </Descriptions>
                    </Card>
                  </Col>
                )}

                {selectedCourse.status === 'REJECTED' && (
                  <Col span={24}>
                    <Card
                      title={<span style={{ color: '#cf1322' }}>Thông tin từ chối duyệt</span>}
                      size="small"
                      headStyle={{ backgroundColor: '#fff1f0', borderColor: '#ffa39e' }}
                      style={{ borderColor: '#ffa39e', backgroundColor: '#fff2f0' }}
                    >
                      <Descriptions column={1}>
                        <Descriptions.Item label="Lý do từ chối">
                          <Text strong style={{ color: '#cf1322' }}>{selectedCourse.rejectReason}</Text>
                        </Descriptions.Item>
                        <Descriptions.Item label="Người thực hiện">
                          {selectedCourse.rejectedBy}
                        </Descriptions.Item>
                        <Descriptions.Item label="Thời gian xử lý">
                          {selectedCourse.rejectedAt ? new Date(selectedCourse.rejectedAt).toLocaleString('vi-VN') : ''}
                        </Descriptions.Item>
                      </Descriptions>
                    </Card>
                  </Col>
                )}
              </Row>
            </TabPane>

            <TabPane tab="Nội dung khóa học" key="curriculum">
              {selectedCourse.chapters && selectedCourse.chapters.length > 0 ? (
                <Collapse defaultActiveKey={[selectedCourse.chapters[0].id]}>
                  {selectedCourse.chapters.map((chapter) => (
                    <Panel header={<Text strong>{chapter.title}</Text>} key={chapter.id}>
                      <List
                        dataSource={chapter.lessons}
                        renderItem={(lesson: LessonDetail) => (
                          <List.Item>
                            <div style={{ width: '100%' }}>
                              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <Space>
                                  {lesson.lessonType === 'VIDEO' && <VideoCameraOutlined style={{ color: '#1890ff' }} />}
                                  {lesson.lessonType === 'PDF' && <FileTextOutlined style={{ color: '#52c41a' }} />}
                                  {lesson.lessonType === 'TEXT' && <FileTextOutlined style={{ color: '#fa8c16' }} />}
                                  {lesson.lessonType === 'QUIZ' && <QuestionCircleOutlined style={{ color: '#eb2f96' }} />}
                                  <Text strong>{lesson.title}</Text>
                                  {lesson.isPreview && <Tag color="green">Preview</Tag>}
                                  <Tag>{lesson.lessonType}</Tag>
                                </Space>
                                {lesson.durationSeconds ? (
                                  <Text type="secondary">
                                    {Math.floor(lesson.durationSeconds / 60)} phút {lesson.durationSeconds % 60} giây
                                  </Text>
                                ) : null}
                              </div>

                              {/* Text content details */}
                              {lesson.textContent && (
                                <div style={{ marginTop: '8px', padding: '8px', background: '#fafafa', borderRadius: '4px', fontSize: '13px' }}>
                                  <Text type="secondary">{lesson.textContent}</Text>
                                </div>
                              )}

                              {/* Resource details */}
                              {lesson.resourceUrl && (
                                <div style={{ marginTop: '8px' }}>
                                  <Text type="secondary">Đường dẫn tài liệu/video: </Text>
                                  <a href={lesson.resourceUrl} target="_blank" rel="noopener noreferrer">
                                    {lesson.resourceUrl}
                                  </a>
                                </div>
                              )}
                            </div>
                          </List.Item>
                        )}
                      />
                    </Panel>
                  ))}
                </Collapse>
              ) : (
                <div style={{ padding: '24px', textAlign: 'center' }}>Khóa học chưa có nội dung.</div>
              )}
            </TabPane>

            <TabPane tab="Trắc nghiệm (Quiz)" key="quizzes">
              {selectedCourse.chapters &&
                selectedCourse.chapters.some((ch) => ch.lessons.some((l) => l.lessonType === 'QUIZ')) ? (
                <div>
                  {selectedCourse.chapters.map((chapter) => {
                    const quizLessons = chapter.lessons.filter((l) => l.lessonType === 'QUIZ');
                    if (quizLessons.length === 0) return null;
                    return (
                      <div key={chapter.id} style={{ marginBottom: '24px' }}>
                        <Title level={5} style={{ color: '#002140', borderBottom: '1px solid #f0f0f0', paddingBottom: '8px' }}>
                          Chương: {chapter.title}
                        </Title>
                        {quizLessons.map((quiz) => (
                          <Card title={`Bài tập: ${quiz.title}`} key={quiz.id} style={{ marginBottom: '16px' }} size="small">
                            {quiz.questions && quiz.questions.length > 0 ? (
                              <List
                                dataSource={quiz.questions}
                                renderItem={(q: Question, idx) => (
                                  <div style={{ marginBottom: '16px', paddingBottom: '16px', borderBottom: idx < quiz.questions!.length - 1 ? '1px dashed #f0f0f0' : 'none' }}>
                                    <div style={{ marginBottom: '8px' }}>
                                      <Text strong>{q.orderIndex}. {q.content}</Text>
                                      <Tag style={{ marginLeft: '8px' }}>{q.questionType}</Tag>
                                      <Tag color="cyan">{q.points} điểm</Tag>
                                    </div>
                                    <List
                                      grid={{ gutter: 16, column: 2 }}
                                      dataSource={q.answers}
                                      renderItem={(ans: Answer) => (
                                        <List.Item>
                                          <Card
                                            size="small"
                                            style={{
                                              borderColor: ans.isCorrect ? '#52c41a' : '#f0f0f0',
                                              backgroundColor: ans.isCorrect ? '#f6ffed' : '#ffffff',
                                            }}
                                          >
                                            <Space>
                                              {ans.isCorrect ? <CheckCircleFilled style={{ color: '#52c41a' }} /> : null}
                                              <Text>{ans.content}</Text>
                                            </Space>
                                          </Card>
                                        </List.Item>
                                      )}
                                    />
                                    {q.explanation && (
                                      <div style={{ marginTop: '8px', padding: '8px', backgroundColor: '#e6f7ff', borderRadius: '4px', fontSize: '13px' }}>
                                        <Text type="secondary"><span style={{ fontWeight: 'bold' }}>Giải thích: </span>{q.explanation}</Text>
                                      </div>
                                    )}
                                  </div>
                                )}
                              />
                            ) : (
                              <div style={{ textAlign: 'center', padding: '12px' }}>Không có câu hỏi trắc nghiệm nào trong bài học này.</div>
                            )}
                          </Card>
                        ))}
                      </div>
                    );
                  })}
                </div>
              ) : (
                <div style={{ padding: '24px', textAlign: 'center' }}>Khóa học này không chứa bài trắc nghiệm nào.</div>
              )}
            </TabPane>

            <TabPane tab="Đánh giá" key="reviews">
              {selectedCourse.reviews && selectedCourse.reviews.length > 0 ? (
                <List
                  itemLayout="horizontal"
                  dataSource={selectedCourse.reviews}
                  renderItem={(rev: Review) => (
                    <List.Item>
                      <List.Item.Meta
                        avatar={<Avatar src={rev.studentAvatar || 'https://joeschmoe.io/api/v1/random'} />}
                        title={
                          <Space>
                            <Text strong>{rev.studentName}</Text>
                            <Rate disabled defaultValue={rev.rating} style={{ fontSize: '12px' }} />
                            <Text type="secondary" style={{ fontSize: '12px' }}>
                              {new Date(rev.createdAt).toLocaleString('vi-VN')}
                            </Text>
                          </Space>
                        }
                        description={rev.comment}
                      />
                    </List.Item>
                  )}
                />
              ) : (
                <div style={{ padding: '24px', textAlign: 'center' }}>Khóa học chưa có đánh giá nào.</div>
              )}
            </TabPane>
          </Tabs>
        )}
      </Modal>

      {/* Block Reason Modal */}
      <Modal
        title="Lý do chặn khóa học"
        open={blockOpen}
        onCancel={() => setBlockOpen(false)}
        onOk={handleBlockCourse}
        okText="Chặn khóa học"
        cancelText="Hủy"
        okButtonProps={{ danger: true, loading: submittingBlock }}
      >
        <Form layout="vertical" style={{ marginTop: '16px' }} noValidate>
          <Form.Item label="Nhập lý do chặn (bắt buộc):">
            <Input.TextArea
              rows={4}
              value={blockReason}
              onChange={(e) => setBlockReason(e.target.value)}
              placeholder="Ví dụ: Khóa học chứa nội dung vi phạm chính sách"
            />
            {blockReasonError && (
              <div style={{ color: '#ff4d4f', marginTop: '4px', fontSize: '14px' }}>
                {blockReasonError}
              </div>
            )}
          </Form.Item>
        </Form>
      </Modal>


      {/* Review Note Modal (Approve/Reject) */}
      <Modal
        title={actionType === 'APPROVE' ? 'Ghi chú Duyệt khóa học' : 'Lý do từ chối duyệt'}
        open={noteOpen}
        onCancel={() => setNoteOpen(false)}
        onOk={submitReview}
        okText="Gửi kết quả"
        cancelText="Hủy"
        confirmLoading={submittingReview}
      >
        <div style={{ marginTop: '16px' }}>
          <Text style={{ display: 'block', marginBottom: '8px' }}>Gửi lời nhắn tới giảng viên:</Text>
          <Input.TextArea
            rows={4}
            value={reviewNote}
            onChange={(e) => setReviewNote(e.target.value)}
            placeholder="Lời nhắn/Lý do xét duyệt..."
          />
        </div>
      </Modal>

      {/* Reason Viewer Modal */}
      <Modal
        title={
          <Space>
            <EyeOutlined style={{ color: '#1890ff' }} />
            <span>{reasonModalTitle}</span>
          </Space>
        }
        open={reasonModalOpen}
        onCancel={() => setReasonModalOpen(false)}
        footer={[
          <Button key="close" onClick={() => setReasonModalOpen(false)}>
            Đóng
          </Button>
        ]}
      >
        <div style={{ marginTop: 16, padding: '16px', background: '#fafafa', borderRadius: '8px', border: '1px solid #f0f0f0' }}>
          <Text style={{ fontSize: '15px', whiteSpace: 'pre-wrap' }}>{reasonModalContent}</Text>
        </div>
      </Modal>
    </div>
  );
};

export default AdminCourses;
