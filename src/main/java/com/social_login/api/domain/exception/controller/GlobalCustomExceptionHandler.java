package com.social_login.api.domain.exception.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.social_login.api.domain.exception.CustomApiResponseException;
import com.social_login.api.domain.exception.CustomConflictErrorException;
import com.social_login.api.domain.exception.CustomNotMatchedFormatException;
import com.social_login.api.domain.message.Message;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalCustomExceptionHandler {
    @ExceptionHandler(value = {CustomConflictErrorException.class})
    public ResponseEntity<?> customConflictErrorException(CustomConflictErrorException ex){
        Message message = new Message();
        log.warn("ERROR STACKTRACE => {}", ex.getStackTrace());

        message.setStatus(HttpStatus.CONFLICT);
        message.setMessage("conflicted");
        message.setMemo(ex.getMessage());
        return new ResponseEntity<>(message, message.getStatus());
    }

    @ExceptionHandler(value = {CustomNotMatchedFormatException.class})
    public ResponseEntity<?> customNotMatchedFormatException(CustomNotMatchedFormatException ex){
        Message message = new Message();
        log.warn("ERROR STACKTRACE => {}", ex.getStackTrace());

        message.setStatus(HttpStatus.BAD_REQUEST);
        message.setMessage("not_matched_format");
        message.setMemo(ex.getMessage());
        return new ResponseEntity<>(message, message.getStatus());
    }

    @ExceptionHandler(value = {CustomApiResponseException.class})
    public ResponseEntity<?> customApiResponseException(CustomApiResponseException ex){
        Message message = new Message();
        log.warn("ERROR STACKTRACE => {}", ex.getStackTrace());

        message.setStatus(HttpStatus.BAD_REQUEST);
        message.setMessage("api_res_error");
        message.setMemo(ex.getMessage());
        return new ResponseEntity<>(message, message.getStatus());
    }
}
