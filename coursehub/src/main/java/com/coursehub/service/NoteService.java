package com.coursehub.service;

import com.coursehub.entity.NoteEntity;

import java.util.List;
import java.util.UUID;

public interface NoteService {
    NoteEntity createNote(UUID userId, UUID lessonId, String content, Integer timestampSeconds);
    List<NoteEntity> getMyNotesForLesson(UUID userId, UUID lessonId);
    List<NoteEntity> getMyNotesForCourse(UUID userId, UUID courseId);
    void deleteNote(UUID userId, UUID noteId);
}

