import React, { useEffect, useState, useRef } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { Typography, Tabs, Form, message, Tag, List, Space, Progress, Checkbox, Radio, Alert, Breadcrumb, Avatar, Modal, Select, Popconfirm } from 'antd';
import {
  PlayCircleOutlined,
  BookOutlined,
  CheckCircleOutlined,
  FileTextOutlined,
  DeleteOutlined,
  CompassOutlined,
  ArrowLeftOutlined,
  ClockCircleOutlined,
  ZoomInOutlined,
  ZoomOutOutlined,
  DownloadOutlined,
  SafetyCertificateOutlined,
  CloseCircleOutlined,
  HomeOutlined,
  UserOutlined,
  TrophyOutlined,
  FlagFilled,
  MessageOutlined,
  EditOutlined,
  FlagOutlined,
} from '@ant-design/icons';
import axiosInstance from '../api/axiosInstance';
import { Footer } from '../components/common/Layout/Footer';
import { Button } from '../components/common/UI/Button';
import { Input } from '../components/common/UI/Input';
import { commentService } from '../services/commentService';
import { reportService } from '../services/reportService';
import { Loading } from '../components/common/UI/Loading';
import { useAuth } from '../context/AuthContext';

const { Title, Text, Paragraph } = Typography;

const LearningPage: React.FC = () => {
  const { courseId } = useParams<{ courseId: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();

  const [course, setCourse] = useState<any>(null);
  const [chapters, setChapters] = useState<any[]>([]);
  const [activeLesson, setActiveLesson] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [notes, setNotes] = useState<any[]>([]);
  const [noteContent, setNoteContent] = useState('');
  const [progressPercent, setProgressPercent] = useState<number>(0);

  // Lesson Comments State
  const [comments, setComments] = useState<any[]>([]);
  const [commentContent, setCommentContent] = useState('');
  const [commentsLoading, setCommentsLoading] = useState(false);
  const [replyingToId, setReplyingToId] = useState<string | null>(null);
  const [replyContent, setReplyContent] = useState('');
  const [editingCommentId, setEditingCommentId] = useState<string | null>(null);
  const [editingContent, setEditingContent] = useState('');

  // Comment Report Modal State
  const [reportModalOpen, setReportModalOpen] = useState(false);
  const [reportingComment, setReportingComment] = useState<any | null>(null);
  const [reportReason, setReportReason] = useState('Spam');
  const [reportDescription, setReportDescription] = useState('');
  const [submittingReport, setSubmittingReport] = useState(false);

  // Course Report Modal State
  const [courseReportModalOpen, setCourseReportModalOpen] = useState(false);
  const [courseReportReason, setCourseReportReason] = useState('Spam');
  const [courseReportDescription, setCourseReportDescription] = useState('');
  const [submittingCourseReport, setSubmittingCourseReport] = useState(false);
  const [reportDescriptionError, setReportDescriptionError] = useState('');
  const [courseReportDescriptionError, setCourseReportDescriptionError] = useState('');
  const [pdfZoom, setPdfZoom] = useState<number>(100);

  const [expandedChapters, setExpandedChapters] = useState<Set<string>>(new Set());

  // Inline Quiz state
  const [quizLoading, setQuizLoading] = useState(false);
  const [quizConfig, setQuizConfig] = useState<any>(null);
  const [quizQuestions, setQuizQuestions] = useState<any[]>([]);
  const [quizAttempt, setQuizAttempt] = useState<any>(null);
  const [quizSelectedAnswers, setQuizSelectedAnswers] = useState<{ [questionId: string]: string[] }>({});
  const [quizResult, setQuizResult] = useState<any>(null);
  const [quizTimeLeft, setQuizTimeLeft] = useState<number | null>(null);

  const videoRef = useRef<HTMLVideoElement>(null);

  const fetchLearningData = async () => {
    try {
      setLoading(true);
      const res = await axiosInstance.get(`/learning/courses/${courseId}`);
      if (res.data?.success && res.data?.data) {
        const data = res.data.data;
        setCourse(data.course);
        setChapters(data.chapters || []);
        calculateProgress(data.chapters || []);
        fetchNotes();
        // Expand all chapters by default
        const allIds = new Set<string>((data.chapters || []).map((ch: any) => ch.id));
        setExpandedChapters(allIds);
      }
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Không thể tải nội dung học tập.');
      navigate('/courses');
    } finally {
      setLoading(false);
    }
  };

  const calculateProgress = (chapterList: any[]) => {
    let total = 0;
    let completed = 0;
    chapterList.forEach((ch: any) => {
      ch.lessons?.forEach((les: any) => {
        total++;
        if (les.isCompleted) completed++;
      });
    });
    setProgressPercent(total > 0 ? Math.round((completed / total) * 100) : 0);
  };

  const fetchNotes = async () => {
    try {
      const res = await axiosInstance.get(`/notes/course/${courseId}`);
      if (res.data?.success) setNotes(res.data.data || []);
    } catch (e) {
      console.warn('Failed to load notes');
    }
  };

  const fetchComments = async (lessonId: string) => {
    setCommentsLoading(true);
    try {
      const res = await commentService.getComments(lessonId);
      if (res?.success && res.data) {
        setComments(res.data);
      }
    } catch (e) {
      console.warn('Failed to load comments');
    } finally {
      setCommentsLoading(false);
    }
  };

  useEffect(() => {
    if (activeLesson?.id) {
      fetchComments(activeLesson.id);
    }
  }, [activeLesson?.id]);

  const handleAddComment = async () => {
    if (!activeLesson || !commentContent.trim()) return;
    try {
      const res = await commentService.addComment(activeLesson.id, commentContent.trim());
      if (res?.success) {
        message.success('Đăng câu hỏi/thảo luận thành công!');
        setCommentContent('');
        fetchComments(activeLesson.id);
      }
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Đăng bình luận thất bại.');
    }
  };

  const handleReplyComment = async (parentId: string) => {
    if (!activeLesson || !replyContent.trim()) return;
    try {
      const res = await commentService.addComment(activeLesson.id, replyContent.trim(), parentId);
      if (res?.success) {
        message.success('Phản hồi câu hỏi thành công!');
        setReplyContent('');
        setReplyingToId(null);
        fetchComments(activeLesson.id);
      }
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Phản hồi thất bại.');
    }
  };

  const handleUpdateComment = async (commentId: string) => {
    if (!activeLesson || !editingContent.trim()) return;
    try {
      const res = await commentService.updateComment(commentId, editingContent.trim());
      if (res?.success) {
        message.success('Cập nhật bình luận thành công!');
        setEditingContent('');
        setEditingCommentId(null);
        fetchComments(activeLesson.id);
      }
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Cập nhật thất bại.');
    }
  };

  const handleDeleteComment = async (commentId: string) => {
    if (!activeLesson) return;
    try {
      const res = await commentService.deleteComment(commentId);
      if (res?.success) {
        message.success('Xóa bình luận thành công!');
        fetchComments(activeLesson.id);
      }
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Xóa bình luận thất bại.');
    }
  };

  const handleOpenReportModal = (commentItem: any) => {
    setReportingComment(commentItem);
    setReportReason('Spam');
    setReportDescription('');
    setReportDescriptionError('');
    setReportModalOpen(true);
  };

  const handleReportSubmit = async () => {
    if (!reportingComment) return;
    const isOther = reportReason === 'Khác';
    if (isOther && reportDescription.trim().length === 0) {
      setReportDescriptionError('Lý do báo cáo không được để trống');
      return;
    }
    setReportDescriptionError('');
    setSubmittingReport(true);
    try {
      await reportService.reportComment(reportingComment.id, reportReason, reportDescription);
      message.success('Báo cáo bình luận vi phạm thành công. Báo cáo của bạn đang chờ xử lý.');
      setReportModalOpen(false);
    } catch (err: any) {
      const errMsg = err?.response?.data?.message || 'Gửi báo cáo thất bại.';
      message.error(errMsg);
    } finally {
      setSubmittingReport(false);
    }
  };


  const handleOpenCourseReportModal = () => {
    setCourseReportReason('Spam');
    setCourseReportDescription('');
    setCourseReportDescriptionError('');
    setCourseReportModalOpen(true);
  };

  const handleCourseReportSubmit = async () => {
    const isOther = courseReportReason === 'Khác';
    if (isOther && courseReportDescription.trim().length === 0) {
      setCourseReportDescriptionError('Lý do báo cáo không được để trống');
      return;
    }
    setCourseReportDescriptionError('');
    setSubmittingCourseReport(true);
    try {
      await reportService.reportCourse(courseId!, courseReportReason, courseReportDescription);
      message.success('Báo cáo khóa học vi phạm thành công. Báo cáo của bạn đang chờ xử lý.');
      setCourseReportModalOpen(false);
    } catch (err: any) {
      const errMsg = err?.response?.data?.message || 'Gửi báo cáo thất bại.';
      message.error(errMsg);
    } finally {
      setSubmittingCourseReport(false);
    }
  };


  useEffect(() => {
    if (courseId) fetchLearningData();
  }, [courseId]);

  // Video URL Parser
  const renderVideoPlayer = (url: string) => {
    if (!url) return <Text type="secondary">Chưa thiết lập liên kết video.</Text>;

    const ytReg = /(?:youtube\.com\/(?:[^\/]+\/.+\/|(?:v|e(?:mbed)?)\/|.*[?&]v=)|youtu\.be\/)([^"&?\/\s]{11})/;
    const ytMatch = url.match(ytReg);
    if (ytMatch && ytMatch[1]) {
      return (
        <div style={{ position: 'relative', paddingBottom: '56.25%', height: 0 }}>
          <iframe
            style={{ position: 'absolute', top: 0, left: 0, width: '100%', height: '100%', borderRadius: '8px', border: 'none' }}
            src={`https://www.youtube.com/embed/${ytMatch[1]}`}
            title="YouTube Video Player"
            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
            allowFullScreen
          />
        </div>
      );
    }

    const vimeoReg = /vimeo\.com\/(?:video\/)?([0-9]+)/;
    const vimeoMatch = url.match(vimeoReg);
    if (vimeoMatch && vimeoMatch[1]) {
      return (
        <div style={{ position: 'relative', paddingBottom: '56.25%', height: 0 }}>
          <iframe
            style={{ position: 'absolute', top: 0, left: 0, width: '100%', height: '100%', borderRadius: '8px', border: 'none' }}
            src={`https://player.vimeo.com/video/${vimeoMatch[1]}`}
            allow="autoplay; fullscreen; picture-in-picture"
            allowFullScreen
            title="Vimeo Video Player"
          />
        </div>
      );
    }

    return (
      <video
        key={url}
        ref={videoRef}
        src={url}
        controls
        onEnded={handleAutoMarkComplete}
        style={{ width: '100%', aspectRatio: '16/9', display: 'block', borderRadius: '8px', background: '#000000' }}
      />
    );
  };

  const handleAutoMarkComplete = () => {
    if (activeLesson && !activeLesson.isCompleted) handleMarkComplete();
  };

  const handleMarkComplete = async () => {
    if (!activeLesson) return;
    try {
      const res = await axiosInstance.post('/progress', {
        courseId: courseId,
        lessonId: activeLesson.id
      });
      if (res.data?.success) {
        message.success('Đã cập nhật tiến độ bài học!');
        const updatedRes = await axiosInstance.get(`/learning/courses/${courseId}`);
        if (updatedRes.data?.success) {
          const freshChapters = updatedRes.data.data.chapters || [];
          setChapters(freshChapters);
          calculateProgress(freshChapters);
          const found = freshChapters.flatMap((c: any) => c.lessons || []).find((l: any) => l.id === activeLesson.id);
          if (found) setActiveLesson(found);
        }
      }
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Không thể cập nhật tiến độ.');
    }
  };

  const handleStartQuiz = async () => {
    if (!activeLesson) return;
    try {
      setQuizLoading(true);
      setQuizResult(null);
      setQuizSelectedAnswers({});

      const configRes = await axiosInstance.get(`/quiz/${activeLesson.id}/config`);
      if (configRes.data?.success) setQuizConfig(configRes.data.data);

      const attemptRes = await axiosInstance.post(`/quiz/${activeLesson.id}/attempts`);
      if (attemptRes.data?.success) {
        const attemptData = attemptRes.data.data;
        setQuizAttempt(attemptData);

        const questionsRes = await axiosInstance.get(`/quiz/${activeLesson.id}/questions`);
        if (questionsRes.data?.success) setQuizQuestions(questionsRes.data.data || []);

        if (configRes.data.data?.timeLimitMinutes) {
          const startTime = new Date(attemptData.startedAt).getTime();
          const limitMs = configRes.data.data.timeLimitMinutes * 60 * 1000;
          const elapsed = Date.now() - startTime;
          setQuizTimeLeft(Math.max(0, Math.floor((limitMs - elapsed) / 1000)));
        } else {
          setQuizTimeLeft(null);
        }

        if (attemptData.status === 'PASSED' || attemptData.status === 'FAILED') {
          setQuizResult(attemptData);
        }
      }
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Không thể bắt đầu bài trắc nghiệm.');
    } finally {
      setQuizLoading(false);
    }
  };

  useEffect(() => {
    if (quizTimeLeft === null || quizResult) return;
    if (quizTimeLeft <= 0) {
      message.warning('Hết thời gian làm bài! Tự động nộp bài...');
      handleSubmitQuiz();
      return;
    }
    const timer = setTimeout(() => setQuizTimeLeft(quizTimeLeft - 1), 1000);
    return () => clearTimeout(timer);
  }, [quizTimeLeft, quizResult]);

  const handleSelectQuizAnswer = (questionId: string, answerId: string, isMultiple: boolean) => {
    const selections = quizSelectedAnswers[questionId] || [];
    if (isMultiple) {
      setQuizSelectedAnswers({
        ...quizSelectedAnswers,
        [questionId]: selections.includes(answerId) ? selections.filter(id => id !== answerId) : [...selections, answerId]
      });
    } else {
      setQuizSelectedAnswers({ ...quizSelectedAnswers, [questionId]: [answerId] });
    }
  };

  const handleSubmitQuiz = async () => {
    if (!quizAttempt) return;
    try {
      setQuizLoading(true);
      const res = await axiosInstance.post(`/quiz/${activeLesson.id}/attempts/${quizAttempt.id}/submit`, {
        selectedAnswers: quizSelectedAnswers
      });
      if (res.data?.success) {
        setQuizResult(res.data.data);
        message.success('Đã nộp bài trắc nghiệm thành công!');
        const updatedRes = await axiosInstance.get(`/learning/courses/${courseId}`);
        if (updatedRes.data?.success) {
          setChapters(updatedRes.data.data.chapters || []);
          calculateProgress(updatedRes.data.data.chapters || []);
        }
        const questionsRes = await axiosInstance.get(`/quiz/${activeLesson.id}/questions`);
        if (questionsRes.data?.success) setQuizQuestions(questionsRes.data.data || []);
      }
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Không thể nộp bài trắc nghiệm.');
    } finally {
      setQuizLoading(false);
    }
  };

  const formatTimer = (seconds: number) => {
    const minutes = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${minutes}:${secs < 10 ? '0' : ''}${secs}`;
  };

  const handleAddNote = async () => {
    if (!noteContent.trim()) return;
    try {
      const res = await axiosInstance.post('/notes', {
        courseId: courseId,
        lessonId: activeLesson?.id || null,
        content: noteContent
      });
      if (res.data?.success) {
        message.success('Đã thêm ghi chú thành công!');
        setNoteContent('');
        fetchNotes();
      }
    } catch (e) {
      message.error('Không thể thêm ghi chú.');
    }
  };

  const handleDeleteNote = async (id: string) => {
    try {
      const res = await axiosInstance.delete(`/notes/${id}`);
      if (res.data?.success) {
        message.success('Đã xóa ghi chú.');
        fetchNotes();
      }
    } catch (e) {
      message.error('Không thể xóa ghi chú.');
    }
  };

  const toggleChapter = (chapId: string) => {
    setExpandedChapters(prev => {
      const next = new Set(prev);
      if (next.has(chapId)) next.delete(chapId);
      else next.add(chapId);
      return next;
    });
  };

  if (loading) {
    return <Loading message="Đang chuẩn bị không gian học tập..." />;
  }

  const renderCommentNode = (item: any, depth: number = 0) => {
    const isOwner = user && item.userId === user.id;
    const isReplying = replyingToId === item.id;
    const isEditing = editingCommentId === item.id;

    return (
      <div 
        key={item.id} 
        style={{ 
          padding: '12px 0', 
          borderBottom: depth === 0 ? '1px solid #f0f0f0' : 'none',
          marginLeft: depth > 0 ? 24 : 0,
          borderLeft: depth > 0 ? '2px solid #e8e8e8' : 'none',
          paddingLeft: depth > 0 ? 16 : 0
        }}
      >
        <div style={{ display: 'flex', gap: 12, alignItems: 'flex-start' }}>
          <Avatar src={item.userAvatar} icon={<UserOutlined />} size={depth > 0 ? 'small' : 'default'} />
          <div style={{ flex: 1 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <Text strong style={{ fontSize: 13 }}>{item.userName}</Text>
              <Text type="secondary" style={{ fontSize: 11 }}>
                {new Date(item.createdAt).toLocaleString('vi-VN')}
              </Text>
            </div>

            {isEditing ? (
              <div style={{ marginTop: 8 }}>
                <Input.TextArea
                  rows={2}
                  value={editingContent}
                  onChange={(e) => setEditingContent(e.target.value)}
                  style={{ marginBottom: 8 }}
                />
                <Space>
                  <Button 
                    type="primary" 
                    size="small" 
                    onClick={() => handleUpdateComment(item.id)}
                    disabled={!editingContent.trim()}
                  >
                    Lưu
                  </Button>
                  <Button 
                    size="small" 
                    onClick={() => {
                      setEditingCommentId(null);
                      setEditingContent('');
                    }}
                  >
                    Hủy
                  </Button>
                </Space>
              </div>
            ) : (
              <Paragraph style={{ margin: '4px 0', color: 'var(--text-body)', fontSize: 14 }}>
                {item.content}
              </Paragraph>
            )}

            {/* Comment Actions */}
            {!isEditing && (
              <Space size="middle" style={{ marginTop: 4, display: 'flex' }}>
                {depth < 3 && (
                  <Button
                    type="link"
                    size="small"
                    icon={<MessageOutlined />}
                    onClick={() => {
                      setReplyingToId(item.id);
                      setReplyContent('');
                    }}
                    style={{ padding: 0, fontSize: 12, color: 'var(--primary)' }}
                  >
                    Trả lời
                  </Button>
                )}
                {isOwner && (
                  <>
                    <Button
                      type="link"
                      size="small"
                      icon={<EditOutlined />}
                      onClick={() => {
                        setEditingCommentId(item.id);
                        setEditingContent(item.content);
                      }}
                      style={{ padding: 0, fontSize: 12, color: '#fa8c16' }}
                    >
                      Sửa
                    </Button>
                    <Popconfirm
                      title="Bạn có chắc chắn muốn xóa bình luận này?"
                      onConfirm={() => handleDeleteComment(item.id)}
                      okText="Xóa"
                      cancelText="Hủy"
                    >
                      <Button
                        type="link"
                        danger
                        size="small"
                        icon={<DeleteOutlined />}
                        style={{ padding: 0, fontSize: 12 }}
                      >
                        Xóa
                      </Button>
                    </Popconfirm>
                  </>
                )}
                {user && !isOwner && (
                  <Button
                    type="link"
                    danger
                    size="small"
                    icon={<FlagOutlined />}
                    onClick={() => handleOpenReportModal(item)}
                    style={{ padding: 0, fontSize: 12 }}
                  >
                    Báo cáo
                  </Button>
                )}
              </Space>
            )}

            {/* Reply Input Form */}
            {isReplying && (
              <div style={{ marginTop: 12, marginBottom: 12 }}>
                <Input.TextArea
                  rows={2}
                  placeholder={`Phản hồi bình luận của ${item.userName}...`}
                  value={replyContent}
                  onChange={(e) => setReplyContent(e.target.value)}
                  style={{ marginBottom: 8 }}
                />
                <Space>
                  <Button 
                    type="primary" 
                    size="small" 
                    onClick={() => handleReplyComment(item.id)}
                    disabled={!replyContent.trim()}
                  >
                    Gửi phản hồi
                  </Button>
                  <Button 
                    size="small" 
                    onClick={() => {
                      setReplyingToId(null);
                      setReplyContent('');
                    }}
                  >
                    Hủy
                  </Button>
                </Space>
              </div>
            )}

            {/* Render nested replies recursively */}
            {item.replies && item.replies.length > 0 && (
              <div style={{ marginTop: 12 }}>
                {item.replies.map((reply: any) => renderCommentNode(reply, depth + 1))}
              </div>
            )}
          </div>
        </div>
      </div>
    );
  };

  // Count total lessons
  const totalLessons = chapters.reduce((acc: number, ch: any) => acc + (ch.lessons?.length || 0), 0);
  const completedLessons = chapters.reduce((acc: number, ch: any) => {
    return acc + (ch.lessons?.filter((l: any) => l.isCompleted).length || 0);
  }, 0);

  return (
    <div
      style={{
        background: 'var(--bg-primary)',
        minHeight: '100vh',
        display: 'flex',
        flexDirection: 'column',
        width: '100%',
        maxWidth: '100%',
        overflowX: 'hidden',
      }}
    >
      {/* Top Navbar */}
      <div
        style={{
          background: '#FFFFFF',
          borderBottom: '1px solid var(--border-color)',
          boxShadow: '0 1px 4px rgba(0,0,0,0.06)',
          padding: '0 24px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          position: 'sticky',
          top: 0,
          zIndex: 100,
          height: '60px',
          width: '100%',
          maxWidth: '100%',
          boxSizing: 'border-box',
          flexShrink: 0,
        }}
      >
        <Space size="small" style={{ minWidth: 0, flex: 1, overflow: 'hidden' }}>
          <Button
            type="text"
            icon={<ArrowLeftOutlined />}
            onClick={() => navigate(`/courses/${course?.slug}`)}
            style={{ paddingLeft: 0, flexShrink: 0, color: 'var(--primary)', fontWeight: 600 }}
          >
            <span className="hidden-mobile">Quay lại</span>
          </Button>
          <Text
            strong
            ellipsis
            style={{ color: 'var(--text-h)', fontWeight: 700, fontSize: 15, maxWidth: 400 }}
          >
            {course?.title}
          </Text>
        </Space>

        <Space size="small" style={{ flexShrink: 0 }}>
          {/* Progress pill */}
          <div
            style={{
              background: progressPercent >= 100 ? '#F0FDF4' : '#EFF6FF',
              border: `1px solid ${progressPercent >= 100 ? '#BBF7D0' : 'var(--primary-lighter)'}`,
              borderRadius: 20,
              padding: '4px 12px',
              display: 'flex',
              alignItems: 'center',
              gap: 6,
            }}
            className="hidden-mobile"
          >
            {progressPercent >= 100 ? (
              <TrophyOutlined style={{ color: '#22C55E', fontSize: 13 }} />
            ) : (
              <CheckCircleOutlined style={{ color: 'var(--primary)', fontSize: 13 }} />
            )}
            <Text style={{ color: progressPercent >= 100 ? '#166534' : 'var(--primary)', fontSize: 12, fontWeight: 600 }}>
              {progressPercent}% hoàn thành
            </Text>
          </div>

          {user && course && user.id !== course.instructor?.id && (
            <Button
              size="small"
              type="text"
              danger
              icon={<FlagFilled />}
              onClick={handleOpenCourseReportModal}
              style={{ borderRadius: 8, display: 'flex', alignItems: 'center' }}
            >
              <span className="hidden-mobile">Báo cáo khóa học</span>
            </Button>
          )}

          <Button
            size="small"
            type="default"
            onClick={() => navigate('/dashboard')}
            style={{ borderRadius: 8 }}
          >
            <span className="hidden-mobile">Bảng điều khiển</span>
            <HomeOutlined className="show-mobile" style={{ display: 'none' }} />
          </Button>
        </Space>
      </div>

      {/* Main Layout Area */}
      <div
        style={{
          flex: 1,
          display: 'flex',
          width: '100%',
          maxWidth: '100%',
          overflow: 'hidden',
          alignItems: 'stretch',
        }}
      >
        {/* Left Side: Lesson Viewer */}
        <div
          style={{
            flex: 1,
            minWidth: 0,
            overflowY: 'auto',
            overflowX: 'hidden',
            padding: '24px',
            boxSizing: 'border-box',
          }}
        >
          {/* Breadcrumb */}
          <Breadcrumb
            style={{ marginBottom: 16 }}
            items={[
              { title: <Link to="/"><HomeOutlined /></Link> },
              { title: <Link to="/courses">Khóa học</Link> },
              { title: <Link to={`/courses/${course?.slug}`}>{course?.title}</Link> },
              ...(activeLesson ? [{ title: activeLesson.title }] : []),
            ]}
          />

          {activeLesson ? (
            <div
              style={{
                background: '#FFFFFF',
                padding: '24px',
                borderRadius: '16px',
                border: '1px solid var(--border-color)',
                boxShadow: '0 4px 12px rgba(0,0,0,0.04)',
              }}
            >
              {/* Lesson Header */}
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 20, flexWrap: 'wrap', gap: 8 }}>
                <div style={{ minWidth: 0, flex: 1 }}>
                  <Title level={3} style={{ color: 'var(--text-h)', margin: 0, fontSize: 'clamp(16px, 2.5vw, 22px)' }}>
                    {activeLesson.title}
                  </Title>
                </div>
                {activeLesson.isCompleted && (
                  <Tag
                    color="success"
                    style={{ fontSize: 12, padding: '3px 10px', borderRadius: 20, flexShrink: 0 }}
                  >
                    <CheckCircleOutlined /> Đã hoàn thành
                  </Tag>
                )}
              </div>

              {/* Resource Content Area */}
              <div style={{ marginBottom: 24 }}>
                {activeLesson.lessonType === 'VIDEO' ? (
                  <div style={{ borderRadius: '10px', overflow: 'hidden', border: '1px solid var(--border-color)' }}>
                    {renderVideoPlayer(activeLesson.resourceUrl)}
                  </div>
                ) : activeLesson.lessonType === 'PDF' ? (
                  <div
                    style={{
                      background: 'var(--bg-primary)',
                      padding: '32px 24px',
                      border: '1px solid var(--border-color)',
                      borderRadius: '12px',
                      textAlign: 'center',
                    }}
                  >
                    <FileTextOutlined style={{ fontSize: 56, color: '#F59E0B', marginBottom: 12 }} />
                    <Title level={4} style={{ marginBottom: 8 }}>Tài liệu PDF đi kèm</Title>
                    <Paragraph style={{ color: 'var(--text-muted)', marginBottom: 20 }}>
                      Bạn có thể mở tài liệu trực tiếp hoặc tải về máy để phục vụ nghiên cứu học tập.
                    </Paragraph>

                    {activeLesson.resourceUrl ? (
                      <Space size="middle" style={{ marginBottom: 20 }}>
                        <Button
                          type="primary"
                          icon={<CompassOutlined />}
                          onClick={() => window.open(activeLesson.resourceUrl, '_blank')}
                          style={{ background: 'var(--primary)', borderColor: 'var(--primary)' }}
                        >
                          Xem tài liệu PDF
                        </Button>
                        <Button
                          type="default"
                          icon={<DownloadOutlined />}
                          href={activeLesson.resourceUrl}
                          download
                          target="_blank"
                        >
                          Tải PDF xuống
                        </Button>
                      </Space>
                    ) : (
                      <Text type="secondary">Chưa đính kèm đường dẫn tài liệu PDF.</Text>
                    )}

                    {activeLesson.resourceUrl && (
                      <div style={{ marginTop: 16, border: '1px solid var(--border-color)', borderRadius: '10px', overflow: 'hidden' }}>
                        <iframe
                          src={`${activeLesson.resourceUrl}#zoom=${pdfZoom}`}
                          style={{ width: '100%', height: '500px', border: 'none', display: 'block' }}
                          title="PDF Document Viewer"
                        />
                        <div style={{ background: '#FFFFFF', padding: '10px', borderTop: '1px solid var(--border-color)', display: 'flex', justifyContent: 'center', gap: 12 }}>
                          <Button size="small" icon={<ZoomInOutlined />} onClick={() => setPdfZoom(prev => Math.min(prev + 20, 200))}>Phóng to</Button>
                          <Button size="small" icon={<ZoomOutOutlined />} onClick={() => setPdfZoom(prev => Math.max(prev - 20, 50))}>Thu nhỏ</Button>
                        </div>
                      </div>
                    )}
                  </div>
                ) : activeLesson.lessonType === 'TEXT' ? (
                  <div
                    style={{
                      background: '#FAFBFF',
                      border: '1px solid var(--border-color)',
                      borderRadius: '12px',
                      padding: '24px',
                    }}
                  >
                    <div
                      className="reading-view-container"
                      style={{ color: '#334155', fontSize: '16px', lineHeight: '1.8' }}
                      dangerouslySetInnerHTML={{ __html: activeLesson.textContent || '<p>Nội dung bài viết chưa được cập nhật.</p>' }}
                    />
                  </div>
                ) : activeLesson.lessonType === 'QUIZ' ? (
                  <div style={{ background: '#FFFFFF', border: '1px solid var(--border-color)', borderRadius: '12px', padding: '24px' }}>
                    {quizAttempt ? (
                      <div>
                        {/* Countdown Header */}
                        {quizTimeLeft !== null && !quizResult && (
                          <div style={{
                            display: 'flex',
                            justifyContent: 'space-between',
                            alignItems: 'center',
                            background: quizTimeLeft < 60 ? '#FEF2F2' : '#EFF6FF',
                            padding: '12px 20px',
                            border: `1px solid ${quizTimeLeft < 60 ? '#FECACA' : 'var(--primary-lighter)'}`,
                            borderRadius: '10px',
                            marginBottom: '20px',
                          }}>
                            <Text strong style={{ color: 'var(--text-h)' }}>Thời gian làm bài còn lại:</Text>
                            <Tag
                              color={quizTimeLeft < 60 ? 'red' : 'blue'}
                              style={{ fontSize: '15px', padding: '4px 12px', borderRadius: 20 }}
                            >
                              <ClockCircleOutlined /> {formatTimer(quizTimeLeft)}
                            </Tag>
                          </div>
                        )}

                        {/* Quiz Results */}
                        {quizResult ? (
                          <div style={{ textAlign: 'center', padding: '16px 0' }}>
                            {quizResult.status === 'PASSED' ? (
                              <div style={{ marginBottom: 16 }}>
                                <CheckCircleOutlined style={{ fontSize: 64, color: '#22C55E' }} />
                              </div>
                            ) : (
                              <div style={{ marginBottom: 16 }}>
                                <CloseCircleOutlined style={{ fontSize: 64, color: '#EF4444' }} />
                              </div>
                            )}
                            <Title level={3} style={{ color: 'var(--text-h)', marginBottom: 8 }}>
                              {quizResult.status === 'PASSED' ? 'Chúc mừng! Bạn đã đạt yêu cầu' : 'Rất tiếc! Bạn chưa đạt'}
                            </Title>
                            <Paragraph style={{ fontSize: 18, color: 'var(--text-muted)' }}>
                              Điểm số của bạn:{' '}
                              <Text strong style={{ fontSize: 28, color: quizResult.status === 'PASSED' ? '#22C55E' : '#EF4444' }}>
                                {quizResult.score}%
                              </Text>
                            </Paragraph>
                            <Paragraph type="secondary" style={{ marginBottom: 24 }}>
                              Điểm tối thiểu để đạt: {quizConfig?.passingScore}%
                            </Paragraph>

                            <Alert
                              message={
                                quizResult.status === 'PASSED'
                                  ? 'Xuất sắc! Bạn có thể tiếp tục sang các chương tiếp theo.'
                                  : 'Hãy ôn luyện thêm và thử lại bài quiz này.'
                              }
                              type={quizResult.status === 'PASSED' ? 'success' : 'warning'}
                              style={{ textAlign: 'left', marginBottom: 24, borderRadius: 10 }}
                            />

                            <Button type="primary" onClick={handleStartQuiz} style={{ background: 'var(--primary)', borderColor: 'var(--primary)' }}>
                              Làm lại Quiz
                            </Button>

                            {/* Detailed answer explanation */}
                            <div style={{ marginTop: 32, textAlign: 'left', borderTop: '1px solid var(--border-color)', paddingTop: 24 }}>
                              <Title level={4} style={{ marginBottom: 16 }}>Chi tiết bài thi của bạn:</Title>
                              <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
                                {quizQuestions.map((q, idx) => {
                                  let snapshotAnswers: string[] = [];
                                  try {
                                    const parsed = JSON.parse(quizResult.answersSnapshot || '{}');
                                    snapshotAnswers = parsed[q.id] || [];
                                  } catch (e) { }

                                  return (
                                    <div
                                      key={q.id}
                                      style={{
                                        background: '#F8FAFC',
                                        border: '1px solid var(--border-color)',
                                        borderRadius: 12,
                                        padding: '16px',
                                      }}
                                    >
                                      <Text strong style={{ display: 'block', marginBottom: 12, color: 'var(--text-h)' }}>
                                        Câu {idx + 1}: {q.content}
                                      </Text>
                                      <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                                        {q.answers?.map((ans: any) => {
                                          const userSelected = snapshotAnswers.includes(ans.id);
                                          const isCorrect = ans.isCorrect;
                                          let bg = 'transparent';
                                          let border = '1px solid var(--border-color)';
                                          let color = 'var(--text-body)';

                                          if (isCorrect) { bg = '#F0FDF4'; border = '1px solid #BBF7D0'; color = '#166534'; }
                                          else if (userSelected && !isCorrect) { bg = '#FEF2F2'; border = '1px solid #FECACA'; color = '#991B1B'; }

                                          return (
                                            <div
                                              key={ans.id}
                                              style={{ padding: '10px 14px', borderRadius: '8px', background: bg, border, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}
                                            >
                                              <span style={{ color }}>{ans.content}{userSelected ? ' (Bạn chọn)' : ''}</span>
                                              {isCorrect && <Tag color="green" style={{ borderRadius: 6 }}>Đúng</Tag>}
                                              {userSelected && !isCorrect && <Tag color="red" style={{ borderRadius: 6 }}>Sai</Tag>}
                                            </div>
                                          );
                                        })}
                                      </div>
                                      {q.explanation && (
                                        <div style={{ marginTop: 12, padding: '10px 14px', background: '#EFF6FF', borderLeft: '4px solid var(--primary)', borderRadius: '0 8px 8px 0', fontSize: '13px' }}>
                                          <Text strong>Giải thích: </Text>
                                          <span style={{ color: 'var(--text-body)' }}>{q.explanation}</span>
                                        </div>
                                      )}
                                    </div>
                                  );
                                })}
                              </div>
                            </div>
                          </div>
                        ) : (
                          <Space orientation="vertical" size="large" style={{ width: '100%' }}>
                            <div style={{ borderBottom: '1px solid var(--border-color)', paddingBottom: 12 }}>
                              <Text strong style={{ fontSize: 16, color: 'var(--text-h)' }}>Danh sách câu hỏi:</Text>
                            </div>

                            {quizQuestions.map((q, idx) => {
                              const isMultiple = q.questionType === 'MULTIPLE_CHOICE';
                              const selections = quizSelectedAnswers[q.id] || [];

                              return (
                                <div key={q.id} style={{ marginBottom: 8 }}>
                                  <Paragraph strong style={{ fontSize: 15, color: 'var(--text-h)', marginBottom: 12 }}>
                                    {`Câu hỏi ${idx + 1}: ${q.content}`}
                                    <Tag style={{ marginLeft: 8, borderRadius: 20 }} color="purple">
                                      {isMultiple ? 'Nhiều lựa chọn' : 'Một lựa chọn'}
                                    </Tag>
                                  </Paragraph>
                                  <div style={{ display: 'flex', flexDirection: 'column', gap: 8, paddingLeft: 8 }}>
                                    {q.answers?.map((ans: any) => {
                                      const isSelected = selections.includes(ans.id);
                                      return (
                                        <div
                                          key={ans.id}
                                          onClick={() => handleSelectQuizAnswer(q.id, ans.id, isMultiple)}
                                          style={{
                                            padding: '12px 16px',
                                            borderRadius: '10px',
                                            border: isSelected ? `2px solid var(--primary)` : '1px solid var(--border-color)',
                                            background: isSelected ? 'var(--primary-light)' : '#FFFFFF',
                                            cursor: 'pointer',
                                            display: 'flex',
                                            alignItems: 'center',
                                            gap: 10,
                                            transition: 'all 0.15s ease',
                                          }}
                                        >
                                          {isMultiple ? (
                                            <Checkbox checked={isSelected} onChange={() => { }} />
                                          ) : (
                                            <Radio checked={isSelected} onChange={() => { }} />
                                          )}
                                          <span style={{ color: isSelected ? 'var(--primary-dark)' : 'var(--text-body)' }}>
                                            {ans.content}
                                          </span>
                                        </div>
                                      );
                                    })}
                                  </div>
                                </div>
                              );
                            })}

                            <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: 8 }}>
                              <Button
                                type="primary"
                                icon={<SafetyCertificateOutlined />}
                                onClick={handleSubmitQuiz}
                                loading={quizLoading}
                                style={{ background: 'var(--primary)', borderColor: 'var(--primary)', height: 40 }}
                              >
                                Nộp bài kiểm tra
                              </Button>
                            </div>
                          </Space>
                        )}
                      </div>
                    ) : (
                      <div style={{ textAlign: 'center', padding: '40px 24px' }}>
                        <div style={{
                          width: 80,
                          height: 80,
                          borderRadius: '50%',
                          background: 'linear-gradient(135deg, var(--primary-light), var(--primary-lighter))',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          margin: '0 auto 20px',
                        }}>
                          <SafetyCertificateOutlined style={{ fontSize: 36, color: 'var(--primary)' }} />
                        </div>
                        <Title level={4} style={{ marginBottom: 8 }}>Bài trắc nghiệm đánh giá</Title>
                        <Paragraph style={{ color: 'var(--text-muted)', marginBottom: 24 }}>
                          Bài kiểm tra bao gồm các câu hỏi bao quát chương học. Bạn cần đạt từ{' '}
                          <Text strong>{quizConfig?.passingScore || 80}%</Text> điểm trở lên.
                        </Paragraph>
                        {quizConfig && (
                          <div style={{ display: 'flex', justifyContent: 'center', gap: 24, marginBottom: 24 }}>
                            <div style={{ textAlign: 'center' }}>
                              <Text type="secondary" style={{ fontSize: 12, display: 'block' }}>Số câu hỏi</Text>
                              <Text strong style={{ fontSize: 20 }}>{quizConfig.questionCount || '?'}</Text>
                            </div>
                            {quizConfig.timeLimitMinutes && (
                              <div style={{ textAlign: 'center' }}>
                                <Text type="secondary" style={{ fontSize: 12, display: 'block' }}>Thời gian</Text>
                                <Text strong style={{ fontSize: 20 }}>{quizConfig.timeLimitMinutes} phút</Text>
                              </div>
                            )}
                            <div style={{ textAlign: 'center' }}>
                              <Text type="secondary" style={{ fontSize: 12, display: 'block' }}>Điểm đạt</Text>
                              <Text strong style={{ fontSize: 20 }}>{quizConfig.passingScore || 80}%</Text>
                            </div>
                          </div>
                        )}
                        <Button
                          type="primary"
                          size="large"
                          onClick={handleStartQuiz}
                          loading={quizLoading}
                          style={{ background: 'var(--primary)', borderColor: 'var(--primary)', height: 48, paddingLeft: 32, paddingRight: 32 }}
                        >
                          Bắt đầu thi ngay
                        </Button>
                      </div>
                    )}
                  </div>
                ) : (
                  <Text type="secondary">Kiểu bài học không hỗ trợ.</Text>
                )}
              </div>

              {/* Mark Complete Button */}
              {activeLesson.lessonType !== 'QUIZ' && (
                <div style={{ display: 'flex', justifyContent: 'flex-end', borderTop: '1px solid var(--border-color)', paddingTop: 20 }}>
                  <Button
                    type={activeLesson.isCompleted ? 'default' : 'primary'}
                    icon={<CheckCircleOutlined />}
                    disabled={activeLesson.isCompleted}
                    onClick={handleMarkComplete}
                    style={activeLesson.isCompleted
                      ? { borderRadius: 8, color: '#22C55E', borderColor: '#22C55E' }
                      : { background: 'var(--primary)', borderColor: 'var(--primary)', borderRadius: 8 }
                    }
                  >
                    {activeLesson.isCompleted ? 'Đã hoàn thành bài học' : 'Hoàn thành bài học'}
                  </Button>
                </div>
              )}

              {/* Notes & Comments Area */}
              <div style={{ marginTop: 40 }}>
                <Tabs defaultActiveKey="notes">
                  <Tabs.TabPane tab="Ghi chú cá nhân" key="notes">
                    <div style={{ marginBottom: 20 }}>
                      <Form.Item label={<Text strong>Ghi chú bài học này</Text>} style={{ marginBottom: 12 }}>
                        <Input.TextArea
                          rows={3}
                          value={noteContent}
                          onChange={(e: any) => setNoteContent(e.target.value)}
                          placeholder="Viết ghi chú nhanh về nội dung đang học..."
                          style={{ borderRadius: 10 }}
                        />
                      </Form.Item>
                      <Button
                        type="primary"
                        onClick={handleAddNote}
                        disabled={!noteContent.trim()}
                        style={{ background: 'var(--primary)', borderColor: 'var(--primary)', color: '#fff' }}
                      >
                        Lưu ghi chú
                      </Button>
                    </div>

                    <List
                      dataSource={notes}
                      locale={{ emptyText: 'Chưa có ghi chú nào.' }}
                      renderItem={(note: any) => (
                        <List.Item
                          actions={[
                            <Button type="text" danger icon={<DeleteOutlined />} onClick={() => handleDeleteNote(note.id)} size="small">
                              Xóa
                            </Button>
                          ]}
                          style={{ alignItems: 'flex-start' }}
                        >
                          <List.Item.Meta
                            title={<Text strong style={{ fontSize: 13 }}>{note.lessonTitle || 'Ghi chú khóa học'}</Text>}
                            description={
                              <div>
                                <Paragraph style={{ margin: '4px 0', color: 'var(--text-body)', fontSize: 14 }}>
                                  {note.content}
                                </Paragraph>
                                <Text type="secondary" style={{ fontSize: 11 }}>
                                  {new Date(note.createdAt).toLocaleString('vi-VN')}
                                </Text>
                              </div>
                            }
                          />
                        </List.Item>
                      )}
                    />
                  </Tabs.TabPane>
                  <Tabs.TabPane tab="Hỏi đáp & Thảo luận" key="qa">
                    <div style={{ marginBottom: 20 }}>
                      <Form.Item label={<Text strong style={{ fontSize: 13 }}>Đặt câu hỏi hoặc chia sẻ thảo luận</Text>} style={{ marginBottom: 12 }}>
                        <Input.TextArea
                          rows={3}
                          value={commentContent}
                          onChange={(e: any) => setCommentContent(e.target.value)}
                          placeholder="Nhập nội dung câu hỏi hoặc thảo luận về bài học này..."
                          style={{ borderRadius: 10 }}
                        />
                      </Form.Item>
                      <Button
                        type="primary"
                        onClick={handleAddComment}
                        disabled={!commentContent.trim()}
                        style={{ background: 'var(--primary)', borderColor: 'var(--primary)', color: '#fff' }}
                      >
                        Gửi câu hỏi
                      </Button>
                    </div>

                    {commentsLoading ? (
                      <div style={{ textAlign: 'center', padding: '24px 0' }}>Đang tải bình luận...</div>
                    ) : comments.length === 0 ? (
                      <div style={{ textAlign: 'center', padding: '24px 0', color: 'var(--text-muted)' }}>Chưa có câu hỏi hay thảo luận nào.</div>
                    ) : (
                      <div style={{ display: 'flex', flexDirection: 'column' }}>
                        {comments.map((item: any) => renderCommentNode(item))}
                      </div>
                    )}
                  </Tabs.TabPane>
                </Tabs>
              </div>
            </div>
          ) : (
            /* ====== HERO SECTION — No lesson selected ====== */
            <div
              style={{
                background: '#FFFFFF',
                borderRadius: '16px',
                border: '1px solid var(--border-color)',
                boxShadow: '0 4px 12px rgba(0,0,0,0.04)',
                overflow: 'hidden',
              }}
            >
              {/* Course Thumbnail */}
              <div style={{ position: 'relative', width: '100%' }}>
                <img
                  src={course?.thumbnailUrl || 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?q=80&w=900'}
                  alt={course?.title}
                  style={{ width: '100%', maxHeight: '400px', objectFit: 'cover', display: 'block' }}
                />
                <div style={{
                  position: 'absolute',
                  inset: 0,
                  background: 'linear-gradient(to top, rgba(17,24,39,0.8) 0%, transparent 60%)',
                }} />
                <div style={{ position: 'absolute', bottom: 24, left: 24, right: 24 }}>
                  <Title
                    level={2}
                    style={{ color: '#FFFFFF', margin: 0, fontSize: 'clamp(18px, 3vw, 28px)', textShadow: '0 2px 8px rgba(0,0,0,0.5)' }}
                  >
                    {course?.title}
                  </Title>
                </div>
              </div>

              {/* Course Info */}
              <div style={{ padding: '28px' }}>
                <Paragraph
                  style={{ color: 'var(--text-muted)', fontSize: 15, lineHeight: 1.6, marginBottom: 24 }}
                >
                  {course?.shortDescription || 'Hãy chọn một bài học từ danh sách bên phải để bắt đầu học.'}
                </Paragraph>

                {/* Stats Row */}
                <div style={{
                  display: 'flex',
                  flexWrap: 'wrap',
                  gap: '20px',
                  padding: '20px',
                  background: 'var(--bg-primary)',
                  borderRadius: '12px',
                  marginBottom: 24,
                }}>
                  <div style={{ textAlign: 'center', flex: 1, minWidth: 80 }}>
                    <Text type="secondary" style={{ display: 'block', fontSize: 11, marginBottom: 4, textTransform: 'uppercase', letterSpacing: '0.05em' }}>Chương</Text>
                    <Text strong style={{ fontSize: 24, color: 'var(--primary)' }}>{chapters.length}</Text>
                  </div>
                  <div style={{ textAlign: 'center', flex: 1, minWidth: 80 }}>
                    <Text type="secondary" style={{ display: 'block', fontSize: 11, marginBottom: 4, textTransform: 'uppercase', letterSpacing: '0.05em' }}>Bài học</Text>
                    <Text strong style={{ fontSize: 24, color: 'var(--primary)' }}>{totalLessons}</Text>
                  </div>
                  <div style={{ textAlign: 'center', flex: 1, minWidth: 80 }}>
                    <Text type="secondary" style={{ display: 'block', fontSize: 11, marginBottom: 4, textTransform: 'uppercase', letterSpacing: '0.05em' }}>Hoàn thành</Text>
                    <Text strong style={{ fontSize: 24, color: '#22C55E' }}>{completedLessons}</Text>
                  </div>
                  <div style={{ textAlign: 'center', flex: 1, minWidth: 80 }}>
                    <Text type="secondary" style={{ display: 'block', fontSize: 11, marginBottom: 4, textTransform: 'uppercase', letterSpacing: '0.05em' }}>Cấp độ</Text>
                    <Tag color="blue" style={{ fontSize: 12, padding: '2px 10px', borderRadius: 20, marginTop: 2 }}>
                      {course?.level || 'N/A'}
                    </Tag>
                  </div>
                </div>

                {/* Instructor Info */}
                {course?.instructor && (
                  <div style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: 12,
                    padding: '16px',
                    background: '#EFF6FF',
                    borderRadius: '12px',
                    border: '1px solid var(--primary-lighter)',
                  }}>
                    {course.instructor.avatarUrl ? (
                      <img
                        src={course.instructor.avatarUrl}
                        alt={course.instructor.fullName}
                        style={{ width: 44, height: 44, borderRadius: '50%', objectFit: 'cover', border: '2px solid var(--primary-lighter)' }}
                      />
                    ) : (
                      <div style={{
                        width: 44, height: 44, borderRadius: '50%', background: 'var(--primary)',
                        display: 'flex', alignItems: 'center', justifyContent: 'center',
                      }}>
                        <UserOutlined style={{ color: '#FFFFFF', fontSize: 20 }} />
                      </div>
                    )}
                    <div>
                      <Text strong style={{ display: 'block', color: 'var(--text-h)', fontSize: 14 }}>
                        {course.instructor.fullName || 'Giảng viên'}
                      </Text>
                      <Text style={{ color: 'var(--primary)', fontSize: 12 }}>Giảng viên khóa học</Text>
                    </div>
                  </div>
                )}

                {user && course && user.id !== course.instructor?.id && (
                  <div style={{ marginTop: 20 }}>
                    <Button
                      type="text"
                      danger
                      icon={<FlagFilled />}
                      style={{ padding: 0, height: 'auto', display: 'flex', alignItems: 'center', gap: 6 }}
                      onClick={handleOpenCourseReportModal}
                    >
                      🚩 Báo cáo khóa học
                    </Button>
                  </div>
                )}

                {/* Progress */}
                <div style={{ marginTop: 24 }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
                    <Text strong style={{ color: 'var(--text-h)', fontSize: 14 }}>Tiến trình học tập</Text>
                    <Text style={{ color: 'var(--primary)', fontWeight: 700, fontSize: 14 }}>{progressPercent}%</Text>
                  </div>
                  <Progress
                    percent={progressPercent}
                    showInfo={false}
                    strokeColor={{ '0%': '#2563EB', '100%': '#3B82F6' }}
                    trailColor="#E5E7EB"
                    strokeWidth={10}
                    style={{ borderRadius: 99 }}
                  />
                  <Text type="secondary" style={{ fontSize: 12, marginTop: 6, display: 'block' }}>
                    {completedLessons}/{totalLessons} bài học đã hoàn thành
                  </Text>
                </div>

                <div style={{ marginTop: 20, textAlign: 'center' }}>
                  <Text type="secondary" style={{ fontSize: 14 }}>
                    👈 Chọn một bài học từ danh sách bên phải để bắt đầu
                  </Text>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Right Side: Curriculum Sidebar */}
        <div
          style={{
            width: '300px',
            minWidth: '300px',
            maxWidth: '300px',
            background: '#FFFFFF',
            borderLeft: '1px solid var(--border-color)',
            display: 'flex',
            flexDirection: 'column',
            overflow: 'hidden',
            flexShrink: 0,
          }}
        >
          {/* Sidebar Header */}
          <div style={{ padding: '16px 20px', borderBottom: '1px solid var(--border-color)', background: 'var(--bg-primary)' }}>
            <Text strong style={{ color: 'var(--text-h)', fontSize: 13, textTransform: 'uppercase', letterSpacing: '0.06em', display: 'block', marginBottom: 10 }}>
              Đề cương khóa học
            </Text>
            {/* Progress compact */}
            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              <Progress
                percent={progressPercent}
                showInfo={false}
                strokeColor={{ '0%': '#2563EB', '100%': '#3B82F6' }}
                trailColor="#E5E7EB"
                strokeWidth={6}
                style={{ flex: 1 }}
              />
              <Text style={{ color: 'var(--primary)', fontSize: 12, fontWeight: 700, flexShrink: 0 }}>
                {progressPercent}%
              </Text>
            </div>
            <Text type="secondary" style={{ fontSize: 11, marginTop: 4, display: 'block' }}>
              {completedLessons}/{totalLessons} bài học
            </Text>
          </div>

          {/* Chapters scroll area */}
          <div style={{ flex: 1, overflowY: 'auto', overflowX: 'hidden' }}>
            {chapters.map((chap: any) => {
              const isExpanded = expandedChapters.has(chap.id);
              const chapCompleted = chap.lessons?.filter((l: any) => l.isCompleted).length || 0;
              const chapTotal = chap.lessons?.length || 0;

              return (
                <div key={chap.id} style={{ borderBottom: '1px solid var(--border-color)' }}>
                  {/* Chapter Header */}
                  <div
                    style={{
                      padding: '14px 20px',
                      background: isExpanded ? '#EFF6FF' : '#FFFFFF',
                      cursor: 'pointer',
                      display: 'flex',
                      alignItems: 'flex-start',
                      justifyContent: 'space-between',
                      gap: 8,
                      transition: 'background 0.2s',
                    }}
                    onClick={() => toggleChapter(chap.id)}
                  >
                    <div style={{ flex: 1, minWidth: 0 }}>
                      <Text
                        strong
                        style={{
                          color: isExpanded ? 'var(--primary)' : 'var(--text-h)',
                          fontSize: 13,
                          display: 'block',
                          lineHeight: 1.4,
                        }}
                      >
                        {`Ch.${chap.orderIndex}: ${chap.title}`}
                      </Text>
                      <Text type="secondary" style={{ fontSize: 11, marginTop: 2, display: 'block' }}>
                        {chapCompleted}/{chapTotal} bài hoàn thành
                      </Text>
                    </div>
                    <div style={{ flexShrink: 0, color: isExpanded ? 'var(--primary)' : 'var(--text-muted)', transition: 'transform 0.2s', transform: isExpanded ? 'rotate(180deg)' : 'rotate(0deg)' }}>
                      ▾
                    </div>
                  </div>

                  {/* Lesson Items */}
                  {isExpanded && (
                    <div style={{ background: '#FAFBFF' }}>
                      {chap.lessons?.map((les: any) => {
                        const isSelected = activeLesson?.id === les.id;

                        let iconEl = <BookOutlined style={{ fontSize: 13 }} />;
                        if (les.lessonType === 'VIDEO') iconEl = <PlayCircleOutlined style={{ fontSize: 13, color: 'var(--primary)' }} />;
                        if (les.lessonType === 'PDF') iconEl = <FileTextOutlined style={{ fontSize: 13, color: '#F59E0B' }} />;
                        if (les.lessonType === 'QUIZ') iconEl = <SafetyCertificateOutlined style={{ fontSize: 13, color: '#8B5CF6' }} />;

                        return (
                          <div
                            key={les.id}
                            onClick={() => {
                              setActiveLesson(les);
                              setQuizAttempt(null);
                              setQuizResult(null);
                            }}
                            style={{
                              padding: '10px 20px 10px 28px',
                              cursor: 'pointer',
                              display: 'flex',
                              alignItems: 'center',
                              justifyContent: 'space-between',
                              background: isSelected ? 'var(--primary)' : 'transparent',
                              transition: 'all 0.15s ease',
                              borderLeft: isSelected ? '3px solid var(--primary-dark)' : '3px solid transparent',
                            }}
                            onMouseEnter={(e) => {
                              if (!isSelected) e.currentTarget.style.background = 'var(--primary-light)';
                            }}
                            onMouseLeave={(e) => {
                              if (!isSelected) e.currentTarget.style.background = 'transparent';
                            }}
                          >
                            <div style={{ display: 'flex', alignItems: 'center', gap: 8, minWidth: 0, flex: 1 }}>
                              <span style={{ flexShrink: 0, color: isSelected ? '#FFFFFF' : undefined }}>
                                {iconEl}
                              </span>
                              <Text
                                ellipsis={{ tooltip: les.title }}
                                style={{
                                  color: isSelected ? '#FFFFFF' : 'var(--text-body)',
                                  fontWeight: isSelected ? 600 : 400,
                                  fontSize: 13,
                                  flex: 1,
                                }}
                              >
                                {les.title}
                              </Text>
                            </div>
                            <div style={{ flexShrink: 0, marginLeft: 4 }}>
                              {les.isCompleted ? (
                                <CheckCircleOutlined style={{ color: isSelected ? '#FFFFFF' : '#22C55E', fontSize: 14 }} />
                              ) : (
                                <span style={{ fontSize: '10px', color: isSelected ? 'rgba(255,255,255,0.7)' : 'var(--text-muted)' }}>
                                  {les.lessonType}
                                </span>
                              )}
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        </div>
      </div>

      <Footer />

      {/* Report Comment Modal */}
      <Modal
        title={<span><FlagFilled style={{ color: '#ff4d4f', marginRight: 8 }} />Báo cáo bình luận vi phạm</span>}
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
              onChange={(e: any) => setReportDescription(e.target.value)}
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

      {/* Course Report Modal */}
      <Modal
        title={<span><FlagFilled style={{ color: '#ff4d4f', marginRight: 8 }} />Báo cáo khóa học vi phạm</span>}
        open={courseReportModalOpen}
        onCancel={() => setCourseReportModalOpen(false)}
        onOk={handleCourseReportSubmit}
        okText="Gửi báo cáo"
        cancelText="Hủy"
        confirmLoading={submittingCourseReport}
      >
        <Form layout="vertical" style={{ marginTop: 16 }} noValidate>
          <Form.Item label="Lý do báo cáo" required>
            <Select value={courseReportReason} onChange={setCourseReportReason}>
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
            required={courseReportReason === 'Khác'}
          >
            <Input.TextArea
              rows={4}
              value={courseReportDescription}
              onChange={(e: any) => setCourseReportDescription(e.target.value)}
              placeholder="Mô tả chi tiết về hành vi vi phạm của khóa học..."
            />
            {courseReportDescriptionError && (
              <div style={{ color: '#ff4d4f', marginTop: '4px', fontSize: '14px' }}>
                {courseReportDescriptionError}
              </div>
            )}
          </Form.Item>
        </Form>

      </Modal>
    </div>
  );
};

export default LearningPage;