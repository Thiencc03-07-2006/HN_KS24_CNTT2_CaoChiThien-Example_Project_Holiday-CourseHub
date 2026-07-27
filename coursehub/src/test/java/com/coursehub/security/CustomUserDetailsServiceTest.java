package com.coursehub.security;

import com.coursehub.entity.RoleEntity;
import com.coursehub.entity.UserEntity;
import com.coursehub.enums.UserStatus;
import com.coursehub.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService Unit Tests")
public class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("loadUserByUsername_success — returns UserDetails")
    void loadUserByUsername_success() {
        String email = "test@example.com";
        RoleEntity role = RoleEntity.builder().id(1L).name("ROLE_STUDENT").build();
        UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .email(email)
                .passwordHash("hash")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(role))
                .build();

        given(userRepository.findByEmailWithRoles(email)).willReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(email);
    }

    @Test
    @DisplayName("loadUserByUsername_notFound — throws UsernameNotFoundException")
    void loadUserByUsername_notFound_throwsException() {
        String email = "notfound@example.com";
        given(userRepository.findByEmailWithRoles(email)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found with email");
    }

    @Test
    @DisplayName("loadUserById_success — returns UserDetails")
    void loadUserById_success() {
        UUID id = UUID.randomUUID();
        RoleEntity role = RoleEntity.builder().id(1L).name("ROLE_STUDENT").build();
        UserEntity user = UserEntity.builder()
                .id(id)
                .email("test@example.com")
                .passwordHash("hash")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(role))
                .build();

        given(userRepository.findById(id)).willReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserById(id);

        assertThat(userDetails).isNotNull();
        assertThat(((UserPrincipal) userDetails).getId()).isEqualTo(id);
    }

    @Test
    @DisplayName("loadUserById_notFound — throws UsernameNotFoundException")
    void loadUserById_notFound_throwsException() {
        UUID id = UUID.randomUUID();
        given(userRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserById(id))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found with id");
    }
}
