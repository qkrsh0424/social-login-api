package com.social_login.api.domain.social_login.naver.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.social_login.api.domain.message.Message;
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
public class NaverLoginBusinessService {
    private final UserService userService;
    private final AuthTokenUtils authTokenUtils;

    public void naverLogin(HttpServletResponse response, Map<String, Object> params) throws ParseException {
        // 네이버 액세스토큰
        String token = params.get("token") != null ? params.get("token").toString() : "";
        String header = "Bearer " + token;

        String apiURL = "https://openapi.naver.com/v1/nid/me";

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Authorization", header);
        String responseBody = ApiRequestUtils.get(apiURL, requestHeaders);

        JSONParser parser = new JSONParser();
        Object obj = parser.parse(responseBody);
        JSONObject responseJson = (JSONObject) obj;
        JSONObject userInfoJson = (JSONObject) responseJson.get("response");

        UserEntity entity = UserEntity.builder()
            .id(UUID.randomUUID())
            .username(userInfoJson.get("email").toString())
            .name(userInfoJson.get("name").toString())
            .snsType("naver")
            .createdAt(CustomDateUtils.getCurrentDateTime())
            .updatedAt(CustomDateUtils.getCurrentDateTime())
            .build();
        
        List<UserEntity> duplicationUserNameEntities = userService.findByUsername(entity.getUsername());
        List<UserEntity> duplicationEntities = duplicationUserNameEntities.stream().filter(r -> r.getSnsType().equals("naver")).collect(Collectors.toList());

        // 중복되는 username(email)이 존재하지 않거나, 존재하는데 sns_type이 동일하지 않은 경우 회원가입
        if(duplicationUserNameEntities.size() == 0) {
            userService.saveAndModify(entity);
        }else if(duplicationEntities.size() == 0) {
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
        String accessToken = authTokenUtils.getJwtAccessToken(user.getId(), refreshTokenId);
        // TODO :: 리프레시 토큰 발급 및 저장
        
        ResponseCookie accessTokenCookie = ResponseCookie.from("social_actoken", accessToken).path("/")
                .httpOnly(true)
                .sameSite("Strict")
                // .secure(true)
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
