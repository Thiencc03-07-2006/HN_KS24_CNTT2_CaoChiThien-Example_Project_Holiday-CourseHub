package com.coursehub.controller;

import com.coursehub.dto.request.BecomeInstructorRequest;
import com.coursehub.dto.request.UpdateProfileRequest;
import com.coursehub.dto.response.UserProfileResponse;
import com.coursehub.security.UserPrincipal;
import com.coursehub.service.UserService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("UserController WebMvc Tests")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private com.coursehub.security.CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private com.coursehub.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private com.coursehub.security.JwtAuthEntryPoint jwtAuthEntryPoint;

    @MockitoBean
    private org.springframework.data.redis.connection.RedisConnectionFactory redisConnectionFactory;

    @Test
    @DisplayName("GET /api/v1/users/me — returns profile successfully")
    void getMyProfile_success() throws Exception {
        UUID userId = UUID.randomUUID();
        com.coursehub.entity.UserEntity userEntity = com.coursehub.entity.UserEntity.builder()
                .id(userId)
                .email("user@example.com")
                .status(com.coursehub.enums.UserStatus.ACTIVE)
                .roles(Collections.emptySet())
                .build();
        UserPrincipal principal = UserPrincipal.create(userEntity);
        UserProfileResponse profileResponse = UserProfileResponse.builder()
                .id(userId)
                .email("user@example.com")
                .fullName("Test User")
                .build();

        given(userService.getMyProfile(userId)).willReturn(profileResponse);

        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
        try {
            mockMvc.perform(get("/api/v1/users/me")
                            .with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.fullName").value("Test User"));
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("PUT /api/v1/users/me — updates and returns updated profile")
    void updateMyProfile_success() throws Exception {
        UUID userId = UUID.randomUUID();
        com.coursehub.entity.UserEntity userEntity = com.coursehub.entity.UserEntity.builder()
                .id(userId)
                .email("user@example.com")
                .status(com.coursehub.enums.UserStatus.ACTIVE)
                .roles(Collections.emptySet())
                .build();
        UserPrincipal principal = UserPrincipal.create(userEntity);
        UpdateProfileRequest updateReq = new UpdateProfileRequest();
        updateReq.setFullName("Updated Name");

        UserProfileResponse profileResponse = UserProfileResponse.builder()
                .id(userId)
                .fullName("Updated Name")
                .build();

        given(userService.updateMyProfile(eq(userId), any(UpdateProfileRequest.class))).willReturn(profileResponse);

        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
        try {
            mockMvc.perform(put("/api/v1/users/me")
                            .with(user(principal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.fullName").value("Updated Name"));
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("GET /api/v1/instructors/public/{id} — returns public details")
    void getInstructorPublicProfile_success() throws Exception {
        UUID instructorId = UUID.randomUUID();
        UserProfileResponse publicProfile = UserProfileResponse.builder()
                .id(instructorId)
                .fullName("Expert Instructor")
                .build();

        given(userService.getInstructorPublicProfile(instructorId)).willReturn(publicProfile);

        mockMvc.perform(get("/api/v1/instructors/public/" + instructorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Expert Instructor"));
    }
}
