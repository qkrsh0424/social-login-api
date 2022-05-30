package com.social_login.api.domain.user.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.social_login.api.domain.user.dto.UserDto;

import org.hibernate.annotations.Type;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Entity
@ToString
@Table(name = "user")
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cid")
    private Integer cid;

    @Type(type = "uuid-char")
    @Column(name = "id")
    private UUID id;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "salt")
    private UUID salt;

    @Column(name = "name")
    private String name;

    @Column(name = "roles")
    private String roles;

    @Column(name = "sns_type")
    private String snsType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public List<String> getRoleList(){
        if(this.roles.length() > 0){
            return Arrays.asList(this.roles.replaceAll(" ","").split(","));
        }
        return new ArrayList<>();
    }

    public static UserEntity toEntity(UserDto dto) {
        UserEntity entity = UserEntity.builder()
            .id(dto.getId())
            .username(dto.getUsername())
            .password(dto.getPassword())
            .salt(dto.getSalt())
            .name(dto.getName())
            .roles(dto.getRoles())
            .snsType(dto.getSnsType())
            .createdAt(dto.getCreatedAt())
            .updatedAt(dto.getUpdatedAt())
            .build();
        return entity;
    }
}
