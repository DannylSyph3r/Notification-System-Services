package dev.slethware.apigateway.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;


public record UserPrincipal(UUID id, String email) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // No role-based auth required for this service as per the plan
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return null; // Not used
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
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}