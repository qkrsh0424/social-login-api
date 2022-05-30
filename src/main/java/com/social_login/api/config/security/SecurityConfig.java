package com.social_login.api.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic().disable()
                .csrf().disable()
                .formLogin().disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS); // 세션사용하지 않고 토큰 사용.

        http.cors();
        http
            .authorizeRequests()
            .antMatchers(
                "/api/v1/csrf",
                "/api/v1/login",
                "/api/v1/logout",
                "/api/v1/signup",
                "/api/v1/social-login/**"
            )
            .permitAll()
            .anyRequest().denyAll();
        return http.build();
    }
}
