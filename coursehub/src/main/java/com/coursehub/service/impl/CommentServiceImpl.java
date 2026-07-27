package com.coursehub.service.impl;

import com.coursehub.entity.*;
import com.coursehub.enums.NotificationType;
import com.coursehub.exception.BadRequestException;
import com.coursehub.exception.ResourceNotFoundException;
import com.coursehub.repository.CommentRepository;
import com.coursehub.repository.EnrollmentRepository;
import com.coursehub.repository.LessonRepository;
import com.coursehub.repository.UserRepository;
import com.coursehub.service.CommentService;
import com.coursehub.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public CommentEntity addComment(UUID userId, UUID lessonId, UUID parentId, String content) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        LessonEntity lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));

        // Verify enrollment
        UUID courseId = lesson.getChapter().getCourse().getId();
        boolean isEnrolled = enrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
        boolean isInstructor = lesson.getChapter().getCourse().isOwnedBy(userId);

        if (!isEnrolled && !isInstructor) {
            throw new BadRequestException("VALID_001", "Bạn phải đăng ký khóa học này mới được thảo luận.");
        }

        CommentEntity parent = null;
        if (parentId != null) {
            parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", parentId));

            int level = 2;
            CommentEntity current = parent.getParent();
            while (current != null) {
                level++;
                current = current.getParent();
            }

            if (level > 4) {
                throw new BadRequestException("VALID_001", "Bình luận chỉ hỗ trợ tối đa 4 cấp.");
            }
        }

        CommentEntity comment = CommentEntity.builder()
                .user(user)
                .lesson(lesson)
                .parent(parent)
                .content(content)
                .isHidden(false)
                .createdAt(LocalDateTime.now())
                .build();

        comment = commentRepository.save(comment);

        // Notify parent author
        if (parent != null && !parent.getUser().getId().equals(userId)) {
            notificationService.sendNotification(
                    parent.getUser().getId(),
                    "Phản hồi mới trong thảo luận",
                    user.getFullName() + " đã trả lời bình luận của bạn trong bài học \"" + lesson.getTitle() + "\".",
                    NotificationType.COMMENT_REPLY,
                    "/courses/" + lesson.getChapter().getCourse().getSlug()
            );
        }

        return comment;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentEntity> getLessonComments(UUID lessonId) {
        List<CommentEntity> all = commentRepository.findByLessonIdAndIsHiddenFalseOrderByCreatedAtAsc(lessonId);

        java.util.Map<UUID, CommentEntity> map = new java.util.HashMap<>();
        for (CommentEntity c : all) {
            c.setReplies(new java.util.ArrayList<>());
            map.put(c.getId(), c);
        }

        List<CommentEntity> roots = new java.util.ArrayList<>();
        for (CommentEntity c : all) {
            if (c.getParent() == null) {
                roots.add(c);
            } else {
                CommentEntity parent = map.get(c.getParent().getId());
                if (parent != null) {
                    parent.getReplies().add(c);
                }
            }
        }
        return roots;
    }

    @Override
    @Transactional
    public CommentEntity updateComment(UUID userId, UUID commentId, String content) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        if (!comment.getUser().getId().equals(userId)) {
            throw new BadRequestException("VALID_001", "Bạn không có quyền chỉnh sửa bình luận này.");
        }

        comment.setContent(content);
        return commentRepository.save(comment);
    }

    @Override
    @Transactional
    public void deleteComment(UUID userId, UUID commentId) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        boolean isAdmin = user.hasRole("ROLE_ADMIN");
        boolean isOwner = comment.getUser().getId().equals(userId);
        boolean isCourseInstructor = comment.getLesson().getChapter().getCourse().isOwnedBy(userId);

        if (!isAdmin && !isOwner && !isCourseInstructor) {
            throw new BadRequestException("VALID_001", "Bạn không có quyền xóa bình luận này.");
        }

        commentRepository.delete(comment);
    }

    @Override
    @Transactional
    public void hideComment(UUID adminId, UUID commentId) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));
        comment.setIsHidden(true);
        commentRepository.save(comment);
    }
}
