package com.social_login.api.config.exception;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.social_login.api.domain.message.Message;

public class SecurityExceptionHandlerFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (RefererException e) {
            errorMessage(response, HttpStatus.FORBIDDEN, "invalid_referer", e.getMessage());
            return;
        } catch (CsrfException e) {
            errorMessage(response, HttpStatus.FORBIDDEN, "invalid_csrf", e.getMessage());
            return;
        } catch (Exception e) {
            errorMessage(response, HttpStatus.INTERNAL_SERVER_ERROR, "undefined", "undefined error.");
            return;
        }
    }

    private void errorMessage(HttpServletResponse response, HttpStatus status, String errorMessage, String errorMemo) throws IOException {
        Message message = new Message();
        message.setStatus(status);
        message.setMessage(errorMessage);
        message.setMemo(errorMemo);

        String msg = new ObjectMapper().writeValueAsString(message);
        response.setStatus(status.value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(msg);
        response.getWriter().flush();
    }
}
