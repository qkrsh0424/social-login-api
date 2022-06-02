package com.social_login.api.config.auth;

import java.util.Optional;

import com.social_login.api.domain.user.entity.UserEntity;
import com.social_login.api.domain.user.repository.UserRepository;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PrincipalDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public PrincipalDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserEntity> userOpt = userRepository.findByUsernameAndSnsType(username, "piaar");
        
        if(userOpt.isPresent()) {
            return new PrincipalDetails(userOpt.get());
        } else {
            throw new UsernameNotFoundException("아이디 또는 패스워드를 확인해 주세요.");
        }
    }
}
