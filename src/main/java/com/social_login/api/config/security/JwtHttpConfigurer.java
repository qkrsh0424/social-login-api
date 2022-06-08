package com.social_login.api.config.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.stereotype.Component;

import com.social_login.api.config.auth.JwtAuthenticationFilter;
import com.social_login.api.config.auth.JwtAuthorizationFilter;
import com.social_login.api.domain.refresh_token.repository.RefreshTokenRepository;

@Component
public class JwtHttpConfigurer extends AbstractHttpConfigurer<JwtHttpConfigurer, HttpSecurity> {
    private RefreshTokenRepository refreshTokenRepository;

    public JwtHttpConfigurer(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public void configure(HttpSecurity http) {
        final AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);
        http.addFilterAfter(new JwtAuthenticationFilter(authenticationManager, refreshTokenRepository), JwtAuthorizationFilter.class);
    }
}
