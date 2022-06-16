package com.social_login.api.domain.signup.service;

import java.util.UUID;

import com.social_login.api.domain.exception.CustomConflictErrorException;
import com.social_login.api.domain.signup.dto.SignupDto;
import com.social_login.api.domain.user.entity.UserEntity;
import com.social_login.api.domain.user.service.UserService;
import com.social_login.api.utils.CustomDateUtils;
import com.social_login.api.utils.DataFormatUtils;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SignupBusinessService {
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    public void signup(SignupDto signupDto) {
        // 아이디 & 비밀번호 형식 체크
        DataFormatUtils.checkEmailFormat(signupDto.getUsername());
        DataFormatUtils.checkPasswordFormat(signupDto.getPassword());

        // 아이디 중복 체크
        if (userService.isDuplicatedUsername(signupDto.getUsername())) {
            throw new CustomConflictErrorException("중복된 아이디입니다. 수정 후 다시 시도해주세요.");
        }

        // 비밀번호, 비밀번호 확인 값 검사
        if(!signupDto.getPassword().equals(signupDto.getPasswordCheck())) {
            throw new CustomConflictErrorException("패스워드 불일치.");
        }

        String SALT = UUID.randomUUID().toString();
        String ENC_PASSWORD = passwordEncoder.encode(signupDto.getPassword() + SALT);

        UserEntity userEntity = UserEntity.builder()
                .id(UUID.randomUUID())
                .username(signupDto.getUsername())
                .password(ENC_PASSWORD)
                .salt(SALT)
                .name(signupDto.getName())
                .roles("ROLE_USER")
                .snsType("piaar")
                .createdAt(CustomDateUtils.getCurrentDateTime())
                .updatedAt(CustomDateUtils.getCurrentDateTime())
                .build();
        
        userService.saveAndModify(userEntity);
    }
}
