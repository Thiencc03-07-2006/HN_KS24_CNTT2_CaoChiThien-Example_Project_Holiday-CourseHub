import React, { useEffect, useState } from 'react';
import { Layout, Row, Col, Typography, Spin, Tag, Space, Tabs, Modal, Form, Input, Avatar, message, Progress, Rate, List, Select, Pagination } from 'antd';
import { BookOutlined, GlobalOutlined, SolutionOutlined, StarFilled, UserOutlined, ArrowLeftOutlined, CreditCardOutlined, PlayCircleOutlined, FileTextOutlined, CheckCircleOutlined, HeartFilled, HeartOutlined, DeleteOutlined, EditOutlined, FlagFilled, FlagOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import axiosInstance from '../api/axiosInstance';
import { Card } from '../components/common/UI/Card';
import { Button } from '../components/common/UI/Button';
import { Header } from '../components/common/Layout/Header';
import { Footer } from '../components/common/Layout/Footer';
import { wishlistService } from '../services/wishlistService';
import { favoriteService } from '../services/favoriteService';
import { reviewService } from '../services/reviewService';
import { reportService } from '../services/reportService';


const { Content } = Layout;
const { Title, Paragraph, Text } = Typography;
const { TextArea } = Input;

const CourseDetail: React.FC = () => {
  const { slug } = useParams<{ slug: string }>();
  const { user } = useAuth();
  const navigate = useNavigate();

  const [course, setCourse] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [isEnrolled, setIsEnrolled] = useState(false);
  const [checkingEnrollment, setCheckingEnrollment] = useState(false);
  const [isWishlisted, setIsWishlisted] = useState(false);
  const [isFavorited, setIsFavorited] = useState(false);
  const [enrollmentDetails, setEnrollmentDetails] = useState<any>(null);

  // Modal payment state
  const [paymentOpen, setPaymentOpen] = useState(false);
  const [paymentLoading, setPaymentLoading] = useState(false);
  const [activeOrderId, setActiveOrderId] = useState<string | null>(null);

  // Course report state
  const [reportModalOpen, setReportModalOpen] = useState(false);
  const [submittingReport, setSubmittingReport] = useState(false);
  const [reportForm] = Form.useForm();

  const fetchCourseDetails = async () => {
    setLoading(true);
    try {
      const res = await axiosInstance.get(`/courses/public/${slug}`);
      if (res.data?.success && res.data?.data) {
        const courseData = res.data.data;
        setCourse(courseData);

        // If user is logged in, check enrollment status
        if (user && courseData.id) {
          checkEnrollment(courseData.id);
          checkWishlist(courseData.id);
          checkFavorite(courseData.id);
        }
      }
    } catch (err: any) {
      message.error('Không thể tải chi tiết khóa học.');
      navigate('/courses');
    } finally {
      setLoading(false);
    }
  };

  const checkWishlist = async (courseId: string) => {
    if (!user) return;
    try {
      const res = await wishlistService.checkWishlist(courseId);
      if (res?.success) {
        setIsWishlisted(res.data ?? false);
      }
    } catch (err) {
      console.warn('Failed to check wishlist status');
    }
  };

  const checkFavorite = async (courseId: string) => {
    if (!user) return;
    try {
      const res = await favoriteService.checkFavorite(courseId);
      if (res?.success) {
        setIsFavorited(res.data ?? false);
      }
    } catch (err) {
      console.warn('Failed to check favorite status');
    }
  };

  const handleWishlistToggle = async () => {
    if (!user || !course) {
      message.info('Vui lòng đăng nhập để sử dụng chức năng yêu thích.');
      navigate('/login');
      return;
    }
    try {
      if (isWishlisted) {
        await wishlistService.removeFromWishlist(course.id);
        setIsWishlisted(false);
        message.success('Đã xóa khỏi danh sách mong muốn.');
      } else {
        await wishlistService.addToWishlist(course.id);
        setIsWishlisted(true);
        message.success('Đã thêm vào danh sách mong muốn.');
      }
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Không thể cập nhật danh sách mong muốn.');
    }
  };

  const handleFavoriteToggle = async () => {
    if (!user || !course) {
      message.info('Vui lòng đăng nhập để sử dụng chức năng yêu thích.');
      navigate('/login');
      return;
    }
    try {
      if (isFavorited) {
        await favoriteService.removeFavorite(course.id);
        setIsFavorited(false);
        message.success('Đã xóa khỏi danh sách yêu thích.');
      } else {
        await favoriteService.addFavorite(course.id);
        setIsFavorited(true);
        message.success('Đã thêm vào danh sách yêu thích.');
      }
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Không thể cập nhật danh sách yêu thích.');
    }
  };

  const handleReportSubmit = async (values: { reason: string; description: string }) => {
    setSubmittingReport(true);
    try {
      await reportService.reportCourse(course.id, values.reason, values.description);
      message.success('Báo cáo khóa học vi phạm thành công. Báo cáo của bạn đang chờ xử lý.');
      setReportModalOpen(false);
      reportForm.resetFields();
    } catch (err: any) {
      const errMsg = err?.response?.data?.message || 'Gửi báo cáo thất bại.';
      message.error(errMsg);
    } finally {
      setSubmittingReport(false);
    }
  };

  const checkEnrollment = async (courseId: string) => {
    setCheckingEnrollment(true);
    try {
      const res = await axiosInstance.get(`/enrollments/check/${courseId}`);
      if (res.data?.success) {
        setIsEnrolled(res.data.data ?? false);
      }
    } catch (err) {
      console.warn('Failed to check enrollment status');
    } finally {
      setCheckingEnrollment(false);
    }
  };

  const fetchEnrollmentDetails = async () => {
    if (!user || !course || !isEnrolled) return;
    try {
      const res = await axiosInstance.get(`/enrollments/course/${course.id}/details`);
      if (res.data?.success) {
        setEnrollmentDetails(res.data.data);
      }
    } catch (err) {
      console.warn('Failed to fetch enrollment details');
    }
  };

  useEffect(() => {
    if (isEnrolled && course?.id) {
      fetchEnrollmentDetails();
    }
  }, [isEnrolled, course?.id]);

  useEffect(() => {
    fetchCourseDetails();
  }, [slug, user]);


  const handleEnrollClick = async () => {
    if (!user) {
      message.info('Vui lòng đăng nhập để đăng ký khóa học.');
      navigate('/login');
      return;
    }
    if (course.price > 0) {
      setPaymentLoading(true);
      try {
        const res = await axiosInstance.post('/orders/checkout', { courseId: course.id });
        if (res.data?.success && res.data?.data) {
          setActiveOrderId(res.data.data.id);
          setPaymentOpen(true);
        }
      } catch (err: any) {
        message.error(err.response?.data?.message || 'Khởi tạo đơn hàng thất bại.');
      } finally {
        setPaymentLoading(false);
      }
    } else {
      setPaymentLoading(true);
      try {
        await axiosInstance.post(`/enrollments/${course.id}`);
        message.success('Đăng ký khóa học thành công!');
        setIsEnrolled(true);
      } catch (err: any) {
        message.error(err.response?.data?.message || 'Đăng ký học thất bại.');
      } finally {
        setPaymentLoading(false);
      }
    }
  };

  const handleConfirmPayment = async () => {
    if (!course || !activeOrderId) return;
    setPaymentLoading(true);
    try {
      const mockTxId = 'MOCK-TX-' + Date.now();
      await axiosInstance.post(`/orders/${activeOrderId}/complete?transactionId=${mockTxId}`);
      message.success('Thanh toán thành công và đăng ký khóa học thành công!');
      setIsEnrolled(true);
      setPaymentOpen(false);
      setActiveOrderId(null);
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Thanh toán thất bại.');
    } finally {
      setPaymentLoading(false);
    }
  };

  if (loading || !course) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', background: '#F0F4F8' }}>
        <Spin size="large" description="Đang tải chi tiết khóa học..." />
      </div>
    );
  }

  const tabItems = [
    {
      key: '1',
      label: 'Giới thiệu khóa học',
      children: (
        <div style={{ color: 'var(--text-color)', lineHeight: '1.8' }}>
          <Paragraph style={{ color: 'var(--text-color)', fontSize: 16 }}>{course.description || 'Chưa có mô tả chi tiết cho khóa học này.'}</Paragraph>
          <Title level={4} style={{ color: 'var(--text-color)', marginTop: 24 }}>Bạn sẽ học được gì?</Title>
          <ul style={{ paddingLeft: 20, color: 'var(--text-color)' }}>
            <li>Kiến thức nền tảng và chuyên sâu về môn học.</li>
            <li>Các bài thực hành thực tế, giải quyết tình huống doanh nghiệp.</li>
            <li>Lộ trình bài bản giúp tiết kiệm thời gian tự nghiên cứu.</li>
            <li>Hỗ trợ giải đáp thắc mắc trực tiếp từ giảng viên.</li>
          </ul>
        </div>
      ),
    },
    {
      key: '2',
      label: 'Đề cương chi tiết',
      children: (
        <div style={{ color: 'var(--text-color)' }}>
          <Paragraph>Khóa học gồm {course.chapters?.length || 0} chương và {course.totalLessons || 0} bài học.</Paragraph>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 16, marginTop: 16 }}>
            {course.chapters && course.chapters.length > 0 ? (
              course.chapters.map((chap: any) => (
                <Card
                  key={chap.id}
                  style={{ background: '#FFFFFF', border: '1px solid #E2E8F0', color: 'var(--text-color)' }}
                  bodyStyle={{ padding: 16 }}
                >
                  <Text strong style={{ color: 'var(--text-color)', fontSize: 16 }}>
                    {`Chương ${chap.orderIndex}: ${chap.title}`}
                  </Text>

                  <div style={{ display: 'flex', flexDirection: 'column', gap: 8, marginTop: 12, paddingLeft: 8 }}>
                    {chap.lessons?.map((les: any) => {
                      let icon = <BookOutlined />;
                      if (les.lessonType === 'VIDEO') icon = <PlayCircleOutlined style={{ color: 'var(--primary-color)' }} />;
                      if (les.lessonType === 'PDF') icon = <FileTextOutlined style={{ color: '#fa8c16' }} />;
                      if (les.lessonType === 'QUIZ') icon = <CheckCircleOutlined style={{ color: '#52c41a' }} />;

                      return (
                        <div key={les.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                          <Space>
                            {icon}
                            <Text style={{ color: 'var(--text-color)' }}>{les.title}</Text>
                          </Space>
                          <Tag color={les.isPreview ? 'green' : 'blue'}>{les.lessonType}</Tag>
                        </div>
                      );
                    })}
                  </div>
                </Card>
              ))
            ) : (
              <Text type="secondary">Đang cập nhật đề cương bài học.</Text>
            )}
          </div>
        </div>
      ),
    },
    {
      key: '3',
      label: `Đánh giá & Phản hồi (${course.totalReviews || 0})`,
      children: (
        <CourseReviewsTab
          courseId={course.id}
          isEnrolled={isEnrolled}
          hasCompletedLesson={enrollmentDetails && enrollmentDetails.progressPercent > 0}
          onReviewSubmit={fetchCourseDetails}
        />
      ),
    },
  ];

  return (
    <Layout style={{ minHeight: '100vh', background: '#F0F4F8' }}>
      <Header />

      <div style={{ background: '#FFFFFF', borderBottom: '1px solid #E2E8F0', padding: '12px 24px' }}>
        <div style={{ maxWidth: 1200, margin: '0 auto' }}>
          <Button type="link" icon={<ArrowLeftOutlined />} onClick={() => navigate('/courses')} style={{ paddingLeft: 0 }}>
            Quay lại danh mục
          </Button>
        </div>
      </div>

      <Content style={{ padding: '32px 24px', color: 'var(--text-color)' }}>
        <div style={{ maxWidth: 1200, margin: '0 auto' }}>
          <Row gutter={[24, 24]}>
            {/* Left Content */}
            <Col xs={24} lg={16}>
              <div style={{ marginBottom: 24 }}>
                <Tag color="blue" style={{ marginBottom: 12 }}>{course.category?.name}</Tag>
                <Title style={{ color: 'var(--text-color)', fontSize: 32, marginTop: 0, fontWeight: 700 }}>
                  {course.title}
                </Title>
                <Paragraph style={{ color: 'var(--text-color)', opacity: 0.8, fontSize: 16 }}>
                  {course.shortDescription}
                </Paragraph>

                <Space size="large" wrap style={{ marginTop: 16 }}>
                  <Space>
                    <StarFilled style={{ color: '#fadb14' }} />
                    <Text style={{ color: 'var(--text-color)', fontWeight: 'bold' }}>{course.averageRating?.toFixed(1) || '0.0'}</Text>
                    <Text style={{ color: 'var(--text-color)', opacity: 0.7 }}>({course.totalReviews || 0} đánh giá)</Text>
                  </Space>
                  <Space style={{ color: 'var(--text-color)', opacity: 0.8 }}>
                    <BookOutlined />
                    <span>{course.chapters?.length || 0} Chương</span>
                  </Space>
                  <Space style={{ color: 'var(--text-color)', opacity: 0.8 }}>
                    <GlobalOutlined />
                    <span>{course.language || 'Tiếng Việt'}</span>
                  </Space>
                </Space>
              </div>

              {/* Instructor Section */}
              <Card style={{ background: '#FFFFFF', border: '1px solid #E2E8F0', borderRadius: 8, marginBottom: 24 }}>
                <Space size="middle">
                  <Avatar size={64} src={course.instructor?.avatarUrl} icon={<UserOutlined />} />
                  <div>
                    <Text style={{ color: 'var(--text-color)', opacity: 0.7, fontSize: 12, display: 'block' }}>Giảng viên</Text>
                    <Text strong style={{ color: 'var(--text-color)', fontSize: 18 }}>{course.instructor?.fullName}</Text>
                    <Text style={{ color: 'var(--text-color)', opacity: 0.8, fontSize: 13, display: 'block' }}>{course.instructor?.headline || 'Chuyên gia đào tạo tại CourseHub'}</Text>
                  </div>
                </Space>
              </Card>

              {/* Tab Sections */}
              <Card style={{ background: '#FFFFFF', border: '1px solid #E2E8F0', borderRadius: 8 }}>
                <Tabs defaultActiveKey="1" items={tabItems} />
              </Card>
            </Col>

            {/* Right Sidebar Purchase Card */}
            <Col xs={24} lg={8}>
              <Card
                cover={
                  <img
                    alt={course.title}
                    src={course.thumbnailUrl || 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?q=80&w=600'}
                    style={{ height: 200, objectFit: 'cover' }}
                  />
                }
                style={{
                  background: '#FFFFFF',
                  border: '1px solid #E2E8F0',
                  borderRadius: 8,
                  overflow: 'hidden',
                  position: 'sticky',
                  top: 24,
                  boxShadow: '0 4px 12px rgba(0,0,0,0.05)',
                }}
              >
                <div style={{ textAlign: 'center', padding: '12px 0' }}>
                  <Text style={{ color: 'var(--text-color)', opacity: 0.7, fontSize: 14 }}>Học phí:</Text>
                  <div style={{ color: 'var(--primary-color)', fontSize: 32, fontWeight: 'bold', margin: '8px 0' }}>
                    {course.price === 0
                      ? 'Miễn phí'
                      : new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(course.price)}
                  </div>
                </div>

                <div style={{ marginTop: 16 }}>
                  <div style={{ display: 'flex', gap: 12, width: '100%' }}>
                    {checkingEnrollment ? (
                      <Button disabled size="large" style={{ flex: 1 }}><Spin size="small" /></Button>
                    ) : isEnrolled ? (
                      <Button
                        type="primary"
                        size="large"
                        style={{ background: '#52c41a', borderColor: '#52c41a', flex: 1 }}
                        onClick={() => navigate(`/learning/course/${course.id}`)}
                      >
                        Bắt đầu học ngay
                      </Button>
                    ) : (
                      <Button type="primary" size="large" style={{ flex: 1 }} onClick={handleEnrollClick}>
                        Đăng ký học ngay
                      </Button>
                    )}

                    <Button
                      size="large"
                      icon={isWishlisted ? <FlagFilled style={{ color: '#1890ff' }} /> : <FlagOutlined />}
                      onClick={handleWishlistToggle}
                      style={{
                        borderColor: isWishlisted ? '#1890ff' : undefined,
                        transition: 'transform 0.1s ease',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                      }}
                      onMouseDown={(e) => { e.currentTarget.style.transform = 'scale(0.9)'; }}
                      onMouseUp={(e) => { e.currentTarget.style.transform = 'scale(1)'; }}
                    />

                    <Button
                      size="large"
                      icon={isFavorited ? <HeartFilled style={{ color: '#EF4444' }} /> : <HeartOutlined />}
                      onClick={handleFavoriteToggle}
                      style={{
                        borderColor: isFavorited ? '#EF4444' : undefined,
                        color: isFavorited ? '#EF4444' : undefined,
                        transition: 'transform 0.1s ease',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        gap: 4,
                      }}
                      onMouseDown={(e) => { e.currentTarget.style.transform = 'scale(0.9)'; }}
                      onMouseUp={(e) => { e.currentTarget.style.transform = 'scale(1)'; }}
                    >
                    </Button>
                  </div>
                </div>

                <div style={{ marginTop: 24, display: 'flex', flexDirection: 'column', gap: 12, borderTop: '1px solid #E2E8F0', paddingTop: 16 }}>
                  <Space style={{ color: 'var(--text-color)', opacity: 0.8, fontSize: 13 }}>
                    <SolutionOutlined style={{ color: 'var(--primary-color)' }} />
                    <span>Quyền truy cập khóa học trọn đời</span>
                  </Space>
                  <Space style={{ color: 'var(--text-color)', opacity: 0.8, fontSize: 13 }}>
                    <BookOutlined style={{ color: 'var(--primary-color)' }} />
                    <span>Bài giảng chi tiết chất lượng cao</span>
                  </Space>
                  {course && (!user || user.id !== course.instructor?.id) && (
                    <Button
                      type="text"
                      danger
                      icon={<FlagFilled />}
                      style={{ marginTop: 12, padding: 0, height: 'auto', display: 'flex', alignItems: 'center', gap: 6 }}
                      onClick={() => {
                        if (!user) {
                          message.info('Vui lòng đăng nhập để báo cáo khóa học.');
                          navigate('/login');
                          return;
                        }
                        setReportModalOpen(true);
                      }}
                    >
                      Báo cáo khóa học vi phạm
                    </Button>
                  )}
                </div>
              </Card>
            </Col>
          </Row>
        </div>
      </Content>

      <Footer />

      {/* Course Report Modal */}
      <Modal
        title={<span><FlagFilled style={{ color: '#ff4d4f', marginRight: 8 }} />Báo cáo khóa học vi phạm</span>}
        open={reportModalOpen}
        onCancel={() => {
          setReportModalOpen(false);
          reportForm.resetFields();
        }}
        footer={null}
        width={480}
      >
        <Form
          form={reportForm}
          layout="vertical"
          onFinish={handleReportSubmit}
          style={{ marginTop: 16 }}
          noValidate
        >
          <Form.Item
            name="reason"
            label="Lý do báo cáo"
            rules={[{ required: true, message: 'Trường này là bắt buộc' }]}
          >
            <Select placeholder="Chọn lý do báo cáo...">
              <Select.Option value="Spam">Spam</Select.Option>
              <Select.Option value="Thông tin sai">Thông tin sai</Select.Option>
              <Select.Option value="Lừa đảo">Lừa đảo</Select.Option>
              <Select.Option value="Nội dung phản cảm">Nội dung phản cảm</Select.Option>
              <Select.Option value="Vi phạm bản quyền">Vi phạm bản quyền</Select.Option>
              <Select.Option value="Khác">Khác</Select.Option>
            </Select>
          </Form.Item>

          <Form.Item
            noStyle
            shouldUpdate={(prevValues, currentValues) => prevValues.reason !== currentValues.reason}
          >
            {({ getFieldValue }) => {
              const isOther = getFieldValue('reason') === 'Khác';
              return (
                <Form.Item
                  name="description"
                  label="Mô tả chi tiết"
                  rules={[
                    {
                      required: isOther,
                      message: 'Trường này là bắt buộc'
                    }
                  ]}
                >
                  <TextArea
                    rows={4}
                    placeholder="Mô tả chi tiết về hành vi vi phạm của khóa học..."
                  />
                </Form.Item>
              );
            }}
          </Form.Item>


          <Form.Item style={{ textAlign: 'right', marginBottom: 0 }}>
            <Space>
              <Button onClick={() => {
                setReportModalOpen(false);
                reportForm.resetFields();
              }}>
                Hủy
              </Button>
              <Button type="primary" htmlType="submit" loading={submittingReport} danger>
                Gửi báo cáo
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      {/* Mock Payment Modal */}
      <Modal
        title="Thanh toán học phí (Giao dịch giả lập)"
        open={paymentOpen}
        onCancel={() => setPaymentOpen(false)}
        onOk={handleConfirmPayment}
        okText="Thực hiện thanh toán"
        cancelText="Hủy bỏ"
        confirmLoading={paymentLoading}
        width={450}
      >
        <div style={{ marginTop: 16, textAlign: 'center', marginBottom: 24 }}>
          <Text style={{ fontSize: 14, color: 'var(--text-color)', opacity: 0.7 }}>Tổng chi phí:</Text>
          <div style={{ fontSize: 24, fontWeight: 'bold', color: 'var(--primary-color)', marginTop: 4 }}>
            {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(course?.price || 0)}
          </div>
        </div>

        <Form layout="vertical" requiredMark={false} noValidate>
          <Form.Item label="Số thẻ tín dụng / Ghi nợ" required>
            <Input prefix={<CreditCardOutlined />} placeholder="4111 2222 3333 4444" defaultValue="4111222233334444" />
          </Form.Item>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item label="Hạn dùng (MM/YY)" required>
                <Input placeholder="12/29" defaultValue="12/29" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item label="Mã bảo mật CVV" required>
                <Input placeholder="123" defaultValue="123" />
              </Form.Item>
            </Col>
          </Row>
          <Paragraph type="secondary" style={{ fontSize: 12, marginTop: 8 }}>
            * Đây là môi trường thanh toán demo. Mọi thông tin thẻ bất kỳ đều được chấp nhận và không thực hiện trừ tiền thật.
          </Paragraph>
        </Form>

      </Modal>
    </Layout>
  );
};

// Course Reviews Tab sub-component
interface Review {
  id: string;
  studentId: string;
  studentName: string;
  studentAvatar?: string;
  rating: number;
  comment: string;
  isEdited: boolean;
  courseId: string;
  courseTitle: string;
  createdAt: string;
  updatedAt: string;
}

interface RatingSummary {
  averageRating: number;
  totalReviews: number;
  distribution: Record<string, number>;
}

const CourseReviewsTab: React.FC<{ courseId: string; isEnrolled: boolean; hasCompletedLesson: boolean; onReviewSubmit: () => void }> = ({
  courseId,
  isEnrolled,
  hasCompletedLesson,
  onReviewSubmit,
}) => {
  const { user } = useAuth();
  const [summary, setSummary] = useState<RatingSummary | null>(null);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [page, setPage] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [sort, setSort] = useState('newest');
  const [loading, setLoading] = useState(true);

  // Review Modal State
  const [modalOpen, setModalOpen] = useState(false);
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [editingReviewId, setEditingReviewId] = useState<string | null>(null);

  // Report Review State
  const [reportModalOpen, setReportModalOpen] = useState(false);
  const [reportingReview, setReportingReview] = useState<Review | null>(null);
  const [reportReason, setReportReason] = useState('Spam');
  const [reportDescription, setReportDescription] = useState('');
  const [submittingReport, setSubmittingReport] = useState(false);
  const [commentError, setCommentError] = useState('');
  const [reportDescriptionError, setReportDescriptionError] = useState('');


  const fetchSummary = async () => {
    try {
      const res = await reviewService.getRatingSummary(courseId);
      if (res?.success) {
        setSummary(res.data ?? null);
      }
    } catch (err) {
      console.warn('Failed to load rating summary');
    }
  };

  const fetchReviews = async () => {
    setLoading(true);
    try {
      const res = await reviewService.getCourseReviews(courseId, page, 5, sort);
      if (res?.success && res.data) {
        setReviews(res.data.content);
        setTotalElements(res.data.totalElements);
      }
    } catch (err) {
      message.error('Không thể tải danh sách đánh giá.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSummary();
  }, [courseId]);

  useEffect(() => {
    fetchReviews();
  }, [courseId, page, sort]);

  const handleSubmit = async () => {
    if (!comment || !comment.trim()) {
      setCommentError('Nội dung đánh giá không được để trống');
      return;
    }
    if (comment.trim().length < 5 || comment.trim().length > 1000) {
      setCommentError('Nội dung đánh giá phải từ 5 đến 1000 ký tự');
      return;
    }
    setCommentError('');
    setSubmitting(true);
    try {
      if (editingReviewId) {
        await reviewService.updateReview(editingReviewId, rating, comment);
        message.success('Cập nhật đánh giá thành công.');
      } else {
        await reviewService.createOrUpdateReview(courseId, rating, comment);
        message.success('Đánh giá khóa học thành công.');
      }
      setModalOpen(false);
      setComment('');
      setRating(5);
      setEditingReviewId(null);
      fetchSummary();
      fetchReviews();
      onReviewSubmit();
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Gửi đánh giá thất bại.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleEdit = (review: Review) => {
    setEditingReviewId(review.id);
    setRating(review.rating);
    setComment(review.comment);
    setCommentError('');
    setModalOpen(true);
  };

  const handleDelete = async (reviewId: string) => {
    Modal.confirm({
      title: 'Xác nhận xóa',
      content: 'Bạn có chắc chắn muốn xóa đánh giá này không?',
      okText: 'Xóa',
      okType: 'danger',
      cancelText: 'Hủy',
      onOk: async () => {
        try {
          await reviewService.deleteReview(reviewId);
          message.success('Xóa đánh giá thành công.');
          fetchSummary();
          fetchReviews();
          onReviewSubmit();
        } catch (err) {
          message.error('Xóa đánh giá thất bại.');
        }
      },
    });
  };

  const openAddModal = () => {
    const myReview = reviews.find(r => user && r.studentId === user.id);
    if (myReview) {
      setEditingReviewId(myReview.id);
      setRating(myReview.rating);
      setComment(myReview.comment);
    } else {
      setEditingReviewId(null);
      setRating(5);
      setComment('');
    }
    setCommentError('');
    setModalOpen(true);
  };

  const handleOpenReportModal = (review: Review) => {
    setReportingReview(review);
    setReportReason('Spam');
    setReportDescription('');
    setReportDescriptionError('');
    setReportModalOpen(true);
  };

  const handleReportSubmit = async () => {
    if (!reportingReview) return;
    const isOther = reportReason === 'Khác';
    if (isOther && reportDescription.trim().length === 0) {
      setReportDescriptionError('Lý do báo cáo không được để trống');
      return;
    }
    setReportDescriptionError('');
    setSubmittingReport(true);
    try {
      await reportService.reportReview(reportingReview.id, reportReason, reportDescription);
      message.success('Báo cáo đánh giá vi phạm thành công. Báo cáo của bạn đang chờ xử lý.');
      setReportModalOpen(false);
    } catch (err: any) {
      const errMsg = err?.response?.data?.message || 'Gửi báo cáo thất bại.';
      message.error(errMsg);
    } finally {
      setSubmittingReport(false);
    }
  };


  const totalDist = summary ? Object.values(summary.distribution).reduce((a, b) => a + b, 0) : 0;

  return (
    <div style={{ color: 'var(--text-color)', padding: '8px 0' }}>
      <Row gutter={[32, 24]} align="middle" style={{ marginBottom: 32 }}>
        <Col xs={24} md={8} style={{ textAlign: 'center', borderRight: '1px solid #E2E8F0' }}>
          <div style={{ fontSize: 64, fontWeight: 'bold', color: 'var(--text-color)', lineHeight: 1 }}>
            {summary?.averageRating?.toFixed(1) || '0.0'}
          </div>
          <Rate disabled allowHalf value={summary?.averageRating || 0} style={{ margin: '12px 0 8px', color: '#fadb14' }} />
          <div style={{ color: 'var(--text-color)', opacity: 0.7 }}>
            {summary?.totalReviews || 0} đánh giá
          </div>
        </Col>
        <Col xs={24} md={16}>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
            {[5, 4, 3, 2, 1].map((stars) => {
              const count = summary ? summary.distribution[String(stars)] || 0 : 0;
              const percent = totalDist > 0 ? (count / totalDist) * 100 : 0;
              return (
                <div key={stars} style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                  <span style={{ width: 50, display: 'flex', alignItems: 'center', gap: 4 }}>
                    {stars} <StarFilled style={{ color: '#fadb14', fontSize: 13 }} />
                  </span>
                  <Progress
                    percent={percent}
                    showInfo={false}
                    strokeColor="var(--primary-color)"
                    trailColor="#E2E8F0"
                    style={{ flex: 1, margin: 0 }}
                  />
                  <span style={{ width: 40, textAlign: 'right', fontSize: 13, opacity: 0.8 }}>{count}</span>
                </div>
              );
            })}
          </div>
        </Col>
      </Row>

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid #E2E8F0', paddingBottom: 16, marginBottom: 24 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <span style={{ fontWeight: 600 }}>Sắp xếp theo:</span>
          <Select value={sort} onChange={(val) => { setSort(val); setPage(0); }} style={{ width: 180 }}>
            <Select.Option value="newest">Mới nhất</Select.Option>
            <Select.Option value="oldest">Cũ nhất</Select.Option>
            <Select.Option value="highest">Đánh giá cao nhất</Select.Option>
            <Select.Option value="lowest">Đánh giá thấp nhất</Select.Option>
          </Select>
        </div>
        {isEnrolled && hasCompletedLesson && (
          <Button type="primary" onClick={openAddModal}>
            Viết đánh giá
          </Button>
        )}
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', padding: '40px 0' }}><Spin description="Đang tải đánh giá..." /></div>
      ) : reviews.length === 0 ? (
        <div style={{ textAlign: 'center', padding: '40px 0', opacity: 0.6 }}>Chưa có đánh giá nào cho khóa học này.</div>
      ) : (
        <List
          dataSource={reviews}
          renderItem={(item) => (
            <div style={{ padding: '16px 0', borderBottom: '1px solid #F1F5F9', display: 'flex', gap: 16 }}>
              <Avatar src={item.studentAvatar} icon={<UserOutlined />} size={48} />
              <div style={{ flex: 1 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div>
                    <div style={{ fontWeight: 600, fontSize: 15 }}>{item.studentName}</div>
                    <Space size="middle" style={{ marginTop: 4 }}>
                      <Rate disabled value={item.rating} style={{ fontSize: 12, color: '#fadb14' }} />
                      <span style={{ fontSize: 12, opacity: 0.6 }}>
                        {new Date(item.createdAt).toLocaleDateString('vi-VN')}
                        {item.isEdited && <span style={{ marginLeft: 8, fontStyle: 'italic' }}>(đã chỉnh sửa)</span>}
                      </span>
                    </Space>
                  </div>
                  {user && item.studentId === user.id ? (
                    <Space>
                      <Button type="text" size="small" icon={<EditOutlined />} onClick={() => handleEdit(item)}>Sửa</Button>
                      <Button type="text" danger size="small" icon={<DeleteOutlined />} onClick={() => handleDelete(item.id)}>Xóa</Button>
                    </Space>
                  ) : (
                    user && (
                      <Button
                        type="text"
                        danger
                        size="small"
                        icon={<FlagFilled />}
                        onClick={() => handleOpenReportModal(item)}
                      >
                        Báo cáo
                      </Button>
                    )
                  )}
                </div>
                <div style={{ marginTop: 12, lineHeight: 1.6, fontSize: 14 }}>
                  {item.comment}
                </div>
              </div>
            </div>
          )}
        />
      )}

      {totalElements > 5 && (
        <div style={{ display: 'flex', justifyContent: 'center', marginTop: 24 }}>
          <Pagination
            current={page + 1}
            pageSize={5}
            total={totalElements}
            onChange={(p) => setPage(p - 1)}
            showSizeChanger={false}
          />
        </div>
      )}

      <Modal
        title={editingReviewId ? 'Chỉnh sửa đánh giá' : 'Viết đánh giá khóa học'}
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        onOk={handleSubmit}
        okText="Gửi đánh giá"
        cancelText="Hủy"
        confirmLoading={submitting}
        destroyOnClose
      >
        <Form layout="vertical" style={{ marginTop: 16 }} noValidate>
          <Form.Item label="Đánh giá của bạn" required>
            <Rate value={rating} onChange={setRating} style={{ color: '#fadb14' }} />
          </Form.Item>
          <Form.Item label="Nội dung nhận xét" required>
            <Input.TextArea
              rows={4}
              value={comment}
              onChange={(e) => setComment(e.target.value)}
              placeholder="Chia sẻ cảm nghĩ của bạn về giảng viên và khóa học..."
              maxLength={1000}
            />
            {commentError && (
              <div style={{ color: '#ff4d4f', marginTop: '4px', fontSize: '14px' }}>
                {commentError}
              </div>
            )}
            <div style={{ textAlign: 'right', fontSize: 12, opacity: 0.6, marginTop: 4 }}>
              {comment.length}/1000 ký tự
            </div>
          </Form.Item>
        </Form>
      </Modal>

      {/* Report Review Modal */}
      <Modal
        title={<span><FlagFilled style={{ color: '#ff4d4f', marginRight: 8 }} />Báo cáo đánh giá vi phạm</span>}
        open={reportModalOpen}
        onCancel={() => setReportModalOpen(false)}
        onOk={handleReportSubmit}
        okText="Gửi báo cáo"
        cancelText="Hủy"
        confirmLoading={submittingReport}
      >
        <Form layout="vertical" style={{ marginTop: 16 }} noValidate>
          <Form.Item label="Lý do báo cáo" required>
            <Select value={reportReason} onChange={setReportReason}>
              <Select.Option value="Spam">Spam</Select.Option>
              <Select.Option value="Thông tin sai">Thông tin sai</Select.Option>
              <Select.Option value="Lừa đảo">Lừa đảo</Select.Option>
              <Select.Option value="Nội dung phản cảm">Nội dung phản cảm</Select.Option>
              <Select.Option value="Vi phạm bản quyền">Vi phạm bản quyền</Select.Option>
              <Select.Option value="Khác">Khác</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item
            label="Mô tả chi tiết"
            required={reportReason === 'Khác'}
          >
            <Input.TextArea
              rows={4}
              value={reportDescription}
              onChange={(e) => setReportDescription(e.target.value)}
              placeholder="Mô tả cụ thể về hành vi vi phạm..."
            />
            {reportDescriptionError && (
              <div style={{ color: '#ff4d4f', marginTop: '4px', fontSize: '14px' }}>
                {reportDescriptionError}
              </div>
            )}
          </Form.Item>
        </Form>
      </Modal>

    </div>
  );
};

export default CourseDetail;
