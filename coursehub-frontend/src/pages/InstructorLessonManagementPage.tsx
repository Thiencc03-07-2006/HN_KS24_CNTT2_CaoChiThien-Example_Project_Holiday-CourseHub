import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Typography, Form, Space, message, Modal as AntdModal, List, Tag, Tabs, Checkbox, Radio } from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  ArrowLeftOutlined,
  ArrowUpOutlined,
  ArrowDownOutlined,
  SaveOutlined,
  EyeOutlined,
  SettingOutlined,
  QuestionCircleOutlined
} from '@ant-design/icons';
import axiosInstance from '../api/axiosInstance';
import { Card } from '../components/common/UI/Card';
import { Button } from '../components/common/UI/Button';
import { Input } from '../components/common/UI/Input';
import { Select } from '../components/common/UI/Select';
import { Loading } from '../components/common/UI/Loading';

const { Title, Text } = Typography;
const { Option } = Select;

// Custom Lightweight Rich Text Editor Component for Reading Lessons
const RichTextEditor: React.FC<{ value: string; onChange: (val: string) => void }> = ({ value, onChange }) => {
  const editorRef = React.useRef<HTMLDivElement>(null);

  React.useEffect(() => {
    if (editorRef.current && editorRef.current.innerHTML !== value) {
      editorRef.current.innerHTML = value || '<p><br></p>';
    }
  }, [value]);

  const executeCommand = (command: string, arg: string = '') => {
    document.execCommand(command, false, arg);
    if (editorRef.current) {
      onChange(editorRef.current.innerHTML);
    }
  };

  return (
    <div style={{ border: '1px solid var(--border-color)', borderRadius: '6px', overflow: 'hidden', background: '#fff', color: '#000' }}>
      <div style={{ borderBottom: '1px solid var(--border-color)', background: '#f5f5f5', padding: '6px 12px', display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
        <Button size="small" type="text" style={{ fontWeight: 'bold' }} onClick={() => executeCommand('formatBlock', '<h1>')}>H1</Button>
        <Button size="small" type="text" style={{ fontWeight: 'bold' }} onClick={() => executeCommand('formatBlock', '<h2>')}>H2</Button>
        <Button size="small" type="text" style={{ fontWeight: 'bold' }} onClick={() => executeCommand('formatBlock', '<h3>')}>H3</Button>
        <Button size="small" type="text" style={{ fontWeight: 'bold' }} onClick={() => executeCommand('formatBlock', '<p>')}>P</Button>
        <Button size="small" type="text" style={{ fontWeight: 'bold' }} onClick={() => executeCommand('bold')}>B</Button>
        <Button size="small" type="text" style={{ fontStyle: 'italic' }} onClick={() => executeCommand('italic')}>I</Button>
        <Button size="small" type="text" style={{ textDecoration: 'underline' }} onClick={() => executeCommand('underline')}>U</Button>
        <Button size="small" type="text" onClick={() => executeCommand('insertUnorderedList')}>• Danh sách</Button>
        <Button size="small" type="text" onClick={() => executeCommand('insertOrderedList')}>1. Danh sách</Button>
        <Button size="small" type="text" onClick={() => {
          const url = prompt('Nhập liên kết URL:');
          if (url) executeCommand('createLink', url);
        }}>Liên kết</Button>
        <Button size="small" type="text" onClick={() => {
          const url = prompt('Nhập URL hình ảnh:');
          if (url) executeCommand('insertImage', url);
        }}>Ảnh</Button>
        <Button size="small" type="text" onClick={() => {
          const code = prompt('Nhập mã nguồn / nội dung block code:');
          if (code) {
            executeCommand('insertHTML', `<pre style="background:#f4f4f4;padding:8px;border-radius:4px;font-family:monospace;border:1px solid #ddd;margin:8px 0;"><code>${code}</code></pre>`);
          }
        }}>Code Block</Button>
      </div>
      <div
        ref={editorRef}
        contentEditable
        onBlur={(e) => onChange(e.target.innerHTML)}
        style={{ minHeight: '250px', padding: '12px', outline: 'none', background: '#fff', overflowY: 'auto' }}
      />
    </div>
  );
};

const InstructorLessonManagementPage: React.FC = () => {
  const { courseId, chapterId } = useParams<{ courseId: string; chapterId: string }>();
  const navigate = useNavigate();
  const [chapter, setChapter] = useState<any>(null);
  const [lessons, setLessons] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  // Lesson Edit/Create Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [form] = Form.useForm();
  const [editingLesson, setEditingLesson] = useState<any>(null);

  // Lesson Content Resource Editor State
  const [contentModalOpen, setContentModalOpen] = useState(false);
  const [contentLesson, setContentLesson] = useState<any>(null);
  const [resourceForm] = Form.useForm();
  const [readingHtml, setReadingHtml] = useState('');

  // Quiz Builder Modal State
  const [quizModalOpen, setQuizModalOpen] = useState(false);
  const [quizLesson, setQuizLesson] = useState<any>(null);
  const [quizConfig, setQuizConfig] = useState<any>({
    timeLimitMinutes: 30,
    passingScore: 70,
    maxAttempts: 3,
    shuffleQuestions: false,
    shuffleAnswers: false,
  });
  const [quizQuestions, setQuizQuestions] = useState<any[]>([]);
  const [questionForm] = Form.useForm();
  const [editingQuestion, setEditingQuestion] = useState<any>(null);
  const [answers, setAnswers] = useState<any[]>([]); // Array of { content: '', isCorrect: false }

  // Custom validation states
  const [readingError, setReadingError] = useState('');
  const [configErrors, setConfigErrors] = useState<any>({});


  const fetchChapterAndLessons = async () => {
    try {
      const chapterRes = await axiosInstance.get(`/instructor/courses/${courseId}/chapters`);
      if (chapterRes.data?.success) {
        const found = chapterRes.data.data.find((c: any) => c.id === chapterId);
        setChapter(found);
      }
      const lessonRes = await axiosInstance.get(`/instructor/chapters/${chapterId}/lessons`);
      if (lessonRes.data?.success) {
        setLessons(lessonRes.data.data || []);
      }
    } catch (err) {
      message.error('Không thể tải danh sách bài học.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchChapterAndLessons();
  }, [courseId, chapterId]);

  // Save basic lesson properties
  const handleSaveLesson = async (values: any) => {
    try {
      const payload = {
        title: values.title,
        lessonType: values.lessonType,
        isPreview: values.isPreview === 'true',
      };
      if (editingLesson) {
        await axiosInstance.put(`/instructor/lessons/${editingLesson.id}`, payload);
        message.success('Cập nhật bài học thành công!');
      } else {
        await axiosInstance.post(`/instructor/chapters/${chapterId}/lessons`, payload);
        message.success('Tạo bài học thành công!');
      }
      setIsModalOpen(false);
      form.resetFields();
      setEditingLesson(null);
      fetchChapterAndLessons();
    } catch (err) {
      message.error('Không thể lưu bài học.');
    }
  };

  const handleDeleteLesson = async (lessonId: string) => {
    AntdModal.confirm({
      title: 'Xác nhận xóa',
      content: 'Bạn có chắc chắn muốn xóa bài học này?',
      okText: 'Xóa',
      okType: 'danger',
      cancelText: 'Hủy',
      onOk: async () => {
        try {
          await axiosInstance.delete(`/instructor/lessons/${lessonId}`);
          message.success('Đã xóa bài học.');
          fetchChapterAndLessons();
        } catch (err) {
          message.error('Không thể xóa bài học.');
        }
      },
    });
  };

  // Open resource editor
  const handleOpenContentEditor = (lesson: any) => {
    setContentLesson(lesson);
    setReadingHtml(lesson.textContent || '');
    setReadingError('');
    resourceForm.setFieldsValue({
      resourceUrl: lesson.resourceUrl || '',
      durationSeconds: lesson.durationSeconds || 0,
      isDownloadable: lesson.isDownloadable || false,
    });
    setContentModalOpen(true);
  };

  const handleSaveResource = async (values: any) => {
    if (!contentLesson) return;
    if (contentLesson.lessonType === 'TEXT') {
      const stripped = readingHtml ? readingHtml.replace(/<[^>]*>/g, '').trim() : '';
      if (!stripped) {
        setReadingError('Trường này là bắt buộc');
        return;
      }
    }
    setReadingError('');
    try {
      const payload = {
        resourceUrl: values.resourceUrl || '',
        durationSeconds: values.durationSeconds ? Number(values.durationSeconds) : 0,
        textContent: contentLesson.lessonType === 'TEXT' ? readingHtml : '',
        isDownloadable: values.isDownloadable || false,
      };

      await axiosInstance.put(
        `/instructor/courses/${courseId}/chapters/${chapterId}/lessons/${contentLesson.id}/resource`,
        payload
      );
      message.success('Cập nhật nội dung bài học thành công!');
      setContentModalOpen(false);
      fetchChapterAndLessons();
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Không thể lưu tài nguyên.');
    }
  };


  // Quiz Builder Operations
  const fetchQuizData = async (lessonId: string) => {
    try {
      const configRes = await axiosInstance.get(`/instructor/courses/${courseId}/lessons/${lessonId}/quiz/config`);
      if (configRes.data?.success && configRes.data?.data) {
        setQuizConfig(configRes.data.data);
      } else {
        setQuizConfig({
          timeLimitMinutes: 30,
          passingScore: 70,
          maxAttempts: 3,
          shuffleQuestions: false,
          shuffleAnswers: false,
        });
      }
      const questionsRes = await axiosInstance.get(`/instructor/courses/${courseId}/lessons/${lessonId}/quiz/questions`);
      if (questionsRes.data?.success) {
        setQuizQuestions(questionsRes.data.data || []);
      }
    } catch (err) {
      console.error(err);
      message.error('Không thể tải cấu hình và câu hỏi Quiz.');
    }
  };

  const handleOpenQuizBuilder = (lesson: any) => {
    setQuizLesson(lesson);
    setEditingQuestion(null);
    setAnswers([]);
    setConfigErrors({});
    questionForm.resetFields();
    fetchQuizData(lesson.id);
    setQuizModalOpen(true);
  };

  const handleSaveQuizConfig = async () => {
    if (!quizLesson) return;
    const errors: any = {};
    if (!quizConfig.timeLimitMinutes || quizConfig.timeLimitMinutes < 1) {
      errors.timeLimitMinutes = 'Thời gian giới hạn phải lớn hơn hoặc bằng 1';
    }
    if (!quizConfig.passingScore || quizConfig.passingScore < 1 || quizConfig.passingScore > 100) {
      errors.passingScore = 'Điểm số đạt yêu cầu phải từ 1 đến 100%';
    }
    if (!quizConfig.maxAttempts || quizConfig.maxAttempts < 1) {
      errors.maxAttempts = 'Số lần thi tối đa phải lớn hơn hoặc bằng 1';
    }
    if (Object.keys(errors).length > 0) {
      setConfigErrors(errors);
      return;
    }
    setConfigErrors({});
    try {
      await axiosInstance.post(`/instructor/courses/${courseId}/lessons/${quizLesson.id}/quiz/config`, quizConfig);
      message.success('Đã lưu cấu hình Quiz thành công!');
      fetchQuizData(quizLesson.id);
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Không thể lưu cấu hình Quiz.');
    }
  };


  // Answer modification helpers
  const handleAddAnswer = () => {
    setAnswers([...answers, { content: '', isCorrect: false }]);
  };

  const handleRemoveAnswer = (index: number) => {
    const newAnswers = [...answers];
    newAnswers.splice(index, 1);
    setAnswers(newAnswers);
  };

  const handleAnswerChange = (index: number, field: string, val: any) => {
    const newAnswers = [...answers];
    if (field === 'isCorrect') {
      const qType = questionForm.getFieldValue('questionType');
      if (qType === 'SINGLE_CHOICE' || qType === 'TRUE_FALSE') {
        newAnswers.forEach((a, i) => {
          a.isCorrect = i === index ? val : false;
        });
      } else {
        newAnswers[index].isCorrect = val;
      }
    } else {
      newAnswers[index][field] = val;
    }
    setAnswers(newAnswers);
  };

  const handleQuestionTypeChange = (type: string) => {
    if (type === 'TRUE_FALSE') {
      setAnswers([
        { content: 'Đúng', isCorrect: true },
        { content: 'Sai', isCorrect: false }
      ]);
    } else {
      setAnswers([
        { content: '', isCorrect: false },
        { content: '', isCorrect: false }
      ]);
    }
  };

  const handleSaveQuestion = async (values: any) => {
    if (!quizLesson) return;

    const qType = values.questionType;
    const correctCount = answers.filter(a => a.isCorrect).length;

    if (answers.length === 0) {
      message.error('Vui lòng thêm ít nhất một đáp án.');
      return;
    }

    if (qType === 'SINGLE_CHOICE' && correctCount !== 1) {
      message.error('Câu hỏi lựa chọn duy nhất phải có chính xác 1 đáp án đúng.');
      return;
    }
    if (qType === 'TRUE_FALSE') {
      if (answers.length !== 2) {
        message.error('Câu hỏi Đúng/Sai phải có chính xác 2 đáp án.');
        return;
      }
      if (correctCount !== 1) {
        message.error('Câu hỏi Đúng/Sai phải có chính xác 1 đáp án đúng.');
        return;
      }
    }
    if (qType === 'MULTIPLE_CHOICE' && correctCount < 1) {
      message.error('Câu hỏi nhiều lựa chọn phải có ít nhất 1 đáp án đúng.');
      return;
    }

    const payload = {
      content: values.content,
      questionType: values.questionType,
      points: Number(values.points || 1),
      explanation: values.explanation,
      orderIndex: editingQuestion ? editingQuestion.orderIndex : (quizQuestions.length + 1),
      answers: answers.map((a, index) => ({
        content: a.content,
        isCorrect: a.isCorrect,
        orderIndex: index + 1
      }))
    };

    try {
      if (editingQuestion) {
        await axiosInstance.put(
          `/instructor/courses/${courseId}/lessons/${quizLesson.id}/quiz/questions/${editingQuestion.id}`,
          payload
        );
        message.success('Cập nhật câu hỏi thành công!');
      } else {
        await axiosInstance.post(
          `/instructor/courses/${courseId}/lessons/${quizLesson.id}/quiz/questions`,
          payload
        );
        message.success('Thêm câu hỏi thành công!');
      }
      questionForm.resetFields();
      setEditingQuestion(null);
      setAnswers([]);
      fetchQuizData(quizLesson.id);
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Không thể lưu câu hỏi.');
    }
  };

  const handleEditQuestionClick = (q: any) => {
    setEditingQuestion(q);
    questionForm.setFieldsValue({
      content: q.content,
      questionType: q.questionType,
      points: q.points,
      explanation: q.explanation,
    });
    setAnswers(q.answers.map((a: any) => ({
      content: a.content,
      isCorrect: a.isCorrect
    })));
  };

  const handleDeleteQuestion = async (questionId: string) => {
    if (!quizLesson) return;
    try {
      await axiosInstance.delete(`/instructor/courses/${courseId}/lessons/${quizLesson.id}/quiz/questions/${questionId}`);
      message.success('Đã xóa câu hỏi.');
      fetchQuizData(quizLesson.id);
    } catch (err) {
      message.error('Không thể xóa câu hỏi.');
    }
  };

  const handleMoveQuestion = async (index: number, direction: 'up' | 'down') => {
    if (!quizLesson) return;
    const targetIndex = direction === 'up' ? index - 1 : index + 1;
    if (targetIndex < 0 || targetIndex >= quizQuestions.length) return;

    const list = [...quizQuestions];
    const temp = list[index];
    list[index] = list[targetIndex];
    list[targetIndex] = temp;

    try {
      for (let i = 0; i < list.length; i++) {
        const q = list[i];
        const payload = {
          content: q.content,
          questionType: q.questionType,
          points: q.points,
          explanation: q.explanation,
          orderIndex: i + 1,
          answers: q.answers.map((a: any) => ({
            content: a.content,
            isCorrect: a.isCorrect,
            orderIndex: a.orderIndex
          }))
        };
        await axiosInstance.put(
          `/instructor/courses/${courseId}/lessons/${quizLesson.id}/quiz/questions/${q.id}`,
          payload
        );
      }
      message.success('Đổi vị trí câu hỏi thành công!');
      fetchQuizData(quizLesson.id);
    } catch (err) {
      message.error('Không thể lưu thứ tự sắp xếp câu hỏi.');
    }
  };

  if (loading) {
    return <Loading message="Đang tải danh sách bài học..." />;
  }

  // Check valid video format
  const getResourceUrlPreview = (url: string) => {
    if (!url) return null;
    if (url.includes('youtube.com') || url.includes('youtu.be')) {
      const regExp = /^.*(youtu.be\/|v\/|u\/\w\/|embed\/|watch\?v=|\&v=)([^#\&\?]*).*/;
      const match = url.match(regExp);
      if (match && match[2].length === 11) {
        return (
          <iframe
            src={`https://www.youtube.com/embed/${match[2]}`}
            style={{ width: '100%', height: '240px', border: 'none', borderRadius: '8px' }}
            allowFullScreen
            title="Video Preview"
          />
        );
      }
    }
    return <video src={url} controls style={{ width: '100%', maxHeight: '240px', borderRadius: '8px' }} />;
  };

  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate(`/instructor/courses/${courseId}/chapters`)}>
          Quay lại chương học
        </Button>
      </Space>

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <div>
          <Title level={3} style={{ color: 'var(--text-color)', margin: 0 }}>
            Quản lý bài học
          </Title>
          <Text type="secondary">Chương học: {chapter?.title}</Text>
        </div>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={() => {
            setEditingLesson(null);
            form.resetFields();
            setIsModalOpen(true);
          }}
        >
          Thêm bài học mới
        </Button>
      </div>

      <Card>
        <List
          itemLayout="horizontal"
          dataSource={lessons}
          renderItem={(lesson) => (
            <List.Item
              actions={[
                lesson.lessonType === 'QUIZ' ? (
                  <Button
                    type="primary"
                    size="small"
                    icon={<SettingOutlined />}
                    onClick={() => handleOpenQuizBuilder(lesson)}
                  >
                    Quiz Builder
                  </Button>
                ) : (
                  <Button
                    type="primary"
                    size="small"
                    icon={<EditOutlined />}
                    onClick={() => handleOpenContentEditor(lesson)}
                  >
                    Thiết lập Nội dung
                  </Button>
                ),
                <Button
                  type="text"
                  icon={<EditOutlined />}
                  onClick={() => {
                    setEditingLesson(lesson);
                    form.setFieldsValue({
                      title: lesson.title,
                      orderIndex: lesson.orderIndex,
                      lessonType: lesson.lessonType,
                      durationSeconds: lesson.durationSeconds,
                      isPreview: String(lesson.isPreview),
                    });
                    setIsModalOpen(true);
                  }}
                />,
                <Button
                  type="text"
                  danger
                  icon={<DeleteOutlined />}
                  onClick={() => handleDeleteLesson(lesson.id)}
                />,
              ]}
            >
              <List.Item.Meta
                title={
                  <Space wrap>
                    <Text strong style={{ color: 'var(--text-color)', fontSize: 16 }}>
                      {`Bài ${lesson.orderIndex}: ${lesson.title}`}
                    </Text>
                    <Tag color="cyan">{lesson.lessonType}</Tag>
                    {lesson.isPreview && <Tag color="green">Xem thử miễn phí</Tag>}
                  </Space>
                }
              />
            </List.Item>
          )}
        />
      </Card>

      {/* Lesson Basic Save Modal */}
      <AntdModal
        title={editingLesson ? 'Chỉnh sửa bài học' : 'Thêm bài học mới'}
        open={isModalOpen}
        onCancel={() => setIsModalOpen(false)}
        footer={null}
      >
        <Form form={form} layout="vertical" onFinish={handleSaveLesson} style={{ marginTop: '16px' }} noValidate>
          <Form.Item
            label="Tiêu đề bài học"
            name="title"
            rules={[
              { required: true, message: 'Tiêu đề bài học không được để trống' },
              { min: 5, max: 150, message: 'Tiêu đề bài học phải từ 5 đến 150 ký tự' }
            ]}
          >
            <Input placeholder="Ví dụ: Cài đặt JDK & IDE Eclipse" />
          </Form.Item>

          <Form.Item label="Loại bài học" name="lessonType" rules={[{ required: true, message: 'Loại bài học không được để trống' }]}>
            <Select placeholder="Chọn định dạng bài học">
              <Option value="VIDEO">Video bài giảng</Option>
              <Option value="PDF">Tài liệu PDF</Option>
              <Option value="TEXT">Văn bản hướng dẫn (Reading)</Option>
              <Option value="QUIZ">Trắc nghiệm ôn tập</Option>
            </Select>
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
            <Input type="number" min={1} placeholder="Thứ tự bài học trong chương" />
          </Form.Item>

          <Form.Item label="Cho phép xem thử miễn phí?" name="isPreview" initialValue="false">

            <Select>
              <Option value="false">Không cho phép</Option>
              <Option value="true">Cho phép xem thử</Option>
            </Select>
          </Form.Item>

          <Form.Item style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: 0, marginTop: 16 }}>
            <Space>
              <Button onClick={() => setIsModalOpen(false)}>Hủy</Button>
              <Button type="primary" htmlType="submit">Lưu lại</Button>
            </Space>
          </Form.Item>
        </Form>
      </AntdModal>

      {/* Lesson Content Resource Modal */}
      <AntdModal
        title={`Thiết lập tài nguyên bài học: ${contentLesson?.title}`}
        open={contentModalOpen}
        onCancel={() => setContentModalOpen(false)}
        footer={null}
        width={750}
      >
        <Form form={resourceForm} layout="vertical" onFinish={handleSaveResource} style={{ marginTop: '16px' }} noValidate>
          {contentLesson?.lessonType === 'VIDEO' && (
            <>
              <Form.Item
                label="Đường dẫn Video URL"
                name="resourceUrl"
                rules={[{ required: true, message: 'Trường này là bắt buộc' }, { type: 'url', message: 'Vui lòng nhập URL hợp lệ!' }]}
              >
                <Input placeholder="Nhập URL video: Cloudinary, YouTube, Vimeo..." />
              </Form.Item>

              <Form.Item
                label="Thời lượng Video (giây)"
                name="durationSeconds"
                rules={[
                  { required: true, message: 'Trường này là bắt buộc' },
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
                <Input type="number" min={1} placeholder="Ví dụ: 300 (5 phút)" />
              </Form.Item>

              <Form.Item label="Xem trước Video (Preview)">
                <Form.Item shouldUpdate={(prev, curr) => prev.resourceUrl !== curr.resourceUrl} noStyle>
                  {() => {
                    const url = resourceForm.getFieldValue('resourceUrl');
                    return url ? getResourceUrlPreview(url) : <Text type="secondary">Nhập URL hợp lệ để xem trước video</Text>;
                  }}
                </Form.Item>
              </Form.Item>
            </>
          )}

          {contentLesson?.lessonType === 'PDF' && (
            <>
              <Form.Item
                label="Đường dẫn tài liệu PDF URL"
                name="resourceUrl"
                rules={[{ required: true, message: 'Trường này là bắt buộc' }, { type: 'url', message: 'Vui lòng nhập URL hợp lệ!' }]}
              >
                <Input placeholder="Nhập link tài liệu PDF (https://...)" />
              </Form.Item>

              <Form.Item name="isDownloadable" valuePropName="checked" initialValue={false}>
                <Checkbox style={{ color: 'var(--text-color)' }}>Cho phép học viên tải xuống tệp PDF này</Checkbox>
              </Form.Item>

              <Form.Item shouldUpdate={(prev, curr) => prev.resourceUrl !== curr.resourceUrl} noStyle>
                {() => {
                  const url = resourceForm.getFieldValue('resourceUrl');
                  return url ? (
                    <Button type="default" onClick={() => window.open(url, '_blank')} style={{ marginBottom: 16 }}>
                      Xem thử tệp PDF
                    </Button>
                  ) : null;
                }}
              </Form.Item>
            </>
          )}

          {contentLesson?.lessonType === 'TEXT' && (
            <Form.Item label="Nội dung bài viết (Reading)" required>
              <RichTextEditor value={readingHtml} onChange={(val) => setReadingHtml(val)} />
              {readingError && <div className="input-error">{readingError}</div>}
            </Form.Item>
          )}

          <Form.Item style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: 0, marginTop: 24 }}>
            <Space>
              <Button onClick={() => setContentModalOpen(false)}>Hủy</Button>
              <Button type="primary" htmlType="submit" icon={<SaveOutlined />}>Lưu nội dung</Button>
            </Space>
          </Form.Item>
        </Form>

      </AntdModal>

      {/* Quiz Builder Drawer/Modal */}
      <AntdModal
        title={`Quiz Builder: ${quizLesson?.title}`}
        open={quizModalOpen}
        onCancel={() => setQuizModalOpen(false)}
        footer={null}
        width={900}
        style={{ top: 20 }}
      >
        <Tabs defaultActiveKey="questions">
          {/* TAB 1: QUESTIONS LIST & QUESTION CRUD */}
          <Tabs.TabPane tab="Quản lý Câu hỏi" key="questions" icon={<QuestionCircleOutlined />}>
            <div style={{ display: 'flex', gap: '20px', marginTop: 16 }}>
              {/* Left Column: Question List */}
              <div style={{ width: '45%', borderRight: '1px solid var(--border-color)', paddingRight: '16px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
                  <Text strong>Danh sách Câu hỏi ({quizQuestions.length})</Text>
                  <Button
                    type="primary"
                    size="small"
                    icon={<PlusOutlined />}
                    onClick={() => {
                      setEditingQuestion(null);
                      questionForm.resetFields();
                      setAnswers([
                        { content: '', isCorrect: false },
                        { content: '', isCorrect: false }
                      ]);
                    }}
                  >
                    Thêm câu hỏi
                  </Button>
                </div>

                <div style={{ maxHeight: '450px', overflowY: 'auto' }}>
                  {quizQuestions.length === 0 ? (
                    <Text type="secondary" style={{ display: 'block', textAlign: 'center', margin: '24px 0' }}>
                      Chưa có câu hỏi nào được tạo.
                    </Text>
                  ) : (
                    <List
                      dataSource={quizQuestions}
                      renderItem={(q, index) => (
                        <div
                          style={{
                            padding: '10px',
                            background: editingQuestion?.id === q.id ? '#e6f7ff' : '#fafafa',
                            border: '1px solid #d9d9d9',
                            borderRadius: '4px',
                            marginBottom: '8px',
                            color: '#333'
                          }}
                        >
                          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                            <span style={{ fontWeight: 'bold' }}>Câu {index + 1} ({q.points}đ):</span>
                            <Space size="small">
                              <Button
                                size="small"
                                type="text"
                                icon={<ArrowUpOutlined />}
                                disabled={index === 0}
                                onClick={() => handleMoveQuestion(index, 'up')}
                              />
                              <Button
                                size="small"
                                type="text"
                                icon={<ArrowDownOutlined />}
                                disabled={index === quizQuestions.length - 1}
                                onClick={() => handleMoveQuestion(index, 'down')}
                              />
                            </Space>
                          </div>
                          <div style={{ margin: '6px 0', fontSize: '14px', wordBreak: 'break-word' }}>{q.content}</div>
                          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 8 }}>
                            <Tag color="purple">{q.questionType}</Tag>
                            <Space>
                              <Button size="small" icon={<EditOutlined />} onClick={() => handleEditQuestionClick(q)}>Sửa</Button>
                              <Button size="small" danger icon={<DeleteOutlined />} onClick={() => handleDeleteQuestion(q.id)} />
                            </Space>
                          </div>
                        </div>
                      )}
                    />
                  )}
                </div>
              </div>

              {/* Right Column: Question Details Form */}
              <div style={{ width: '55%' }}>
                <Text strong>{editingQuestion ? 'Cập nhật câu hỏi' : 'Tạo câu hỏi mới'}</Text>
                <Form form={questionForm} layout="vertical" onFinish={handleSaveQuestion} style={{ marginTop: '12px' }} noValidate>
                  <Form.Item label="Nội dung câu hỏi" name="content" rules={[{ required: true, message: 'Trường này là bắt buộc' }]}>
                    <Input.TextArea rows={3} placeholder="Ví dụ: Đâu là ngôn ngữ lập trình hướng đối tượng?" />
                  </Form.Item>

                  <div style={{ display: 'flex', gap: '16px' }}>
                    <Form.Item label="Loại câu hỏi" name="questionType" style={{ flex: 1 }} initialValue="SINGLE_CHOICE">
                      <Select onChange={handleQuestionTypeChange}>
                        <Option value="SINGLE_CHOICE">Single Choice (1 đáp án đúng)</Option>
                        <Option value="MULTIPLE_CHOICE">Multiple Choice (nhiều đáp án đúng)</Option>
                        <Option value="TRUE_FALSE">True / False (Đúng / Sai)</Option>
                      </Select>
                    </Form.Item>

                    <Form.Item
                      label="Điểm số"
                      name="points"
                      style={{ width: '100px' }}
                      initialValue={1}
                      rules={[
                        {
                          validator: (_, value) => {
                            if (value !== undefined && value !== null && value !== '') {
                              if (Number(value) < 0.5) {
                                return Promise.reject(new Error('Điểm tối thiểu là 0.5'));
                              }
                            }
                            return Promise.resolve();
                          }
                        }
                      ]}
                    >
                      <Input type="number" min={0.5} step={0.5} />
                    </Form.Item>
                  </div>


                  <Form.Item label="Giải thích đáp án" name="explanation">
                    <Input.TextArea rows={2} placeholder="Giải thích vì sao đáp án đó đúng khi học viên nộp bài..." />
                  </Form.Item>

                  {/* Answers Editor Block */}
                  <div style={{ marginBottom: 16 }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
                      <Text strong>Các phương án trả lời ({answers.length})</Text>
                      {questionForm.getFieldValue('questionType') !== 'TRUE_FALSE' && (
                        <Button type="dashed" size="small" icon={<PlusOutlined />} onClick={handleAddAnswer}>
                          Thêm đáp án
                        </Button>
                      )}
                    </div>

                    <Form.Item shouldUpdate noStyle>
                      {() => {
                        const qType = questionForm.getFieldValue('questionType');
                        return (
                          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                            {answers.map((ans, idx) => (
                              <div key={idx} style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                                {qType === 'MULTIPLE_CHOICE' ? (
                                  <Checkbox
                                    checked={ans.isCorrect}
                                    onChange={(e) => handleAnswerChange(idx, 'isCorrect', e.target.checked)}
                                  />
                                ) : (
                                  <Radio
                                    checked={ans.isCorrect}
                                    onChange={(e) => handleAnswerChange(idx, 'isCorrect', e.target.checked)}
                                  />
                                )}
                                <Input
                                  placeholder={`Đáp án ${idx + 1}`}
                                  value={ans.content}
                                  onChange={(e) => handleAnswerChange(idx, 'content', e.target.value)}
                                  style={{ flex: 1 }}
                                  disabled={qType === 'TRUE_FALSE'} // True/False has fixed content
                                />
                                {qType !== 'TRUE_FALSE' && (
                                  <Button
                                    type="text"
                                    danger
                                    icon={<DeleteOutlined />}
                                    onClick={() => handleRemoveAnswer(idx)}
                                  />
                                )}
                              </div>
                            ))}
                          </div>
                        );
                      }}
                    </Form.Item>
                  </div>

                  <Form.Item style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: 0 }}>
                    <Space>
                      {editingQuestion && (
                        <Button onClick={() => {
                          setEditingQuestion(null);
                          setAnswers([]);
                          questionForm.resetFields();
                        }}>
                          Hủy sửa
                        </Button>
                      )}
                      <Button type="primary" htmlType="submit" icon={<SaveOutlined />}>
                        {editingQuestion ? 'Lưu câu hỏi' : 'Tạo câu hỏi'}
                      </Button>
                    </Space>
                  </Form.Item>
                </Form>
              </div>
            </div>
          </Tabs.TabPane>

          {/* TAB 2: CONFIGURATION */}
          <Tabs.TabPane tab="Cấu hình bài Quiz" key="config" icon={<SettingOutlined />}>
            <div style={{ padding: '24px', maxWidth: '500px' }}>
              <div style={{ marginBottom: 16 }}>
                <Text>Thời gian giới hạn (phút):</Text>
                <Input
                  type="number"
                  min={1}
                  value={quizConfig.timeLimitMinutes}
                  onChange={(e) => setQuizConfig({ ...quizConfig, timeLimitMinutes: Number(e.target.value) })}
                  style={{ marginTop: 8 }}
                />
                {configErrors.timeLimitMinutes && <div className="input-error">{configErrors.timeLimitMinutes}</div>}
              </div>

              <div style={{ marginBottom: 16 }}>
                <Text>Điểm số đạt yêu cầu (%):</Text>
                <Input
                  type="number"
                  min={1}
                  max={100}
                  value={quizConfig.passingScore}
                  onChange={(e) => setQuizConfig({ ...quizConfig, passingScore: Number(e.target.value) })}
                  style={{ marginTop: 8 }}
                />
                {configErrors.passingScore && <div className="input-error">{configErrors.passingScore}</div>}
              </div>

              <div style={{ marginBottom: 16 }}>
                <Text>Số lần thi tối đa:</Text>
                <Input
                  type="number"
                  min={1}
                  value={quizConfig.maxAttempts}
                  onChange={(e) => setQuizConfig({ ...quizConfig, maxAttempts: Number(e.target.value) })}
                  style={{ marginTop: 8 }}
                />
                {configErrors.maxAttempts && <div className="input-error">{configErrors.maxAttempts}</div>}
              </div>


              <div style={{ marginBottom: 16 }}>
                <Checkbox
                  checked={quizConfig.shuffleQuestions}
                  onChange={(e) => setQuizConfig({ ...quizConfig, shuffleQuestions: e.target.checked })}
                  style={{ color: 'var(--text-color)' }}
                >
                  Xáo trộn danh sách câu hỏi khi học viên làm bài
                </Checkbox>
              </div>

              <div style={{ marginBottom: 24 }}>
                <Checkbox
                  checked={quizConfig.shuffleAnswers}
                  onChange={(e) => setQuizConfig({ ...quizConfig, shuffleAnswers: e.target.checked })}
                  style={{ color: 'var(--text-color)' }}
                >
                  Xáo trộn các phương án trả lời trong từng câu hỏi
                </Checkbox>
              </div>

              <Button type="primary" icon={<SaveOutlined />} onClick={handleSaveQuizConfig}>
                Lưu cấu hình Quiz
              </Button>
            </div>
          </Tabs.TabPane>

          {/* TAB 3: PREVIEW */}
          <Tabs.TabPane tab="Xem trước Quiz (Preview)" key="preview" icon={<EyeOutlined />}>
            <div style={{ padding: '16px', maxHeight: '500px', overflowY: 'auto' }}>
              <div style={{ marginBottom: 16, borderBottom: '1px solid #eee', paddingBottom: 12 }}>
                <Title level={4}>{quizLesson?.title}</Title>
                <Space>
                  <Tag color="blue">Giới hạn thời gian: {quizConfig.timeLimitMinutes} phút</Tag>
                  <Tag color="green">Điểm đạt: {quizConfig.passingScore}%</Tag>
                  <Tag color="orange">Lượt thi tối đa: {quizConfig.maxAttempts}</Tag>
                </Space>
              </div>

              {quizQuestions.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '32px' }}>Chưa có câu hỏi nào để xem trước.</div>
              ) : (
                <Space orientation="vertical" size="large" style={{ width: '100%' }}>
                  {quizQuestions.map((q, idx) => (
                    <Card key={q.id} title={`Câu hỏi ${idx + 1} (${q.points}đ): ${q.content}`}>
                      <Tag color="purple" style={{ marginBottom: 12 }}>{q.questionType}</Tag>

                      <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', marginTop: 8 }}>
                        {q.answers?.map((ans: any) => (
                          <div
                            key={ans.id}
                            style={{
                              padding: '8px 12px',
                              borderRadius: '4px',
                              background: ans.isCorrect ? '#f6ffed' : '#f5f5f5',
                              border: ans.isCorrect ? '1px solid #b7eb8f' : '1px solid #d9d9d9',
                              color: '#333'
                            }}
                          >
                            <Space>
                              {ans.isCorrect ? <Tag color="green">ĐÚNG</Tag> : <Tag color="default">SAI</Tag>}
                              <span>{ans.content}</span>
                            </Space>
                          </div>
                        ))}
                      </div>

                      {q.explanation && (
                        <div style={{ marginTop: '12px', background: '#fffbe6', padding: '8px', borderRadius: '4px', border: '1px solid #ffe58f', color: '#666' }}>
                          <strong>Giải thích:</strong> {q.explanation}
                        </div>
                      )}
                    </Card>
                  ))}
                </Space>
              )}
            </div>
          </Tabs.TabPane>
        </Tabs>
      </AntdModal>
    </div>
  );
};

export default InstructorLessonManagementPage;
