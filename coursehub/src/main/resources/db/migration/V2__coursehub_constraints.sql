-- ============================================================
-- CourseHub - Flyway Migration V2: Constraints (Consolidated v2.0)
-- Source: database/02_constraints.sql
-- Replaces: V3__extend_reports.sql + V6__add_lesson_cascade_delete.sql
-- Run order: 2nd (after V1 schema)
-- ============================================================
-- ============================================================
-- CourseHub - Foreign Keys & Constraints (CONSOLIDATED v2.0)
-- Target: MySQL 8.x
-- Generated: 2026-07-22
-- Chay SAU 01_schema.sql
-- ============================================================

-- ============================================================
-- FOREIGN KEYS: categories (self-ref)
-- ============================================================
ALTER TABLE categories
    ADD CONSTRAINT fk_categories_parent
        FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL;

-- ============================================================
-- FOREIGN KEYS: user_roles
-- ============================================================
ALTER TABLE user_roles
    ADD CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE;

-- ============================================================
-- FOREIGN KEYS: refresh_tokens
-- ============================================================
ALTER TABLE refresh_tokens
    ADD CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- ============================================================
-- FOREIGN KEYS: password_reset_tokens
-- ============================================================
ALTER TABLE password_reset_tokens
    ADD CONSTRAINT fk_prt_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- ============================================================
-- FOREIGN KEYS: instructor_profiles
-- ============================================================
ALTER TABLE instructor_profiles
    ADD CONSTRAINT fk_instructor_profiles_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- ============================================================
-- FOREIGN KEYS: courses
-- ============================================================
ALTER TABLE courses
    ADD CONSTRAINT fk_courses_instructor
        FOREIGN KEY (instructor_id) REFERENCES users(id),
    ADD CONSTRAINT fk_courses_category
        FOREIGN KEY (category_id) REFERENCES categories(id),
    ADD CONSTRAINT fk_courses_blocked_by
        FOREIGN KEY (blocked_by) REFERENCES users(id) ON DELETE SET NULL;

-- ============================================================
-- FOREIGN KEYS: chapters
-- ============================================================
ALTER TABLE chapters
    ADD CONSTRAINT fk_chapters_course
        FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE;

-- ============================================================
-- FOREIGN KEYS: lessons
-- ============================================================
ALTER TABLE lessons
    ADD CONSTRAINT fk_lessons_chapter
        FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE;

-- ============================================================
-- FOREIGN KEYS: lesson_resources
-- ============================================================
ALTER TABLE lesson_resources
    ADD CONSTRAINT fk_lesson_resources_lesson
        FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE;

-- ============================================================
-- FOREIGN KEYS: quiz_configs
-- ============================================================
ALTER TABLE quiz_configs
    ADD CONSTRAINT fk_quiz_configs_lesson
        FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE;

-- ============================================================
-- FOREIGN KEYS: questions
-- ============================================================
ALTER TABLE questions
    ADD CONSTRAINT fk_questions_lesson
        FOREIGN KEY (quiz_id) REFERENCES lessons(id) ON DELETE CASCADE;

-- ============================================================
-- FOREIGN KEYS: answers
-- ============================================================
ALTER TABLE answers
    ADD CONSTRAINT fk_answers_question
        FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE;

-- ============================================================
-- FOREIGN KEYS: enrollments
-- ============================================================
ALTER TABLE enrollments
    ADD CONSTRAINT fk_enrollments_user
        FOREIGN KEY (user_id) REFERENCES users(id),
    ADD CONSTRAINT fk_enrollments_course
        FOREIGN KEY (course_id) REFERENCES courses(id);

-- ============================================================
-- FOREIGN KEYS: progress (V6: ON DELETE CASCADE cho lesson_id)
-- ============================================================
ALTER TABLE progress
    ADD CONSTRAINT fk_progress_enrollment
        FOREIGN KEY (enrollment_id) REFERENCES enrollments(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_progress_lesson
        FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE;

-- ============================================================
-- FOREIGN KEYS: quiz_attempts (V6: ON DELETE CASCADE cho lesson_id)
-- ============================================================
ALTER TABLE quiz_attempts
    ADD CONSTRAINT fk_quiz_attempts_enrollment
        FOREIGN KEY (enrollment_id) REFERENCES enrollments(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_quiz_attempts_lesson
        FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE;

-- ============================================================
-- FOREIGN KEYS: reviews
-- ============================================================
ALTER TABLE reviews
    ADD CONSTRAINT fk_reviews_enrollment
        FOREIGN KEY (enrollment_id) REFERENCES enrollments(id);

-- ============================================================
-- FOREIGN KEYS: comments
-- ============================================================
ALTER TABLE comments
    ADD CONSTRAINT fk_comments_lesson
        FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_comments_user
        FOREIGN KEY (user_id) REFERENCES users(id),
    ADD CONSTRAINT fk_comments_parent
        FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE;

-- ============================================================
-- FOREIGN KEYS: notifications
-- ============================================================
ALTER TABLE notifications
    ADD CONSTRAINT fk_notifications_recipient
        FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE CASCADE;

-- ============================================================
-- FOREIGN KEYS: reports
-- ============================================================
ALTER TABLE reports
    ADD CONSTRAINT fk_reports_reporter
        FOREIGN KEY (reporter_id) REFERENCES users(id);

-- ============================================================
-- FOREIGN KEYS: wishlists
-- ============================================================
ALTER TABLE wishlists
    ADD CONSTRAINT fk_wishlists_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_wishlists_course
        FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE;

-- ============================================================
-- FOREIGN KEYS: favorites
-- ============================================================
ALTER TABLE favorites
    ADD CONSTRAINT fk_favorites_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_favorites_course
        FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE;

-- ============================================================
-- FOREIGN KEYS: notes
-- ============================================================
ALTER TABLE notes
    ADD CONSTRAINT fk_notes_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_notes_lesson
        FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE;

-- ============================================================
-- FOREIGN KEYS: course_approval_history
-- ============================================================
ALTER TABLE course_approval_history
    ADD CONSTRAINT fk_cah_course
        FOREIGN KEY (course_id) REFERENCES courses(id),
    ADD CONSTRAINT fk_cah_actor
        FOREIGN KEY (actor_id) REFERENCES users(id);

-- ============================================================
-- FOREIGN KEYS: orders
-- ============================================================
ALTER TABLE orders
    ADD CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id) REFERENCES users(id);

-- ============================================================
-- FOREIGN KEYS: order_items
-- ============================================================
ALTER TABLE order_items
    ADD CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_order_items_course
        FOREIGN KEY (course_id) REFERENCES courses(id);

-- ============================================================
-- CHECK CONSTRAINTS (MySQL 8.0.16+)
-- ============================================================

-- users.status
ALTER TABLE users
    ADD CONSTRAINT chk_users_status
        CHECK (status IN ('PENDING_VERIFICATION','ACTIVE','SOFT_LOCKED','BANNED','DELETED'));

-- courses.level
ALTER TABLE courses
    ADD CONSTRAINT chk_courses_level
        CHECK (level IN ('BEGINNER','INTERMEDIATE','ADVANCED','ALL_LEVELS'));

-- courses.status (bao gom BLOCKED_EDITED tu enum CourseStatus)
ALTER TABLE courses
    ADD CONSTRAINT chk_courses_status
        CHECK (status IN ('DRAFT','PENDING_REVIEW','APPROVED','PUBLISHED','REJECTED','ARCHIVED','BLOCKED','BLOCKED_EDITED'));

-- lessons.lesson_type
ALTER TABLE lessons
    ADD CONSTRAINT chk_lessons_type
        CHECK (lesson_type IN ('VIDEO','PDF','TEXT','QUIZ'));

-- lesson_resources.video_status
ALTER TABLE lesson_resources
    ADD CONSTRAINT chk_lesson_resources_video_status
        CHECK (video_status IN ('NONE','UPLOADING','UPLOADED','PROCESSING','READY','TRANSCODE_FAILED','ERROR'));

-- questions.question_type
ALTER TABLE questions
    ADD CONSTRAINT chk_questions_type
        CHECK (question_type IN ('SINGLE_CHOICE','MULTIPLE_CHOICE','TRUE_FALSE'));

-- enrollments.status
ALTER TABLE enrollments
    ADD CONSTRAINT chk_enrollments_status
        CHECK (status IN ('ACTIVE','COMPLETED','REFUNDED'));

-- quiz_attempts.status
ALTER TABLE quiz_attempts
    ADD CONSTRAINT chk_quiz_attempts_status
        CHECK (status IN ('IN_PROGRESS','PASSED','FAILED','TIMED_OUT'));

-- reviews.rating
ALTER TABLE reviews
    ADD CONSTRAINT chk_reviews_rating
        CHECK (rating BETWEEN 1 AND 5);

-- notifications.notification_type
ALTER TABLE notifications
    ADD CONSTRAINT chk_notifications_type
        CHECK (notification_type IN ('SYSTEM','COURSE_APPROVED','COURSE_REJECTED','COMMENT_REPLY','NEW_ENROLLMENT','REPORT_RESOLVED'));

-- reports.reportable_type (V3: them REVIEW)
ALTER TABLE reports
    ADD CONSTRAINT chk_reports_type
        CHECK (reportable_type IN ('COURSE','COMMENT','REVIEW','USER'));

-- reports.status (V3: REVIEWING thay UNDER_REVIEW; them REJECTED)
ALTER TABLE reports
    ADD CONSTRAINT chk_reports_status
        CHECK (status IN ('PENDING','REVIEWING','RESOLVED','REJECTED','AUTO_ESCALATED'));

-- course_approval_history.action
ALTER TABLE course_approval_history
    ADD CONSTRAINT chk_cah_action
        CHECK (action IN ('SUBMIT','APPROVE','REJECT','BLOCK','ARCHIVE'));

-- orders.payment_status
ALTER TABLE orders
    ADD CONSTRAINT chk_orders_payment_status
        CHECK (payment_status IN ('PENDING','COMPLETED','FAILED','REFUNDED'));

-- ============================================================
-- UNIQUE CONSTRAINTS (da co trong CREATE TABLE, ghi lai de ro rang)
-- ============================================================
-- reports: moi reporter chi report moi entity 1 lan
CREATE UNIQUE INDEX idx_reports_unique_per_reporter
    ON reports(reporter_id, reportable_type, reportable_id);