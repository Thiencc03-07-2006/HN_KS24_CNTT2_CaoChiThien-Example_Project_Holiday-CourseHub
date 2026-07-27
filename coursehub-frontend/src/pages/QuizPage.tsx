import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Typography, Space, Radio, Checkbox, message, Alert, Tag } from 'antd';
import { CheckCircleOutlined, CloseCircleOutlined, ClockCircleOutlined, SafetyCertificateOutlined } from '@ant-design/icons';
import axiosInstance from '../api/axiosInstance';
import { Card } from '../components/common/UI/Card';
import { Button } from '../components/common/UI/Button';
import { Loading } from '../components/common/UI/Loading';

const { Title, Text, Paragraph } = Typography;

const QuizPage: React.FC = () => {
  const { quizId } = useParams<{ quizId: string }>(); // quizId is actually the lessonId
  const navigate = useNavigate();

  const [loading, setLoading] = useState(true);
  const [config, setConfig] = useState<any>(null);
  const [questions, setQuestions] = useState<any[]>([]);
  const [attempt, setAttempt] = useState<any>(null);
  const [selectedAnswers, setSelectedAnswers] = useState<{ [questionId: string]: string[] }>({});
  const [result, setResult] = useState<any>(null);
  const [timeLeft, setTimeLeft] = useState<number | null>(null);

  const startQuiz = async () => {
    try {
      setLoading(true);
      // 1. Get Quiz Config
      const configRes = await axiosInstance.get(`/quiz/${quizId}/config`);
      if (configRes.data?.success) {
        setConfig(configRes.data.data);
      }

      // 2. Start attempt or retrieve existing in-progress attempt
      const attemptRes = await axiosInstance.post(`/quiz/${quizId}/attempts`);
      if (attemptRes.data?.success) {
        const attemptData = attemptRes.data.data;
        setAttempt(attemptData);

        // 3. Load Questions
        const questionsRes = await axiosInstance.get(`/quiz/${quizId}/questions`);
        if (questionsRes.data?.success) {
          setQuestions(questionsRes.data.data || []);
        }

        // Initialize selections if answersSnapshot exists
        if (attemptData.answersSnapshot) {
          try {
            const snapshot = JSON.parse(attemptData.answersSnapshot);
            setSelectedAnswers(snapshot);
          } catch (e) {
            console.error('Failed to parse answers snapshot:', e);
          }
        }

        // 4. Calculate Time Left
        if (configRes.data.data?.timeLimitMinutes) {
          const startTime = new Date(attemptData.startedAt).getTime();
          const limitMs = configRes.data.data.timeLimitMinutes * 60 * 1000;
          const elapsed = Date.now() - startTime;
          const remaining = Math.max(0, Math.floor((limitMs - elapsed) / 1000));
          setTimeLeft(remaining);
        }
      }
    } catch (err: any) {
      console.error(err);
      message.error(err.response?.data?.message || 'Không thể bắt đầu bài trắc nghiệm.');
      navigate(-1);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    startQuiz();
  }, [quizId]);

  // Countdown timer
  useEffect(() => {
    if (timeLeft === null || result) return;
    if (timeLeft <= 0) {
      message.warning('Đã hết thời gian làm bài! Hệ thống đang tự động nộp bài...');
      handleSubmit();
      return;
    }

    const timer = setTimeout(() => {
      setTimeLeft(timeLeft - 1);
    }, 1000);

    return () => clearTimeout(timer);
  }, [timeLeft, result]);

  const handleSelectAnswer = (questionId: string, answerId: string, isMultiple: boolean) => {
    const currentSelections = selectedAnswers[questionId] || [];
    if (isMultiple) {
      if (currentSelections.includes(answerId)) {
        setSelectedAnswers({
          ...selectedAnswers,
          [questionId]: currentSelections.filter(id => id !== answerId)
        });
      } else {
        setSelectedAnswers({
          ...selectedAnswers,
          [questionId]: [...currentSelections, answerId]
        });
      }
    } else {
      setSelectedAnswers({
        ...selectedAnswers,
        [questionId]: [answerId]
      });
    }
  };

  const handleSubmit = async () => {
    if (!attempt) return;
    try {
      setLoading(true);
      const payload = {
        selectedAnswers: selectedAnswers
      };
      const res = await axiosInstance.post(`/quiz/${quizId}/attempts/${attempt.id}/submit`, payload);
      if (res.data?.success) {
        setResult(res.data.data);
        message.success('Đã nộp bài thi trắc nghiệm thành công!');
      }
    } catch (err: any) {
      message.error(err.response?.data?.message || 'Không thể nộp bài.');
    } finally {
      setLoading(false);
    }
  };

  const formatTimer = (seconds: number) => {
    const minutes = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${minutes}:${secs < 10 ? '0' : ''}${secs}`;
  };

  if (loading) {
    return <Loading message="Đang xử lý thông tin bài trắc nghiệm..." />;
  }

  return (
    <div style={{ maxWidth: 800, margin: '0 auto', padding: '24px' }}>
      {/* Sticky Top Header for Timer */}
      {timeLeft !== null && !result && (
        <div style={{
          position: 'sticky',
          top: 0,
          zIndex: 100,
          background: '#141b2d',
          padding: '12px 24px',
          borderRadius: '8px',
          border: '1px solid #2e3b52',
          marginBottom: '24px',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          boxShadow: '0 4px 12px rgba(0,0,0,0.1)'
        }}>
          <Text style={{ color: '#fff', fontSize: '16px', fontWeight: 'bold' }}>
            Thời gian làm bài còn lại:
          </Text>
          <Tag color={timeLeft < 60 ? 'red' : 'blue'} style={{ fontSize: '18px', padding: '4px 12px', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <ClockCircleOutlined />
            {formatTimer(timeLeft)}
          </Tag>
        </div>
      )}

      {result ? (
        <Card style={{ textAlign: 'center', padding: '32px' }}>
          {result.status === 'PASSED' ? (
            <CheckCircleOutlined style={{ fontSize: 72, color: '#52c41a', marginBottom: 16 }} />
          ) : (
            <CloseCircleOutlined style={{ fontSize: 72, color: '#f5222d', marginBottom: 16 }} />
          )}
          <Title level={3} style={{ color: 'var(--text-color)' }}>
            {result.status === 'PASSED' ? 'Chúc mừng! Bạn đã ĐẠT' : 'Rất tiếc! Bạn CHƯA ĐẠT'}
          </Title>
          
          <div style={{ margin: '24px 0', padding: '16px', background: '#f5f5f5', borderRadius: '8px' }}>
            <Paragraph style={{ fontSize: 18, color: '#333', margin: 0 }}>
              Điểm số của bạn: <Text strong style={{ fontSize: 24, color: result.status === 'PASSED' ? '#52c41a' : '#f5222d' }}>{result.score}%</Text>
            </Paragraph>
            <Paragraph style={{ color: '#666', marginTop: 8 }}>
              Yêu cầu điểm đạt tối thiểu: <Text strong>{config?.passingScore}%</Text>
            </Paragraph>
          </div>

          <Alert
            message={result.status === 'PASSED' 
              ? "Tuyệt vời! Bạn đã hoàn thành xuất sắc và đủ điểm vượt qua bài kiểm tra." 
              : "Bạn không đạt đủ số điểm cần thiết. Đừng lo lắng, hãy ôn tập lại lý thuyết bài học và thử lại."
            }
            type={result.status === 'PASSED' ? "success" : "warning"}
            showIcon
            style={{ marginBottom: 24, textAlign: 'left' }}
          />

          <Button type="primary" size="large" onClick={() => navigate(-1)}>
            Quay lại bài học
          </Button>
        </Card>
      ) : (
        <Space orientation="vertical" size="large" style={{ width: '100%' }}>
          <div style={{ marginBottom: 16 }}>
            <Title level={3} style={{ color: 'var(--text-color)', margin: 0 }}>
              Bài kiểm tra trắc nghiệm
            </Title>
            <Space style={{ marginTop: 8 }}>
              <Tag color="cyan">Tổng số: {questions.length} câu hỏi</Tag>
              <Tag color="blue">Yêu cầu đạt: {config?.passingScore}%</Tag>
            </Space>
          </div>

          {questions.map((q, idx) => {
            const isMultiple = q.questionType === 'MULTIPLE_CHOICE';
            const selections = selectedAnswers[q.id] || [];

            return (
              <Card 
                key={q.id} 
                title={
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', width: '100%' }}>
                    <Text strong style={{ color: 'var(--text-color)', fontSize: '16px', whiteSpace: 'normal', wordBreak: 'break-word' }}>
                      {`Câu hỏi ${idx + 1}: ${q.content}`}
                    </Text>
                    <Tag color="purple">{isMultiple ? 'Nhiều lựa chọn' : 'Một lựa chọn'}</Tag>
                  </div>
                }
              >
                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', marginTop: 8 }}>
                  {q.answers?.map((ans: any) => {
                    const isSelected = selections.includes(ans.id);
                    return (
                      <div 
                        key={ans.id}
                        onClick={() => handleSelectAnswer(q.id, ans.id, isMultiple)}
                        style={{
                          padding: '12px 16px',
                          borderRadius: '6px',
                          border: isSelected ? '1px solid var(--primary-color)' : '1px solid var(--border-color)',
                          background: isSelected ? 'rgba(24, 144, 255, 0.05)' : 'transparent',
                          cursor: 'pointer',
                          display: 'flex',
                          alignItems: 'center',
                          gap: '12px',
                          transition: 'all 0.2s'
                        }}
                      >
                        {isMultiple ? (
                          <Checkbox checked={isSelected} onChange={() => {}} />
                        ) : (
                          <Radio checked={isSelected} onChange={() => {}} />
                        )}
                        <span style={{ color: 'var(--text-color)', fontSize: '15px' }}>{ans.content}</span>
                      </div>
                    );
                  })}
                </div>
              </Card>
            );
          })}

          <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: 16, marginBottom: 40 }}>
            <Button type="primary" size="large" icon={<SafetyCertificateOutlined />} onClick={handleSubmit}>
              Nộp bài kiểm tra
            </Button>
          </div>
        </Space>
      )}
    </div>
  );
};

export default QuizPage;
