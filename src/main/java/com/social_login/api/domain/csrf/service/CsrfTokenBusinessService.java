package com.social_login.api.domain.csrf.service;

import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import com.social_login.api.config.utils.CsrfTokenUtils;
import com.social_login.api.utils.CustomCookieInterface;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class CsrfTokenBusinessService {
    
    public void getCsrfToken(HttpServletResponse response) {
        // 토큰 생성 및 쿠키 설정
        String csrfTokenId = UUID.randomUUID().toString();
        String csrfJwtToken = CsrfTokenUtils.getCsrfJwtToken(csrfTokenId);

        ResponseCookie csrfJwt = ResponseCookie.from("csrf_jwt", csrfJwtToken)
            .httpOnly(true)
            .domain(CustomCookieInterface.COOKIE_DOMAIN)
            .secure(CustomCookieInterface.SECURE)
            .sameSite("Strict")
            .path("/")
            .maxAge(CustomCookieInterface.CSRF_TOKEN_COOKIE_EXPIRATION)
            .build();

        ResponseCookie csrfToken = ResponseCookie.from("csrf_token", csrfTokenId)
                .domain(CustomCookieInterface.COOKIE_DOMAIN)
                .secure(CustomCookieInterface.SECURE)
                .sameSite("Strict")
                .path("/")
                .maxAge(CustomCookieInterface.CSRF_TOKEN_COOKIE_EXPIRATION)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, csrfJwt.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, csrfToken.toString());
    }
}
