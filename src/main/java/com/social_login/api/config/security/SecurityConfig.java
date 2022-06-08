package com.social_login.api.config.security;

import com.social_login.api.config.auth.JwtAuthenticationProvider;
import com.social_login.api.config.auth.JwtAuthorizationFilter;
import com.social_login.api.config.auth.PrincipalDetailsService;
import com.social_login.api.config.csrf.CsrfAuthenticationFilter;
import com.social_login.api.config.exception.SecurityExceptionHandlerFilter;
import com.social_login.api.config.referer.RefererAuthenticationFilter;
import com.social_login.api.domain.refresh_token.repository.RefreshTokenRepository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final RefreshTokenRepository refreshTokenRepository;
    private final PrincipalDetailsService principalDetailsService;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .httpBasic().disable()
            .csrf().disable()
            .formLogin().disable()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS); // 세션사용하지 않고 토큰 사용.

        http
            .cors();

        http
            .authorizeRequests()
            .antMatchers(
                    "/api/v1/csrf",
                    "/api/v1/login",
                    "/api/v1/logout",
                    "/api/v1/signup",
                    "/api/v1/social-login/**",
                    "/api/v1/user/login-check")
            .permitAll()
            .anyRequest().denyAll();

        http
            .apply(new JwtHttpConfigurer(refreshTokenRepository));

        http
            .addFilterBefore(new RefererAuthenticationFilter(), CsrfFilter.class)
            .addFilterAfter(new CsrfAuthenticationFilter(), RefererAuthenticationFilter.class)
            .addFilterAfter(new JwtAuthorizationFilter(refreshTokenRepository), CsrfAuthenticationFilter.class)
            .addFilterBefore(new SecurityExceptionHandlerFilter(), RefererAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationProvider jwtAuthenticationProvider() {
        return new JwtAuthenticationProvider(principalDetailsService, passwordEncoder());
    }
}
