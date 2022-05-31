package com.social_login.api.domain.exception;

public class CustomConflictErrorException extends RuntimeException {

    public CustomConflictErrorException(String message) {
        super(message);
    }

    public CustomConflictErrorException(Throwable cause) {
        super(cause);
    }

    public CustomConflictErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public CustomConflictErrorException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
