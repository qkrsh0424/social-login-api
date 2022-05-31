package com.social_login.api.domain.signup.controller;

import com.social_login.api.domain.message.Message;
import com.social_login.api.domain.signup.dto.SignupDto;
import com.social_login.api.domain.signup.service.SignupBusinessService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/signup")
@RequiredArgsConstructor
public class SignupController {
    private final SignupBusinessService signupBusinessService;

    @PostMapping("")
    public ResponseEntity<?> signup(@RequestBody SignupDto signupDto) {
        Message message = new Message();

        signupBusinessService.signup(signupDto);

        message.setStatus(HttpStatus.OK);
        message.setMessage("success");
        return new ResponseEntity<>(message, message.getStatus());
    }
}
