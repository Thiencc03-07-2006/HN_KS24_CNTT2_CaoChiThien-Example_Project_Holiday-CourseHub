import React, { useEffect, useState } from 'react';
import { Typography, Space, Tag, message, Tabs, Card, Form, Select, Input, Modal, Descriptions } from 'antd';
import { reportService } from '../services/reportService';
import type { ReportResponse } from '../services/reportService';
import { Table } from '../components/common/UI/Table';
import { Button } from '../components/common/UI/Button';
import { Loading } from '../components/common/UI/Loading';
import { EmptyState } from '../components/common/UI/EmptyState';
import { WarningOutlined, CommentOutlined, SearchOutlined, CheckCircleOutlined, DeleteOutlined } from '@ant-design/icons';

const { Title, Paragraph } = Typography;
const { TabPane } = Tabs;
const { TextArea } = Input;

const AdminReportsPage: React.FC = () => {
  const [courseReports, setCourseReports] = useState<ReportResponse[]>([]);
  const [commentReports, setCommentReports] = useState<ReportResponse[]>([]);
  const [loading, setLoading] = useState(true);

  // Filters state
  const [courseStatusFilter, setCourseStatusFilter] = useState<string>('ALL');
  const [courseReasonFilter, setCourseReasonFilter] = useState<string>('ALL');
  const [courseSearch, setCourseSearch] = useState<string>('');

  const [commentStatusFilter, setCommentStatusFilter] = useState<string>('ALL');
  const [commentReasonFilter, setCommentReasonFilter] = useState<string>('ALL');
  const [commentSearch, setCommentSearch] = useState<string>('');

  // Modals state
  const [detailReport, setDetailReport] = useState<ReportResponse | null>(null);
  const [statusReport, setStatusReport] = useState<ReportResponse | null>(null);
  const [statusForm] = Form.useForm();
  const [submittingStatus, setSubmittingStatus] = useState(false);

  const fetchReports = async () => {
    setLoading(true);
    try {
      const [coursesRes, commentsRes] = await Promise.all([
        reportService.getAdminCourseReports(),
        reportService.getAdminCommentAndReviewReports(),
      ]);
      if (coursesRes?.success && coursesRes?.data) {
        setCourseReports(coursesRes.data);
      }
      if (commentsRes?.success && commentsRes?.data) {
        setCommentReports(commentsRes.data);
      }
    } catch (err) {
      message.error('Không thể tải danh sách báo cáo vi phạm.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReports();
  }, []);

  const handleUpdateStatusSubmit = async (values: { status: string; adminNote: string }) => {
    if (!statusReport) return;
    setSubmittingStatus(true);
    try {
      if (statusReport.reportableType === 'COURSE') {
        await reportService.updateCourseReportStatus(statusReport.id, values.status, values.adminNote);
      } else {
        await reportService.updateCommentOrReviewReportStatus(statusReport.id, values.status, values.adminNote);
      }
      message.success('Cập nhật trạng thái xử lý báo cáo thành công.');
      setStatusReport(null);
      statusForm.resetFields();
      fetchReports();
    } catch (err) {
      message.error('Cập nhật trạng thái xử lý báo cáo thất bại.');
    } finally {
      setSubmittingStatus(false);
    }
  };

  const handleDeleteReport = async (report: ReportResponse) => {
    Modal.confirm({
      title: 'Xác nhận xóa báo cáo',
      content: 'Bạn có chắc chắn muốn xóa bản ghi báo cáo này khỏi hệ thống không?',
      okText: 'Xóa',
      okType: 'danger',
      cancelText: 'Hủy',
      onOk: async () => {
        try {
          if (report.reportableType === 'COURSE') {
            await reportService.deleteCourseReport(report.id);
          } else {
            await reportService.deleteCommentOrReviewReport(report.id);
          }
          message.success('Đã xóa bản ghi báo cáo thành công.');
          fetchReports();
        } catch (err) {
          message.error('Xóa bản ghi báo cáo thất bại.');
        }
      },
    });
  };

  const getStatusTag = (status: string) => {
    switch (status) {
      case 'PENDING':
        return <Tag color="warning">Chờ xử lý</Tag>;
      case 'REVIEWING':
        return <Tag color="processing">Đang xem xét</Tag>;
      case 'RESOLVED':
        return <Tag color="success">Đã giải quyết</Tag>;
      case 'REJECTED':
        return <Tag color="error">Đã từ chối</Tag>;
      case 'AUTO_ESCALATED':
        return <Tag color="magenta">Auto Escalated</Tag>;
      default:
        return <Tag>{status}</Tag>;
    }
  };

  const filterReports = (reports: ReportResponse[], statusFilter: string, reasonFilter: string, search: string) => {
    return reports.filter(r => {
      const matchesStatus = statusFilter === 'ALL' || r.status === statusFilter;
      const matchesReason = reasonFilter === 'ALL' || r.reason === reasonFilter;
      const matchesSearch = !search ||
        r.reporterName.toLowerCase().includes(search.toLowerCase()) ||
        (r.targetTitle && r.targetTitle.toLowerCase().includes(search.toLowerCase())) ||
        (r.description && r.description.toLowerCase().includes(search.toLowerCase()));
      return matchesStatus && matchesReason && matchesSearch;
    });
  };

  const filteredCourseReports = filterReports(courseReports, courseStatusFilter, courseReasonFilter, courseSearch);
  const filteredCommentReports = filterReports(commentReports, commentStatusFilter, commentReasonFilter, commentSearch);

  const courseColumns = [
    {
      title: 'Khóa học bị báo cáo',
      dataIndex: 'targetTitle',
      key: 'targetTitle',
      render: (text: string) => <span style={{ fontWeight: 600, color: 'var(--primary-color)' }}>{text}</span>,
    },
    {
      title: 'Người báo cáo',
      dataIndex: 'reporterName',
      key: 'reporterName',
    },
    {
      title: 'Lý do',
      dataIndex: 'reason',
      key: 'reason',
      render: (reason: string) => <Tag color="red">{reason}</Tag>,
    },
    {
      title: 'Mô tả chi tiết',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => getStatusTag(status),
    },
    {
      title: 'Hành động',
      key: 'actions',
      render: (_: any, record: ReportResponse) => (
        <Space>
          <Button type="default" size="small" onClick={() => setDetailReport(record)}>
            Chi tiết
          </Button>
          <Button type="primary" size="small" onClick={() => {
            setStatusReport(record);
            statusForm.setFieldsValue({ status: record.status, adminNote: record.adminNote || '' });
          }}>
            Xử lý
          </Button>
          <Button type="default" size="small" danger onClick={() => handleDeleteReport(record)}>
            <DeleteOutlined />
          </Button>
        </Space>
      ),
    },
  ];

  const commentColumns = [
    {
      title: 'Nội dung bị báo cáo',
      dataIndex: 'targetTitle',
      key: 'targetTitle',
      ellipsis: true,
      render: (text: string) => <span style={{ fontStyle: 'italic' }}>"{text}"</span>,
    },
    {
      title: 'Loại',
      dataIndex: 'reportableType',
      key: 'reportableType',
      render: (type: string) => <Tag color={type === 'REVIEW' ? 'purple' : 'cyan'}>{type === 'REVIEW' ? 'Đánh giá' : 'Bình luận'}</Tag>,
    },
    {
      title: 'Người báo cáo',
      dataIndex: 'reporterName',
      key: 'reporterName',
    },
    {
      title: 'Lý do',
      dataIndex: 'reason',
      key: 'reason',
      render: (reason: string) => <Tag color="volcano">{reason}</Tag>,
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => getStatusTag(status),
    },
    {
      title: 'Hành động',
      key: 'actions',
      render: (_: any, record: ReportResponse) => (
        <Space>
          <Button type="default" size="small" onClick={() => setDetailReport(record)}>
            Chi tiết
          </Button>
          <Button type="primary" size="small" onClick={() => {
            setStatusReport(record);
            statusForm.setFieldsValue({ status: record.status, adminNote: record.adminNote || '' });
          }}>
            Xử lý
          </Button>
          <Button type="default" size="small" danger onClick={() => handleDeleteReport(record)}>
            <DeleteOutlined />
          </Button>
        </Space>
      ),
    },
  ];

  const reasons = ['Spam', 'Thông tin sai', 'Lừa đảo', 'Nội dung phản cảm', 'Vi phạm bản quyền', 'Khác'];

  if (loading) {
    return <Loading message="Đang tải danh sách báo cáo vi phạm..." />;
  }

  return (
    <div style={{ padding: '4px 0' }}>
      <Title level={3} style={{ color: 'var(--text-color)', marginBottom: 24 }}>
        Báo cáo vi phạm (Content Moderation)
      </Title>

      <Tabs defaultActiveKey="courses" type="card">
        <TabPane
          tab={
            <span>
              <WarningOutlined />
              Báo cáo khóa học ({courseReports.length})
            </span>
          }
          key="courses"
        >
          <Card style={{ marginBottom: 16 }}>
            <Space wrap size="middle">
              <Input
                placeholder="Tìm người báo cáo, khóa học..."
                value={courseSearch}
                onChange={e => setCourseSearch(e.target.value)}
                prefix={<SearchOutlined />}
                style={{ width: 250 }}
              />
              <Select
                value={courseStatusFilter}
                onChange={setCourseStatusFilter}
                style={{ width: 150 }}
                placeholder="Trạng thái"
              >
                <Select.Option value="ALL">Tất cả trạng thái</Select.Option>
                <Select.Option value="PENDING">Chờ xử lý</Select.Option>
                <Select.Option value="REVIEWING">Đang xem xét</Select.Option>
                <Select.Option value="RESOLVED">Đã giải quyết</Select.Option>
                <Select.Option value="REJECTED">Đã từ chối</Select.Option>
              </Select>

              <Select
                value={courseReasonFilter}
                onChange={setCourseReasonFilter}
                style={{ width: 180 }}
                placeholder="Lý do"
              >
                <Select.Option value="ALL">Tất cả lý do</Select.Option>
                {reasons.map(r => (
                  <Select.Option key={r} value={r}>
                    {r}
                  </Select.Option>
                ))}
              </Select>
            </Space>
          </Card>

          {filteredCourseReports.length === 0 ? (
            <EmptyState description="Không tìm thấy báo cáo khóa học nào." />
          ) : (
            <Table columns={courseColumns} dataSource={filteredCourseReports} rowKey="id" />
          )}
        </TabPane>

        <TabPane
          tab={
            <span>
              <CommentOutlined />
              Báo cáo Bình luận/Đánh giá ({commentReports.length})
            </span>
          }
          key="comments"
        >
          <Card style={{ marginBottom: 16 }}>
            <Space wrap size="middle">
              <Input
                placeholder="Tìm nội dung, người báo cáo..."
                value={commentSearch}
                onChange={e => setCommentSearch(e.target.value)}
                prefix={<SearchOutlined />}
                style={{ width: 250 }}
              />
              <Select
                value={commentStatusFilter}
                onChange={setCommentStatusFilter}
                style={{ width: 150 }}
                placeholder="Trạng thái"
              >
                <Select.Option value="ALL">Tất cả trạng thái</Select.Option>
                <Select.Option value="PENDING">Chờ xử lý</Select.Option>
                <Select.Option value="REVIEWING">Đang xem xét</Select.Option>
                <Select.Option value="RESOLVED">Đã giải quyết</Select.Option>
                <Select.Option value="REJECTED">Đã từ chối</Select.Option>
              </Select>

              <Select
                value={commentReasonFilter}
                onChange={setCommentReasonFilter}
                style={{ width: 180 }}
                placeholder="Lý do"
              >
                <Select.Option value="ALL">Tất cả lý do</Select.Option>
                {reasons.map(r => (
                  <Select.Option key={r} value={r}>
                    {r}
                  </Select.Option>
                ))}
              </Select>
            </Space>
          </Card>

          {filteredCommentReports.length === 0 ? (
            <EmptyState description="Không tìm thấy báo cáo bình luận hoặc đánh giá nào." />
          ) : (
            <Table columns={commentColumns} dataSource={filteredCommentReports} rowKey="id" />
          )}
        </TabPane>
      </Tabs>

      {/* Report Details Modal */}
      <Modal
        title="Chi tiết báo cáo vi phạm"
        open={detailReport !== null}
        onCancel={() => setDetailReport(null)}
        footer={[
          <Button key="close" type="default" onClick={() => setDetailReport(null)}>
            Đóng
          </Button>,
        ]}
        width={700}
      >
        {detailReport && (
          <Descriptions bordered column={1} size="small" style={{ marginTop: 16 }}>
            <Descriptions.Item label="Mã báo cáo">{detailReport.id}</Descriptions.Item>
            <Descriptions.Item label="Loại đối tượng">{detailReport.reportableType}</Descriptions.Item>
            <Descriptions.Item label="Mã đối tượng bị báo cáo">{detailReport.reportableId}</Descriptions.Item>
            <Descriptions.Item label="Nội dung/Khóa học">{detailReport.targetTitle}</Descriptions.Item>
            <Descriptions.Item label="Người gửi báo cáo">{detailReport.reporterName}</Descriptions.Item>
            <Descriptions.Item label="Lý do">
              <Tag color="red">{detailReport.reason}</Tag>
            </Descriptions.Item>
            <Descriptions.Item label="Mô tả của người báo cáo">
              <Paragraph style={{ whiteSpace: 'pre-line' }}>{detailReport.description || '(Không có mô tả)'}</Paragraph>
            </Descriptions.Item>
            <Descriptions.Item label="Trạng thái">{getStatusTag(detailReport.status)}</Descriptions.Item>
            <Descriptions.Item label="Thời gian báo cáo">
              {new Date(detailReport.createdAt).toLocaleString()}
            </Descriptions.Item>
            {detailReport.adminNote && (
              <Descriptions.Item label="Ghi chú xử lý (Admin)">
                <Paragraph style={{ fontStyle: 'italic', color: '#1E3A8A' }}>{detailReport.adminNote}</Paragraph>
              </Descriptions.Item>
            )}
          </Descriptions>
        )}
      </Modal>

      {/* Update Status Modal */}
      <Modal
        title="Xử lý báo cáo vi phạm"
        open={statusReport !== null}
        onCancel={() => setStatusReport(null)}
        footer={null}
      >
        {statusReport && (
          <Form
            form={statusForm}
            layout="vertical"
            onFinish={handleUpdateStatusSubmit}
            style={{ marginTop: 16 }}
            noValidate
          >
            <Form.Item
              name="status"
              label="Trạng thái xử lý"
              rules={[{ required: true, message: 'Trường này là bắt buộc' }]}
            >
              <Select>
                <Select.Option value="PENDING">Chờ xử lý (Pending)</Select.Option>
                <Select.Option value="REVIEWING">Đang xem xét (Reviewing)</Select.Option>
                <Select.Option value="RESOLVED">Đã giải quyết (Resolved)</Select.Option>
                <Select.Option value="REJECTED">Từ chối xử lý (Rejected)</Select.Option>
              </Select>
            </Form.Item>

            <Form.Item
              name="adminNote"
              label="Ghi chú của quản trị viên"
              rules={[{ required: true, message: 'Trường này là bắt buộc' }]}
            >
              <TextArea rows={4} placeholder="Nhập lý do giải quyết, từ chối hoặc kết quả xử lý vi phạm..." />
            </Form.Item>


            <Form.Item style={{ textAlign: 'right', marginBottom: 0 }}>
              <Space>
                <Button type="default" onClick={() => setStatusReport(null)}>
                  Hủy
                </Button>
                <Button type="primary" htmlType="submit" loading={submittingStatus} icon={<CheckCircleOutlined />}>
                  Cập nhật
                </Button>
              </Space>
            </Form.Item>
          </Form>
        )}
      </Modal>
    </div>
  );
};

export default AdminReportsPage;
