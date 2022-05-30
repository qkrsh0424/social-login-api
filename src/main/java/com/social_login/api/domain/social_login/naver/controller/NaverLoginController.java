package com.social_login.api.domain.social_login.naver.controller;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.social_login.api.domain.message.Message;
import com.social_login.api.domain.social_login.naver.service.NaverLoginBusinessService;

import org.json.simple.parser.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/social-login/naver")
@RequiredArgsConstructor
public class NaverLoginController {
    private final NaverLoginBusinessService naverLoginBusinessService;
    
    @GetMapping("")
    public void naverLogin(HttpServletResponse response, @RequestParam Map<String, Object> params) throws ParseException {
        naverLoginBusinessService.naverLogin(response, params);
    }
}
