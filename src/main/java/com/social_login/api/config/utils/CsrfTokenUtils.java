package com.social_login.api.config.utils;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.Cookie;
import javax.xml.bind.DatatypeConverter;

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
public class CsrfTokenUtils {
    private static String csrfJwtSecret;

    @Value("${app.jwt.csrf.secret}")
    private void setCsrfJwtSecret(String csrfJwtSecret) {
        CsrfTokenUtils.csrfJwtSecret = csrfJwtSecret;
    }

    public static String getCsrfJwtSecret() {
        return csrfJwtSecret;
    }

    public static String getCsrfJwtToken(String csrfTokenId) {
        JwtBuilder builder = Jwts.builder()
            .setSubject("CSRF_JWT")
            .setHeader(createHeader())
            .setClaims(createClaims(csrfTokenId))
            .setExpiration(createTokenExpiration(CustomJwtInterface.CSRF_TOKEN_JWT_EXPIRATION))
            .signWith(SignatureAlgorithm.HS256, createSigningKey(csrfJwtSecret));

        return builder.compact();
    }

    private static Map<String, Object> createHeader() {
        Map<String, Object> header = new HashMap<>();
        header.put("typ", "JWT");
        header.put("alg", "HS256");
        header.put("regDate", System.currentTimeMillis());
        return header;
    }

    // JWT Palyod
    private static Map<String, Object> createClaims(String csrfTokenId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("csrfId", csrfTokenId);
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
}
