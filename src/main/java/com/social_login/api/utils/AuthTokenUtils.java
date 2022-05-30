package com.social_login.api.utils;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.Cookie;
import javax.xml.bind.DatatypeConverter;

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

    @Value("${app.jwt.access.secret}")
    private String accessTokenSecret;

    @Value("${app.jwt.refresh.secret}")
    private String refreshTokenSecret;

    public String getJwtAccessToken(UUID id, UUID refreshTokenId) {
        JwtBuilder builder = Jwts.builder()
            .setSubject("JWT_ACT")
            .setHeader(createHeader())
            .setClaims(createClaims(id, refreshTokenId))
            .setExpiration(createTokenExpiration(CustomJwtInterface.JWT_TOKEN_EXPIRATION))
            .signWith(SignatureAlgorithm.HS256, createSigningKey(accessTokenSecret));

        return builder.compact();
    }

    public String getJwtRefreshToken(UUID id) {
        JwtBuilder builder = Jwts.builder()
            .setSubject("JWT_RFT")
            .setHeader(createHeader())
            .setClaims(createRefreshTokenClaims(id))
            .setExpiration(createTokenExpiration(CustomJwtInterface.REFRESH_TOKEN_JWT_EXPIRATION))
            .signWith(SignatureAlgorithm.HS256, createSigningKey(refreshTokenSecret));
        
        return builder.compact();
    }
    
    // JWT Header
    private Map<String, Object> createHeader() {
        Map<String, Object> header = new HashMap<>();
        header.put("typ", "JWT");
        header.put("alg", "HS256");
        header.put("regDate", System.currentTimeMillis());
        return header;
    }

    // JWT Palyod
    private Map<String, Object> createClaims(UUID id, UUID refreshTokenId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", id);
        claims.put("refreshTokenId", refreshTokenId);
        return claims;
    }

    private Map<String, Object> createRefreshTokenClaims(UUID id) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", id);;
        return claims;
    }

    private Date createTokenExpiration(Integer expirationTime) {
        Date expiration = new Date(System.currentTimeMillis() + expirationTime);
        return expiration;
    }

    private Key createSigningKey(String tokenSecret) {
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(tokenSecret);
        return new SecretKeySpec(apiKeySecretBytes, SignatureAlgorithm.HS256.getJcaName()); 
    }

    public boolean isValidToken(Cookie jwtCookie) {
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
