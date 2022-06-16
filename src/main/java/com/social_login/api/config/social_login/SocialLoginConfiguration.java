package com.social_login.api.config.social_login;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("app.login.key")
public class SocialLoginConfiguration {
    private Map<String, String> naver;
    private Map<String, String> kakao;
    private Map<String, String> google;
    private Map<String, String> facebook;
    private String state;

    public Map<String, String> getNaver() {
        return naver;
    }
    public void setNaver(Map<String, String> naver) {
        this.naver = naver;
    }
    public Map<String, String> getKakao() {
        return kakao;
    }
    public void setKakao(Map<String, String> kakao) {
        this.kakao = kakao;
    }
    public Map<String, String> getGoogle() {
        return google;
    }
    public void setGoogle(Map<String, String> google) {
        this.google = google;
    }
    public Map<String, String> getFacebook() {
        return facebook;
    }
    public void setFacebook(Map<String, String> facebook) {
        this.facebook = facebook;
    }
    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }
}
