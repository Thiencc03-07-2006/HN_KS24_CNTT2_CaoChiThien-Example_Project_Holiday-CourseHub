package com.coursehub.constant;

/**
 * Application-wide constants for CourseHub
 */
public final class AppConstants {

    private AppConstants() {}

    // ==================== Auth ====================
    public static final int OTP_LENGTH = 6;
    public static final int OTP_TTL_MINUTES = 5;
    public static final int OTP_MAX_ATTEMPTS = 3;
    public static final int LOGIN_MAX_ATTEMPTS = 5;
    public static final int LOGIN_LOCK_DURATION_MINUTES = 15;
    public static final int PASSWORD_RESET_TTL_MINUTES = 15;
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    // ==================== Pagination ====================
    public static final int DEFAULT_PAGE_SIZE = 12;
    public static final int MAX_PAGE_SIZE = 100;
    public static final int DEFAULT_PAGE = 0;

    // ==================== Review ====================
    public static final double REVIEW_MIN_PROGRESS_PERCENT = 30.0;

    // ==================== Report ====================
    public static final int REPORT_AUTO_ESCALATE_THRESHOLD = 5;

    // ==================== Course ====================
    public static final int COURSE_TITLE_MIN_LENGTH = 10;
    public static final int COURSE_TITLE_MAX_LENGTH = 80;

    // ==================== Chapter ====================
    public static final int CHAPTER_TITLE_MIN_LENGTH = 5;
    public static final int CHAPTER_TITLE_MAX_LENGTH = 100;

    // ==================== Lesson ====================
    public static final int LESSON_TITLE_MIN_LENGTH = 5;
    public static final int LESSON_TITLE_MAX_LENGTH = 150;

    // ==================== Instructor ====================
    public static final int INSTRUCTOR_HEADLINE_MAX_LENGTH = 80;
    public static final int INSTRUCTOR_BIO_MIN_LENGTH = 100;

    // ==================== File Upload ====================
    public static final long MAX_AVATAR_SIZE_BYTES = 2L * 1024 * 1024;       // 2MB
    public static final long MAX_THUMBNAIL_SIZE_BYTES = 5L * 1024 * 1024;    // 5MB
    public static final long MAX_VIDEO_SIZE_BYTES = 2L * 1024 * 1024 * 1024; // 2GB
    public static final String[] ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/png", "image/jpg"};
    public static final String[] ALLOWED_VIDEO_TYPES = {"video/mp4", "video/quicktime", "video/x-msvideo"};
    public static final String[] ALLOWED_PDF_TYPES = {"application/pdf"};

    // ==================== Redis Key Prefixes ====================
    public static final String REDIS_OTP_PREFIX = "otp:";
    public static final String REDIS_OTP_ATTEMPTS_PREFIX = "otp_attempts:";
    public static final String REDIS_LOGIN_FAIL_PREFIX = "login_fail:";
    public static final String REDIS_ACCOUNT_LOCKED_PREFIX = "account_locked:";

    // ==================== S3 Prefixes ====================
    public static final String S3_AVATARS_PREFIX = "avatars/";
    public static final String S3_THUMBNAILS_PREFIX = "thumbnails/";
    public static final String S3_VIDEOS_RAW_PREFIX = "videos/raw/";
    public static final String S3_VIDEOS_HLS_PREFIX = "videos/hls/";
    public static final String S3_DOCUMENTS_PREFIX = "documents/";

    // ==================== Roles ====================
    public static final String ROLE_STUDENT = "ROLE_STUDENT";
    public static final String ROLE_INSTRUCTOR = "ROLE_INSTRUCTOR";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
}
