package com.social_login.api.domain.signup.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignupDto {
    private String username;
    private String password;
    private String passwordCheck;
    private String name;
}
