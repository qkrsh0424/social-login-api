package com.social_login.api.utils;

import java.util.regex.Pattern;

import com.social_login.api.domain.exception.CustomNotMatchedFormatException;

public class DataFormatUtils {

    public static void checkEmailFormat(String email) {
        boolean isEmailAddressFormat = Pattern.compile("^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$").matcher(email).find();

        if (!isEmailAddressFormat) {
            throw new CustomNotMatchedFormatException("이메일 형식이 올바르지 않습니다.");
        }
    }

    public static void checkPasswordFormat(String password) {
        boolean isPasswordFormat = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[.?!@#$%^&*])[A-Za-z\\d.?!@#$%^&*]{8,20}$").matcher(password).find();

        if (!isPasswordFormat) {
            throw new CustomNotMatchedFormatException("비밀번호 형식이 올바르지 않습니다.");
        }
    }
}
