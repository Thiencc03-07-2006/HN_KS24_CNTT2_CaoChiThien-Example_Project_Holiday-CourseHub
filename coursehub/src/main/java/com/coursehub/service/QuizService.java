package com.coursehub.service;

import com.coursehub.dto.request.QuestionDto;
import com.coursehub.dto.request.QuizConfigDto;
import com.coursehub.dto.request.SubmitQuizRequest;
import com.coursehub.entity.QuizAttemptEntity;
import com.coursehub.dto.response.QuestionResponse;
import com.coursehub.dto.response.QuizConfigResponse;

import java.util.List;
import java.util.UUID;

public interface QuizService {
    QuizConfigResponse getQuizConfig(UUID lessonId);
    QuizConfigResponse saveQuizConfig(UUID instructorId, UUID courseId, UUID lessonId, QuizConfigDto dto);
    
    QuestionResponse addQuestion(UUID instructorId, UUID courseId, UUID lessonId, QuestionDto dto);
    QuestionResponse updateQuestion(UUID instructorId, UUID courseId, UUID lessonId, UUID questionId, QuestionDto dto);
    void deleteQuestion(UUID instructorId, UUID courseId, UUID lessonId, UUID questionId);

    List<QuestionResponse> getQuizQuestionsForInstructor(UUID instructorId, UUID courseId, UUID lessonId);
    List<QuestionResponse> getQuizQuestionsForStudent(UUID studentId, UUID lessonId);
    QuizAttemptEntity startQuizAttempt(UUID studentId, UUID lessonId);
    QuizAttemptEntity submitQuizAttempt(UUID studentId, UUID lessonId, UUID attemptId, SubmitQuizRequest request);
    boolean hasCompletedAttempt(UUID studentId, UUID lessonId);
}
