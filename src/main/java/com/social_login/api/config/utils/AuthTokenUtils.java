package com.social_login.api.config.utils;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.Cookie;
import javax.xml.bind.DatatypeConverter;

import com.social_login.api.domain.user.entity.UserEntity;
import com.social_login.api.utils.CustomJwtInterface;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class AuthTokenUtils {
    private static String accessTokenSecret;
    private static String refreshTokenSecret;

    @Value("${app.jwt.access.secret}")
    private void setAccessTokenSecret(String accessTokenSecret) {
        AuthTokenUtils.accessTokenSecret = accessTokenSecret;
    }

    public static String getAccessTokenSecret() {
        return accessTokenSecret;
    }

    @Value("${app.jwt.refresh.secret}")
    private void setRefreshTokenSecret(String refreshTokenSecret) {
        AuthTokenUtils.refreshTokenSecret = refreshTokenSecret;
    }

    public static String getRefreshTokenSecret() {
        return refreshTokenSecret;
    }

    public static String getJwtAccessToken(UserEntity user, UUID refreshTokenId) {
        JwtBuilder builder = Jwts.builder()
            .setSubject("JWT_ACT")
            .setHeader(createHeader())
            .setClaims(createClaims(user, refreshTokenId))
            .setExpiration(createTokenExpiration(CustomJwtInterface.JWT_TOKEN_EXPIRATION))
            .signWith(SignatureAlgorithm.HS256, createSigningKey(accessTokenSecret));

        return builder.compact();
    }

    public static String getJwtRefreshToken(UserEntity user) {
        JwtBuilder builder = Jwts.builder()
            .setSubject("JWT_RFT")
            .setHeader(createHeader())
            .setClaims(createRefreshTokenClaims(user))
            .setExpiration(createTokenExpiration(CustomJwtInterface.REFRESH_TOKEN_JWT_EXPIRATION))
            .signWith(SignatureAlgorithm.HS256, createSigningKey(refreshTokenSecret));
        
        return builder.compact();
    }
    
    // JWT Header
    private static Map<String, Object> createHeader() {
        Map<String, Object> header = new HashMap<>();
        header.put("typ", "JWT");
        header.put("alg", "HS256");
        header.put("regDate", System.currentTimeMillis());
        return header;
    }

    // JWT Palyod
    private static Map<String, Object> createClaims(UserEntity user, UUID refreshTokenId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("name", user.getName());
        claims.put("username", user.getUsername());
        claims.put("roles", user.getRoles());
        claims.put("refreshTokenId", refreshTokenId);
        return claims;
    }

    private static Map<String, Object> createRefreshTokenClaims(UserEntity user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("name", user.getName());
        claims.put("username", user.getUsername());
        claims.put("roles", user.getRoles());
        return claims;
    }

    private static Date createTokenExpiration(Integer expirationTime) {
        Date expiration = new Date(System.currentTimeMillis() + expirationTime);
        return expiration;
    }

    private static Key createSigningKey(String tokenSecret) {
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(tokenSecret);
        return new SecretKeySpec(apiKeySecretBytes, SignatureAlgorithm.HS256.getJcaName()); 
    }

    public static boolean isValidToken(Cookie jwtCookie) {
        String accessToken = jwtCookie.getValue();

        try {
            Claims claims = Jwts.parser().setSigningKey(accessTokenSecret).parseClaimsJws(accessToken).getBody();
            log.info("expireTime :" + claims.getExpiration());
            log.info("email :" + claims.get("email"));
            log.info("roles :" + claims.get("roles"));
            return true;
        } catch (ExpiredJwtException exception) {
            log.error("Token Expired");
            return false;
        } catch (JwtException exception) {
            log.error("Token Tampered");
            return false;
        } catch (NullPointerException exception) {
            log.error("Token is null");
            return false;
        }
    }
}
