package com.social_login.api.config.csrf;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.social_login.api.config.exception.CsrfException;
import com.social_login.api.config.utils.CsrfTokenUtils;

import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

public class CsrfAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        // GET 메소드 통과
        if (request.getMethod().equals("GET")) {
            chain.doFilter(request, response);
            return;
        }

        try {
            // 일종의 저장소
            Cookie csrfJwt = WebUtils.getCookie(request, "csrf_jwt");

            String csrfJwtToken = csrfJwt.getValue();
            // 실제 CSRF 토큰 값
            String csrfToken = request.getHeader("X-XSRF-TOKEN");

            Claims claims = Jwts.parser().setSigningKey(CsrfTokenUtils.getCsrfJwtSecret()).parseClaimsJws(csrfJwtToken).getBody();

            // Cookie값과 csrf설정 헤더값이 동일하지 않다면
            if (!claims.get("csrfId").equals(csrfToken)) {
                throw new CsrfException("This is invalid Csrf token.");
            } else {
                chain.doFilter(request, response);
                return;
            }
        } catch (ExpiredJwtException e) { // 토큰 만료
            throw new CsrfException("Csrf jwt expired.");
        } catch (NullPointerException e) { // CSRF 쿠키가 존재하지 않는다면
            throw new CsrfException("Csrf cookie does not exist.");
        } catch (IllegalArgumentException e) {
            throw new CsrfException("Csrf jwt does not exist.");
        } catch (UnsupportedJwtException e) {
            throw new CsrfException("ClaimsJws argument does not represent an Claims JWS");
        } catch (MalformedJwtException e) {
            throw new CsrfException("ClaimsJws string is not a valid JWS. ");
        } catch (SignatureException e) {
            throw new CsrfException("ClaimsJws JWS signature validation fails");
        }
    }
}
