package com.social_login.api.domain.social_login.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.social_login.api.config.social_login.SocialLoginConfiguration;
import com.social_login.api.config.utils.AuthTokenUtils;
import com.social_login.api.domain.message.Message;
import com.social_login.api.domain.refresh_token.entity.RefreshTokenEntity;
import com.social_login.api.domain.refresh_token.repository.RefreshTokenRepository;
import com.social_login.api.domain.user.entity.UserEntity;
import com.social_login.api.domain.user.service.UserService;
import com.social_login.api.utils.ApiRequestUtils;
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
public class SocialLoginBusinessService {
    private final UserService userService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SocialLoginConfiguration socialLoginConfiguration;

    /**
     * Naver Login
     * 
     * @param response
     * @param params
     * @throws ParseException
     */
    public void naverLogin(HttpServletResponse response, Map<String, Object> params) throws ParseException {
        // 네이버 액세스토큰(인가토큰)
        String token = params.get("token") != null ? params.get("token").toString() : "";

        // 네이버 인증 토큰 요청 api
        Map<String, String> requestHeaders = new HashMap<>();
        String apiURL = "https://nid.naver.com/oauth2.0/token?" 
            + "grant_type=authorization_code"
            + "&client_id=" + socialLoginConfiguration.getNaver().get("id")
            + "&code=" + token
            + "&client_secret=" + socialLoginConfiguration.getNaver().get("secret");
        String responseBody = ApiRequestUtils.get(apiURL, requestHeaders);
        
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(responseBody);
        JSONObject responseJson = (JSONObject) obj;

        // 네이버 유저 프로필조회 api
        apiURL = "https://openapi.naver.com/v1/nid/me";
        
        String header = "Bearer " + responseJson.get("access_token");
        requestHeaders.put("Authorization", header);
        responseBody = ApiRequestUtils.get(apiURL, requestHeaders);

        obj = parser.parse(responseBody);
        responseJson = (JSONObject) obj;
        JSONObject userInfoJson = (JSONObject) responseJson.get("response");

        UserEntity entity = UserEntity.builder()
            .id(UUID.randomUUID())
            .username(userInfoJson.get("email").toString())
            .name(userInfoJson.get("name").toString())
            .roles("ROLE_USER")
            .snsType("naver")
            .snsResponseId(userInfoJson.get("id").toString())
            .createdAt(CustomDateUtils.getCurrentDateTime())
            .updatedAt(CustomDateUtils.getCurrentDateTime())
            .build();
        
        UserEntity duplicatedUser = userService.searchDuplicatedUserBySnsTypeAndSnsResponseId("naver", entity.getSnsResponseId());

        // sns_type이 naver면서, sns_response_id와 동일한 값이 존재하지 않는다면 회원가입
        if(duplicatedUser  == null) {
            userService.saveAndModify(entity);
        }else {
            entity = duplicatedUser;
        }

        try {
            this.createAccessToken(response, entity);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Kakao Login
     * 
     * @param response
     * @param params
     * @throws ParseException
     */
    public void kakaoLogin(HttpServletResponse response, Map<String, Object> params) throws ParseException {
        // 카카오 액세스토큰(인가토큰)
        String token = params.get("token") != null ? params.get("token").toString() : "";

        // 카카오 인증 토큰 요청 api
        Map<String, String> requestHeaders = new HashMap<>();
        String apiURL = "https://kauth.kakao.com/oauth/token?"
             + "grant_type=authorization_code"
             + "&client_id=" + socialLoginConfiguration.getKakao().get("id")
             + "&code=" + token
            + "&client_secret=" + socialLoginConfiguration.getKakao().get("secret");
        String responseBody = ApiRequestUtils.get(apiURL, requestHeaders);
        
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(responseBody);
        JSONObject responseJson = (JSONObject) obj;

        // 카카오 유저 프로필조회 api
        apiURL = "https://kapi.kakao.com/v2/user/me";
        String header = "Bearer " + responseJson.get("access_token");
        requestHeaders.put("Authorization", header);
        responseBody = ApiRequestUtils.get(apiURL, requestHeaders);

        obj = parser.parse(responseBody);
        responseJson = (JSONObject) obj;
        JSONObject userInfoJson = (JSONObject) responseJson.get("kakao_account");
        JSONObject profileInfoJson = (JSONObject) userInfoJson.get("profile");

        UserEntity entity = UserEntity.builder()
            .id(UUID.randomUUID())
            .username(userInfoJson.get("email").toString())
            .name(profileInfoJson.get("nickname").toString())
            .roles("ROLE_USER")
            .snsType("kakao")
            .snsResponseId(responseJson.get("id").toString())
            .createdAt(CustomDateUtils.getCurrentDateTime())
            .updatedAt(CustomDateUtils.getCurrentDateTime())
            .build();
        
        UserEntity duplicatedUser = userService.searchDuplicatedUserBySnsTypeAndSnsResponseId("kakao", entity.getSnsResponseId());

        // sns_type이 kakao면서, sns_response_id와 동일한 값이 존재하지 않는다면 회원가입
        if(duplicatedUser  == null) {
            userService.saveAndModify(entity);
        }else {
            entity = duplicatedUser;
        }

        try {
            this.createAccessToken(response, entity);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Google Login
     * 
     * @param response
     * @param params
     * @throws ParseException
     */
    public void googleLogin(HttpServletResponse response, Map<String, Object> params) throws ParseException {
        // 구글 액세스토큰 
        String token = params.get("token") != null ? params.get("token").toString() : "";
        
        // 구글 인증 토큰 요청 api
        Map<String, String> requestHeaders = new HashMap<>();
        String apiURL = "https://oauth2.googleapis.com/token?"
            + "grant_type=authorization_code"
            + "&client_id=" + socialLoginConfiguration.getGoogle().get("id")
            + "&code=" + token
            + "&client_secret=" + socialLoginConfiguration.getGoogle().get("secret")
            + "&redirect_uri=" + socialLoginConfiguration.getGoogle().get("redirect")
            + "&state=hihi";

        String responseBody = ApiRequestUtils.post(apiURL, requestHeaders);
        
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(responseBody);
        JSONObject responseJson = (JSONObject) obj;

        // 구글 유저 프로필조회 api
        apiURL = "https://www.googleapis.com/oauth2/v2/userinfo";
        
        String header = "Bearer " + responseJson.get("access_token");
        requestHeaders.put("Authorization", header);
        responseBody = ApiRequestUtils.get(apiURL, requestHeaders);

        obj = parser.parse(responseBody);
        responseJson = (JSONObject) obj;

        UserEntity entity = UserEntity.builder()
            .id(UUID.randomUUID())
            .username(responseJson.get("email").toString())
            .name(responseJson.get("name").toString())
            .roles("ROLE_USER")
            .snsType("google")
            .snsResponseId(responseJson.get("id").toString())
            .createdAt(CustomDateUtils.getCurrentDateTime())
            .updatedAt(CustomDateUtils.getCurrentDateTime())
            .build();
        
        UserEntity duplicatedUser = userService.searchDuplicatedUserBySnsTypeAndSnsResponseId("google", entity.getSnsResponseId());
        
        // sns_type이 google이면서, sns_response_id와 동일한 값이 존재하지 않는다면 회원가입
        if(duplicatedUser  == null) {
            userService.saveAndModify(entity);
        }else {
            entity = duplicatedUser;
        }
        
        try {
            this.createAccessToken(response, entity);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Facebook Login
     * 
     * @param response
     * @param params
     * @throws ParseException
     */
    public void facebookLogin(HttpServletResponse response, Map<String, Object> params) throws ParseException {
        // 페이스북 액세스토큰 
        String token = params.get("token") != null ? params.get("token").toString() : "";

        // 페이스북 인증 토큰 요청 api
        Map<String, String> requestHeaders = new HashMap<>();
        String apiURL = "https://graph.facebook.com/v14.0/oauth/access_token?"
            + "client_id=" + socialLoginConfiguration.getFacebook().get("id")
            + "&redirect_uri=" + socialLoginConfiguration.getFacebook().get("redirect")
            + "&code=" + token
            + "&client_secret=" + socialLoginConfiguration.getFacebook().get("secret");
        String responseBody = ApiRequestUtils.get(apiURL, requestHeaders);

        JSONParser parser = new JSONParser();
        Object obj = parser.parse(responseBody);
        JSONObject responseJson = (JSONObject) obj;

        String authToken = responseJson.get("access_token").toString();
        // 페이스북 유저 프로필조회 토큰 발급
        apiURL = "https://graph.facebook.com/me?access_token=" + authToken;
        responseBody = ApiRequestUtils.get(apiURL, new HashMap<>());
        obj = parser.parse(responseBody);
        responseJson = (JSONObject) obj;
        String USER_ID = responseJson.get("id").toString();

        // 페이스북 유저 프로필조회 api
        apiURL = "https://graph.facebook.com/" + USER_ID + "?fields=name,email&access_token=" + authToken;
        responseBody = ApiRequestUtils.get(apiURL, new HashMap<>());
        Object userInfo = parser.parse(responseBody);
        System.out.println(userInfo);
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
        
        UserEntity duplicatedUser = userService.searchDuplicatedUserBySnsTypeAndSnsResponseId("facebook", entity.getSnsResponseId());
    
        // sns_type이 facebook이면서, sns_response_id와 동일한 값이 존재하지 않는다면 회원가입
        if(duplicatedUser  == null) {
            userService.saveAndModify(entity);
        }else {
            entity = duplicatedUser;
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

        // 초과 발급된 리프레시 토큰 삭제
        Integer ALLOWED_ACCESS_COUNT = 3;
        refreshTokenRepository.deleteOldRefreshTokenForUser(user.getId().toString(), ALLOWED_ACCESS_COUNT);

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
