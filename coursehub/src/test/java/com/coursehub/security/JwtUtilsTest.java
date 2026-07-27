package com.coursehub.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtUtils Unit Tests")
public class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private final String testSecret = "CourseHubSuperSecretKeyForJWT2024MustBe256BitsLongForHS256Algorithm!";
    private final long testExpiration = 900000L; // 15 mins
    private final String testIssuer = "coursehub.com";

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(jwtUtils, "accessTokenExpirationMs", testExpiration);
        ReflectionTestUtils.setField(jwtUtils, "issuer", testIssuer);
        jwtUtils.init();
    }

    @Test
    @DisplayName("generateAndParseToken — generates signed token, extracts subject and claims successfully")
    void generateAndParseToken_success() {
        UUID userId = UUID.randomUUID();
        String email = "student@coursehub.com";

        String token = jwtUtils.generateAccessToken(userId, email);

        assertThat(token).isNotBlank();
        assertThat(jwtUtils.validateToken(token)).isTrue();
        assertThat(jwtUtils.extractEmail(token)).isEqualTo(email);
        assertThat(jwtUtils.extractUserId(token)).isEqualTo(userId);
    }

    @Test
    @DisplayName("validateToken_invalid — returns false on malformed token")
    void validateToken_invalid() {
        String invalidToken = "this.is.not.a.jwt.token";
        assertThat(jwtUtils.validateToken(invalidToken)).isFalse();
    }

    @Test
    @DisplayName("validateToken_expired — returns false on expired token")
    void validateToken_expired() {
        // Set short expiration to mock immediate expire
        JwtUtils expiredUtils = new JwtUtils();
        ReflectionTestUtils.setField(expiredUtils, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(expiredUtils, "accessTokenExpirationMs", -1000L); // negative time
        ReflectionTestUtils.setField(expiredUtils, "issuer", testIssuer);
        expiredUtils.init();

        UUID userId = UUID.randomUUID();
        String email = "student@coursehub.com";

        String token = expiredUtils.generateAccessToken(userId, email);

        assertThat(jwtUtils.validateToken(token)).isFalse(); // validate with main utils (normal clock)
    }
}
