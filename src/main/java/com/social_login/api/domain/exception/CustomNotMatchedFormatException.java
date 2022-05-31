package com.social_login.api.domain.exception;

public class CustomNotMatchedFormatException extends RuntimeException {

    public CustomNotMatchedFormatException(String message) {
        super(message);
    }

    public CustomNotMatchedFormatException(Throwable cause) {
        super(cause);
    }

    public CustomNotMatchedFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public CustomNotMatchedFormatException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
