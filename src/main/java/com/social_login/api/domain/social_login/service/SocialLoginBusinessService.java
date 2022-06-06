package com.social_login.api.domain.social_login.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.social_login.api.config.social_login.SocialLoginConfiguration;
import com.social_login.api.config.utils.AuthTokenUtils;
import com.social_login.api.domain.exception.CustomApiResponseException;
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
     * @throws IOException
     * @throws ParseException
     */
    public void naverLogin(HttpServletResponse response, Map<String, Object> params) throws IOException {
        // 네이버 액세스토큰(인가토큰)
        String token = params.get("token") != null ? params.get("token").toString() : "";
        String USERNAME = null;
        String NAME = null;
        String SNS_RESPONSE_ID = null;

        try {
            // 네이버 인증 토큰 요청 api
            // Required Variable : grant_type, client_id, client_secret, code, state
            Map<String, String> requestHeaders = new HashMap<>();
            String apiURL = "https://nid.naver.com/oauth2.0/token"
                    + "?grant_type=authorization_code"
                    + "&client_id=" + socialLoginConfiguration.getNaver().get("id")
                    + "&client_secret=" + socialLoginConfiguration.getNaver().get("secret")
                    + "&code=" + token
                    + "&state=" + socialLoginConfiguration.getState();
            String responseBody = ApiRequestUtils.get(apiURL, requestHeaders);

            JSONParser parser = new JSONParser();
            JSONObject responseJson = new JSONObject();
            JSONObject userInfoJson = new JSONObject();
            Object obj = parser.parse(responseBody);
            responseJson = (JSONObject) obj;

            // 네이버 유저 프로필조회 api
            // Requried Header : Authorization Bearer ${ACCESS_TOKEN}
            apiURL = "https://openapi.naver.com/v1/nid/me";
            String authToken = responseJson.get("access_token").toString();
            String header = "Bearer " + authToken;
            requestHeaders.put("Authorization", header);
            responseBody = ApiRequestUtils.get(apiURL, requestHeaders);

            obj = parser.parse(responseBody);
            responseJson = (JSONObject) obj;
            userInfoJson = (JSONObject) responseJson.get("response");

            USERNAME = userInfoJson.get("email").toString();
            NAME = userInfoJson.get("name").toString();
            SNS_RESPONSE_ID = userInfoJson.get("id").toString();
        } catch (ParseException e) {
            throw new CustomApiResponseException("요청 응답이 올바르지 않습니다.");
        }

        UserEntity entity = UserEntity.builder()
                .id(UUID.randomUUID())
                .username(USERNAME)
                .name(NAME)
                .roles("ROLE_USER")
                .snsType("naver")
                .snsResponseId(SNS_RESPONSE_ID)
                .createdAt(CustomDateUtils.getCurrentDateTime())
                .updatedAt(CustomDateUtils.getCurrentDateTime())
                .build();
                
        UserEntity duplicatedUser = userService.searchDuplicatedUserBySnsTypeAndSnsResponseId("naver", entity.getSnsResponseId());
        // sns_type이 naver면서, sns_response_id와 동일한 값이 존재하지 않는다면 회원가입
        if (duplicatedUser == null) {
            userService.saveAndModify(entity);
        } else {
            entity = duplicatedUser;
        }

        // 우리 사이트 access token, refresh token 생성
        UUID refreshTokenId = UUID.randomUUID();
        this.createAccessToken(response, entity, refreshTokenId);
        this.createRefreshToken(entity, refreshTokenId);
    }

    /**
     * Kakao Login
     * 
     * @param response
     * @param params
     * @throws IOException
     * @throws ParseException
     */
    public void kakaoLogin(HttpServletResponse response, Map<String, Object> params) throws IOException {
        // 카카오 액세스토큰(인가토큰)
        String token = params.get("token") != null ? params.get("token").toString() : "";
        String USERNAME = null;
        String NAME = null;
        String SNS_RESPONSE_ID = null;
        
        try {
            // 카카오 인증 토큰 요청 api
            // Required Variable : grant_type, client_id, redirect_uri, code, client_secret
            Map<String, String> requestHeaders = new HashMap<>();
            String apiURL = "https://kauth.kakao.com/oauth/token"
                    + "?grant_type=authorization_code"
                    + "&client_id=" + socialLoginConfiguration.getKakao().get("id")
                    + "&code=" + token
                    + "&client_secret=" + socialLoginConfiguration.getKakao().get("secret");
            String responseBody = ApiRequestUtils.post(apiURL, requestHeaders);

            JSONParser parser = new JSONParser();
            JSONObject responseJson = new JSONObject();
            Object obj = parser.parse(responseBody);
            responseJson = (JSONObject) obj;

            // 카카오 유저 프로필조회 api
            // Requried Header : Authorization Bearer ${ACCESS_TOKEN}
            apiURL = "https://kapi.kakao.com/v2/user/me";
            String authToken = responseJson.get("access_token").toString();
            String header = "Bearer " + authToken;
            requestHeaders.put("Authorization", header);
            responseBody = ApiRequestUtils.get(apiURL, requestHeaders);

            obj = parser.parse(responseBody);
            responseJson = (JSONObject) obj;
            JSONObject userInfoJson = (JSONObject) responseJson.get("kakao_account");
            JSONObject profileInfoJson = (JSONObject) userInfoJson.get("profile");

            USERNAME = userInfoJson.get("email").toString();
            NAME = profileInfoJson.get("nickname").toString();
            SNS_RESPONSE_ID = responseJson.get("id").toString();
        } catch (ParseException e) {
            throw new CustomApiResponseException("요청 응답이 올바르지 않습니다.");
        }

        UserEntity entity = UserEntity.builder()
                .id(UUID.randomUUID())
                .username(USERNAME)
                .name(NAME)
                .roles("ROLE_USER")
                .snsType("kakao")
                .snsResponseId(SNS_RESPONSE_ID)
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

        // 우리 사이트 access token, refresh token 생성
        UUID refreshTokenId = UUID.randomUUID();
        this.createAccessToken(response, entity, refreshTokenId);
        this.createRefreshToken(entity, refreshTokenId);
    }

    /**
     * Google Login
     * 
     * @param response
     * @param params
     * @throws ParseException
     * @throws IOException
     */
    public void googleLogin(HttpServletResponse response, Map<String, Object> params) throws IOException {
        // 구글 액세스토큰
        String token = params.get("token") != null ? params.get("token").toString() : "";
        String USERNAME = null;
        String NAME = null;
        String SNS_RESPONSE_ID = null;

        try {
            // 구글 인증 토큰 요청 api
            // Required Variable : client_id, client_secret, code, grant_type, redirect_uri
            Map<String, String> requestHeaders = new HashMap<>();
            String apiURL = "https://oauth2.googleapis.com/token"
                    + "?client_id=" + socialLoginConfiguration.getGoogle().get("id")
                    + "&client_secret=" + socialLoginConfiguration.getGoogle().get("secret")
                    + "&code=" + token
                    + "&grant_type=authorization_code"
                    + "&redirect_uri=" + socialLoginConfiguration.getGoogle().get("redirect");

            String responseBody = ApiRequestUtils.post(apiURL, requestHeaders);

            JSONParser parser = new JSONParser();
            JSONObject responseJson = new JSONObject();
            Object obj = parser.parse(responseBody);
            responseJson = (JSONObject) obj;

            // 구글 유저 프로필조회 api
            // Requried Header : Authorization Bearer ${ACCESS_TOKEN}
            apiURL = "https://www.googleapis.com/oauth2/v2/userinfo";
            String authToken = responseJson.get("access_token").toString();
            String header = "Bearer " + authToken;
            requestHeaders.put("Authorization", header);
            responseBody = ApiRequestUtils.get(apiURL, requestHeaders);

            obj = parser.parse(responseBody);
            responseJson = (JSONObject) obj;

            USERNAME = responseJson.get("email").toString();
            NAME = responseJson.get("name").toString();
            SNS_RESPONSE_ID = responseJson.get("id").toString();
        } catch (ParseException e) {
            throw new CustomApiResponseException("요청 응답이 올바르지 않습니다.");
        }

        UserEntity entity = UserEntity.builder()
            .id(UUID.randomUUID())
            .username(USERNAME)
            .name(NAME)
            .roles("ROLE_USER")
            .snsType("google")
            .snsResponseId(SNS_RESPONSE_ID)
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
        
        // 우리 사이트 access token, refresh token 생성
        UUID refreshTokenId = UUID.randomUUID();
        this.createAccessToken(response, entity, refreshTokenId);
        this.createRefreshToken(entity, refreshTokenId);
    }

    /**
     * Facebook Login
     * 
     * @param response
     * @param params
     * @throws ParseException
     * @throws IOException
     */
    public void facebookLogin(HttpServletResponse response, Map<String, Object> params) throws IOException {
        // 페이스북 액세스토큰 
        String token = params.get("token") != null ? params.get("token").toString() : "";
        String USERNAME = null;
        String NAME = null;
        String SNS_RESPONSE_ID = null;

        try {
            // 페이스북 인증 토큰 요청 api
            // Required Variable : client_id, redirect_uri, client_secret, code
            Map<String, String> requestHeaders = new HashMap<>();
            String apiURL = "https://graph.facebook.com/v14.0/oauth/access_token"
                    + "?client_id=" + socialLoginConfiguration.getFacebook().get("id")
                    + "&redirect_uri=" + socialLoginConfiguration.getFacebook().get("redirect")
                    + "&client_secret=" + socialLoginConfiguration.getFacebook().get("secret")
                    + "&code=" + token;
            String responseBody = ApiRequestUtils.get(apiURL, requestHeaders);

            JSONParser parser = new JSONParser();
            JSONObject responseJson = new JSONObject();
            JSONObject userInfoJson = new JSONObject();
            Object obj = parser.parse(responseBody);
            responseJson = (JSONObject) obj;

            String authToken = responseJson.get("access_token").toString();

            // 페이스북 유저 프로필조회 토큰 발급
            // Required Variable : access_token
            apiURL = "https://graph.facebook.com/me"
                    + "?access_token=" + authToken;
            responseBody = ApiRequestUtils.get(apiURL, new HashMap<>());

            obj = parser.parse(responseBody);
            responseJson = (JSONObject) obj;
            String USER_ID = responseJson.get("id").toString();

            // 페이스북 유저 프로필조회 api
            // Required Variable : fields, access_token
            apiURL = "https://graph.facebook.com/" + USER_ID
                    + "?fields=name,email"
                    + "&access_token=" + authToken;
            responseBody = ApiRequestUtils.get(apiURL, new HashMap<>());
            Object userInfo = parser.parse(responseBody);
            userInfoJson = (JSONObject) userInfo;

            USERNAME = userInfoJson.get("email").toString();
            NAME = userInfoJson.get("name").toString();
            SNS_RESPONSE_ID = userInfoJson.get("id").toString();
        } catch (ParseException e) {
            throw new CustomApiResponseException("요청 응답이 올바르지 않습니다.");
        }

        UserEntity entity = UserEntity.builder()
                .id(UUID.randomUUID())
                .username(USERNAME)
                .name(NAME)
                .roles("ROLE_USER")
                .snsType("facebook")
                .snsResponseId(SNS_RESPONSE_ID)
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
        
        // 우리 사이트 access token, refresh token 생성
        UUID refreshTokenId = UUID.randomUUID();
        this.createAccessToken(response, entity, refreshTokenId);
        this.createRefreshToken(entity, refreshTokenId);
    }

    private void createAccessToken(HttpServletResponse response, UserEntity user, UUID refreshTokenId) throws IOException {
        String accessToken = AuthTokenUtils.getJwtAccessToken(user, refreshTokenId);

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

    private void createRefreshToken(UserEntity user, UUID refreshTokenId) {
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
    }
}
