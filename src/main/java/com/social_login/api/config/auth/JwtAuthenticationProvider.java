package com.social_login.api.config.auth;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {
    private final PrincipalDetailsService principalDetailsService;
    private final PasswordEncoder passwordEncoder;

    // 실제 아이디 & 비밀번호 검증
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;
        String USERNAME = token.getName();
        String PASSWORD = token.getCredentials().toString();

        PrincipalDetails principalDetails = principalDetailsService.loadUserByUsername(USERNAME);
        String fullPassword = PASSWORD + principalDetails.getSalt();

        // 비밀번호 비교
        if (!passwordEncoder.matches(fullPassword, principalDetails.getPassword())) {
            throw new BadCredentialsException("입력한 아이디 및 패스워드를 확인해 주세요.");
        }

        return new UsernamePasswordAuthenticationToken(principalDetails, fullPassword, principalDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
