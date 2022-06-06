package com.social_login.api.config.auth;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.social_login.api.config.exception.AuthenticationMethodNotAllowedException;
import com.social_login.api.config.utils.AuthTokenUtils;
import com.social_login.api.domain.message.Message;
import com.social_login.api.domain.refresh_token.entity.RefreshTokenEntity;
import com.social_login.api.domain.refresh_token.repository.RefreshTokenRepository;
import com.social_login.api.domain.user.entity.UserEntity;
import com.social_login.api.utils.CustomCookieInterface;
import com.social_login.api.utils.CustomDateUtils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private RefreshTokenRepository refreshTokenRepository;
    private boolean postOnly = true;

    public JwtAuthenticationFilter(AuthenticationManager authenticationmanager, RefreshTokenRepository refreshTokenRepository) {
        super.setAuthenticationManager(authenticationmanager);
        this.refreshTokenRepository = refreshTokenRepository;

        setFilterProcessesUrl("/api/v1/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        // 로그인 메소드 POST로 제한
        if (this.postOnly && !request.getMethod().equals("POST")) {
            throw new AuthenticationMethodNotAllowedException("Authentication method not supported: " + request.getMethod());
        }

        // UsernamePasswordAuthToken 생성
        try{
            UserEntity user = new ObjectMapper().readValue(request.getInputStream(), UserEntity.class);
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());
            return this.getAuthenticationManager().authenticate(authToken);
        } catch (IOException e) {
            throw new AuthenticationServiceException("입력한 아이디 및 패스워드를 확인해 주세요.");
        }
    }

    // 로그인 성공 시
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
            Authentication authentication) throws IOException, ServletException {
                UserEntity user = ((PrincipalDetails) authentication.getPrincipal()).getUser();
                UUID REFRESH_TOKEN_ID = UUID.randomUUID();

                // ac_token, rf_token 생성
                String accessToken = AuthTokenUtils.getJwtAccessToken(user, REFRESH_TOKEN_ID);
                String refreshToken = AuthTokenUtils.getJwtRefreshToken(user);

                // 리프레시 토큰 저장
                try{
                    RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.builder()
                        .id(REFRESH_TOKEN_ID)
                        .userId(user.getId())
                        .refreshToken(refreshToken)
                        .createdAt(CustomDateUtils.getCurrentDateTime())
                        .updatedAt(CustomDateUtils.getCurrentDateTime())
                        .build();
                    refreshTokenRepository.save(refreshTokenEntity);
                } catch (Exception e) {
                    throw new AuthenticationServiceException("다시 시도해 주세요.");
                }

                // 초과 발급된 리프레시 토큰 삭제
                try{
                    Integer ALLOWED_ACCESS_COUNT = 3;
                    refreshTokenRepository.deleteOldRefreshTokenForUser(user.getId().toString(), ALLOWED_ACCESS_COUNT);
                } catch (Exception e) {
                    throw new AuthenticationServiceException("다시 시도해 주세요.");
                }

                // ac_token 쿠키 생성
                ResponseCookie tokenCookie = ResponseCookie.from("ac_token", accessToken)
                    .httpOnly(true)
                    .domain(CustomCookieInterface.COOKIE_DOMAIN)
                    .secure(CustomCookieInterface.SECURE)
                    .sameSite("Strict")
                    .path("/")
                    .maxAge(CustomCookieInterface.JWT_TOKEN_COOKIE_EXPIRATION)
                    .build();

                response.addHeader(HttpHeaders.SET_COOKIE , tokenCookie.toString());

                Message message = new Message();
                message.setStatus(HttpStatus.OK);
                message.setMessage("success");
                message.setMemo("login");
                    
                String msg = new ObjectMapper().writeValueAsString(message);
                response.setStatus(message.getStatus().value());
                response.setCharacterEncoding("UTF-8");
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write(msg);
                response.getWriter().flush();
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException failed) throws IOException, ServletException {

        Object exceptionClass = failed.getClass();
        if (exceptionClass.equals(AuthenticationMethodNotAllowedException.class)) {
            String msg = errorMessage(HttpStatus.METHOD_NOT_ALLOWED, "method_not_allowed", failed.getMessage());
            response.setStatus(HttpStatus.METHOD_NOT_ALLOWED.value());
            response.setContentType(MediaType.APPLICATION_JSON.toString());
            response.getWriter().write(msg);
            response.getWriter().flush();
            return;
        }

        Message message = new Message();
        message.setMessage("login_error");
        message.setStatus(HttpStatus.UNAUTHORIZED);
        message.setMemo("username not exist or password not matched.");

        ObjectMapper om = new ObjectMapper();
        String oms = om.writeValueAsString(message);

        response.setStatus(message.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON.toString());
        response.getWriter().write(oms);
        response.getWriter().flush();
        super.unsuccessfulAuthentication(request, response, failed);
    }

    private String errorMessage(HttpStatus status, String errorMessage, String errorMemo) throws IOException {
        Message message = new Message();

        message.setStatus(status);
        message.setMessage(errorMessage);
        message.setMemo(errorMemo);
        return new ObjectMapper().writeValueAsString(message);
    }
}
