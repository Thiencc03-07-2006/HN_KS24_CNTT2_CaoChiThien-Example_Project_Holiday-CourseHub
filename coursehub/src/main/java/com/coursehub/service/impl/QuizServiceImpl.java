package com.coursehub.service.impl;

import com.coursehub.dto.request.AnswerDto;
import com.coursehub.dto.request.QuestionDto;
import com.coursehub.dto.request.QuizConfigDto;
import com.coursehub.dto.request.SubmitQuizRequest;
import com.coursehub.entity.*;
import com.coursehub.enums.LessonType;
import com.coursehub.enums.QuizAttemptStatus;
import com.coursehub.exception.BadRequestException;
import com.coursehub.exception.CourseHubException;
import com.coursehub.exception.ResourceNotFoundException;
import com.coursehub.dto.response.QuizConfigResponse;
import com.coursehub.dto.response.QuestionResponse;
import com.coursehub.dto.response.AnswerResponse;
import com.coursehub.repository.*;
import com.coursehub.service.EnrollmentService;
import com.coursehub.service.QuizService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizServiceImpl implements QuizService {

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final QuizConfigRepository quizConfigRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ProgressRepository progressRepository;
    private final EnrollmentService enrollmentService;
    private final ObjectMapper objectMapper;

    private QuizConfigResponse mapQuizConfigToResponse(QuizConfigEntity entity) {
        if (entity == null) return null;
        return QuizConfigResponse.builder()
                .id(entity.getId())
                .lessonId(entity.getLesson().getId())
                .lessonTitle(entity.getLesson().getTitle())
                .passingScore(entity.getPassingScore())
                .timeLimit(entity.getTimeLimitMinutes())
                .timeLimitMinutes(entity.getTimeLimitMinutes())
                .maxAttempts(entity.getMaxAttempts())
                .shuffleQuestions(Boolean.TRUE.equals(entity.getShuffleQuestions()))
                .shuffleAnswers(Boolean.TRUE.equals(entity.getShuffleAnswers()))
                .showCorrectAnswer(true)
                .createdAt(entity.getLesson().getCreatedAt())
                .updatedAt(entity.getLesson().getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public QuizConfigResponse getQuizConfig(UUID lessonId) {
        QuizConfigEntity entity = quizConfigRepository.findByLessonId(lessonId)
                .orElseGet(() -> {
                    LessonEntity lesson = lessonRepository.findById(lessonId)
                            .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));
                    return QuizConfigEntity.builder()
                            .lesson(lesson)
                            .passingScore(new BigDecimal("70.00"))
                            .maxAttempts(3)
                            .shuffleQuestions(false)
                            .shuffleAnswers(false)
                            .build();
                });
        return mapQuizConfigToResponse(entity);
    }

    @Override
    @Transactional
    public QuizConfigResponse saveQuizConfig(UUID instructorId, UUID courseId, UUID lessonId, QuizConfigDto dto) {
        getOwnedCourse(courseId, instructorId);
        LessonEntity lesson = getLessonInCourse(courseId, lessonId);

        QuizConfigEntity config = quizConfigRepository.findByLessonId(lessonId)
                .orElseGet(() -> QuizConfigEntity.builder().lesson(lesson).build());

        config.setTimeLimitMinutes(dto.getTimeLimitMinutes());
        config.setPassingScore(dto.getPassingScore() != null ? dto.getPassingScore() : new BigDecimal("70.00"));
        config.setMaxAttempts(dto.getMaxAttempts() != null ? dto.getMaxAttempts() : 3);
        config.setShuffleQuestions(dto.isShuffleQuestions());
        config.setShuffleAnswers(dto.isShuffleAnswers());

        return mapQuizConfigToResponse(quizConfigRepository.save(config));
    }

    private QuestionResponse mapQuestionToResponse(QuestionEntity q, boolean revealCorrectness) {
        if (q == null) return null;
        List<AnswerResponse> answerResponses = new ArrayList<>();
        if (q.getAnswers() != null) {
            answerResponses = q.getAnswers().stream()
                    .map(a -> AnswerResponse.builder()
                            .id(a.getId())
                            .content(a.getContent())
                            .orderIndex(a.getOrderIndex())
                            .isCorrect(revealCorrectness ? a.getIsCorrect() : null)
                            .build())
                    .collect(Collectors.toList());
        }

        return QuestionResponse.builder()
                .id(q.getId())
                .content(q.getContent())
                .questionType(q.getQuestionType())
                .points(q.getPoints())
                .orderIndex(q.getOrderIndex())
                .explanation(revealCorrectness ? q.getExplanation() : null)
                .answers(answerResponses)
                .build();
    }

    @Override
    @Transactional
    public QuestionResponse addQuestion(UUID instructorId, UUID courseId, UUID lessonId, QuestionDto dto) {
        getOwnedCourse(courseId, instructorId);
        LessonEntity lesson = getLessonInCourse(courseId, lessonId);

        if (lesson.getLessonType() != LessonType.QUIZ) {
            throw new BadRequestException("VALID_001", "Bài học không phải loại trắc nghiệm.");
        }

        log.info("===== CREATE QUESTION =====  lessonId={}, content='{}'", lessonId, dto.getContent());

        QuestionEntity question = QuestionEntity.builder()
                .quiz(lesson)
                .content(dto.getContent())
                .questionType(dto.getQuestionType() != null ? dto.getQuestionType() : "SINGLE_CHOICE")
                .points(dto.getPoints() != null ? dto.getPoints() : BigDecimal.ONE)
                .orderIndex(dto.getOrderIndex() != null ? dto.getOrderIndex() : 1)
                .explanation(dto.getExplanation())
                .build();

        question = questionRepository.save(question);
        log.info("  -> Question saved with ID={}", question.getId());

        if (dto.getAnswers() != null) {
            List<AnswerEntity> answers = new ArrayList<>();
            for (int i = 0; i < dto.getAnswers().size(); i++) {
                AnswerDto aDto = dto.getAnswers().get(i);
                boolean correct = aDto.isCorrect();
                log.info("  -> Answer[{}]: content='{}', isCorrect={}, orderIndex={}",
                        i, aDto.getContent(), correct, aDto.getOrderIndex());
                answers.add(AnswerEntity.builder()
                        .question(question)
                        .content(aDto.getContent())
                        .isCorrect(correct)
                        .orderIndex(aDto.getOrderIndex() != null ? aDto.getOrderIndex() : i + 1)
                        .build());
            }
            List<AnswerEntity> saved = answerRepository.saveAll(answers);
            log.info("  -> Saved {} answers. Correct count={}",
                    saved.size(), saved.stream().filter(a -> Boolean.TRUE.equals(a.getIsCorrect())).count());
            question.setAnswers(saved);
        }
        log.info("============================");

        return mapQuestionToResponse(question, true);
    }

    @Override
    @Transactional
    public QuestionResponse updateQuestion(UUID instructorId, UUID courseId, UUID lessonId, UUID questionId, QuestionDto dto) {
        getOwnedCourse(courseId, instructorId);
        QuestionEntity question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question", "id", questionId));

        if (!question.getQuiz().getId().equals(lessonId)) {
            throw new BadRequestException("VALID_001", "Câu hỏi không thuộc bài trắc nghiệm này.");
        }

        log.info("===== UPDATE QUESTION ===== questionId={}, content='{}'", questionId, dto.getContent());

        question.setContent(dto.getContent());
        if (dto.getQuestionType() != null) question.setQuestionType(dto.getQuestionType());
        if (dto.getPoints() != null) question.setPoints(dto.getPoints());
        if (dto.getOrderIndex() != null) question.setOrderIndex(dto.getOrderIndex());
        question.setExplanation(dto.getExplanation());

        // Update answers
        if (dto.getAnswers() != null) {
            // Delete old answers
            List<AnswerEntity> oldAnswers = answerRepository.findByQuestionIdOrderByOrderIndexAsc(questionId);
            answerRepository.deleteAll(oldAnswers);
            answerRepository.flush();

            List<AnswerEntity> answers = new ArrayList<>();
            for (int i = 0; i < dto.getAnswers().size(); i++) {
                AnswerDto aDto = dto.getAnswers().get(i);
                boolean correct = aDto.isCorrect();
                log.info("  -> Answer[{}]: content='{}', isCorrect={}", i, aDto.getContent(), correct);
                answers.add(AnswerEntity.builder()
                        .question(question)
                        .content(aDto.getContent())
                        .isCorrect(correct)
                        .orderIndex(aDto.getOrderIndex() != null ? aDto.getOrderIndex() : i + 1)
                        .build());
            }
            List<AnswerEntity> saved = answerRepository.saveAll(answers);
            log.info("  -> Updated {} answers. Correct count={}",
                    saved.size(), saved.stream().filter(a -> Boolean.TRUE.equals(a.getIsCorrect())).count());
            question.setAnswers(saved);
        }
        log.info("============================");

        return mapQuestionToResponse(questionRepository.save(question), true);
    }

    @Override
    @Transactional
    public void deleteQuestion(UUID instructorId, UUID courseId, UUID lessonId, UUID questionId) {
        getOwnedCourse(courseId, instructorId);
        QuestionEntity question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question", "id", questionId));
        if (!question.getQuiz().getId().equals(lessonId)) {
            throw new BadRequestException("VALID_001", "Câu hỏi không thuộc bài trắc nghiệm này.");
        }
        questionRepository.delete(question);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionResponse> getQuizQuestionsForStudent(UUID studentId, UUID lessonId) {
        LessonEntity lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));

        // Check enrollment
        boolean enrolled = enrollmentRepository.existsByUserIdAndCourseId(studentId, lesson.getChapter().getCourse().getId());
        if (!enrolled) {
            throw new CourseHubException("AUTHZ_003", "Bạn chưa đăng ký khóa học này.", HttpStatus.FORBIDDEN);
        }

        List<QuestionEntity> questions = questionRepository.findByQuizIdOrderByOrderIndexAsc(lessonId);
        QuizConfigResponse config = getQuizConfig(lessonId);

        if (config.isShuffleQuestions()) {
            Collections.shuffle(questions);
        }
        if (config.isShuffleAnswers()) {
            for (QuestionEntity q : questions) {
                if (q.getAnswers() != null) {
                    Collections.shuffle(q.getAnswers());
                }
            }
        }

        boolean reveal = hasCompletedAttempt(studentId, lessonId);
        return questions.stream()
                .map(q -> mapQuestionToResponse(q, reveal))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public QuizAttemptEntity startQuizAttempt(UUID studentId, UUID lessonId) {
        LessonEntity lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));
        
        UUID courseId = lesson.getChapter().getCourse().getId();
        EnrollmentEntity enrollment = enrollmentRepository.findByUserIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new CourseHubException("AUTHZ_003", "Bạn chưa đăng ký khóa học này.", HttpStatus.FORBIDDEN));

        QuizConfigResponse config = getQuizConfig(lessonId);
        long attemptsCount = quizAttemptRepository.countByEnrollmentIdAndLessonId(enrollment.getId(), lessonId);

        if (attemptsCount >= config.getMaxAttempts()) {
            throw new BadRequestException("QUIZ_001", "Bạn đã hết số lần làm bài trắc nghiệm này.");
        }

        // Check if there is an in-progress attempt
        List<QuizAttemptEntity> attempts = quizAttemptRepository.findByEnrollmentIdAndLessonIdOrderByStartedAtDesc(enrollment.getId(), lessonId);
        if (!attempts.isEmpty() && attempts.get(0).getStatus() == QuizAttemptStatus.IN_PROGRESS) {
            return attempts.get(0); // Return existing in-progress attempt
        }

        QuizAttemptEntity attempt = QuizAttemptEntity.builder()
                .enrollment(enrollment)
                .lesson(lesson)
                .status(QuizAttemptStatus.IN_PROGRESS)
                .startedAt(LocalDateTime.now())
                .build();

        return quizAttemptRepository.save(attempt);
    }

    @Override
    @Transactional
    public QuizAttemptEntity submitQuizAttempt(UUID studentId, UUID lessonId, UUID attemptId, SubmitQuizRequest request) {
        QuizAttemptEntity attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("QuizAttempt", "id", attemptId));

        if (attempt.getStatus() != QuizAttemptStatus.IN_PROGRESS) {
            throw new BadRequestException("QUIZ_002", "Bài thi trắc nghiệm đã được nộp hoặc hết hạn.");
        }

        QuizConfigResponse config = getQuizConfig(lessonId);
        if (config != null && config.getTimeLimitMinutes() != null) {
            long durationSeconds = java.time.Duration.between(attempt.getStartedAt(), LocalDateTime.now()).getSeconds();
            long limitSeconds = config.getTimeLimitMinutes() * 60L;
            if (durationSeconds > limitSeconds + 30) { // 30 seconds grace period
                attempt.setStatus(QuizAttemptStatus.TIMED_OUT);
                attempt.setScore(BigDecimal.ZERO);
                attempt.setSubmittedAt(LocalDateTime.now());
                try {
                    attempt.setAnswersSnapshot(objectMapper.writeValueAsString(request.getSelectedAnswers()));
                } catch (Exception ex) {
                    log.error("Failed to serialize answers snapshot: {}", ex.getMessage());
                }
                return quizAttemptRepository.save(attempt);
            }
        }

        log.info("===== SUBMIT QUIZ ===== attemptId={}, studentId={}, lessonId={}", attemptId, studentId, lessonId);
        log.info("  -> Raw selectedAnswers payload: {}", request.getSelectedAnswers());

        List<QuestionEntity> questions = questionRepository.findByQuizIdOrderByOrderIndexAsc(lessonId);
        BigDecimal totalPoints = BigDecimal.ZERO;
        BigDecimal earnedPoints = BigDecimal.ZERO;
        int correctCount = 0;
        int wrongCount = 0;

        for (QuestionEntity q : questions) {
            totalPoints = totalPoints.add(q.getPoints());

            // Collect correct answer IDs from DB — using null-safe Boolean check
            List<UUID> correctAnswers = q.getAnswers().stream()
                    .filter(a -> Boolean.TRUE.equals(a.getIsCorrect()))
                    .map(AnswerEntity::getId)
                    .collect(Collectors.toList());

            // Get user's selected answer IDs from request
            Map<UUID, List<UUID>> selected = request.getSelectedAnswers();
            List<UUID> userAnswers = (selected != null) ? selected.get(q.getId()) : null;
            if (userAnswers == null) {
                userAnswers = new ArrayList<>();
            }

            boolean isCorrect = new HashSet<>(correctAnswers).equals(new HashSet<>(userAnswers));

            log.info("  -> Question ID={} | correctAnswerIds={} | studentAnswerIds={} | match={}",
                    q.getId(), correctAnswers, userAnswers, isCorrect);

            if (isCorrect) {
                earnedPoints = earnedPoints.add(q.getPoints());
                correctCount++;
            } else {
                wrongCount++;
            }
        }

        BigDecimal percentage = BigDecimal.ZERO;
        if (totalPoints.compareTo(BigDecimal.ZERO) > 0) {
            percentage = earnedPoints.multiply(BigDecimal.valueOf(100))
                    .divide(totalPoints, 2, RoundingMode.HALF_UP);
        }

        boolean passed = percentage.compareTo(config.getPassingScore()) >= 0;

        log.info("===== RESULT =====");
        log.info("  -> Total Points   : {}", totalPoints);
        log.info("  -> Earned Points  : {}", earnedPoints);
        log.info("  -> Score (%)      : {}", percentage);
        log.info("  -> Passing Score  : {}", config.getPassingScore());
        log.info("  -> Correct Count  : {}", correctCount);
        log.info("  -> Wrong Count    : {}", wrongCount);
        log.info("  -> PASSED         : {}", passed);
        log.info("==================");

        attempt.setScore(percentage);
        attempt.setStatus(passed ? QuizAttemptStatus.PASSED : QuizAttemptStatus.FAILED);
        attempt.setSubmittedAt(LocalDateTime.now());

        // Snapshot user answers as JSON
        try {
            attempt.setAnswersSnapshot(objectMapper.writeValueAsString(request.getSelectedAnswers()));
        } catch (Exception ex) {
            log.error("Failed to serialize answers snapshot: {}", ex.getMessage());
        }

        attempt = quizAttemptRepository.save(attempt);
        final QuizAttemptEntity finalAttempt = attempt;

        // If passed, auto complete the quiz lesson
        if (passed) {
            ProgressEntity progress = progressRepository.findByEnrollmentIdAndLessonId(finalAttempt.getEnrollment().getId(), lessonId)
                    .orElseGet(() -> ProgressEntity.builder()
                            .enrollment(finalAttempt.getEnrollment())
                            .lesson(finalAttempt.getLesson())
                            .build());

            progress.setIsCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());
            progressRepository.save(progress);

            enrollmentService.updateEnrollmentProgress(finalAttempt.getEnrollment().getId());
        }

        return attempt;
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionResponse> getQuizQuestionsForInstructor(UUID instructorId, UUID courseId, UUID lessonId) {
        getOwnedCourse(courseId, instructorId);
        getLessonInCourse(courseId, lessonId);
        List<QuestionEntity> questions = questionRepository.findByQuizIdOrderByOrderIndexAsc(lessonId);
        return questions.stream()
                .map(q -> mapQuestionToResponse(q, true))
                .collect(Collectors.toList());
    }

    private CourseEntity getOwnedCourse(UUID courseId, UUID instructorId) {
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        if (!course.isOwnedBy(instructorId)) {
            throw new CourseHubException("AUTHZ_003", "Bạn không phải chủ sở hữu của khóa học này.", HttpStatus.FORBIDDEN);
        }
        return course;
    }

    private LessonEntity getLessonInCourse(UUID courseId, UUID lessonId) {
        LessonEntity lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));
        if (!lesson.getChapter().getCourse().getId().equals(courseId)) {
            throw new BadRequestException("VALID_001", "Bài học không thuộc khóa học này.");
        }
        return lesson;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasCompletedAttempt(UUID studentId, UUID lessonId) {
        LessonEntity lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));
        UUID courseId = lesson.getChapter().getCourse().getId();
        Optional<EnrollmentEntity> enrollmentOpt = enrollmentRepository.findByUserIdAndCourseId(studentId, courseId);
        if (enrollmentOpt.isEmpty()) return false;

        List<QuizAttemptEntity> attempts = quizAttemptRepository.findByEnrollmentIdAndLessonIdOrderByStartedAtDesc(enrollmentOpt.get().getId(), lessonId);
        return attempts.stream().anyMatch(a -> a.getStatus() == QuizAttemptStatus.PASSED || a.getStatus() == QuizAttemptStatus.FAILED);
    }
}
