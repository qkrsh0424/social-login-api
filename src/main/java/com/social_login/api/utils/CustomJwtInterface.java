package com.social_login.api.utils;

public interface CustomJwtInterface {
    final static Integer JWT_TOKEN_EXPIRATION = 10 * 60 * 1000;  // milliseconds - 10분

    final static Integer REFRESH_TOKEN_JWT_EXPIRATION = 5 * 24 * 60 * 60 * 1000;   // milliseconds - 5일/**/
}
