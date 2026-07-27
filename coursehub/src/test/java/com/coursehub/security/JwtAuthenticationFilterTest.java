package com.coursehub.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter Unit Tests")
public class JwtAuthenticationFilterTest {

    @Mock private JwtUtils jwtUtils;
    @Mock private CustomUserDetailsService userDetailsService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("doFilterInternal_validToken — extracts token, authenticates user, sets SecurityContext")
    void doFilterInternal_validToken() throws ServletException, IOException {
        String jwt = "valid.jwt.token";
        UUID userId = UUID.randomUUID();
        UserDetails userDetails = mock(UserDetails.class);
        given(userDetails.getAuthorities()).willReturn(Collections.emptyList());

        given(request.getHeader("Authorization")).willReturn("Bearer " + jwt);
        given(jwtUtils.validateToken(jwt)).willReturn(true);
        given(jwtUtils.extractUserId(jwt)).willReturn(userId);
        given(userDetailsService.loadUserById(userId)).willReturn(userDetails);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal_noHeader — does not authenticate, calls filterChain")
    void doFilterInternal_noHeader() throws ServletException, IOException {
        given(request.getHeader("Authorization")).willReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal_invalidToken — does not authenticate, calls filterChain")
    void doFilterInternal_invalidToken() throws ServletException, IOException {
        String jwt = "invalid.jwt.token";
        given(request.getHeader("Authorization")).willReturn("Bearer " + jwt);
        given(jwtUtils.validateToken(jwt)).willReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}
