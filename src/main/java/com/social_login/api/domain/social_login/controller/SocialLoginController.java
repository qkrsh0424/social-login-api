package com.social_login.api.domain.social_login.controller;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.social_login.api.domain.social_login.service.SocialLoginBusinessService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/social-login")
@RequiredArgsConstructor
public class SocialLoginController {
    private final SocialLoginBusinessService socialLoginBusinessService;
    
    @GetMapping("/naver")
    public void naverLogin(HttpServletResponse response, @RequestParam Map<String, Object> params) throws IOException {     
        socialLoginBusinessService.naverLogin(response, params);
    }

    @GetMapping("/kakao")
    public void kakaoLogin(HttpServletResponse response,  @RequestParam Map<String, Object> params) throws IOException { 
        socialLoginBusinessService.kakaoLogin(response, params);
    }

    @GetMapping("/google")
    public void googleLogin(HttpServletResponse response,  @RequestParam Map<String, Object> params) throws IOException {
        socialLoginBusinessService.googleLogin(response, params);
    }

    @GetMapping("/facebook")
    public void facebookLogin(HttpServletResponse response,  @RequestParam Map<String, Object> params) throws IOException {
        socialLoginBusinessService.facebookLogin(response, params);
    }
}
