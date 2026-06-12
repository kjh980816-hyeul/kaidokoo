package app.kaidoku.fancafe.infra.oauth;

import app.kaidoku.fancafe.member.Provider;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

/** 네이버 로그인 어댑터. https://developers.naver.com/docs/login/api/ */
@Component
public class NaverOAuthClient implements OAuthClient {

    private static final String AUTHORIZE_URL = "https://nid.naver.com/oauth2.0/authorize";
    private static final String TOKEN_URL = "https://nid.naver.com/oauth2.0/token";
    private static final String PROFILE_URL = "https://openapi.naver.com/v1/nid/me";

    private final RestClient restClient = RestClient.create();
    private final String clientId;
    private final String clientSecret;

    public NaverOAuthClient(@Value("${app.oauth.naver.client-id:}") String clientId,
                            @Value("${app.oauth.naver.client-secret:}") String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public Provider provider() {
        return Provider.NAVER;
    }

    @Override
    public boolean isConfigured() {
        return !clientId.isBlank() && !clientSecret.isBlank();
    }

    @Override
    public String authorizeUrl(String redirectUri, String state) {
        return UriComponentsBuilder.fromUriString(AUTHORIZE_URL)
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", state)
                .encode()
                .toUriString();
    }

    @Override
    public OAuthProfile fetchProfile(String code, String redirectUri) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("code", code);

        TokenResponse token = restClient.post().uri(TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(TokenResponse.class);
        if (token == null || token.accessToken() == null) {
            throw new IllegalStateException("네이버 토큰 교환 실패");
        }

        NaverMe me = restClient.get().uri(PROFILE_URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.accessToken())
                .retrieve()
                .body(NaverMe.class);
        if (me == null || me.response() == null || me.response().id() == null) {
            throw new IllegalStateException("네이버 프로필 조회 실패");
        }
        String nickname = me.response().nickname();
        if (nickname == null || nickname.isBlank()) {
            nickname = "선원";
        }
        return new OAuthProfile(me.response().id(), nickname);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TokenResponse(@JsonProperty("access_token") String accessToken) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record NaverMe(Response response) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        record Response(String id, String nickname) {
        }
    }
}
