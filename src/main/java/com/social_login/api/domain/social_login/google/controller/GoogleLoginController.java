package com.social_login.api.domain.social_login.google.controller;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.social_login.api.domain.social_login.google.service.GoogleLoginBusinessService;

import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/social-login/google")
@RequiredArgsConstructor
public class GoogleLoginController {
    private final GoogleLoginBusinessService googleLoginBusinessService;

    @GetMapping("")
    public void googleLogin(HttpServletResponse response,  @RequestParam Map<String, Object> params) throws ParseException {
        googleLoginBusinessService.googleLogin(response, params);
    }
}
