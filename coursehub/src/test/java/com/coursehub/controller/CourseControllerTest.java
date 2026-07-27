package com.coursehub.controller;

import com.coursehub.dto.request.CreateCourseRequest;
import com.coursehub.dto.response.CourseResponse;
import com.coursehub.dto.response.PageResponse;
import com.coursehub.security.UserPrincipal;
import com.coursehub.service.CourseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CourseController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("CourseController WebMvc Tests")
public class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CourseService courseService;

    @MockitoBean
    private com.coursehub.security.CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private com.coursehub.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private com.coursehub.security.JwtAuthEntryPoint jwtAuthEntryPoint;

    @MockitoBean
    private org.springframework.data.redis.connection.RedisConnectionFactory redisConnectionFactory;

    @Test
    @DisplayName("GET /api/v1/courses/public/search — returns list")
    void searchCourses_success() throws Exception {
        CourseResponse response = CourseResponse.builder().title("Course 1").build();
        PageResponse<CourseResponse> pageResponse = PageResponse.from(new org.springframework.data.domain.PageImpl<>(Collections.singletonList(response)));

        given(courseService.searchCourses(any(), any(), any(), any(), any(), any(), any(), any(), eq(0), eq(12)))
                .willReturn(pageResponse);

        mockMvc.perform(get("/api/v1/courses/public/search")
                        .param("keyword", "Java")
                        .param("page", "0")
                        .param("size", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].title").value("Course 1"));
    }

    @Test
    @DisplayName("POST /api/v1/instructor/courses — creates course")
    void createCourse_success() throws Exception {
        UUID instructorId = UUID.randomUUID();
        com.coursehub.entity.UserEntity userEntity = com.coursehub.entity.UserEntity.builder()
                .id(instructorId)
                .email("instructor@example.com")
                .status(com.coursehub.enums.UserStatus.ACTIVE)
                .roles(Collections.emptySet())
                .build();
        UserPrincipal principal = UserPrincipal.create(userEntity);
        CreateCourseRequest createReq = new CreateCourseRequest();
        createReq.setTitle("New Course Title Long Enough");
        createReq.setShortDescription("Short description that has at least twenty characters.");
        createReq.setPrice(BigDecimal.valueOf(99.00));
        createReq.setCategoryId(1L);
        createReq.setLevel(com.coursehub.enums.CourseLevel.BEGINNER);

        CourseResponse courseResponse = CourseResponse.builder().id(UUID.randomUUID()).title("New Course Title Long Enough").build();

        given(courseService.createCourse(eq(instructorId), any(CreateCourseRequest.class))).willReturn(courseResponse);

        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
        try {
            mockMvc.perform(post("/api/v1/instructor/courses")
                            .with(user(principal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createReq)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.title").value("New Course Title Long Enough"));
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("DELETE /api/v1/instructor/courses/{id} — deletes course")
    void deleteCourse_success() throws Exception {
        UUID instructorId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        com.coursehub.entity.UserEntity userEntity = com.coursehub.entity.UserEntity.builder()
                .id(instructorId)
                .email("instructor@example.com")
                .status(com.coursehub.enums.UserStatus.ACTIVE)
                .roles(Collections.emptySet())
                .build();
        UserPrincipal principal = UserPrincipal.create(userEntity);

        doNothing().when(courseService).deleteCourse(instructorId, courseId);

        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
        try {
            mockMvc.perform(delete("/api/v1/instructor/courses/" + courseId)
                            .with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }
}
