package com.coursehub.security;

import com.coursehub.entity.UserEntity;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Custom UserDetails implementation wrapping UserEntity for Spring Security context.
 */
@Getter
public class UserPrincipal implements UserDetails {

    private final UUID id;
    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;
    private final boolean accountNonLocked;

    private UserPrincipal(UserEntity user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPasswordHash() != null ? user.getPasswordHash() : "";
        this.authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toSet());
        this.enabled = switch (user.getStatus()) {
            case ACTIVE, SOFT_LOCKED -> true;
            default -> false;
        };
        this.accountNonLocked = user.getStatus() != com.coursehub.enums.UserStatus.BANNED
                && user.getStatus() != com.coursehub.enums.UserStatus.SOFT_LOCKED;
    }

    public static UserPrincipal create(UserEntity user) {
        return new UserPrincipal(user);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public boolean hasRole(String roleName) {
        return authorities.stream()
                .anyMatch(a -> a.getAuthority().equals(roleName));
    }
}
