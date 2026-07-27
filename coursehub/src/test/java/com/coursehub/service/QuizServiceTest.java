package com.coursehub.service;

import com.coursehub.dto.request.AnswerDto;
import com.coursehub.dto.request.QuestionDto;
import com.coursehub.dto.request.SubmitQuizRequest;
import com.coursehub.dto.response.QuestionResponse;
import com.coursehub.dto.response.QuizAttemptResponse;
import com.coursehub.entity.*;
import com.coursehub.enums.LessonType;
import com.coursehub.enums.QuizAttemptStatus;
import com.coursehub.repository.*;
import com.coursehub.service.impl.QuizServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;


import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for QuizServiceImpl — covering the full quiz flow:
 *  - Question creation with correct isCorrect flag
 *  - Answer saving with isCorrect persisted correctly
 *  - Grading logic: single correct answer, multiple correct, wrong, mixed
 *  - Result scoring
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("QuizService Unit Tests")
class QuizServiceTest {

    @Mock private CourseRepository courseRepository;
    @Mock private LessonRepository lessonRepository;
    @Mock private QuizConfigRepository quizConfigRepository;
    @Mock private QuestionRepository questionRepository;
    @Mock private AnswerRepository answerRepository;
    @Mock private QuizAttemptRepository quizAttemptRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private ProgressRepository progressRepository;
    @Mock private EnrollmentService enrollmentService;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks private QuizServiceImpl quizService;

    // ============================================================
    //  Helper factories
    // ============================================================

    private UUID uuid() { return UUID.randomUUID(); }

    private LessonEntity quizLesson(UUID lessonId, UUID courseId) {
        ChapterEntity chapter = new ChapterEntity();
        CourseEntity course = new CourseEntity();
        course.setId(courseId);
        chapter.setCourse(course);
        LessonEntity lesson = new LessonEntity();
        lesson.setId(lessonId);
        lesson.setTitle("Quiz Lesson");
        lesson.setLessonType(LessonType.QUIZ);
        lesson.setChapter(chapter);
        return lesson;
    }

    private AnswerEntity answer(UUID qId, String content, boolean isCorrect, int orderIndex) {
        return AnswerEntity.builder()
                .id(uuid())
                .content(content)
                .isCorrect(isCorrect)
                .orderIndex(orderIndex)
                .build();
    }

    private QuestionEntity questionWithAnswers(UUID lessonId, List<AnswerEntity> answers) {
        UUID qId = uuid();
        QuestionEntity q = QuestionEntity.builder()
                .id(qId)
                .content("Sample question?")
                .questionType("SINGLE_CHOICE")
                .points(BigDecimal.ONE)
                .orderIndex(1)
                .answers(answers)
                .build();
        answers.forEach(a -> a.setQuestion(q));
        return q;
    }

    private CourseEntity courseOwnedBy(UUID instructorId) {
        CourseEntity c = new CourseEntity();
        c.setId(uuid());
        // CourseEntity.isOwnedBy() checks this.instructor.getId()
        UserEntity instructor = new UserEntity();
        instructor.setId(instructorId);
        c.setInstructor(instructor);
        return c;
    }

    // ============================================================
    // 1. AnswerDto isCorrect deserialization (unit-level check)
    // ============================================================

    @Nested
    @DisplayName("AnswerDto isCorrect field binding")
    class AnswerDtoTests {

        @Test
        @DisplayName("AnswerDto.setIsCorrect(true) should make isCorrect() return true")
        void answerDto_setIsCorrect_true() {
            AnswerDto dto = new AnswerDto();
            dto.setIsCorrect(true);
            assertThat(dto.isCorrect()).isTrue();
        }

        @Test
        @DisplayName("AnswerDto.setIsCorrect(false) should make isCorrect() return false")
        void answerDto_setIsCorrect_false() {
            AnswerDto dto = new AnswerDto();
            dto.setIsCorrect(false);
            assertThat(dto.isCorrect()).isFalse();
        }

        @Test
        @DisplayName("AnswerDto default isCorrect should be null (wrapper Boolean)")
        void answerDto_default_isCorrect_null() {
            AnswerDto dto = new AnswerDto();
            // With Boolean wrapper, default is null, not false
            // isCorrect() convenience method treats null as false
            assertThat(dto.isCorrect()).isFalse();
        }

        @Test
        @DisplayName("Jackson can deserialize isCorrect:true from JSON")
        void answerDto_jackson_deserialization() throws Exception {
            ObjectMapper mapper = new ObjectMapper();
            String json = "{\"content\":\"Answer A\",\"isCorrect\":true,\"orderIndex\":1}";
            AnswerDto dto = mapper.readValue(json, AnswerDto.class);
            assertThat(dto.isCorrect()).isTrue();
            assertThat(dto.getContent()).isEqualTo("Answer A");
        }

        @Test
        @DisplayName("Jackson can deserialize isCorrect:false from JSON")
        void answerDto_jackson_deserialization_false() throws Exception {
            ObjectMapper mapper = new ObjectMapper();
            String json = "{\"content\":\"Answer B\",\"isCorrect\":false,\"orderIndex\":2}";
            AnswerDto dto = mapper.readValue(json, AnswerDto.class);
            assertThat(dto.isCorrect()).isFalse();
        }
    }

    // ============================================================
    // 2. addQuestion — verify isCorrect is saved correctly
    // ============================================================

    @Nested
    @DisplayName("addQuestion — isCorrect persistence")
    class AddQuestionTests {

        @Test
        @DisplayName("Single correct answer should be saved with isCorrect=true")
        void addQuestion_singleCorrectAnswer_savedCorrectly() {
            UUID instructorId = uuid(), courseId = uuid(), lessonId = uuid();
            CourseEntity course = courseOwnedBy(instructorId);
            LessonEntity lesson = quizLesson(lessonId, courseId);

            when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
            when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

            // Capture answers saved to repository
            List<AnswerEntity> capturedAnswers = new ArrayList<>();
            when(answerRepository.saveAll(anyList())).thenAnswer(inv -> {
                List<AnswerEntity> args = inv.getArgument(0);
                // Simulate DB assigning IDs
                args.forEach(a -> a.setId(uuid()));
                capturedAnswers.addAll(args);
                return args;
            });

            UUID questionId = uuid();
            when(questionRepository.save(any(QuestionEntity.class))).thenAnswer(inv -> {
                QuestionEntity q = inv.getArgument(0);
                q.setId(questionId);
                return q;
            });

            QuestionDto dto = QuestionDto.builder()
                    .content("What is 2 + 2?")
                    .questionType("SINGLE_CHOICE")
                    .points(BigDecimal.ONE)
                    .orderIndex(1)
                    .answers(List.of(
                            AnswerDto.builder().content("3").isCorrect(false).orderIndex(1).build(),
                            AnswerDto.builder().content("4").isCorrect(true).orderIndex(2).build(),
                            AnswerDto.builder().content("5").isCorrect(false).orderIndex(3).build()
                    ))
                    .build();

            QuestionResponse response = quizService.addQuestion(instructorId, courseId, lessonId, dto);

            // === CRITICAL ASSERTIONS ===
            assertThat(capturedAnswers).hasSize(3);

            long correctCount = capturedAnswers.stream()
                    .filter(a -> Boolean.TRUE.equals(a.getIsCorrect()))
                    .count();
            assertThat(correctCount)
                    .as("Exactly 1 answer must be saved as isCorrect=true")
                    .isEqualTo(1);

            AnswerEntity correctAnswer = capturedAnswers.stream()
                    .filter(a -> Boolean.TRUE.equals(a.getIsCorrect()))
                    .findFirst().orElseThrow();
            assertThat(correctAnswer.getContent()).isEqualTo("4");

            // Verify response
            assertThat(response).isNotNull();
            assertThat(response.getAnswers()).hasSize(3);
            long responseCorrectCount = response.getAnswers().stream()
                    .filter(a -> Boolean.TRUE.equals(a.getIsCorrect()))
                    .count();
            assertThat(responseCorrectCount).isEqualTo(1);
        }

        @Test
        @DisplayName("Multiple correct answers should all be saved with isCorrect=true")
        void addQuestion_multipleCorrectAnswers_allSavedCorrectly() {
            UUID instructorId = uuid(), courseId = uuid(), lessonId = uuid();
            CourseEntity course = courseOwnedBy(instructorId);
            LessonEntity lesson = quizLesson(lessonId, courseId);

            when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
            when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

            List<AnswerEntity> capturedAnswers = new ArrayList<>();
            when(answerRepository.saveAll(anyList())).thenAnswer(inv -> {
                List<AnswerEntity> args = inv.getArgument(0);
                args.forEach(a -> a.setId(uuid()));
                capturedAnswers.addAll(args);
                return args;
            });

            when(questionRepository.save(any())).thenAnswer(inv -> {
                QuestionEntity q = inv.getArgument(0);
                q.setId(uuid());
                return q;
            });

            QuestionDto dto = QuestionDto.builder()
                    .content("Select all even numbers:")
                    .questionType("MULTIPLE_CHOICE")
                    .points(new BigDecimal("2"))
                    .answers(List.of(
                            AnswerDto.builder().content("1").isCorrect(false).orderIndex(1).build(),
                            AnswerDto.builder().content("2").isCorrect(true).orderIndex(2).build(),
                            AnswerDto.builder().content("3").isCorrect(false).orderIndex(3).build(),
                            AnswerDto.builder().content("4").isCorrect(true).orderIndex(4).build()
                    ))
                    .build();

            quizService.addQuestion(instructorId, courseId, lessonId, dto);

            assertThat(capturedAnswers).hasSize(4);
            long correctCount = capturedAnswers.stream()
                    .filter(a -> Boolean.TRUE.equals(a.getIsCorrect()))
                    .count();
            assertThat(correctCount)
                    .as("2 answers must be saved as isCorrect=true")
                    .isEqualTo(2);
        }
    }

    // ============================================================
    // 3. submitQuizAttempt — grading logic
    // ============================================================

    @Nested
    @DisplayName("submitQuizAttempt — grading")
    class SubmitQuizTests {

        private UUID lessonId, attemptId, enrollmentId, studentId;
        private EnrollmentEntity enrollment;
        private LessonEntity lesson;
        private QuizAttemptEntity attempt;

        @BeforeEach
        void setup() {
            lessonId = uuid();
            attemptId = uuid();
            enrollmentId = uuid();
            studentId = uuid();

            UUID courseId = uuid();
            lesson = quizLesson(lessonId, courseId);

            UserEntity student = new UserEntity();
            student.setId(studentId);

            enrollment = new EnrollmentEntity();
            enrollment.setId(enrollmentId);
            enrollment.setUser(student);

            attempt = QuizAttemptEntity.builder()
                    .id(attemptId)
                    .enrollment(enrollment)
                    .lesson(lesson)
                    .status(QuizAttemptStatus.IN_PROGRESS)
                    .build();
        }

        private void setupQuizConfig(UUID lessonId) {
            when(quizConfigRepository.findByLessonId(lessonId)).thenReturn(Optional.empty());
            when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        }

        private void mockAttemptSave() {
            when(quizAttemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        }

        @Test
        @DisplayName("Student chooses the correct single answer → score = 100%")
        void submitQuiz_correctSingleAnswer_score100() throws Exception {
            UUID correctAnswerId = uuid();
            UUID wrongAnswerId = uuid();

            QuestionEntity q = QuestionEntity.builder()
                    .id(uuid())
                    .content("What is the capital of France?")
                    .questionType("SINGLE_CHOICE")
                    .points(BigDecimal.ONE)
                    .orderIndex(1)
                    .answers(List.of(
                            AnswerEntity.builder().id(correctAnswerId).content("Paris").isCorrect(true).orderIndex(1).build(),
                            AnswerEntity.builder().id(wrongAnswerId).content("London").isCorrect(false).orderIndex(2).build()
                    ))
                    .build();

            when(quizAttemptRepository.findById(attemptId)).thenReturn(Optional.of(attempt));
            when(questionRepository.findByQuizIdOrderByOrderIndexAsc(lessonId)).thenReturn(List.of(q));
            setupQuizConfig(lessonId);
            mockAttemptSave();
            when(objectMapper.writeValueAsString(any())).thenReturn("{}");

            SubmitQuizRequest request = new SubmitQuizRequest();
            request.setSelectedAnswers(Map.of(q.getId(), List.of(correctAnswerId)));

            QuizAttemptEntity result = quizService.submitQuizAttempt(studentId, lessonId, attemptId, request);

            assertThat(result.getScore()).isEqualByComparingTo(new BigDecimal("100.00"));
            assertThat(result.getStatus()).isEqualTo(QuizAttemptStatus.PASSED);
        }

        @Test
        @DisplayName("Student chooses wrong answer → score = 0%")
        void submitQuiz_wrongAnswer_score0() throws Exception {
            UUID correctAnswerId = uuid();
            UUID wrongAnswerId = uuid();

            QuestionEntity q = QuestionEntity.builder()
                    .id(uuid())
                    .content("What is 2 + 2?")
                    .questionType("SINGLE_CHOICE")
                    .points(BigDecimal.ONE)
                    .orderIndex(1)
                    .answers(List.of(
                            AnswerEntity.builder().id(correctAnswerId).content("4").isCorrect(true).orderIndex(1).build(),
                            AnswerEntity.builder().id(wrongAnswerId).content("5").isCorrect(false).orderIndex(2).build()
                    ))
                    .build();

            when(quizAttemptRepository.findById(attemptId)).thenReturn(Optional.of(attempt));
            when(questionRepository.findByQuizIdOrderByOrderIndexAsc(lessonId)).thenReturn(List.of(q));
            setupQuizConfig(lessonId);
            mockAttemptSave();
            when(objectMapper.writeValueAsString(any())).thenReturn("{}");

            SubmitQuizRequest request = new SubmitQuizRequest();
            request.setSelectedAnswers(Map.of(q.getId(), List.of(wrongAnswerId)));

            QuizAttemptEntity result = quizService.submitQuizAttempt(studentId, lessonId, attemptId, request);

            assertThat(result.getScore()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.getStatus()).isEqualTo(QuizAttemptStatus.FAILED);
        }

        @Test
        @DisplayName("Multiple questions: student gets 2/3 correct → score ~66.67%")
        void submitQuiz_multipleQuestions_partialCorrect() throws Exception {
            UUID q1CorrectId = uuid(), q1WrongId = uuid();
            UUID q2CorrectId = uuid(), q2WrongId = uuid();
            UUID q3CorrectId = uuid(), q3WrongId = uuid();

            QuestionEntity q1 = QuestionEntity.builder()
                    .id(uuid()).content("Q1").questionType("SINGLE_CHOICE").points(BigDecimal.ONE).orderIndex(1)
                    .answers(List.of(
                            AnswerEntity.builder().id(q1CorrectId).content("A").isCorrect(true).orderIndex(1).build(),
                            AnswerEntity.builder().id(q1WrongId).content("B").isCorrect(false).orderIndex(2).build()
                    )).build();

            QuestionEntity q2 = QuestionEntity.builder()
                    .id(uuid()).content("Q2").questionType("SINGLE_CHOICE").points(BigDecimal.ONE).orderIndex(2)
                    .answers(List.of(
                            AnswerEntity.builder().id(q2CorrectId).content("C").isCorrect(true).orderIndex(1).build(),
                            AnswerEntity.builder().id(q2WrongId).content("D").isCorrect(false).orderIndex(2).build()
                    )).build();

            QuestionEntity q3 = QuestionEntity.builder()
                    .id(uuid()).content("Q3").questionType("SINGLE_CHOICE").points(BigDecimal.ONE).orderIndex(3)
                    .answers(List.of(
                            AnswerEntity.builder().id(q3CorrectId).content("E").isCorrect(true).orderIndex(1).build(),
                            AnswerEntity.builder().id(q3WrongId).content("F").isCorrect(false).orderIndex(2).build()
                    )).build();

            when(quizAttemptRepository.findById(attemptId)).thenReturn(Optional.of(attempt));
            when(questionRepository.findByQuizIdOrderByOrderIndexAsc(lessonId))
                    .thenReturn(List.of(q1, q2, q3));
            setupQuizConfig(lessonId);
            mockAttemptSave();
            when(objectMapper.writeValueAsString(any())).thenReturn("{}");

            // Student gets Q1 correct, Q2 wrong, Q3 correct
            SubmitQuizRequest request = new SubmitQuizRequest();
            request.setSelectedAnswers(Map.of(
                    q1.getId(), List.of(q1CorrectId),  // ✔
                    q2.getId(), List.of(q2WrongId),    // ✘
                    q3.getId(), List.of(q3CorrectId)   // ✔
            ));

            QuizAttemptEntity result = quizService.submitQuizAttempt(studentId, lessonId, attemptId, request);

            // 2/3 = 66.67%
            assertThat(result.getScore()).isEqualByComparingTo(new BigDecimal("66.67"));
            assertThat(result.getStatus()).isEqualTo(QuizAttemptStatus.FAILED); // < 70% passing
        }

        @Test
        @DisplayName("Multiple correct answers: student selects all correct → full points")
        void submitQuiz_multipleChoice_allCorrect_fullPoints() throws Exception {
            UUID a1Id = uuid(), a2Id = uuid(), a3Id = uuid();

            QuestionEntity q = QuestionEntity.builder()
                    .id(uuid()).content("Select all prime numbers")
                    .questionType("MULTIPLE_CHOICE").points(new BigDecimal("2")).orderIndex(1)
                    .answers(List.of(
                            AnswerEntity.builder().id(a1Id).content("2").isCorrect(true).orderIndex(1).build(),
                            AnswerEntity.builder().id(a2Id).content("3").isCorrect(true).orderIndex(2).build(),
                            AnswerEntity.builder().id(a3Id).content("4").isCorrect(false).orderIndex(3).build()
                    )).build();

            when(quizAttemptRepository.findById(attemptId)).thenReturn(Optional.of(attempt));
            when(questionRepository.findByQuizIdOrderByOrderIndexAsc(lessonId)).thenReturn(List.of(q));
            setupQuizConfig(lessonId);
            mockAttemptSave();
            when(objectMapper.writeValueAsString(any())).thenReturn("{}");

            SubmitQuizRequest request = new SubmitQuizRequest();
            // Student selects both correct answers
            request.setSelectedAnswers(Map.of(q.getId(), List.of(a1Id, a2Id)));

            QuizAttemptEntity result = quizService.submitQuizAttempt(studentId, lessonId, attemptId, request);

            assertThat(result.getScore()).isEqualByComparingTo(new BigDecimal("100.00"));
            assertThat(result.getStatus()).isEqualTo(QuizAttemptStatus.PASSED);
        }

        @Test
        @DisplayName("Multiple correct answers: student selects only one of them → 0 points for that question")
        void submitQuiz_multipleChoice_partialSelection_zeroPoints() throws Exception {
            UUID a1Id = uuid(), a2Id = uuid(), a3Id = uuid();

            QuestionEntity q = QuestionEntity.builder()
                    .id(uuid()).content("Select all even numbers")
                    .questionType("MULTIPLE_CHOICE").points(new BigDecimal("2")).orderIndex(1)
                    .answers(List.of(
                            AnswerEntity.builder().id(a1Id).content("2").isCorrect(true).orderIndex(1).build(),
                            AnswerEntity.builder().id(a2Id).content("4").isCorrect(true).orderIndex(2).build(),
                            AnswerEntity.builder().id(a3Id).content("5").isCorrect(false).orderIndex(3).build()
                    )).build();

            when(quizAttemptRepository.findById(attemptId)).thenReturn(Optional.of(attempt));
            when(questionRepository.findByQuizIdOrderByOrderIndexAsc(lessonId)).thenReturn(List.of(q));
            setupQuizConfig(lessonId);
            mockAttemptSave();
            when(objectMapper.writeValueAsString(any())).thenReturn("{}");

            SubmitQuizRequest request = new SubmitQuizRequest();
            // Student selects only one of two correct answers
            request.setSelectedAnswers(Map.of(q.getId(), List.of(a1Id)));

            QuizAttemptEntity result = quizService.submitQuizAttempt(studentId, lessonId, attemptId, request);

            assertThat(result.getScore()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Student skips a question (no answer) → 0 points for that question")
        void submitQuiz_skippedQuestion_zeroPoints() throws Exception {
            UUID correctId = uuid();

            QuestionEntity q = QuestionEntity.builder()
                    .id(uuid()).content("Skipped question")
                    .questionType("SINGLE_CHOICE").points(BigDecimal.ONE).orderIndex(1)
                    .answers(List.of(
                            AnswerEntity.builder().id(correctId).content("A").isCorrect(true).orderIndex(1).build()
                    )).build();

            when(quizAttemptRepository.findById(attemptId)).thenReturn(Optional.of(attempt));
            when(questionRepository.findByQuizIdOrderByOrderIndexAsc(lessonId)).thenReturn(List.of(q));
            setupQuizConfig(lessonId);
            mockAttemptSave();
            when(objectMapper.writeValueAsString(any())).thenReturn("{}");

            SubmitQuizRequest request = new SubmitQuizRequest();
            request.setSelectedAnswers(Collections.emptyMap()); // student answered nothing

            QuizAttemptEntity result = quizService.submitQuizAttempt(studentId, lessonId, attemptId, request);

            assertThat(result.getScore()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.getStatus()).isEqualTo(QuizAttemptStatus.FAILED);
        }

        @Test
        @DisplayName("All questions answered correctly → PASSED with score >= passingScore")
        void submitQuiz_allCorrect_passed() throws Exception {
            UUID c1 = uuid(), c2 = uuid();

            QuestionEntity q1 = QuestionEntity.builder()
                    .id(uuid()).content("Q1").questionType("SINGLE_CHOICE").points(BigDecimal.ONE).orderIndex(1)
                    .answers(List.of(
                            AnswerEntity.builder().id(c1).content("A").isCorrect(true).orderIndex(1).build(),
                            AnswerEntity.builder().id(uuid()).content("B").isCorrect(false).orderIndex(2).build()
                    )).build();

            QuestionEntity q2 = QuestionEntity.builder()
                    .id(uuid()).content("Q2").questionType("SINGLE_CHOICE").points(BigDecimal.ONE).orderIndex(2)
                    .answers(List.of(
                            AnswerEntity.builder().id(c2).content("C").isCorrect(true).orderIndex(1).build(),
                            AnswerEntity.builder().id(uuid()).content("D").isCorrect(false).orderIndex(2).build()
                    )).build();

            when(quizAttemptRepository.findById(attemptId)).thenReturn(Optional.of(attempt));
            when(questionRepository.findByQuizIdOrderByOrderIndexAsc(lessonId)).thenReturn(List.of(q1, q2));
            setupQuizConfig(lessonId);
            mockAttemptSave();
            when(objectMapper.writeValueAsString(any())).thenReturn("{}");

            SubmitQuizRequest request = new SubmitQuizRequest();
            request.setSelectedAnswers(Map.of(
                    q1.getId(), List.of(c1),
                    q2.getId(), List.of(c2)
            ));

            QuizAttemptEntity result = quizService.submitQuizAttempt(studentId, lessonId, attemptId, request);

            assertThat(result.getScore()).isEqualByComparingTo(new BigDecimal("100.00"));
            assertThat(result.getStatus()).isEqualTo(QuizAttemptStatus.PASSED);
        }

        @Test
        @DisplayName("TRUE_FALSE question: student selects 'Đúng' which is correct → PASSED")
        void submitQuiz_trueFalse_selectTrue_correct() throws Exception {
            UUID trueId = uuid(), falseId = uuid();

            QuestionEntity q = QuestionEntity.builder()
                    .id(uuid()).content("Mặt trời mọc ở hướng Đông?")
                    .questionType("TRUE_FALSE").points(BigDecimal.ONE).orderIndex(1)
                    .answers(List.of(
                            AnswerEntity.builder().id(trueId).content("Đúng").isCorrect(true).orderIndex(1).build(),
                            AnswerEntity.builder().id(falseId).content("Sai").isCorrect(false).orderIndex(2).build()
                    )).build();

            when(quizAttemptRepository.findById(attemptId)).thenReturn(Optional.of(attempt));
            when(questionRepository.findByQuizIdOrderByOrderIndexAsc(lessonId)).thenReturn(List.of(q));
            setupQuizConfig(lessonId);
            mockAttemptSave();
            when(objectMapper.writeValueAsString(any())).thenReturn("{}");

            SubmitQuizRequest request = new SubmitQuizRequest();
            request.setSelectedAnswers(Map.of(q.getId(), List.of(trueId)));

            QuizAttemptEntity result = quizService.submitQuizAttempt(studentId, lessonId, attemptId, request);

            assertThat(result.getScore()).isEqualByComparingTo(new BigDecimal("100.00"));
            assertThat(result.getStatus()).isEqualTo(QuizAttemptStatus.PASSED);
        }
    }
}
