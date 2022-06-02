package com.social_login.api.config.exception;

public class RefererException extends RuntimeException {

    public RefererException(String message) {
        super(message);
    }

    public RefererException(Throwable cause) {
        super(cause);
    }

    public RefererException(String message, Throwable cause) {
        super(message, cause);
    }

    public RefererException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
