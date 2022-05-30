package com.social_login.api.utils;

public interface CustomCookieInterface {

    final static String COOKIE_DOMAIN = "localhost"; // DEV : localhost

    final static Integer JWT_TOKEN_COOKIE_EXPIRATION = 5 * 24 * 60 * 60; // seconds - 5일
}
