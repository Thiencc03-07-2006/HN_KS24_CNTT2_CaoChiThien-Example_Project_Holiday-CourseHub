import React, { useEffect, useState } from 'react';
import { Table, Button, Space, Modal, Input, Rate, message, Typography, Select, Row, Col } from 'antd';
import { DeleteOutlined, SearchOutlined, ReloadOutlined } from '@ant-design/icons';
import { reviewService } from '../services/reviewService';
import type { Review } from '../services/reviewService';


const { Title, Text } = Typography;
const { Option } = Select;

const AdminReviews: React.FC = () => {
  const [reviews, setReviews] = useState<Review[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);

  // Filters state
  const [keyword, setKeyword] = useState('');
  const [rating, setRating] = useState<number | undefined>(undefined);
  const [courseIdFilter, setCourseIdFilter] = useState('');
  const [userIdFilter, setUserIdFilter] = useState('');

  const fetchReviews = async () => {
    setLoading(true);
    try {
      const res = await reviewService.getReviewsForAdmin({
        keyword: keyword || undefined,
        courseId: courseIdFilter.trim() || undefined,
        userId: userIdFilter.trim() || undefined,
        rating: rating || undefined,
        page,
        size: pageSize,
      });
      if (res?.success && res.data) {
        setReviews(res.data.content || []);
        setTotal(res.data.totalElements || 0);
      }
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Không thể tải danh sách đánh giá.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReviews();
  }, [page, pageSize, rating]);

  const handleSearch = () => {
    setPage(0);
    fetchReviews();
  };

  const handleReset = () => {
    setKeyword('');
    setRating(undefined);
    setCourseIdFilter('');
    setUserIdFilter('');
    setPage(0);
    // Since rating state reset is async, triggering reload after reset
    setTimeout(() => {
      fetchReviews();
    }, 50);
  };

  const handleDelete = (reviewId: string) => {
    Modal.confirm({
      title: 'Xác nhận xóa đánh giá',
      content: 'Bạn có chắc chắn muốn xóa đánh giá này không? Hành động này sẽ cập nhật điểm trung bình của khóa học.',
      okText: 'Xóa',
      okType: 'danger',
      cancelText: 'Hủy',
      onOk: async () => {
        try {
          await reviewService.deleteReview(reviewId);
          message.success('Đã xóa đánh giá thành công.');
          fetchReviews();
        } catch (err) {
          message.error('Xóa đánh giá thất bại.');
        }
      },
    });
  };

  const columns = [
    {
      title: 'Khóa học',
      dataIndex: 'courseTitle',
      key: 'courseTitle',
      width: '20%',
      render: (text: string, record: Review) => (
        <div>
          <Text strong style={{ color: 'var(--text-color)' }}>{text}</Text>
          <div style={{ fontSize: 10, opacity: 0.6 }}>ID: {record.courseId}</div>
        </div>
      ),
    },
    {
      title: 'Học viên',
      dataIndex: 'studentName',
      key: 'studentName',
      width: '18%',
      render: (text: string, record: Review) => (
        <div>
          <div style={{ fontWeight: 600 }}>{text}</div>
          <div style={{ fontSize: 10, opacity: 0.6 }}>Student ID: {record.studentId}</div>
        </div>
      ),
    },
    {
      title: 'Đánh giá',
      dataIndex: 'rating',
      key: 'rating',
      width: '15%',
      render: (stars: number) => <Rate disabled value={stars} style={{ fontSize: 13, color: '#fadb14' }} />,
    },
    {
      title: 'Nội dung nhận xét',
      dataIndex: 'comment',
      key: 'comment',
      width: '32%',
      render: (text: string, record: Review) => (
        <div>
          <div style={{ color: 'var(--text-color)', lineHeight: 1.5 }}>{text}</div>
          {record.isEdited && <span style={{ fontSize: 11, fontStyle: 'italic', opacity: 0.6 }}>(đã chỉnh sửa)</span>}
        </div>
      ),
    },
    {
      title: 'Ngày tạo',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: '15%',
      render: (dateStr: string) => new Date(dateStr).toLocaleString('vi-VN'),
    },
    {
      title: 'Thao tác',
      key: 'action',
      width: '10%',
      render: (_: any, record: Review) => (
        <Space size="middle">
          <Button
            type="text"
            danger
            icon={<DeleteOutlined />}
            onClick={() => handleDelete(record.id)}
          >
            Xóa
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <Title level={3} style={{ margin: 0, color: 'var(--text-color)' }}>
          Quản lý đánh giá học viên
        </Title>
        <Button icon={<ReloadOutlined />} onClick={fetchReviews}>Làm mới</Button>
      </div>

      {/* Filter panel */}
      <div style={{ background: '#F8FAFC', padding: 20, borderRadius: 12, border: '1px solid #E2E8F0', marginBottom: 24 }}>
        <Row gutter={[16, 16]}>
          <Col xs={24} md={8}>
            <Text style={{ fontSize: 13, display: 'block', marginBottom: 6 }}>Từ khóa (Nhận xét, Tên học viên, Tên khóa học):</Text>
            <Input
              prefix={<SearchOutlined />}
              placeholder="Nhập nội dung tìm kiếm..."
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              onPressEnter={handleSearch}
            />
          </Col>
          <Col xs={12} md={4}>
            <Text style={{ fontSize: 13, display: 'block', marginBottom: 6 }}>Số sao:</Text>
            <Select
              placeholder="Tất cả số sao"
              style={{ width: '100%' }}
              value={rating}
              onChange={setRating}
              allowClear
            >
              {[5, 4, 3, 2, 1].map((stars) => (
                <Option key={stars} value={stars}>
                  {stars} sao
                </Option>
              ))}
            </Select>
          </Col>
          <Col xs={24} md={6}>
            <Text style={{ fontSize: 13, display: 'block', marginBottom: 6 }}>Mã khóa học (Course ID):</Text>
            <Input
              placeholder="Nhập Course UUID..."
              value={courseIdFilter}
              onChange={(e) => setCourseIdFilter(e.target.value)}
              onPressEnter={handleSearch}
            />
          </Col>
          <Col xs={24} md={6}>
            <Text style={{ fontSize: 13, display: 'block', marginBottom: 6 }}>Mã học viên (User ID):</Text>
            <Input
              placeholder="Nhập User UUID..."
              value={userIdFilter}
              onChange={(e) => setUserIdFilter(e.target.value)}
              onPressEnter={handleSearch}
            />
          </Col>
        </Row>
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 12, marginTop: 16 }}>
          <Button onClick={handleReset}>Xóa bộ lọc</Button>
          <Button type="primary" onClick={handleSearch}>Tìm kiếm</Button>
        </div>
      </div>

      <Table
        dataSource={reviews}
        columns={columns}
        rowKey="id"
        loading={loading}
        pagination={{
          current: page + 1,
          pageSize,
          total,
          onChange: (p, s) => {
            setPage(p - 1);
            setPageSize(s);
          },
          showSizeChanger: true,
          pageSizeOptions: ['10', '20', '50'],
        }}
        style={{ background: '#FFFFFF' }}
      />
    </div>
  );
};

export default AdminReviews;
