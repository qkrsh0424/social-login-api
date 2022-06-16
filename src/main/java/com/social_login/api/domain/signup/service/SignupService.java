package com.social_login.api.domain.signup.service;

import com.social_login.api.domain.user.entity.UserEntity;
import com.social_login.api.domain.user.repository.UserRepository;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SignupService {
    private final UserRepository userRepository;

    public void saveAndModify(UserEntity entity) {
        userRepository.save(entity);
    }
}
