package com.coursehub.controller;

import com.coursehub.dto.request.LoginRequest;
import com.coursehub.dto.request.RegisterRequest;
import com.coursehub.dto.request.VerifyOtpRequest;
import com.coursehub.dto.response.AuthResponse;
import com.coursehub.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController WebMvc Tests")
public class AuthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private AuthService authService;

        @MockitoBean
        private com.coursehub.security.CustomUserDetailsService customUserDetailsService;

        @MockitoBean
        private com.coursehub.security.JwtAuthenticationFilter jwtAuthenticationFilter;

        @MockitoBean
        private com.coursehub.security.JwtAuthEntryPoint jwtAuthEntryPoint;

        @MockitoBean
        private org.springframework.data.redis.connection.RedisConnectionFactory redisConnectionFactory;

        @Test
        @DisplayName("POST /api/v1/auth/register — returns 201 Created")
        void register_success() throws Exception {
                RegisterRequest req = new RegisterRequest();
                req.setEmail("new@example.com");
                req.setPassword("Password@123");
                req.setFullName("New User");

                doNothing().when(authService).register(any(RegisterRequest.class));

                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("POST /api/v1/auth/verify-otp — returns 200 OK")
        void verifyOtp_success() throws Exception {
                VerifyOtpRequest req = new VerifyOtpRequest();
                req.setEmail("new@example.com");
                req.setOtpCode("123456");

                doNothing().when(authService).verifyOtp(any(VerifyOtpRequest.class));

                mockMvc.perform(post("/api/v1/auth/verify-otp")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("POST /api/v1/auth/login — returns 200 OK and sets HTTP cookie")
        void login_success() throws Exception {
                LoginRequest req = new LoginRequest();
                req.setEmail("user@example.com");
                req.setPassword("Password@123");

                AuthResponse authResponse = AuthResponse.builder()
                                .accessToken("mock-access-token")
                                .refreshToken("mock-refresh-token")
                                .expiresInMs(900000L)
                                .user(AuthResponse.UserDto.builder()
                                                .id(UUID.randomUUID())
                                                .email("user@example.com")
                                                .fullName("User")
                                                .roles(Collections.singleton("ROLE_STUDENT"))
                                                .build())
                                .build();

                given(authService.login(any(LoginRequest.class))).willReturn(authResponse);

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isOk())
                                .andExpect(cookie().exists("refreshToken"))
                                .andExpect(cookie().httpOnly("refreshToken", true))
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.accessToken").value("mock-access-token"))
                                .andExpect(jsonPath("$.data.refreshToken").isEmpty()); // hidden
        }
}
