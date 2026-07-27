-- ============================================================
-- CourseHub - Indexes (CONSOLIDATED v2.0)
-- Target: MySQL 8.x
-- Generated: 2026-07-22
-- Chay SAU 02_constraints.sql
-- ============================================================

USE coursehub;

-- ============================================================
-- users
-- ============================================================
CREATE INDEX idx_users_email  ON users(email);
CREATE INDEX idx_users_status ON users(status);

-- ============================================================
-- user_roles
-- ============================================================
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);

-- ============================================================
-- refresh_tokens
-- ============================================================
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token   ON refresh_tokens(token);

-- ============================================================
-- password_reset_tokens
-- ============================================================
CREATE INDEX idx_prt_token ON password_reset_tokens(token);

-- ============================================================
-- instructor_profiles
-- ============================================================
CREATE INDEX idx_instructor_profiles_user_id ON instructor_profiles(user_id);

-- ============================================================
-- categories
-- ============================================================
CREATE INDEX idx_categories_parent_id ON categories(parent_id);
CREATE INDEX idx_categories_slug      ON categories(slug);

-- ============================================================
-- courses
-- ============================================================
CREATE INDEX idx_courses_instructor_id ON courses(instructor_id);
CREATE INDEX idx_courses_category_id   ON courses(category_id);
CREATE INDEX idx_courses_status        ON courses(status);
CREATE INDEX idx_courses_slug          ON courses(slug);
CREATE INDEX idx_courses_avg_rating    ON courses(average_rating DESC);
-- Full-text search (MySQL FULLTEXT thay PostgreSQL GIN)
CREATE FULLTEXT INDEX idx_courses_fts  ON courses(title, short_description);

-- ============================================================
-- chapters
-- ============================================================
CREATE INDEX idx_chapters_course_id    ON chapters(course_id);
CREATE INDEX idx_chapters_course_order ON chapters(course_id, order_index);

-- ============================================================
-- lessons
-- ============================================================
CREATE INDEX idx_lessons_chapter_id    ON lessons(chapter_id);
CREATE INDEX idx_lessons_chapter_order ON lessons(chapter_id, order_index);

-- ============================================================
-- lesson_resources
-- ============================================================
CREATE INDEX idx_lesson_resources_lesson_id ON lesson_resources(lesson_id);

-- ============================================================
-- questions
-- ============================================================
CREATE INDEX idx_questions_quiz_id    ON questions(quiz_id);
CREATE INDEX idx_questions_quiz_order ON questions(quiz_id, order_index);

-- ============================================================
-- answers
-- ============================================================
CREATE INDEX idx_answers_question_id ON answers(question_id);

-- ============================================================
-- enrollments
-- ============================================================
CREATE INDEX idx_enrollments_user_id   ON enrollments(user_id);
CREATE INDEX idx_enrollments_course_id ON enrollments(course_id);

-- ============================================================
-- progress
-- ============================================================
CREATE INDEX idx_progress_enrollment_id ON progress(enrollment_id);
CREATE INDEX idx_progress_lesson_id     ON progress(lesson_id);

-- ============================================================
-- quiz_attempts
-- ============================================================
CREATE INDEX idx_quiz_attempts_enrollment_id ON quiz_attempts(enrollment_id);
CREATE INDEX idx_quiz_attempts_lesson_id     ON quiz_attempts(lesson_id);

-- ============================================================
-- reviews
-- ============================================================
CREATE INDEX idx_reviews_enrollment_id ON reviews(enrollment_id);

-- ============================================================
-- comments
-- ============================================================
CREATE INDEX idx_comments_lesson_id  ON comments(lesson_id);
CREATE INDEX idx_comments_user_id    ON comments(user_id);
CREATE INDEX idx_comments_parent_id  ON comments(parent_id);

-- ============================================================
-- notifications
-- ============================================================
CREATE INDEX idx_notifications_recipient_id ON notifications(recipient_id);
CREATE INDEX idx_notifications_is_read      ON notifications(recipient_id, is_read);

-- ============================================================
-- reports
-- ============================================================
CREATE INDEX idx_reports_reporter_id ON reports(reporter_id);
CREATE INDEX idx_reports_reportable  ON reports(reportable_type, reportable_id);
CREATE INDEX idx_reports_status      ON reports(status);

-- ============================================================
-- notes
-- ============================================================
CREATE INDEX idx_notes_user_lesson ON notes(user_id, lesson_id);

-- ============================================================
-- course_approval_history
-- ============================================================
CREATE INDEX idx_cah_course_id ON course_approval_history(course_id);


-- ============================================================
-- orders
-- ============================================================
CREATE INDEX idx_orders_user_id ON orders(user_id);

-- ============================================================
-- order_items
-- ============================================================
CREATE INDEX idx_order_items_order_id ON order_items(order_id);