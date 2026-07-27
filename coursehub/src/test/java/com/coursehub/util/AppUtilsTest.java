package com.coursehub.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("AppUtils Unit Tests")
public class AppUtilsTest {

    @Test
    @DisplayName("toSlug — normalizes title to ascii URL-friendly slug")
    void toSlug_normalizesTitle() {
        assertThat(AppUtils.toSlug(null)).isEmpty();
        assertThat(AppUtils.toSlug("Lập trình Java Spring Boot")).isEqualTo("lap-trinh-java-spring-boot");
        assertThat(AppUtils.toSlug("C++ & Python & JS 101!")).isEqualTo("c-python-js-101");
        assertThat(AppUtils.toSlug("Đường đời")).isEqualTo("duong-doi");
    }

    @Test
    @DisplayName("toUniqueSlug — generates unique slug appending short UUID")
    void toUniqueSlug_appendsUUID() {
        String slug = AppUtils.toUniqueSlug("Java Core");
        assertThat(slug).startsWith("java-core-");
        assertThat(slug.length()).isEqualTo("java-core-".length() + 8);
    }

    @Test
    @DisplayName("isAllowedContentType — checks content type in allowed array")
    void isAllowedContentType_checksArray() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("image/png");

        assertThat(AppUtils.isAllowedContentType(null, new String[]{"image/png"})).isFalse();
        assertThat(AppUtils.isAllowedContentType(file, new String[]{"image/jpeg"})).isFalse();
        assertThat(AppUtils.isAllowedContentType(file, new String[]{"image/png", "image/jpeg"})).isTrue();
    }

    @Test
    @DisplayName("isWithinSizeLimit — validates file size")
    void isWithinSizeLimit_validatesSize() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getSize()).thenReturn(500L);

        assertThat(AppUtils.isWithinSizeLimit(null, 1000L)).isFalse();
        assertThat(AppUtils.isWithinSizeLimit(file, 400L)).isFalse();
        assertThat(AppUtils.isWithinSizeLimit(file, 500L)).isTrue();
        assertThat(AppUtils.isWithinSizeLimit(file, 1000L)).isTrue();
    }

    @Test
    @DisplayName("generateS3Key — builds path using prefix, userId, UUID, and extension")
    void generateS3Key_buildsPath() {
        UUID userId = UUID.fromString("9386c52a-9fc9-450f-90e9-74d326f582f3");
        String key = AppUtils.generateS3Key("uploads/", userId, "my_avatar.png");

        assertThat(key).startsWith("uploads/9386c52a-9fc9-450f-90e9-74d326f582f3/");
        assertThat(key).endsWith(".png");
    }

    @Test
    @DisplayName("maskEmail — masks email to protect privacy")
    void maskEmail_masksProperly() {
        assertThat(AppUtils.maskEmail(null)).isEqualTo("***");
        assertThat(AppUtils.maskEmail("abc")).isEqualTo("***");
        assertThat(AppUtils.maskEmail("ab@example.com")).isEqualTo("a***@example.com");
        assertThat(AppUtils.maskEmail("hello@example.com")).isEqualTo("h***o@example.com");
        assertThat(AppUtils.maskEmail("johndoe@example.com")).isEqualTo("j***e@example.com");
    }
}
