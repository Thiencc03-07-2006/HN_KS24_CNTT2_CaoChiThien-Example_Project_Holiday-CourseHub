package com.coursehub.service.impl;

import com.coursehub.entity.LessonEntity;
import com.coursehub.entity.NoteEntity;
import com.coursehub.entity.UserEntity;
import com.coursehub.exception.BadRequestException;
import com.coursehub.exception.ResourceNotFoundException;
import com.coursehub.repository.EnrollmentRepository;
import com.coursehub.repository.LessonRepository;
import com.coursehub.repository.NoteRepository;
import com.coursehub.repository.UserRepository;
import com.coursehub.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    @Transactional
    public NoteEntity createNote(UUID userId, UUID lessonId, String content, Integer timestampSeconds) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        LessonEntity lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));

        // Verify enrollment
        UUID courseId = lesson.getChapter().getCourse().getId();
        boolean isEnrolled = enrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
        if (!isEnrolled) {
            throw new BadRequestException("VALID_001", "Bạn phải đăng ký khóa học này để lưu ghi chú cá nhân.");
        }

        NoteEntity note = NoteEntity.builder()
                .user(user)
                .lesson(lesson)
                .content(content)
                .timestampSeconds(timestampSeconds)
                .createdAt(LocalDateTime.now())
                .build();

        return noteRepository.save(note);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteEntity> getMyNotesForLesson(UUID userId, UUID lessonId) {
        return noteRepository.findByUserIdAndLessonIdOrderByTimestampSecondsAsc(userId, lessonId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteEntity> getMyNotesForCourse(UUID userId, UUID courseId) {
        return noteRepository.findByUserIdAndCourseId(userId, courseId);
    }

    @Override
    @Transactional
    public void deleteNote(UUID userId, UUID noteId) {
        NoteEntity note = noteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Note", "id", noteId));
        noteRepository.delete(note);
    }
}

