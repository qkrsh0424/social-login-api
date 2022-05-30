package com.social_login.api.domain.social_login.facebook.controller;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.social_login.api.domain.social_login.facebook.service.FacebookLoginBusinessService;

import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/social-login/facebook")
@RequiredArgsConstructor
public class FacebookLoginController {
    private final FacebookLoginBusinessService facebookLoginBusinessService;

    @GetMapping("")
    public void googleLogin(HttpServletResponse response,  @RequestParam Map<String, Object> params) throws ParseException {
        facebookLoginBusinessService.facebookLogin(response, params);
    }
}
