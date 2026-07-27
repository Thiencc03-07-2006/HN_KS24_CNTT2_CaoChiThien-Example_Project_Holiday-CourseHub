package com.coursehub.service;

import com.coursehub.entity.*;
import com.coursehub.enums.LessonType;
import com.coursehub.enums.UserStatus;
import com.coursehub.exception.BadRequestException;
import com.coursehub.exception.ResourceNotFoundException;
import com.coursehub.repository.EnrollmentRepository;
import com.coursehub.repository.LessonRepository;
import com.coursehub.repository.NoteRepository;
import com.coursehub.repository.UserRepository;
import com.coursehub.service.impl.NoteServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NoteService Unit Tests")
public class NoteServiceImplTest {

    @Mock private NoteRepository noteRepository;
    @Mock private UserRepository userRepository;
    @Mock private LessonRepository lessonRepository;
    @Mock private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private NoteServiceImpl noteService;

    @Test
    @DisplayName("createNote_success — valid data and user enrolled → note saved")
    void createNote_success() {
        UUID userId = UUID.randomUUID();
        UUID lessonId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        UserEntity user = UserEntity.builder().id(userId).email("user@example.com").build();
        CourseEntity course = CourseEntity.builder().id(courseId).build();
        ChapterEntity chapter = ChapterEntity.builder().course(course).build();
        LessonEntity lesson = LessonEntity.builder().id(lessonId).chapter(chapter).build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(lessonRepository.findById(lessonId)).willReturn(Optional.of(lesson));
        given(enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)).willReturn(true);
        given(noteRepository.save(any(NoteEntity.class))).willAnswer(inv -> inv.getArgument(0));

        NoteEntity result = noteService.createNote(userId, lessonId, "My personal note", 120);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("My personal note");
        assertThat(result.getTimestampSeconds()).isEqualTo(120);
        verify(noteRepository).save(any(NoteEntity.class));
    }

    @Test
    @DisplayName("createNote_notEnrolled — user not enrolled → BadRequestException")
    void createNote_notEnrolled_throwsException() {
        UUID userId = UUID.randomUUID();
        UUID lessonId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        UserEntity user = UserEntity.builder().id(userId).email("user@example.com").build();
        CourseEntity course = CourseEntity.builder().id(courseId).build();
        ChapterEntity chapter = ChapterEntity.builder().course(course).build();
        LessonEntity lesson = LessonEntity.builder().id(lessonId).chapter(chapter).build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(lessonRepository.findById(lessonId)).willReturn(Optional.of(lesson));
        given(enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)).willReturn(false);

        assertThatThrownBy(() -> noteService.createNote(userId, lessonId, "Note content", 120))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Bạn phải đăng ký khóa học này");
    }

    @Test
    @DisplayName("createNote_userNotFound — invalid userId → ResourceNotFoundException")
    void createNote_userNotFound_throwsException() {
        UUID userId = UUID.randomUUID();
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.createNote(userId, UUID.randomUUID(), "Content", 10))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getMyNotesForLesson — returns user notes for a lesson")
    void getMyNotesForLesson_returnsNotes() {
        UUID userId = UUID.randomUUID();
        UUID lessonId = UUID.randomUUID();
        NoteEntity note = NoteEntity.builder().content("Note 1").build();

        given(noteRepository.findByUserIdAndLessonIdOrderByTimestampSecondsAsc(userId, lessonId))
                .willReturn(List.of(note));

        List<NoteEntity> result = noteService.getMyNotesForLesson(userId, lessonId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("Note 1");
    }

    @Test
    @DisplayName("getMyNotesForCourse — returns user notes for a course")
    void getMyNotesForCourse_returnsNotes() {
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        NoteEntity note = NoteEntity.builder().content("Note Course").build();

        given(noteRepository.findByUserIdAndCourseId(userId, courseId)).willReturn(List.of(note));

        List<NoteEntity> result = noteService.getMyNotesForCourse(userId, courseId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("Note Course");
    }

    @Test
    @DisplayName("deleteNote_success — valid userId and noteId → deleted")
    void deleteNote_success() {
        UUID userId = UUID.randomUUID();
        UUID noteId = UUID.randomUUID();
        NoteEntity note = NoteEntity.builder().id(noteId).build();

        given(noteRepository.findByIdAndUserId(noteId, userId)).willReturn(Optional.of(note));

        noteService.deleteNote(userId, noteId);

        verify(noteRepository).delete(note);
    }

    @Test
    @DisplayName("deleteNote_notFound — invalid id or user unauthorized → ResourceNotFoundException")
    void deleteNote_notFound_throwsException() {
        UUID userId = UUID.randomUUID();
        UUID noteId = UUID.randomUUID();

        given(noteRepository.findByIdAndUserId(noteId, userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.deleteNote(userId, noteId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
