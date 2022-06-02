package com.social_login.api.config.exception;

public class CsrfException extends RuntimeException {

    public CsrfException(String message) {
        super(message);
    }

    public CsrfException(Throwable cause) {
        super(cause);
    }

    public CsrfException(String message, Throwable cause) {
        super(message, cause);
    }

    public CsrfException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
