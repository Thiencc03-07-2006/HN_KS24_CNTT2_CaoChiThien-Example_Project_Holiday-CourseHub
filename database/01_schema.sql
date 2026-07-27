-- ============================================================
-- CourseHub - Database Schema (CONSOLIDATED v2.0)
-- Target: MySQL 8.x
-- Generated: 2026-07-22
-- Scope: Tong hop V1..V6 migration + doi chieu toan bo Entity
-- ============================================================
-- Thu tu chay:
--   1. 01_schema.sql    (file nay)
--   2. 02_constraints.sql
--   3. 03_indexes.sql
--   4. 04_sample_data.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS coursehub
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE coursehub;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS notes;
DROP TABLE IF EXISTS favorites;
DROP TABLE IF EXISTS wishlists;
DROP TABLE IF EXISTS course_approval_history;
DROP TABLE IF EXISTS reports;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS quiz_attempts;
DROP TABLE IF EXISTS progress;
DROP TABLE IF EXISTS enrollments;
DROP TABLE IF EXISTS quiz_configs;
DROP TABLE IF EXISTS answers;
DROP TABLE IF EXISTS questions;
DROP TABLE IF EXISTS lesson_resources;
DROP TABLE IF EXISTS lessons;
DROP TABLE IF EXISTS chapters;
DROP TABLE IF EXISTS courses;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS instructor_profiles;
DROP TABLE IF EXISTS password_reset_tokens;
DROP TABLE IF EXISTS refresh_tokens;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- NHOM 1: Bang doc lap (khong phu thuoc bang khac)
-- ============================================================

-- TABLE: roles | Entity: RoleEntity | PK: BIGINT AUTO_INCREMENT
CREATE TABLE roles (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL UNIQUE,
    description VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- TABLE: categories | Entity: CategoryEntity | PK: BIGINT AUTO_INCREMENT
CREATE TABLE categories (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    parent_id   BIGINT       NULL,
    name        VARCHAR(100) NOT NULL UNIQUE,
    slug        VARCHAR(120) NOT NULL UNIQUE,
    icon        VARCHAR(100),
    description VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;



-- ============================================================
-- NHOM 2: users va bang phu thuoc users
-- ============================================================

-- TABLE: users | Entity: UserEntity
-- UUID luu VARCHAR(36) - khop @JdbcTypeCode(Types.VARCHAR)
-- UserStatus: PENDING_VERIFICATION|ACTIVE|SOFT_LOCKED|BANNED|DELETED
CREATE TABLE users (
    id            VARCHAR(36)  NOT NULL PRIMARY KEY,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    full_name     VARCHAR(100) NOT NULL,
    phone_number  VARCHAR(20)  UNIQUE,
    avatar_url    VARCHAR(500),
    bio           TEXT,
    status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING_VERIFICATION',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at    DATETIME     NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- TABLE: user_roles | UserEntity.roles @JoinTable(name="user_roles")
CREATE TABLE user_roles (
    user_id VARCHAR(36) NOT NULL,
    role_id BIGINT      NOT NULL,
    PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- TABLE: refresh_tokens | Entity: RefreshTokenEntity | PK: BIGINT AUTO_INCREMENT
CREATE TABLE refresh_tokens (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    user_id     VARCHAR(36)  NOT NULL,
    token       VARCHAR(512) NOT NULL UNIQUE,
    expiry_date DATETIME     NOT NULL,
    revoked     TINYINT(1)   NOT NULL DEFAULT 0,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- TABLE: password_reset_tokens | Entity: PasswordResetTokenEntity | PK: BIGINT AUTO_INCREMENT
CREATE TABLE password_reset_tokens (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    user_id     VARCHAR(36)  NOT NULL,
    token       VARCHAR(128) NOT NULL UNIQUE,
    expiry_date DATETIME     NOT NULL,
    used        TINYINT(1)   NOT NULL DEFAULT 0,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- TABLE: instructor_profiles | Entity: InstructorProfileEntity | PK: UUID VARCHAR(36)
CREATE TABLE instructor_profiles (
    id                    VARCHAR(36)  NOT NULL PRIMARY KEY,
    user_id               VARCHAR(36)  NOT NULL UNIQUE,
    headline              VARCHAR(80)  NOT NULL,
    detailed_bio          TEXT         NOT NULL,
    website_url           VARCHAR(255),
    linkedin_url          VARCHAR(255),
    payout_bank_name      VARCHAR(100),
    payout_account_number VARCHAR(50),
    payout_account_name   VARCHAR(100),
    total_students        INT          NOT NULL DEFAULT 0,
    total_courses         INT          NOT NULL DEFAULT 0,
    average_rating        DOUBLE       NOT NULL DEFAULT 0.00,
    created_at            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- NHOM 3: courses va bang phu thuoc
-- ============================================================

-- TABLE: courses | Entity: CourseEntity | PK: UUID VARCHAR(36)
-- Ket hop V1 + V4 (blocked_*) + V5 (rejected_*)
-- CourseLevel: BEGINNER|INTERMEDIATE|ADVANCED|ALL_LEVELS
-- CourseStatus: DRAFT|PENDING_REVIEW|APPROVED|PUBLISHED|REJECTED|ARCHIVED|BLOCKED|BLOCKED_EDITED
CREATE TABLE courses (
    id                VARCHAR(36)   NOT NULL PRIMARY KEY,
    instructor_id     VARCHAR(36)   NOT NULL,
    category_id       BIGINT        NOT NULL,
    title             VARCHAR(200)  NOT NULL,
    slug              VARCHAR(255)  NOT NULL UNIQUE,
    short_description VARCHAR(500)  NOT NULL,
    description       TEXT          NOT NULL,
    price             DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    thumbnail_url     VARCHAR(500),
    promo_video_url   VARCHAR(500),
    level             VARCHAR(20)   NOT NULL DEFAULT 'BEGINNER',
    language          VARCHAR(50)   NOT NULL DEFAULT 'Vietnamese',
    status            VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',
    average_rating    DECIMAL(3,2)  NOT NULL DEFAULT 0.00,
    total_reviews     INT           NOT NULL DEFAULT 0,
    created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at        DATETIME      NULL,
    -- V4: blocking fields
    blocked_reason    VARCHAR(500)  DEFAULT NULL,
    blocked_by        VARCHAR(36)   DEFAULT NULL,
    blocked_at        DATETIME      DEFAULT NULL,
    -- V5: rejection fields (rejectedBy = String length=36 trong Entity)
    rejected_reason   VARCHAR(500)  DEFAULT NULL,
    rejected_by       VARCHAR(36)   DEFAULT NULL,
    rejected_at       DATETIME      DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;



-- ============================================================
-- NHOM 4: Cau truc bai hoc
-- ============================================================

-- TABLE: chapters | Entity: ChapterEntity | PK: UUID VARCHAR(36)
CREATE TABLE chapters (
    id          VARCHAR(36)  NOT NULL PRIMARY KEY,
    course_id   VARCHAR(36)  NOT NULL,
    title       VARCHAR(255) NOT NULL,
    order_index INT          NOT NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- TABLE: lessons | Entity: LessonEntity | PK: UUID VARCHAR(36)
-- LessonType: VIDEO|PDF|TEXT|QUIZ
CREATE TABLE lessons (
    id          VARCHAR(36)  NOT NULL PRIMARY KEY,
    chapter_id  VARCHAR(36)  NOT NULL,
    title       VARCHAR(255) NOT NULL,
    order_index INT          NOT NULL,
    lesson_type VARCHAR(20)  NOT NULL,
    is_preview  TINYINT(1)   NOT NULL DEFAULT 0,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- TABLE: lesson_resources | Entity: LessonResourceEntity | PK: UUID VARCHAR(36)
-- VideoStatus: NONE|UPLOADING|UPLOADED|PROCESSING|READY|TRANSCODE_FAILED|ERROR
CREATE TABLE lesson_resources (
    id               VARCHAR(36) NOT NULL PRIMARY KEY,
    lesson_id        VARCHAR(36) NOT NULL UNIQUE,
    resource_url     VARCHAR(500),
    duration_seconds INT,
    text_content     TEXT,
    is_downloadable  TINYINT(1)  NOT NULL DEFAULT 0,
    video_status     VARCHAR(20) NOT NULL DEFAULT 'NONE'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- TABLE: quiz_configs | Entity: QuizConfigEntity | PK: UUID VARCHAR(36)
CREATE TABLE quiz_configs (
    id                 VARCHAR(36)  NOT NULL PRIMARY KEY,
    lesson_id          VARCHAR(36)  NOT NULL UNIQUE,
    time_limit_minutes INT,
    passing_score      DECIMAL(5,2) NOT NULL DEFAULT 70.00,
    max_attempts       INT          NOT NULL DEFAULT 3,
    shuffle_questions  TINYINT(1)   NOT NULL DEFAULT 0,
    shuffle_answers    TINYINT(1)   NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- TABLE: questions | Entity: QuestionEntity | PK: UUID VARCHAR(36)
-- quiz_id FK -> lessons.id (QuestionEntity.quiz: LessonEntity)
CREATE TABLE questions (
    id            VARCHAR(36)  NOT NULL PRIMARY KEY,
    quiz_id       VARCHAR(36)  NOT NULL,
    content       TEXT         NOT NULL,
    question_type VARCHAR(20)  NOT NULL DEFAULT 'SINGLE_CHOICE',
    points        DECIMAL(5,2) NOT NULL DEFAULT 1.00,
    order_index   INT          NOT NULL,
    explanation   TEXT,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- TABLE: answers | Entity: AnswerEntity | PK: UUID VARCHAR(36)
CREATE TABLE answers (
    id          VARCHAR(36) NOT NULL PRIMARY KEY,
    question_id VARCHAR(36) NOT NULL,
    content     TEXT        NOT NULL,
    is_correct  TINYINT(1)  NOT NULL DEFAULT 0,
    order_index INT         NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- NHOM 5: Enrollment va hoc tap
-- ============================================================

-- TABLE: enrollments | Entity: EnrollmentEntity | PK: UUID VARCHAR(36)
-- EnrollmentStatus: ACTIVE|COMPLETED|REFUNDED
CREATE TABLE enrollments (
    id               VARCHAR(36)  NOT NULL PRIMARY KEY,
    user_id          VARCHAR(36)  NOT NULL,
    course_id        VARCHAR(36)  NOT NULL,
    enrollment_date  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    progress_percent DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    status           VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    UNIQUE KEY uq_enrollment_user_course (user_id, course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- TABLE: progress | Entity: ProgressEntity | PK: UUID VARCHAR(36)
-- V6: fk_progress_lesson -> ON DELETE CASCADE
CREATE TABLE progress (
    id            VARCHAR(36) NOT NULL PRIMARY KEY,
    enrollment_id VARCHAR(36) NOT NULL,
    lesson_id     VARCHAR(36) NOT NULL,
    is_completed  TINYINT(1)  NOT NULL DEFAULT 0,
    completed_at  DATETIME    NULL,
    UNIQUE KEY uq_progress_enrollment_lesson (enrollment_id, lesson_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- TABLE: quiz_attempts | Entity: QuizAttemptEntity | PK: UUID VARCHAR(36)
-- V6: fk_quiz_attempts_lesson -> ON DELETE CASCADE
-- QuizAttemptStatus: IN_PROGRESS|PASSED|FAILED|TIMED_OUT
CREATE TABLE quiz_attempts (
    id               VARCHAR(36)  NOT NULL PRIMARY KEY,
    enrollment_id    VARCHAR(36)  NOT NULL,
    lesson_id        VARCHAR(36)  NOT NULL,
    score            DECIMAL(5,2),
    status           VARCHAR(20)  NOT NULL DEFAULT 'IN_PROGRESS',
    answers_snapshot JSON,
    started_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    submitted_at     DATETIME     NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- NHOM 6: Reviews, Comments, Notifications, Reports
-- ============================================================

-- TABLE: reviews | Entity: ReviewEntity | PK: UUID VARCHAR(36)
CREATE TABLE reviews (
    id            VARCHAR(36) NOT NULL PRIMARY KEY,
    enrollment_id VARCHAR(36) NOT NULL UNIQUE,
    rating        INT         NOT NULL,
    comment       TEXT        NOT NULL,
    is_hidden     TINYINT(1)  NOT NULL DEFAULT 0,
    created_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- TABLE: comments | Entity: CommentEntity | PK: UUID VARCHAR(36)
CREATE TABLE comments (
    id         VARCHAR(36) NOT NULL PRIMARY KEY,
    lesson_id  VARCHAR(36) NOT NULL,
    user_id    VARCHAR(36) NOT NULL,
    parent_id  VARCHAR(36),
    content    TEXT        NOT NULL,
    is_hidden  TINYINT(1)  NOT NULL DEFAULT 0,
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- TABLE: notifications | Entity: NotificationEntity | PK: UUID VARCHAR(36)
-- NotificationType: SYSTEM|COURSE_APPROVED|COURSE_REJECTED|COMMENT_REPLY|NEW_ENROLLMENT|REPORT_RESOLVED
CREATE TABLE notifications (
    id                VARCHAR(36)  NOT NULL PRIMARY KEY,
    recipient_id      VARCHAR(36)  NOT NULL,
    title             VARCHAR(150) NOT NULL,
    content           TEXT         NOT NULL,
    notification_type VARCHAR(50)  NOT NULL,
    target_url        VARCHAR(500),
    is_read           TINYINT(1)   NOT NULL DEFAULT 0,
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- TABLE: reports | Entity: ReportEntity | PK: UUID VARCHAR(36)
-- V3: them admin_note, updated_at; cap nhat CHECK reportable_type va status
-- reportable_id: UUID @JdbcTypeCode(VARCHAR) -> VARCHAR(36)
-- ReportStatus: PENDING|REVIEWING|RESOLVED|REJECTED|AUTO_ESCALATED
CREATE TABLE reports (
    id              VARCHAR(36)  NOT NULL PRIMARY KEY,
    reporter_id     VARCHAR(36)  NOT NULL,
    reportable_type VARCHAR(50)  NOT NULL,
    reportable_id   VARCHAR(36)  NOT NULL,
    reason          VARCHAR(100) NOT NULL,
    description     TEXT,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    admin_note      VARCHAR(255) DEFAULT NULL,
    updated_at      DATETIME     DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- NHOM 7: Wishlist, Favorites, Notes
-- ============================================================

-- TABLE: wishlists | Entity: WishlistEntity @IdClass(WishlistId)
-- PK composite: user_id + course_id (ca hai UUID VARCHAR(36))
-- Column "added_at" -> field "createdAt" trong Entity (name = "added_at")
CREATE TABLE wishlists (
    user_id   VARCHAR(36) NOT NULL,
    course_id VARCHAR(36) NOT NULL,
    added_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- TABLE: favorites | Entity: FavoriteEntity @IdClass(FavoriteId)
CREATE TABLE favorites (
    user_id   VARCHAR(36) NOT NULL,
    course_id VARCHAR(36) NOT NULL,
    added_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- TABLE: notes | Entity: NoteEntity | PK: UUID VARCHAR(36)
CREATE TABLE notes (
    id                VARCHAR(36) NOT NULL PRIMARY KEY,
    user_id           VARCHAR(36) NOT NULL,
    lesson_id         VARCHAR(36) NOT NULL,
    content           TEXT        NOT NULL,
    timestamp_seconds INT,
    created_at        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- NHOM 8: Course Approval History va Instructor Relations
-- ============================================================

-- TABLE: course_approval_history | Entity: CourseApprovalHistoryEntity | PK: UUID VARCHAR(36)
CREATE TABLE course_approval_history (
    id         VARCHAR(36) NOT NULL PRIMARY KEY,
    course_id  VARCHAR(36) NOT NULL,
    actor_id   VARCHAR(36) NOT NULL,
    action     VARCHAR(20) NOT NULL,
    note       TEXT,
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;



-- ============================================================
-- NHOM 9: Orders & Payments
-- ============================================================



-- TABLE: orders | Entity: OrderEntity | PK: UUID VARCHAR(36)
-- PaymentStatus: PENDING|COMPLETED|FAILED|REFUNDED
CREATE TABLE orders (
    id              VARCHAR(36)   NOT NULL PRIMARY KEY,
    user_id         VARCHAR(36)   NOT NULL,
    total_amount    DECIMAL(12,2) NOT NULL,
    discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    final_amount    DECIMAL(12,2) NOT NULL,
    payment_status  VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    transaction_id  VARCHAR(100)  UNIQUE,
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- TABLE: order_items | Entity: OrderItemEntity | PK: UUID VARCHAR(36)
CREATE TABLE order_items (
    id        VARCHAR(36)   NOT NULL PRIMARY KEY,
    order_id  VARCHAR(36)   NOT NULL,
    course_id VARCHAR(36)   NOT NULL,
    price     DECIMAL(12,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
