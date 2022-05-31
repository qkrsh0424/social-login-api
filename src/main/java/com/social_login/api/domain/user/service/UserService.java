package com.social_login.api.domain.user.service;

import java.util.List;
import java.util.Optional;

import com.social_login.api.domain.exception.CustomNotMatchedFormatException;
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

    // 아이디 중복 체크
    public boolean isDuplicatedUsername(String username) {
        if (username == null) {
            throw new CustomNotMatchedFormatException("아이디 정보를 입력해 주세요.");
        }

        Optional<UserEntity> userEntityOpt = userRepository.findByUsernameAndSnsType(username, "piaar");
        return userEntityOpt.isPresent();
    }

    public boolean isDuplicatedUserBySnsTypeAndSnsResponseId(String snsType, String snsResponseId) {
        Optional<UserEntity> userEntityOpt = userRepository.findBySnsTypeAndSnsResponseId(snsType, snsResponseId);
        return userEntityOpt.isPresent();
    }
    
}
