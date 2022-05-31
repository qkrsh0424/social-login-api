package com.social_login.api.domain.social_login.facebook.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.social_login.api.domain.message.Message;
import com.social_login.api.domain.refresh_token.entity.RefreshTokenEntity;
import com.social_login.api.domain.refresh_token.repository.RefreshTokenRepository;
import com.social_login.api.domain.user.entity.UserEntity;
import com.social_login.api.domain.user.service.UserService;
import com.social_login.api.utils.ApiRequestUtils;
import com.social_login.api.utils.AuthTokenUtils;
import com.social_login.api.utils.CustomCookieInterface;
import com.social_login.api.utils.CustomDateUtils;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FacebookLoginBusinessService {
    private final UserService userService;
    private final RefreshTokenRepository refreshTokenRepository;

    public void facebookLogin(HttpServletResponse response, Map<String, Object> params) throws ParseException {
        // 페이스북 액세스토큰 
        String token = params.get("token") != null ? params.get("token").toString() : "";

        // 페이스북 인증토큰 발급
        String apiURL = "https://graph.facebook.com/me?access_token=" + token;
        String responseBody = ApiRequestUtils.get(apiURL, new HashMap<>());
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(responseBody);
        JSONObject responseJson = (JSONObject) obj;
        String USER_ID = responseJson.get("id").toString();

        // 페이스북 유저 프로필조회 api
        String userInfoReqApiURL = "https://graph.facebook.com/" + USER_ID + "?fields=name,email&access_token=" + token;
        responseBody = ApiRequestUtils.get(userInfoReqApiURL, new HashMap<>());
        JSONParser parser2 = new JSONParser();
        Object userInfo = parser2.parse(responseBody);
        JSONObject userInfoJson = (JSONObject) userInfo;

        UserEntity entity = UserEntity.builder()
            .id(UUID.randomUUID())
            .username(userInfoJson.get("email").toString())
            .name(userInfoJson.get("name").toString())
            .roles("ROLE_USER")
            .snsType("facebook")
            .snsResponseId(userInfoJson.get("id").toString())
            .createdAt(CustomDateUtils.getCurrentDateTime())
            .updatedAt(CustomDateUtils.getCurrentDateTime())
            .build();
        
        boolean isduplicatedUser = userService.isDuplicatedUserBySnsTypeAndSnsResponseId("facebook", entity.getSnsResponseId());

        // sns_type이 facebook이면서, sns_response_id와 동일한 값이 존재하지 않는다면 회원가입
        if(!isduplicatedUser) {
            userService.saveAndModify(entity);
        }
        
        try {
            this.createAccessToken(response, entity);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void createAccessToken(HttpServletResponse response, UserEntity user) throws IOException {
        UUID refreshTokenId = UUID.randomUUID();
        String accessToken = AuthTokenUtils.getJwtAccessToken(user, refreshTokenId);
        
        // Refresh Token 저장
        String refreshToken = AuthTokenUtils.getJwtRefreshToken(user);
        RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.builder()
            .id(refreshTokenId)
            .userId(user.getId())
            .refreshToken(refreshToken)
            .createdAt(CustomDateUtils.getCurrentDateTime())
            .updatedAt(CustomDateUtils.getCurrentDateTime())
            .build();
        refreshTokenRepository.save(refreshTokenEntity);
        
        // Access Token 발급
        ResponseCookie accessTokenCookie = ResponseCookie.from("ac_token", accessToken).path("/")
                .httpOnly(true)
                .sameSite("Strict")
                .secure(CustomCookieInterface.SECURE)
                .maxAge(CustomCookieInterface.JWT_TOKEN_COOKIE_EXPIRATION).build();

        Message message = new Message();
        message.setMessage("success");
        message.setStatus(HttpStatus.OK);

        ObjectMapper om = new ObjectMapper();
        String oms = om.writeValueAsString(message);

        response.setStatus(HttpStatus.OK.value());
        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        response.setContentType(MediaType.APPLICATION_JSON.toString());
        response.getWriter().write(oms);
        response.getWriter().flush();
    }
}

