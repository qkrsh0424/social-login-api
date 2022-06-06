package com.social_login.api.domain.user.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.social_login.api.domain.user.entity.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    Optional<UserEntity> findById(UUID id);
    List<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByUsernameAndSnsType(String username, String snsType);
    Optional<UserEntity> findBySnsTypeAndSnsResponseId(String snsType, String snsResponseId);
}
