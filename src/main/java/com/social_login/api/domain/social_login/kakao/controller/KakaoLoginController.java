package com.social_login.api.domain.social_login.kakao.controller;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.social_login.api.domain.social_login.kakao.service.KakaoLoginBusinessService;

import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/social-login/kakao")
@RequiredArgsConstructor
public class KakaoLoginController {
    private final KakaoLoginBusinessService kakaoLoginBusinessService;

    @GetMapping("")
    public void kakaoLogin(HttpServletResponse response,  @RequestParam Map<String, Object> params) throws ParseException {
        kakaoLoginBusinessService.kakaoLogin(response, params);
    }
}
