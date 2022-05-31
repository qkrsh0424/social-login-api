package com.social_login.api.config.auth;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.social_login.api.domain.refresh_token.entity.RefreshTokenEntity;
import com.social_login.api.domain.refresh_token.repository.RefreshTokenRepository;
import com.social_login.api.domain.user.entity.UserEntity;
import com.social_login.api.utils.AuthTokenUtils;
import com.social_login.api.utils.CustomCookieInterface;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

public class JwtAuthorizationFilter extends OncePerRequestFilter {
    private RefreshTokenRepository refreshTokenRepository;

    public JwtAuthorizationFilter (RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
                Cookie jwtTokenCookie = WebUtils.getCookie(request, "ac_token");

                // 액세스 토큰 쿠키가 있는지 확인, 만약에 없다면 체인을 계속 타게하고 있다면 검증한다.
                if (jwtTokenCookie == null) {
                    filterChain.doFilter(request, response);
                    return;
                }

                String accessToken = jwtTokenCookie.getValue();
                Claims claims = null;
                boolean accessTokenExpired = false;

                try {
                    claims = Jwts.parser().setSigningKey(AuthTokenUtils.getAccessTokenSecret()).parseClaimsJws(accessToken).getBody();
                } catch (ExpiredJwtException e) {
                    // 만료된 액세스토큰 claims를 이용해 리프레시 토큰 id를 추출할 수 있다.
                    claims = e.getClaims();
                    accessTokenExpired = true;
                } catch (UnsupportedJwtException e) {
                    filterChain.doFilter(request, response);
                    return;
                } catch (MalformedJwtException e) {
                    filterChain.doFilter(request, response);
                    return;
                } catch (SignatureException e) {
                    filterChain.doFilter(request, response);
                    return;
                } catch (IllegalArgumentException e) {
                    filterChain.doFilter(request, response);
                    return;
                } catch (Exception e) {
                    filterChain.doFilter(request, response);
                    return;
                }

                if(claims == null) {
                    filterChain.doFilter(request, response);
                    return;
                }

                // 액세스 토큰 만료된 경우
                if(accessTokenExpired) {
                    // 리프레시 토큰을 확인해 액세스토큰 발급하는 경우
                    // 액세스 클레임에서 refreshTokenId에 대응하는 RefreshToken값 조회
                    Optional<RefreshTokenEntity> refreshTokenEntityOpt = refreshTokenRepository.findById(UUID.fromString(claims.get("refreshTokenId").toString()));
                    
                    if(refreshTokenEntityOpt.isPresent()) {
                        RefreshTokenEntity refreshTokenEntity = refreshTokenEntityOpt.get();
                        Claims refreshTokenClaims = null;

                        try{
                            refreshTokenClaims = Jwts.parser().setSigningKey(AuthTokenUtils.getRefreshTokenSecret()).parseClaimsJws(refreshTokenEntity.getRefreshToken()).getBody();
                        } catch (Exception e) {
                            filterChain.doFilter(request, response);
                            return;
                        }

                        UUID id = UUID.fromString(refreshTokenClaims.get("id").toString());
                        String name = refreshTokenClaims.get("name").toString();
                        String username = refreshTokenClaims.get("username").toString();
                        String roles = refreshTokenClaims.get("roles").toString();

                        UserEntity user = UserEntity.builder()
                            .id(id)
                            .name(name)
                            .username(username)
                            .roles(roles)
                            .build();

                        // 새로운 액세스 토큰 발급
                        String newAccessToken = AuthTokenUtils.getJwtAccessToken(user, refreshTokenEntityOpt.get().getId());
                        // 새로운 엑세스 토큰을 쿠키에 저장
                        ResponseCookie tokenCookie = ResponseCookie.from("ac_token", newAccessToken)
                            .httpOnly(true)
                            .secure(true)
                            .sameSite("Strict")
                            .domain(CustomCookieInterface.COOKIE_DOMAIN)
                            .path("/")
                            .maxAge(CustomCookieInterface.JWT_TOKEN_COOKIE_EXPIRATION)
                            .build();

                        response.addHeader(HttpHeaders.SET_COOKIE, tokenCookie.toString());

                        UserEntity userEntity = UserEntity.builder()
                            .id(id)
                            .name(name)
                            .username(username)
                            .roles(roles)
                            .build();
                        
                        this.saveAuthenticationToSecurityContextHolder(userEntity);
                    }
                    filterChain.doFilter(request, response);
                    return;
                }

                UUID id = UUID.fromString(claims.get("id").toString());
                String name = claims.get("name").toString();
                String username = claims.get("username").toString();
                String roles = claims.get("roles").toString();

                UserEntity userEntity = UserEntity.builder()
                        .id(id)
                        .name(name)
                        .username(username)
                        .roles(roles)
                        .build();
                this.saveAuthenticationToSecurityContextHolder(userEntity);

                filterChain.doFilter(request, response);
            }

    private void saveAuthenticationToSecurityContextHolder(UserEntity userEntity) {
        PrincipalDetails principalDetails = new PrincipalDetails(userEntity);

        // Jwt 토큰 서명을 통해서 서명이 정상이면 Authentication 객체를 만들어준다.
        Authentication authentication = new UsernamePasswordAuthenticationToken(principalDetails, null,
                principalDetails.getAuthorities());

        // 강제로 시큐리티의 세션에 접근하여 Authentication 객체를 저장.
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    
}
