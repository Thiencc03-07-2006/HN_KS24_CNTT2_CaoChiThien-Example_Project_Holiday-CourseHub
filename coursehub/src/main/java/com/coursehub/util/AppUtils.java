package com.coursehub.util;

import com.coursehub.constant.AppConstants;
import org.springframework.web.multipart.MultipartFile;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Utility class for common operations across the application.
 */
public final class AppUtils {

    private AppUtils() {}

    /**
     * Generate a URL-friendly slug from a title.
     * Example: "Lập trình Java Spring Boot" -> "lap-trinh-java-spring-boot"
     */
    public static String toSlug(String title) {
        if (title == null) return "";
        String normalized = Normalizer.normalize(title, Normalizer.Form.NFD);
        String ascii = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        // Map Vietnamese specific characters
        ascii = ascii.replace("đ", "d").replace("Đ", "d");
        return ascii.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    /**
     * Generate a unique slug by appending UUID if base slug already exists.
     */
    public static String toUniqueSlug(String title) {
        return toSlug(title) + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Validate file content type.
     */
    public static boolean isAllowedContentType(MultipartFile file, String[] allowedTypes) {
        return file != null && Arrays.asList(allowedTypes).contains(file.getContentType());
    }

    /**
     * Validate file size.
     */
    public static boolean isWithinSizeLimit(MultipartFile file, long maxBytes) {
        return file != null && file.getSize() <= maxBytes;
    }

    /**
     * Generate a unique S3 key for uploaded files.
     */
    public static String generateS3Key(String prefix, UUID userId, String originalFilename) {
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return prefix + userId.toString() + "/" + UUID.randomUUID() + ext;
    }

    /**
     * Mask email for logging/display (e.g., c***@gmail.com).
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        String[] parts = email.split("@");
        String local = parts[0];
        String domain = parts[1];
        if (local.length() <= 2) return local.charAt(0) + "***@" + domain;
        return local.charAt(0) + "***" + local.charAt(local.length() - 1) + "@" + domain;
    }
}
