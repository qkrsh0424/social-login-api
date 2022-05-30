package com.social_login.api.domain.user.service;

import java.util.List;

import com.social_login.api.domain.user.entity.UserEntity;
import com.social_login.api.domain.user.repository.UserRepository;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public List<UserEntity> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public void saveAndModify(UserEntity entity) {
        userRepository.save(entity);
    }
    
}
