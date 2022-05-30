package com.social_login.api.domain.user.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.social_login.api.domain.user.entity.UserEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@ToString
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
    private UUID id;
    private String username;
    private String password;
    private UUID salt;
    private String name;
    private String roles;
    private String snsType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
