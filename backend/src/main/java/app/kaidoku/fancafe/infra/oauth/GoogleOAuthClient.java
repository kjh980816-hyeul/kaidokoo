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

/** 구글 로그인 어댑터(OIDC code flow, 스코프 openid+profile 최소). */
@Component
public class GoogleOAuthClient implements OAuthClient {

    private static final String AUTHORIZE_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USERINFO_URL = "https://openidconnect.googleapis.com/v1/userinfo";

    private final RestClient restClient = RestClient.create();
    private final String clientId;
    private final String clientSecret;

    public GoogleOAuthClient(@Value("${app.oauth.google.client-id:}") String clientId,
                             @Value("${app.oauth.google.client-secret:}") String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public Provider provider() {
        return Provider.GOOGLE;
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
                .queryParam("scope", "openid profile")
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
        form.add("redirect_uri", redirectUri);
        form.add("code", code);

        TokenResponse token = restClient.post().uri(TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(TokenResponse.class);
        if (token == null || token.accessToken() == null) {
            throw new IllegalStateException("구글 토큰 교환 실패");
        }

        GoogleUser user = restClient.get().uri(USERINFO_URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.accessToken())
                .retrieve()
                .body(GoogleUser.class);
        if (user == null || user.sub() == null) {
            throw new IllegalStateException("구글 프로필 조회 실패");
        }
        String nickname = user.name();
        if (nickname == null || nickname.isBlank()) {
            nickname = "선원";
        }
        return new OAuthProfile(user.sub(), nickname);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TokenResponse(@JsonProperty("access_token") String accessToken) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record GoogleUser(String sub, String name) {
    }
}
