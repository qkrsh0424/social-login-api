package com.social_login.api.config.exception;

import org.springframework.security.core.AuthenticationException;

public class AuthenticationMethodNotAllowedException extends AuthenticationException {
    public AuthenticationMethodNotAllowedException(String msg) {
        super(msg);
    }
    public AuthenticationMethodNotAllowedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
