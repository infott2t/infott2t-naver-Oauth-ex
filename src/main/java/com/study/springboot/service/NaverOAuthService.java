package com.study.springboot.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.study.springboot.dto.NaverUserProfile;

@Service
public class NaverOAuthService {

    private static final String AUTHORIZE_URL = "https://nid.naver.com/oauth2.0/authorize";
    private static final String TOKEN_URL = "https://nid.naver.com/oauth2.0/token";
    private static final String PROFILE_URL = "https://openapi.naver.com/v1/nid/me";

    private final RestClient restClient;

    @Value("${app.naver.client-id:}")
    private String clientId;

    @Value("${app.naver.client-secret:}")
    private String clientSecret;

    @Value("${app.naver.redirect-uri:http://localhost:8080/oauth/naver/callback}")
    private String redirectUri;

    @Value("${app.naver.mock-enabled:true}")
    private boolean mockEnabled;

    public NaverOAuthService() {
        this.restClient = RestClient.create();
    }

    public boolean isRealOAuthConfigured() {
        return clientId != null && !clientId.isBlank()
                && clientSecret != null && !clientSecret.isBlank();
    }

    public boolean isMockEnabled() {
        return mockEnabled;
    }

    public String buildAuthorizeUrl(String state) {
        return UriComponentsBuilder.fromUriString(AUTHORIZE_URL)
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", state)
                .build()
                .encode()
                .toUriString();
    }

    @SuppressWarnings("unchecked")
    public NaverUserProfile fetchUserProfile(String code, String state) {
        String tokenRequestUri = UriComponentsBuilder.fromUriString(TOKEN_URL)
                .queryParam("grant_type", "authorization_code")
                .queryParam("client_id", clientId)
                .queryParam("client_secret", clientSecret)
                .queryParam("code", code)
                .queryParam("state", state)
                .build()
                .encode()
                .toUriString();

        Map<String, Object> tokenBody = restClient.get()
                .uri(tokenRequestUri)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        if (tokenBody == null || tokenBody.get("access_token") == null) {
            throw new IllegalStateException("네이버 액세스 토큰 발급에 실패했습니다.");
        }

        String accessToken = String.valueOf(tokenBody.get("access_token"));

        Map<String, Object> profileBody = restClient.get()
                .uri(PROFILE_URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        if (profileBody == null || !(profileBody.get("response") instanceof Map<?, ?> responseMap)) {
            throw new IllegalStateException("네이버 사용자 프로필 조회에 실패했습니다.");
        }

        Map<String, Object> response = (Map<String, Object>) responseMap;
        String naverId = response.get("id") != null ? String.valueOf(response.get("id")) : null;
        String email = response.get("email") != null ? String.valueOf(response.get("email")) : null;
        String name = response.get("name") != null
                ? String.valueOf(response.get("name"))
                : (response.get("nickname") != null ? String.valueOf(response.get("nickname")) : "네이버회원");
        String profileImage = response.get("profile_image") != null
                ? String.valueOf(response.get("profile_image"))
                : null;

        return new NaverUserProfile(naverId, email, name, profileImage);
    }
}
