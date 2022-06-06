package com.social_login.api.domain.exception;

public class CustomApiResponseException extends RuntimeException {

    public CustomApiResponseException(String message) {
        super(message);
    }

    public CustomApiResponseException(Throwable cause) {
        super(cause);
    }

    public CustomApiResponseException(String message, Throwable cause) {
        super(message, cause);
    }

    public CustomApiResponseException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
