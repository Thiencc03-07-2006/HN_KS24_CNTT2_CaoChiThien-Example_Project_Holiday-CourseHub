package com.coursehub.service;

import com.coursehub.entity.CommentEntity;

import java.util.List;
import java.util.UUID;

public interface CommentService {
    CommentEntity addComment(UUID userId, UUID lessonId, UUID parentId, String content);
    List<CommentEntity> getLessonComments(UUID lessonId);
    CommentEntity updateComment(UUID userId, UUID commentId, String content);
    void deleteComment(UUID userId, UUID commentId);
    void hideComment(UUID adminId, UUID commentId);
}
