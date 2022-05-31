package com.social_login.api.domain.user.repository;

import java.util.List;
import java.util.Optional;

import com.social_login.api.domain.user.entity.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    List<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByUsernameAndSnsType(String username, String snsType);
    Optional<UserEntity> findBySnsTypeAndSnsResponseId(String snsType, String snsResponseId);
}
