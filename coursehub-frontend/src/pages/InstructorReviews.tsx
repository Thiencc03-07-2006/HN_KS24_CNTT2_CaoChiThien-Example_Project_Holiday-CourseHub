import React, { useEffect, useState } from 'react';
import { Row, Col, Typography, Spin, Rate, Progress, List, Avatar, message, Pagination, Card } from 'antd';
import { UserOutlined, MessageOutlined, StarFilled } from '@ant-design/icons';
import { reviewService } from '../services/reviewService';
import type { Review, InstructorReviewStats } from '../services/reviewService';


const { Title, Text, Paragraph } = Typography;

const InstructorReviews: React.FC = () => {
  const [stats, setStats] = useState<InstructorReviewStats | null>(null);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(true);
  const [statsLoading, setStatsLoading] = useState(true);

  const fetchStats = async () => {
    setStatsLoading(true);
    try {
      const res = await reviewService.getInstructorStats();
      if (res?.success) {
        setStats(res.data ?? null);
      }
    } catch (err) {
      console.warn('Failed to load instructor stats');
    } finally {
      setStatsLoading(false);
    }
  };

  const fetchReviews = async () => {
    setLoading(true);
    try {
      const res = await reviewService.getInstructorReviews(page, pageSize);
      if (res?.success && res.data) {
        setReviews(res.data.content || []);
        setTotal(res.data.totalElements || 0);
      }
    } catch (err) {
      message.error('Không thể tải danh sách đánh giá.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStats();
  }, []);

  useEffect(() => {
    fetchReviews();
  }, [page, pageSize]);

  const totalDist = stats ? Object.values(stats.distribution).reduce((a, b) => a + b, 0) : 0;

  return (
    <div>
      <Title level={3} style={{ color: 'var(--text-color)', marginBottom: 24 }}>
        Đánh giá của học viên
      </Title>

      {/* Stats Summary Panel */}
      {statsLoading ? (
        <Card style={{ marginBottom: 24, textAlign: 'center' }}>
          <Spin description="Đang tải dữ liệu thống kê..." />
        </Card>
      ) : (
        <Card style={{ marginBottom: 24, borderRadius: 12, border: '1px solid #E2E8F0', boxShadow: '0 4px 12px rgba(0,0,0,0.03)' }}>
          <Row gutter={[32, 24]} align="middle">
            <Col xs={24} md={8} style={{ textAlign: 'center', borderRight: '1px solid #F1F5F9' }}>
              <Text type="secondary" style={{ display: 'block', fontSize: 13, textTransform: 'uppercase', fontWeight: 600 }}>Điểm trung bình</Text>
              <div style={{ fontSize: 72, fontWeight: 800, color: 'var(--text-color)', lineHeight: 1.1, margin: '8px 0' }}>
                {stats?.averageRating?.toFixed(1) || '0.0'}
              </div>
              <Rate disabled allowHalf value={stats?.averageRating || 0} style={{ color: '#fadb14' }} />
              <div style={{ marginTop: 12, fontSize: 14, color: 'var(--text-color)', opacity: 0.8, fontWeight: 500 }}>
                {stats?.totalReviews || 0} lượt đánh giá tổng cộng
              </div>
            </Col>
            
            <Col xs={24} md={16}>
              <Text type="secondary" style={{ display: 'block', fontSize: 13, textTransform: 'uppercase', fontWeight: 600, marginBottom: 16 }}>Phân bố điểm xếp hạng</Text>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                {[5, 4, 3, 2, 1].map((stars) => {
                  const count = stats ? stats.distribution[String(stars)] || 0 : 0;
                  const percent = totalDist > 0 ? (count / totalDist) * 100 : 0;
                  return (
                    <div key={stars} style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                      <span style={{ width: 50, display: 'flex', alignItems: 'center', gap: 4, fontWeight: 500 }}>
                        {stars} <StarFilled style={{ color: '#fadb14', fontSize: 13 }} />
                      </span>
                      <Progress
                        percent={percent}
                        showInfo={false}
                        strokeColor="var(--primary-color)"
                        trailColor="#F1F5F9"
                        strokeWidth={10}
                        style={{ flex: 1, margin: 0 }}
                      />
                      <span style={{ width: 45, textAlign: 'right', fontSize: 13, fontWeight: 600, color: 'var(--text-color)', opacity: 0.8 }}>{count}</span>
                    </div>
                  );
                })}
              </div>
            </Col>
          </Row>
        </Card>
      )}

      {/* Reviews list */}
      <Title level={4} style={{ color: 'var(--text-color)', marginBottom: 16, display: 'flex', alignItems: 'center', gap: 8 }}>
        <MessageOutlined /> Nhận xét chi tiết
      </Title>

      <Card style={{ borderRadius: 12, border: '1px solid #E2E8F0' }}>
        {loading ? (
          <div style={{ textAlign: 'center', padding: '40px 0' }}><Spin description="Đang tải danh sách nhận xét..." /></div>
        ) : reviews.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '40px 0', opacity: 0.6 }}>Chưa có học viên nào đánh giá khóa học của bạn.</div>
        ) : (
          <>
            <List
              dataSource={reviews}
              renderItem={(item) => (
                <div style={{ padding: '20px 0', borderBottom: '1px solid #F1F5F9', display: 'flex', gap: 16 }}>
                  <Avatar src={item.studentAvatar} icon={<UserOutlined />} size={48} style={{ flexShrink: 0 }} />
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', flexWrap: 'wrap', gap: 8 }}>
                      <div>
                        <Text strong style={{ fontSize: 15, color: 'var(--text-color)' }}>{item.studentName}</Text>
                        <span style={{ fontSize: 12, opacity: 0.6, marginLeft: 12 }}>
                          {new Date(item.createdAt).toLocaleDateString('vi-VN')}
                          {item.isEdited && <span style={{ marginLeft: 8, fontStyle: 'italic' }}>(đã chỉnh sửa)</span>}
                        </span>
                      </div>
                      <Rate disabled value={item.rating} style={{ fontSize: 11, color: '#fadb14' }} />
                    </div>
                    
                    <div style={{ margin: '6px 0 10px', fontSize: 12, color: 'var(--primary-color)', fontWeight: 500 }}>
                      Khóa học: {item.courseTitle}
                    </div>

                    <Paragraph style={{ margin: 0, color: 'var(--text-color)', lineHeight: 1.6, fontSize: 14 }}>
                      {item.comment}
                    </Paragraph>
                  </div>
                </div>
              )}
            />

            {total > pageSize && (
              <div style={{ display: 'flex', justifyContent: 'center', marginTop: 24 }}>
                <Pagination
                  current={page + 1}
                  pageSize={pageSize}
                  total={total}
                  onChange={(p, s) => {
                    setPage(p - 1);
                    setPageSize(s);
                  }}
                  showSizeChanger={false}
                />
              </div>
            )}
          </>
        )}
      </Card>
    </div>
  );
};

export default InstructorReviews;
