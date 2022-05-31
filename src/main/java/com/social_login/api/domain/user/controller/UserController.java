package com.social_login.api.domain.user.controller;

import com.social_login.api.config.auth.PrincipalDetails;
import com.social_login.api.domain.message.Message;
import com.social_login.api.domain.user.dto.UserDto;
import com.social_login.api.domain.user.entity.UserEntity;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @GetMapping("/login-check")
    public ResponseEntity<?> loginCheck(){
        Message message = new Message();
        UserDto userDto = new UserDto();

        // Security Filter 에서 넘어오는 SecurityContextHolder 를 읽어서 유저상태를 검사한다.
        if (!(SecurityContextHolder.getContext().getAuthentication().getName() == null ||
                SecurityContextHolder.getContext().getAuthentication().getName() .equals("anonymousUser"))) {
            PrincipalDetails pd = (PrincipalDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UserEntity userEntity = pd.getUser();
            userDto.setId(userEntity.getId());
            userDto.setUsername(userEntity.getUsername());
            userDto.setRoles(userEntity.getRoles());
            
            message.setStatus(HttpStatus.OK);
            message.setMessage("loged");
            message.setMemo("already login");
            message.setData(userDto);
        } else {
            message.setStatus(HttpStatus.OK);
            message.setMessage("need_login");
            message.setMemo("need login");
        }
        return new ResponseEntity<>(message, message.getStatus());
    }
}
