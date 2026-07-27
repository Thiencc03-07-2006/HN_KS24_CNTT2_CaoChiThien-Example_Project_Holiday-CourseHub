import React, { useEffect, useState } from 'react';
import { Row, Col, Typography, message, Table, Rate, Empty, Tooltip } from 'antd';
import { BookOutlined, DollarOutlined, SolutionOutlined, StarOutlined, CommentOutlined, HeartOutlined, RiseOutlined } from '@ant-design/icons';
import { dashboardService } from '../services/dashboardService';
import { Card } from '../components/common/UI/Card';
import { Loading } from '../components/common/UI/Loading';

const { Title, Text } = Typography;

const InstructorDashboardPage: React.FC = () => {
  const [stats, setStats] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const res = await dashboardService.getInstructorStatistics();
        if (res?.success && res?.data) {
          setStats(res.data);
        }
      } catch (err) {
        message.error('Không thể tải số liệu thống kê giảng viên.');
      } finally {
        setLoading(false);
      }
    };
    fetchStats();
  }, []);

  if (loading) {
    return <Loading message="Đang tải dữ liệu tổng quan..." />;
  }

  // Custom Chart calculation
  const enrollmentTimelineRaw = stats?.enrollmentTimeline || [];

  // Group by date
  const groupedByDate: { [date: string]: { date: string; total: number; details: Array<{ courseName: string; count: number }> } } = {};
  for (const item of enrollmentTimelineRaw) {
    const rawDate = item.date || item.enrollmentDate || 'Unknown';
    const course = item.courseName || item.course_name || 'N/A';
    const count = Number(item.enrollmentCount !== undefined ? item.enrollmentCount : (item.enrollment_count !== undefined ? item.enrollment_count : 0));

    if (!groupedByDate[rawDate]) {
      groupedByDate[rawDate] = { date: rawDate, total: 0, details: [] };
    }
    groupedByDate[rawDate].total += count;
    groupedByDate[rawDate].details.push({ courseName: course, count: count });
  }

  // Convert to sorted array
  const timeline = Object.values(groupedByDate).sort((a, b) => a.date.localeCompare(b.date));
  const maxTimelineCount = timeline.length > 0 ? Math.max(...timeline.map((t: any) => t.total), 1) : 1;

  const topCoursesColumns = [
    {
      title: 'Tên khóa học',
      dataIndex: ['course', 'title'],
      key: 'title',
      render: (text: string) => <span style={{ fontWeight: 600, color: 'var(--primary-color)' }}>{text}</span>,
    },
    {
      title: 'Danh mục',
      dataIndex: ['course', 'category'],
      key: 'category',
      render: (cat: any) => cat?.name || 'N/A',
    },
    {
      title: 'Đánh giá',
      dataIndex: ['course', 'averageRating'],
      key: 'averageRating',
      render: (rating: number) => <Rate disabled allowHalf value={rating} style={{ fontSize: 12, color: '#fadb14' }} />,
    },
    {
      title: 'Học viên',
      dataIndex: ['course', 'totalStudents'],
      key: 'totalStudents',
      render: (val: number) => `👥 ${val || 0}`,
    },
    {
      title: 'Lượt yêu thích',
      dataIndex: 'favoriteCount',
      key: 'favoriteCount',
      render: (val: number) => `❤️ ${val || 0}`,
    },
    {
      title: 'Học phí',
      dataIndex: ['course', 'price'],
      key: 'price',
      render: (price: number) =>
        price === 0
          ? 'Miễn phí'
          : new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price),
    },
  ];

  return (
    <div style={{ padding: '4px 0' }}>
      <Title level={3} style={{ color: 'var(--text-color)', marginBottom: 24 }}>
        Tổng quan giảng viên
      </Title>

      {stats && (
        <>
          {/* Metrics Grid */}
          <Row gutter={[16, 16]}>
            <Col xs={24} sm={12} md={8} lg={6}>
              <Card style={{ textAlign: 'center', padding: '16px' }}>
                <BookOutlined style={{ fontSize: '32px', color: 'var(--primary-color)', marginBottom: '12px' }} />
                <div>
                  <Text style={{ fontSize: '14px', display: 'block', opacity: 0.8 }}>Khóa học đã xuất bản</Text>
                  <Title level={2} style={{ margin: '4px 0', color: 'var(--text-color)' }}>
                    {stats.publishedCourses} / {stats.totalCourses}
                  </Title>
                </div>
              </Card>
            </Col>

            <Col xs={24} sm={12} md={8} lg={6}>
              <Card style={{ textAlign: 'center', padding: '16px' }}>
                <SolutionOutlined style={{ fontSize: '32px', color: 'var(--primary-color)', marginBottom: '12px' }} />
                <div>
                  <Text style={{ fontSize: '14px', display: 'block', opacity: 0.8 }}>Tổng số học viên</Text>
                  <Title level={2} style={{ margin: '4px 0', color: 'var(--text-color)' }}>
                    {stats.totalStudents}
                  </Title>
                </div>
              </Card>
            </Col>

            <Col xs={24} sm={12} md={8} lg={6}>
              <Card style={{ textAlign: 'center', padding: '16px' }}>
                <DollarOutlined style={{ fontSize: '32px', color: '#52c41a', marginBottom: '12px' }} />
                <div>
                  <Text style={{ fontSize: '14px', display: 'block', opacity: 0.8 }}>Tổng doanh thu tích lũy</Text>
                  <Title level={2} style={{ margin: '4px 0', color: '#52c41a', fontWeight: 700 }}>
                    {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(stats.totalRevenue)}
                  </Title>
                </div>
              </Card>
            </Col>

            <Col xs={24} sm={12} md={8} lg={6}>
              <Card style={{ textAlign: 'center', padding: '16px' }}>
                <HeartOutlined style={{ fontSize: '32px', color: '#EF4444', marginBottom: '12px' }} />
                <div>
                  <Text style={{ fontSize: '14px', display: 'block', opacity: 0.8 }}>Tổng lượt yêu thích</Text>
                  <Title level={2} style={{ margin: '4px 0', color: 'var(--text-color)' }}>
                    {stats.totalFavorites || 0}
                  </Title>
                </div>
              </Card>
            </Col>
          </Row>

          <Row gutter={[16, 16]} style={{ marginTop: 20 }}>
            <Col xs={24} sm={12} md={12}>
              <Card style={{ textAlign: 'center', padding: '16px' }}>
                <StarOutlined style={{ fontSize: '32px', color: '#fadb14', marginBottom: '12px' }} />
                <div>
                  <Text style={{ fontSize: '14px', display: 'block', opacity: 0.8 }}>Đánh giá trung bình</Text>
                  <Title level={2} style={{ margin: '4px 0', color: 'var(--text-color)' }}>
                    {stats.averageRating.toFixed(1)} / 5.0
                  </Title>
                </div>
              </Card>
            </Col>

            <Col xs={24} sm={12} md={12}>
              <Card style={{ textAlign: 'center', padding: '16px' }}>
                <CommentOutlined style={{ fontSize: '32px', color: 'var(--primary-color)', marginBottom: '12px' }} />
                <div>
                  <Text style={{ fontSize: '14px', display: 'block', opacity: 0.8 }}>Tổng số nhận xét</Text>
                  <Title level={2} style={{ margin: '4px 0', color: 'var(--text-color)' }}>
                    {stats.totalReviews}
                  </Title>
                </div>
              </Card>
            </Col>
          </Row>

          {/* Timeline and Top Favorite Courses */}
          <Row gutter={[20, 20]} style={{ marginTop: 24 }}>
            <Col xs={24} lg={12}>
              <Card title={<span><RiseOutlined style={{ marginRight: 8, color: 'var(--primary-color)' }} />Xu hướng đăng ký khóa học (Timeline)</span>}>
                {timeline.length === 0 ? (
                  <Empty description="Chưa có dữ liệu timeline." />
                ) : (
                  <div style={{ display: 'flex', flexDirection: 'column', height: 260, justifyContent: 'space-between', padding: '10px 0' }}>
                    <div style={{ display: 'flex', alignItems: 'flex-end', height: 200, gap: 12, borderBottom: '2px solid #E2E8F0', paddingBottom: 8 }}>
                      {timeline.map((item: any, idx: number) => {
                        const heightPercent = (item.total / maxTimelineCount) * 100;
                        const tooltipTitle = (
                          <div style={{ padding: '4px' }}>
                            <div style={{ fontWeight: 'bold' }}>{item.date.split('-').reverse().join('/')}</div>
                            <div style={{ height: 8 }} />
                            {item.details.map((detail: any, dIdx: number) => (
                              <div key={dIdx}>* {detail.courseName}: {detail.count} học viên</div>
                            ))}
                          </div>
                        );
                        return (
                          <Tooltip key={idx} title={tooltipTitle}>
                            <div style={{ flex: 1, display: 'flex', flexDirection: 'column', justifyContent: 'flex-end', alignItems: 'center', cursor: 'pointer', height: '100%' }}>
                              <div
                                style={{
                                  width: '100%',
                                  minWidth: 16,
                                  height: `${Math.max(heightPercent, 5)}%`,
                                  background: 'var(--primary-color)',
                                  borderRadius: '4px 4px 0 0',
                                  transition: 'background 0.2s',
                                }}
                                onMouseEnter={(e) => { e.currentTarget.style.background = '#1D4ED8'; }}
                                onMouseLeave={(e) => { e.currentTarget.style.background = 'var(--primary-color)'; }}
                              />
                            </div>
                          </Tooltip>
                        );
                      })}
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 11, color: 'var(--text-muted)' }}>
                      <span>{timeline[0]?.date ? timeline[0].date.split('-').reverse().join('/') : ''}</span>
                      <span>{timeline[Math.floor(timeline.length / 2)]?.date ? timeline[Math.floor(timeline.length / 2)].date.split('-').reverse().join('/') : ''}</span>
                      <span>{timeline[timeline.length - 1]?.date ? timeline[timeline.length - 1].date.split('-').reverse().join('/') : ''}</span>
                    </div>
                  </div>
                )}
              </Card>
            </Col>

            <Col xs={24} lg={12}>
              <Card title={<span><HeartOutlined style={{ marginRight: 8, color: '#EF4444' }} />Khóa học được yêu thích nhất</span>}>
                {stats.topFavoriteCourses?.length === 0 ? (
                  <Empty description="Chưa có khóa học nào được lưu yêu thích." />
                ) : (
                  <Table
                    columns={topCoursesColumns}
                    dataSource={stats.topFavoriteCourses}
                    rowKey="id"
                    pagination={false}
                    size="small"
                  />
                )}
              </Card>
            </Col>
          </Row>
        </>
      )}
    </div>
  );
};

export default InstructorDashboardPage;
