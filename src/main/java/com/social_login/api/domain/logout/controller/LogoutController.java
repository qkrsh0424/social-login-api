package com.social_login.api.domain.logout.controller;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.social_login.api.domain.message.Message;
import com.social_login.api.utils.CustomCookieInterface;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/logout")
@RequiredArgsConstructor
public class LogoutController {

    @PostMapping("")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Message message = new Message();
        
        // access_token제거
        ResponseCookie accessToken = ResponseCookie.from("ac_token", null)
            .path("/")
            .sameSite("Strict")
            .domain(CustomCookieInterface.COOKIE_DOMAIN)
            .maxAge(0)
            .secure(CustomCookieInterface.SECURE)
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, accessToken.toString());

        message.setStatus(HttpStatus.OK);
        message.setMessage("success");
        message.setMemo("logout");

        return new ResponseEntity<>(message, message.getStatus());
    }
}
