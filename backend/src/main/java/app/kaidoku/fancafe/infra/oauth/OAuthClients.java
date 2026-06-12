package app.kaidoku.fancafe.infra.oauth;

import app.kaidoku.fancafe.common.ApiException;
import app.kaidoku.fancafe.member.Provider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 제공자 → 어댑터 레지스트리. 키 미설정(미지원) 제공자는 404로 거른다. */
@Component
public class OAuthClients {

    private final Map<Provider, OAuthClient> clients;

    public OAuthClients(List<OAuthClient> clientList) {
        this.clients = clientList.stream()
                .collect(Collectors.toUnmodifiableMap(OAuthClient::provider, Function.identity()));
    }

    public OAuthClient get(Provider provider) {
        OAuthClient client = clients.get(provider);
        if (client == null || !client.isConfigured()) {
            throw ApiException.notFound("지원하지 않는 로그인 방식입니다: " + provider);
        }
        return client;
    }
}
