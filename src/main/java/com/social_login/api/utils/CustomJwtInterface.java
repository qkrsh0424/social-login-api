package com.social_login.api.utils;

public interface CustomJwtInterface {
    final static Integer JWT_TOKEN_EXPIRATION = 10 * 60 * 1000;  // milliseconds - 10분
    // final static Integer JWT_TOKEN_EXPIRATION = 1 * 60 * 1000;  // milliseconds - 1분
    // final static Integer JWT_TOKEN_EXPIRATION = 5 * 1000;  // milliseconds - 5초

    final static Integer REFRESH_TOKEN_JWT_EXPIRATION = 5 * 24 * 60 * 60 * 1000;   // milliseconds - 5일/**/

    final static Integer CSRF_TOKEN_JWT_EXPIRATION = 100 * 1000;  // milliseconds - 5000ms -> 5s
}
