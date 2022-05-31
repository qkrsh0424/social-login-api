package com.social_login.api.config.security;

import com.social_login.api.config.auth.JwtAuthenticationFilter;
import com.social_login.api.config.auth.JwtAuthenticationProvider;
import com.social_login.api.config.auth.JwtAuthorizationFilter;
import com.social_login.api.config.auth.PrincipalDetailsService;
import com.social_login.api.domain.refresh_token.repository.RefreshTokenRepository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final RefreshTokenRepository refreshTokenRepository;
    private final PrincipalDetailsService principalDetailsService;
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
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
                "/api/v1/social-login/**",
                "/api/v1/user/login-check"
            )
            .permitAll()
            .anyRequest().denyAll();
        http
            .addFilterBefore(new JwtAuthorizationFilter(refreshTokenRepository), JwtAuthenticationFilter.class)
            .addFilterBefore(new JwtAuthenticationFilter(authenticationManager(), refreshTokenRepository), UsernamePasswordAuthenticationFilter.class);
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
